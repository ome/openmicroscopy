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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import omero.grid.AMI_InternalRepository_getDescription;
import omero.grid.AMI_InternalRepository_getProxy;
import omero.grid.AMI_Tables_getTable;
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
import omero.grid.TablePrxHelper;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.LocalException;
import Ice.UserException;

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

    private final Set<String> tableIds = new HashSet<String>();
    
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

    public void close() {
        synchronized (tableIds) {
            for (String id : tableIds) {
                TablePrx table = 
                    TablePrxHelper.uncheckedCast(
                            sf.adapter.getCommunicator().stringToProxy(id).ice_oneway());
                try {
                    table.close();
                } catch (Exception e) {
                    log.warn("Exception while closing table oneway: "+e);
                }
                
            }
            tableIds.clear();
        }
    }

    // Acquisition framework
    // =========================================================================

    private void allow(Ice.ObjectPrx prx) {
        if (prx != null && sf.control != null) {
            sf.control.identities().add(
                    new Ice.Identity[]{prx.ice_getIdentity()});
        }
    }
    
    private void register(TablePrx prx) {
        if (prx != null) {
            synchronized(tableIds) {
                tableIds.add(
                    Ice.Util.identityToString(prx.ice_getIdentity()));
            }
        }
    }
    
    private void checkAcquisitionWait(int seconds) throws ApiUsageException {
        if (seconds > (3 * 60)) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = "Delay is too long. Maximum = 3 minutes.";
            throw aue;
        }
    }

    /**
     * A task that gets applied to various proxies to test their validity.
     * Usually defined inline as anonymous classes.
     * 
     * @see {@link ProcessorCheck}
     */
    private interface RepeatTask<U extends Ice.ObjectPrx> {
        void requestService(Ice.ObjectPrx server, ResultHolder holder)
                throws ServerError;
    }

    /**
     * One implementation of {@link RepeatTask} which is reused locally.
     */
    private static final class ProcessorCheck implements
            RepeatTask<ProcessorPrx> {
        public void requestService(Ice.ObjectPrx prx, ResultHolder holder)
                throws ServerError {
            ProcessorPrx server = ProcessorPrxHelper.checkedCast(prx);
            holder.set(server);
        }
    }
    
    private class ResultHolder<U> {

        private final CountDownLatch c = new CountDownLatch(1);

        private final int timeout;

        private volatile U rv = null;

        ResultHolder(int timeoutSeconds) {
            timeout = timeoutSeconds;
        }

        void set(U obj) {
            if (obj != null) {
                rv = obj;
                c.countDown();
            }
        }

        U get() {
            try {
                c.await(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // oh well.
            }
            return rv;
        }
    }

    @SuppressWarnings("unchecked")
    private <U extends Ice.ObjectPrx> U lookup(List<Ice.ObjectPrx> objectPrxs,
            int seconds, RepeatTask<U> task) throws ServerError {

        ResultHolder<U> holder = new ResultHolder<U>(seconds);
        for (Ice.ObjectPrx prx : objectPrxs) {
            if (prx != null) {
                task.requestService(prx, holder);
            }
        }
        return holder.get();
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
                allow(proxy);
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

        RepositoryPrx repoPrx = (RepositoryPrx) lookup(Arrays
                .<Ice.ObjectPrx> asList(repos), 60,
                new RepeatTask<RepositoryPrx>() {
                    public void requestService(Ice.ObjectPrx prx,
                            final SharedResourcesI.ResultHolder holder) {

                        final InternalRepositoryPrx server = InternalRepositoryPrxHelper
                                .uncheckedCast(prx);

                        server
                                .getDescription_async(new AMI_InternalRepository_getDescription() {

                                    @Override
                                    public void ice_exception(LocalException ex) {
                                        holder.set(null);
                                    }

                                    @Override
                                    public void ice_exception(UserException ex) {
                                        holder.set(null);
                                    }

                                    @Override
                                    public void ice_response(
                                            OriginalFile description) {
                                        /*
                                        if (description != null
                                                && description.getId()
                                                        .getValue() == repo) {
                                                        */
                                        // At the moment there are no non-LegacyRepositoryI
                                        // repository implementations, and legacy ones are restricted
                                        // to being a singleton in the grid, so ignore the repo
                                        // number for the moment and return;
                                        if (true) {
                                            server
                                                    .getProxy_async(new AMI_InternalRepository_getProxy() {

                                                        @Override
                                                        public void ice_exception(
                                                                LocalException ex) {
                                                            holder.set(null);
                                                        }

                                                        @Override
                                                        public void ice_exception(
                                                                UserException ex) {
                                                            holder.set(null);
                                                        }

                                                        @Override
                                                        public void ice_response(
                                                                RepositoryPrx __ret) {
                                                            holder.set(__ret);
                                                        }
                                                    });
                                        }
                                    }
                                });
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
    public TablePrx openTable(final OriginalFile file, final Current __current)
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
        TablePrx tablePrx = (TablePrx) lookup(Arrays
                .<Ice.ObjectPrx> asList(tables), 60,
                new RepeatTask<TablePrx>() {
                    public void requestService(Ice.ObjectPrx prx,
                            final ResultHolder holder) {
                        final TablesPrx server = TablesPrxHelper
                                .uncheckedCast(prx);
                        server.getTable_async(new AMI_Tables_getTable() {

                            @Override
                            public void ice_exception(LocalException ex) {
                                holder.set(null);
                            }

                            @Override
                            public void ice_response(TablePrx __ret) {
                                holder.set(__ret);
                            }

                            @Override
                            public void ice_exception(UserException ex) {
                                holder.set(null);
                            }
                        }, file);
                    }
                });
        
        allow(tablePrx);
        register(tablePrx);
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
        ProcessorPrx server = (ProcessorPrx) lookup(Arrays
                .<Ice.ObjectPrx> asList(procs), seconds,
                new ProcessorCheck());

        long timeout = System.currentTimeMillis() + 60 * 60 * 1000L;
        InteractiveProcessorI ip = new InteractiveProcessorI(sf.principal,
                sf.sessionManager, sf.executor, server, unloadedJob, timeout);
        Ice.Identity id = new Ice.Identity();
        id.category = current.id.name;
        id.name = Ice.Util.generateUUID();
        Ice.ObjectPrx rv = sf.registerServant(current, id, ip);
        allow(rv);
        return InteractiveProcessorPrxHelper.uncheckedCast(rv);

    }
    
    //
    // NON-INTERFACE METHODS: in order to re-use the logic of this
    // class, several methods are defined here which are not in
    // the remote interface.
    //
    
    /**
     * Chooses on {@link ProcessorPrx} at random.
     */
    public ProcessorPrx chooseProcessor() throws ServerError {

        ProcessorPrx[] procs = registry.lookupProcessors();
        ProcessorPrx server = (ProcessorPrx) lookup(Arrays
                .<Ice.ObjectPrx> asList(procs), 15,
                new ProcessorCheck());
        return server;

    }
}
