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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ome.api.JobHandle;
import ome.model.IObject;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.Filterable;
import omero.ApiUsageException;
import omero.InternalException;
import omero.RTime;
import omero.ServerError;
import omero.ValidationException;
import omero.api.JobHandlePrx;
import omero.constants.categories.PROCESSCALLBACK;
import omero.constants.categories.PROCESSORCALLBACK;
import omero.constants.topics.PROCESSORACCEPTS;
import omero.grid.AMI_InternalRepository_getDescription;
import omero.grid.AMI_InternalRepository_getProxy;
import omero.grid.AMI_Tables_getTable;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.InternalRepositoryPrxHelper;
import omero.grid.ParamsHelper;
import omero.grid.ProcessorCallbackPrx;
import omero.grid.ProcessorCallbackPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid.TablePrx;
import omero.grid.TablePrxHelper;
import omero.grid.TablesPrx;
import omero.grid.TablesPrxHelper;
import omero.grid._ProcessorCallbackDisp;
import omero.grid._SharedResourcesOperations;
import omero.model.Format;
import omero.model.FormatI;
import omero.model.Job;
import omero.model.JobStatus;
import omero.model.JobStatusI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
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
        _SharedResourcesOperations, BlitzOnly, ServiceFactoryAware,
        ParamsHelper.Acquirer { // FIXME

    private final static Log log = LogFactory.getLog(SharedResourcesI.class);

    private final Set<String> tableIds = new HashSet<String>();

    private final Set<String> processorIds = new HashSet<String>();

    private final TopicManager topicManager;

    private final Registry registry;

    private final ScriptRepoHelper helper;

    private final long waitMillis;

    private ServiceFactoryI sf;

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
            Registry registry, ScriptRepoHelper helper) {
        this(be, topicManager, registry, helper, 5000);
    }

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
                Registry registry, ScriptRepoHelper helper, long waitMillis) {
        super(null, be);
        this.waitMillis = waitMillis;
        this.topicManager = topicManager;
        this.registry = registry;
        this.helper = helper;
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
        void requestService(Ice.ObjectPrx server, ResultHolder<U> holder)
                throws ServerError;
    }
    
    private static class ResultHolder<U> {

        private final static Log log = LogFactory.getLog(ResultHolder.class);

        private final CountDownLatch c = new CountDownLatch(1);

        private final long timeout;

        private volatile U rv = null;

        ResultHolder(long timeoutMillis) {
            timeout = timeoutMillis;
        }

        void set(U obj) {
            if (obj != null) {
                rv = obj;
                c.countDown();
            }
        }

        U get() {
            try {
                c.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (rv == null) {
                log.debug("Nothing found.");
            }
            return rv;
        }
    }

    @SuppressWarnings("unchecked")
    private <U extends Ice.ObjectPrx> U lookup(long millis, List<Ice.ObjectPrx> objectPrxs,
            RepeatTask<U> task) throws ServerError {

        ResultHolder<U> holder = new ResultHolder<U>(millis);
        for (Ice.ObjectPrx prx : objectPrxs) {
            if (prx != null) {
                task.requestService(prx, holder);
            }
        }
        return holder.get();
    }

    // Public interface
    // =========================================================================

    static String QUERY = "select o from OriginalFile o where o.format.value = 'Repository'";

    public RepositoryPrx getScriptRepository(Current __current)
            throws ServerError {
        InternalRepositoryPrx[] repos = registry.lookupRepositories();
        InternalRepositoryPrx prx = null;
        if (repos != null) {
            for (int i = 0; i < repos.length; i++) {
                if (repos[i] != null) {
                    if (repos[i].toString().contains(helper.getUuid())) {
                        prx = repos[i];
                    }
                }
            }
        }
        return prx == null ? null : prx.getProxy();
    }

    @SuppressWarnings("unchecked")
    public RepositoryMap repositories(Current current) throws ServerError {

        // TODO
        // Possibly need to throttle the numbers of acquisitions per time.
        // Need to keep up with closing
        // might need to cache the found repositories.

        final String query = QUERY;
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
                if (desc == null || desc.getId() == null) {
                    log.warn("Description is null for " + i);
                    continue;
                }
                RepositoryPrx proxy = i.getProxy();
                map.descriptions.add(desc);
                map.proxies.add(proxy);
                found.add(desc.getId().getValue());
                sf.allow(proxy);
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

    public boolean areTablesEnabled(Current __current) throws ServerError {
        TablesPrx[] tables = registry.lookupTables();
        return null != lookup(waitMillis, Arrays.<Ice.ObjectPrx> asList(tables),
                new RepeatTask<TablesPrx>() {
                    public void requestService(Ice.ObjectPrx prx,
                            final ResultHolder<TablesPrx> holder) {
                        final TablesPrx server = TablesPrxHelper
                                .checkedCast(prx);
                        holder.set(server);
                    }
                });
    }

    public TablePrx newTable(final long repo, String path, Current __current)
            throws ServerError {

        if (true) {
            // Overriding repository logic for creation. As long as the
            // security system is still in charge, we need to have the files
            // being created for the proper user.
            Format omero_tables = new FormatI();
            omero_tables.setValue(rstring("OMERO.tables"));
            final OriginalFile file = new OriginalFileI();
            RTime time = omero.rtypes.rtime(System.currentTimeMillis());
            file.setAtime(time);
            file.setMtime(time);
            file.setCtime(time);
            file.setSha1(omero.rtypes.rstring("UNKNOWN"));
            file.setFormat(omero_tables);
            file.setSize(omero.rtypes.rlong(0));
            file.setPath(omero.rtypes.rstring(path));
            file.setName(omero.rtypes.rstring(path));
            Long id = (Long) sf.executor.execute(sf.principal, new Executor.SimpleWork(this, "newTable", repo, path) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    try {
                        IObject obj = (IObject) new IceMapper().reverse(file);
                        return sf.getUpdateService().saveAndReturnObject(obj).getId();
                    } catch (Exception e) {
                        log.error(e);
                        return null;
                    }
                }
                
            });
            if (id == null) {
                throw new InternalException(null, null, "Failed to save file");
            }
            file.setId(omero.rtypes.rlong(id));
            file.unload();
            return openTable(file, __current);
        }
        
        // NEVER REACHED IN CURRENT CODE.
        
        // Okay. All's valid.
        InternalRepositoryPrx[] repos = registry.lookupRepositories();

        RepositoryPrx repoPrx = (RepositoryPrx) lookup(waitMillis,
                Arrays.<Ice.ObjectPrx> asList(repos),
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
        TablePrx tablePrx = (TablePrx) lookup(waitMillis,
                Arrays.<Ice.ObjectPrx> asList(tables),
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
        
        sf. allow(tablePrx);
        register(tablePrx);
        return tablePrx;

    }

    // Check job
    // Lookup processor
    // Create wrapper (InteractiveProcessor)
    // Create session (with session)
    // Setup environment
    // Send off to processor
    public InteractiveProcessorPrx acquireProcessor(final Job submittedJob,
            int seconds, final Current current) throws ServerError {

        checkAcquisitionWait(seconds);

        // Check job
        final IceMapper mapper = new IceMapper();
        final ome.model.jobs.Job savedJob = saveJob(submittedJob, mapper);
        if (savedJob == null) {
            throw new ApiUsageException(null, null, "Could not submit job. ");
        }

        // Okay. All's valid.
        final Job job = (Job) mapper.map(savedJob);
        Ice.Identity acceptId = new Ice.Identity();
        acceptId.name = UUID.randomUUID().toString();
        acceptId.category = PROCESSORCALLBACK.value;
        ResultHolder<String> holder = new ResultHolder<String>(seconds*1000);
        Callback callback = new Callback(sf, holder, job);
        ProcessorPrx server = callback.activateAndWait(current, acceptId);

        // Nothing left to try
        if (server == null) {
            updateJob(job.getId().getValue(), "Error", "No processor available");
            throw new omero.ResourceError(null, null, "No processor available.");
        }

        long timeout = System.currentTimeMillis() + 60 * 60 * 1000L;

        InteractiveProcessorI ip = new InteractiveProcessorI(sf.principal,
                sf.sessionManager, sf.executor, server, job, timeout,
                sf.control, new ParamsHelper(this, sf.getExecutor(), sf.getPrincipal()));
        Ice.Identity procId = sessionedID("InteractiveProcessor");
        Ice.ObjectPrx rv = sf.registerServant(procId, ip);
        sf.allow(rv);
        return InteractiveProcessorPrxHelper.uncheckedCast(rv);
    }

    public void addProcessor(ProcessorPrx proc, Current __current)
            throws ServerError {
        topicManager.register(PROCESSORACCEPTS.value, proc, false);
        processorIds.add(Ice.Util.identityToString(proc.ice_getIdentity()));
        if (sf.control != null) {
            sf.control.categories().add(
                    new String[]{PROCESSORCALLBACK.value, PROCESSCALLBACK.value});
        }
    }

    public void removeProcessor(ProcessorPrx proc, Current __current)
            throws ServerError {
        topicManager.unregister(PROCESSORACCEPTS.value, proc);
        processorIds.remove(Ice.Util.identityToString(proc.ice_getIdentity()));
    }

    //
    // HELPERS
    //
    // =========================================================================


    private Ice.Identity sessionedID(String type) {
        String key = type + "-" + UUID.randomUUID();
        return sf.getIdentity(key);
    }

    private ome.model.jobs.Job saveJob(final Job submittedJob,
            final IceMapper mapper) {
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
                            handle.submit((ome.model.jobs.Job) mapper
                                    .reverse(submittedJob));
                            return handle.getJob();
                        } catch (ApiUsageException e) {
                            return null;
                        } catch (ObjectNotFoundException onfe) {
                            return null;
                        } finally {
                            if (handle != null) {
                                handle.close();
                            }
                        }
                    }
                });
        return savedJob;
    }

    private void updateJob(final long id, final String status, final String message) {
        sf.executor.execute(sf.principal, new Executor.SimpleWork(this, "updateJob") {
            @Transactional(readOnly = false)
            public Object doWork(Session session,
                    ServiceFactory sf) {

                final JobHandle handle = sf.createJobHandle();
                try {
                    handle.attach(id);
                    handle.setStatusAndMessage(status, message);
                    return null;
                } finally {
                    handle.close();
                }
            }
        });
    }

    private static class Callback extends _ProcessorCallbackDisp {

        private final static Log log = LogFactory.getLog(Callback.class);

        private final Job job;

        private final ServiceFactoryI sf;

        private final ResultHolder<String> holder;

        private final EventContext ec;

        public Callback(ServiceFactoryI sf, ResultHolder<String> holder, Job job) {
            this.sf = sf;
            this.job = job;
            this.holder = holder;
            this.ec = sf.sessionManager.getEventContext(sf.principal);
        }

        public ProcessorPrx activateAndWait(Ice.Current current, Ice.Identity acceptId) throws ServerError {

            Ice.ObjectPrx prx = sf.registerServant(acceptId, this);

            try {
                prx = sf.adapter.createDirectProxy(acceptId);
                ProcessorCallbackPrx cbPrx =
                        ProcessorCallbackPrxHelper.uncheckedCast(prx);

                TopicManager.TopicMessage msg = new TopicManager.TopicMessage(this,
                        PROCESSORACCEPTS.value,
                        new ProcessorPrxHelper(),
                        "willAccept",
                        new omero.model.ExperimenterI(ec.getCurrentUserId(), false),
                        new omero.model.ExperimenterGroupI(ec.getCurrentGroupId(), false),
                        this.job,
                        cbPrx);
                sf.topicManager.onApplicationEvent(msg);
                String server = holder.get();
                Ice.ObjectPrx p = sf.adapter.getCommunicator().stringToProxy(server);
                return ProcessorPrxHelper.uncheckedCast(p);
            } finally {
                sf.unregisterServant(acceptId);
            }
        }

        public void isAccepted(boolean accepted, String sessionUuid, String procConn, Current __current) {

            String reason = "because false returned";

            if (accepted) {
                log.debug(String.format(
                        "Processor with session %s returned %s accepted",
                        sessionUuid, procConn, accepted));
                try {
                    EventContext procEc = sf.sessionManager.getEventContext(new Principal(sessionUuid));
                    if (procEc.isCurrentUserAdmin() ||
                            procEc.getCurrentUserId().equals(ec.getCurrentUserId())) {
                        this.holder.set(procConn);
                    } else {
                        reason = "since disallowed";
                    }
                } catch (Exception e) {
                    reason = "due to exception: " + e.getMessage();
                }
            }

            log.debug(String.format(
                    "Processor with session %s rejected %s",
                    sessionUuid, reason));
            this.holder.set(null);

        }

        public void responseRunning(List<Long> jobIds, Current __current) {
            log.error("responseRunning should not have been called");
        }

    }
}
