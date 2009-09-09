/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.api.JobHandle;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.Filterable;
import omero.ApiUsageException;
import omero.ServerError;
import omero.ValidationException;
import omero.rtypes;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.InternalRepositoryPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid.TablePrx;
import omero.grid.TablesPrx;
import omero.grid.TablesPrxHelper;
import omero.grid._SharedResourcesOperations;
import omero.model.Format;
import omero.model.FormatI;
import omero.model.Job;
import omero.model.JobStatus;
import omero.model.JobStatusI;
import omero.model.OriginalFile;
import omero.util.IceMapper;
import omero.util.ObjectFactoryRegistrar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * Implementation of the SharedResources interface.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.1
 * @see ome.grid.SharedResources
 */
public class SharedResourcesI extends AbstractAmdServant implements
        _SharedResourcesOperations, BlitzOnly, ServiceFactoryAware {

    private final static Log log = LogFactory.getLog(SharedResourcesI.class);

    private final TopicManager topicManager;

    private final Registry registry;

    private ServiceFactoryI sf;

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
            Registry registry) {
        super(null, be);
        this.topicManager = topicManager;
        this.registry = registry;
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.sf = sf;
    }

    // Acquisition framework
    // =========================================================================

    private void checkAcquisitionWait(int seconds) throws ApiUsageException {
        if (seconds > (3 * 60)) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = "Delay is too long. Maximum = 3 minutes.";
            throw aue;
        }
    }

    private interface RepeatTask<U extends Ice.ObjectPrx> {
        U lookupService(Ice.ObjectPrx server) throws ServerError;
    }

    @SuppressWarnings("unchecked")
    private <U extends Ice.ObjectPrx> U repeatLookup(
            List<Ice.ObjectPrx> objectPrxs, int seconds, Ice.Current current,
            RepeatTask<U> task) throws ServerError {

        // Setting up a second communicator to see if the timeout exceptions
        // will go away. A checkedCast or similar will be needed to reconvert
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = current.adapter.getCommunicator().getProperties()
                ._clone();
        Ice.Communicator ic = Ice.Util.initialize(id);
        ObjectFactoryRegistrar.registerObjectFactory(ic,
                ObjectFactoryRegistrar.INSTANCE);
        for (rtypes.ObjectFactory of : rtypes.ObjectFactories.values()) {
            of.register(ic);
        }
        try {
            long start = System.currentTimeMillis();
            long stop = seconds < 0 ? start : (start + (seconds * 1000L));
            do {

                for (Ice.ObjectPrx prx : objectPrxs) {
                    if (prx != null) {

                        prx = ic.stringToProxy(prx.ice_toString()).ice_timeout(
                                5);

                        try {

                            U service = task.lookupService(prx);
                            if (service != null) {
                                return service;
                            }
                        } catch (ServerError se) {
                            log.warn("ServerError on task.lookupService(" + prx
                                    + ") :" + se);
                            // The following exceptions all signify a bad proxy
                        } catch (Ice.ObjectNotExistException onee) {
                            // bad proxy
                        } catch (Ice.NoEndpointException nee) {
                            // bad proxy
                        } catch (Ice.TimeoutException te) {
                            // bad proxy
                        } catch (Exception e) {
                            log.warn("Other exception", e);
                        }
                    }
                }
            } while (stop < System.currentTimeMillis());

            return null;
        } finally {
            ic.destroy();
        }
    }

    static String QUERY = "select o from OriginalFile o where o.format.value = 'Repository'";

    @SuppressWarnings("unchecked")
    public RepositoryMap repositories(Current current) throws ServerError {

        // Possibly need to throttle the numbers of acquisitions per time.
        // Need to keep up with closing
        // might need to cache the found repositories.

        IceMapper mapper = new IceMapper();
        List<OriginalFile> objs = (List<OriginalFile>) mapper
                .map((List<Filterable>) sf.executor.execute(sf.principal,
                        new Executor.SimpleWork(this, "acquireRepositories") {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                return sf.getQueryService().findAllByQuery(
                                        QUERY, null);
                            }
                        }));

        InternalRepositoryPrx[] repos = registry.lookupRepositories();

        RepositoryMap map = new RepositoryMap();
        map.descriptions = new ArrayList<OriginalFile>();
        map.proxies = new ArrayList<RepositoryPrx>();

        List<Long> found = new ArrayList<Long>();
        for (InternalRepositoryPrx i : repos) {
            if (i == null) {
                continue;
            }
            try {
                OriginalFile desc = i.getDescription();
                RepositoryPrx proxy = i.getProxy();
                map.descriptions.add(desc);
                map.proxies.add(proxy);
                found.add(desc.getId().getValue());
            } catch (Ice.LocalException e) {
                // Ok.
            }
        }

        for (OriginalFile r : objs) {
            if (!found.contains(r.getId().getValue())) {
                map.descriptions.add(r);
                map.proxies.add(null);
            }
        }

        return map;
    }

    public TablePrx newTable(final long repo, String path, Current __current)
            throws ServerError {

        // Okay. All's valid.
        InternalRepositoryPrx[] repos = registry.lookupRepositories();
        RepositoryPrx repoPrx = (RepositoryPrx) repeatLookup(Arrays
                .<Ice.ObjectPrx> asList(repos), 60, __current,
                new RepeatTask<RepositoryPrx>() {
                    public RepositoryPrx lookupService(Ice.ObjectPrx prx)
                            throws ServerError {
                        InternalRepositoryPrx server = InternalRepositoryPrxHelper
                                .checkedCast(prx);
                        OriginalFile description = server.getDescription();
                        RepositoryPrx repoPrx = server.getProxy();
                        if (description.getId().getValue() == repo) {
                            return repoPrx;
                        }
                        return null;
                    }
                });

        if (repoPrx == null) {
            return null;
        } else {
            // Attempt to fix an odd timeout exception during register()
            repoPrx = RepositoryPrxHelper.checkedCast(__current.adapter
                    .getCommunicator().stringToProxy(repoPrx.ice_toString()));
        }

        Format omero_tables = new FormatI();
        omero_tables.setValue(rstring("OMERO.tables"));
        OriginalFile file = repoPrx.register(path, omero_tables);
        return openTable(file, __current);

    }

    @SuppressWarnings("unchecked")
    public TablePrx openTable(final OriginalFile file, Current __current)
            throws ServerError {

        // Now make sure the current user has permissions to do this
        if (file == null || file.getId() == null) {

            throw new ValidationException(null, null,
                    "file must be a managed instance.");

        }

        sf.executor.execute(sf.principal, new Executor.SimpleWork(this,
                "checkOriginalFilePermissions", file.getId().getValue()) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getQueryService().get(
                        ome.model.core.OriginalFile.class,
                        file.getId().getValue());

            }
        });
        file.unload();

        // Okay. All's valid.
        TablesPrx[] tables = registry.lookupTables();
        TablePrx tablePrx = (TablePrx) repeatLookup(Arrays
                .<Ice.ObjectPrx> asList(tables), 60, __current,
                new RepeatTask<TablePrx>() {
                    public TablePrx lookupService(Ice.ObjectPrx prx) {
                        TablesPrx server = TablesPrxHelper.checkedCast(prx);
                        return server.getTable(file);
                    }
                });
        return tablePrx;

    }

    public InteractiveProcessorPrx acquireProcessor(final Job submittedJob,
            int seconds, final Current current) throws ServerError {

        checkAcquisitionWait(seconds);

        final IceMapper mapper = new IceMapper();

        // First create the job with a status of WAITING.
        // The InteractiveProcessor will be responsible for its
        // further lifetime.
        final ome.model.jobs.Job savedJob = (ome.model.jobs.Job) sf.executor
                .execute(sf.principal, new Executor.SimpleWork(this,
                        "submitJob") {
                    @Transactional(readOnly = false)
                    public ome.model.jobs.Job doWork(Session session,
                            ServiceFactory sf) {

                        final JobHandle handle = sf.createJobHandle();
                        try {
                            JobStatus status = new JobStatusI();
                            status.setValue(omero.rtypes
                                    .rstring(JobHandle.WAITING));
                            submittedJob.setStatus(status);
                            submittedJob.setMessage(omero.rtypes
                                    .rstring("Interactive job. Waiting."));

                            handle.submit((ome.model.jobs.Job) mapper
                                    .reverse(submittedJob));
                            return handle.getJob();
                        } catch (ApiUsageException e) {
                            return null;
                        } finally {
                            if (handle != null) {
                                handle.close();
                            }
                        }
                    }
                });

        if (savedJob == null) {
            throw new ApiUsageException(null, null, "Could not submit job. ");
        }

        // Unloading job to prevent lazy-initialization exceptions.
        final Job unloadedJob = (Job) mapper.map(savedJob);
        unloadedJob.unload();

        // Lookup processor
        // Create wrapper (InteractiveProcessor)
        // Create session (with session)
        // Setup environment
        // Send off to processor

        // Okay. All's valid.
        ProcessorPrx[] procs = registry.lookupProcessors();
        InteractiveProcessorPrx interactivePrx = (InteractiveProcessorPrx) repeatLookup(
                Arrays.<Ice.ObjectPrx> asList(procs), seconds, current,
                new RepeatTask<InteractiveProcessorPrx>() {
                    public InteractiveProcessorPrx lookupService(
                            Ice.ObjectPrx prx) throws ServerError {
                        ProcessorPrx server = ProcessorPrxHelper
                                .checkedCast(prx);
                        if (server != null) {
                            long timeout = System.currentTimeMillis() + 60 * 60 * 1000L;
                            InteractiveProcessorI ip = new InteractiveProcessorI(
                                    sf.principal, sf.sessionManager,
                                    sf.executor, server, unloadedJob, timeout);
                            Ice.Identity id = new Ice.Identity();
                            id.category = current.id.name;
                            id.name = Ice.Util.generateUUID();
                            Ice.ObjectPrx rv = sf.registerServant(current, id,
                                    ip);
                            return InteractiveProcessorPrxHelper
                                    .uncheckedCast(rv);
                        }
                        return null;
                    }
                });
        return interactivePrx;

    }
}
