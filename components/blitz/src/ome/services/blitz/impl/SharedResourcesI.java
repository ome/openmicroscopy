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
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.ProcessorPrx;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.TablePrx;
import omero.grid.TablesPrx;
import omero.grid._SharedResourcesOperations;
import omero.model.Format;
import omero.model.FormatI;
import omero.model.Job;
import omero.model.JobStatus;
import omero.model.JobStatusI;
import omero.model.OriginalFile;
import omero.util.IceMapper;

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

    private interface RepeatTask<T extends Ice.ObjectPrx, U extends Ice.ObjectPrx> {
        U lookupService(T server) throws ServerError;
    }

    private <T extends Ice.ObjectPrx, U extends Ice.ObjectPrx> U repeatLookup(
            List<T> objectPrxs, int seconds, RepeatTask<T, U> task)
            throws ServerError {

        long start = System.currentTimeMillis();
        long stop = seconds < 0 ? start : (start + (seconds * 1000L));
        do {

            for (T prx : objectPrxs) {
                if (prx != null) {
                    try {

                        U service = task.lookupService(prx);
                        if (service != null) {
                            return service;
                        }

                        try {
                            Thread.sleep((stop - start) / 10);
                        } catch (InterruptedException ie) {
                            // ok.
                        }
                    } catch (Ice.NoEndpointException nee) {
                        // This means that there probably is none.
                        // Wait a little longer
                        try {
                            Thread.sleep((stop - start) / 3);
                        } catch (InterruptedException ie) {
                            // ok.
                        }
                    }
                }
            }
        } while (stop < System.currentTimeMillis());

        return null;
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
                .asList(repos), 60,
                new RepeatTask<InternalRepositoryPrx, RepositoryPrx>() {
                    public RepositoryPrx lookupService(
                            InternalRepositoryPrx server) {
                        try {
                            OriginalFile description = server.getDescription();
                            RepositoryPrx prx = server.getProxy();
                            if (description.getId().getValue() == repo) {
                                return prx;
                            }
                        } catch (ServerError e) {
                            // fall through to null
                        }
                        return null;
                    }
                });

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
        TablePrx tablePrx = (TablePrx) repeatLookup(Arrays.asList(tables), 60,
                new RepeatTask<TablesPrx, TablePrx>() {
                    public TablePrx lookupService(TablesPrx server) {
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
                Arrays.asList(procs), seconds,
                new RepeatTask<ProcessorPrx, InteractiveProcessorPrx>() {
                    public InteractiveProcessorPrx lookupService(
                            ProcessorPrx server) throws ServerError {
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
