/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.api.JobHandle;
import ome.model.IObject;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ParamsCache;
import ome.services.blitz.util.ResultHolder;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.Filterable;
import omero.ApiUsageException;
import omero.InternalException;
import omero.RTime;
import omero.ServerError;
import omero.ValidationException;
import omero.constants.categories.PROCESSCALLBACK;
import omero.constants.categories.PROCESSORCALLBACK;
import omero.constants.topics.PROCESSORACCEPTS;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.ParamsHelper;
import omero.grid.ProcessorPrx;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.TablePrx;
import omero.grid.TablePrxHelper;
import omero.grid.TablesPrx;
import omero.grid.TablesPrxHelper;
import omero.grid._InteractiveProcessorTie;
import omero.grid._SharedResourcesOperations;
import omero.model.Job;
import omero.model.JobStatus;
import omero.model.JobStatusI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SharedResourcesI extends AbstractCloseableAmdServant implements
        _SharedResourcesOperations, BlitzOnly, ServiceFactoryAware,
        ParamsHelper.Acquirer { // FIXME

    /**
     * If no value is passed to this instance, this is the time (1 hour)
     * which will be used for a script session.
     */
    public final static long DEFAULT_TIMEOUT = 60 * 60 * 1000L;

    private final static Logger log = LoggerFactory.getLogger(SharedResourcesI.class);

    private final Set<String> tableIds = new HashSet<String>();

    private final Set<String> processorIds = new HashSet<String>();

    private final TopicManager topicManager;

    private final Registry registry;

    private final ScriptRepoHelper helper;

    private final ParamsCache paramsCache;

    /**
     * How long to wait for shared resources (e.g. TablesPrx) to respond to
     * requests.
     */
    private final long waitMillis;

    /**
     * Length of time (ms) to give a script session to live. Once the session
     * times out, the process will be killed.
     *
     * @since 5.0.3
     */
    private final long timeout;

    private ServiceFactoryI sf;

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
            Registry registry, ScriptRepoHelper helper, ParamsCache cache) {
        this(be, topicManager, registry, helper, cache, 5000);
    }

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
                Registry registry, ScriptRepoHelper helper, ParamsCache cache,
                long waitMillis) {
        this( be, topicManager, registry, helper, cache, waitMillis, DEFAULT_TIMEOUT);
    }

    public SharedResourcesI(BlitzExecutor be, TopicManager topicManager,
                Registry registry, ScriptRepoHelper helper, ParamsCache cache,
                long waitMillis, long timeout) {
        super(null, be);
        this.waitMillis = waitMillis;
        this.topicManager = topicManager;
        this.registry = registry;
        this.helper = helper;
        this.paramsCache = cache;
        this.timeout = timeout;
        // In order to prevent overflow of currentTimeMillis+timeout and more
        // generally to prevent nonsensical timeouts, we limit to 1 year.
        if (timeout > DEFAULT_TIMEOUT * 24 * 365) {
            throw new ome.conditions.InternalException(
                    "Timeout too large: " + timeout);
        }
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.sf = sf;
    }

    @Override
    protected void preClose(Ice.Current current) {
        synchronized (tableIds) {
            for (String id : tableIds) {
                TablePrx table =
                    TablePrxHelper.uncheckedCast(
                            sf.adapter.getCommunicator().stringToProxy(id));
                try {
                    table.close();
                } catch (Ice.NotRegisteredException e) {
                    log.debug("Table already gone: " + id);
                } catch (Exception e) {
                    log.error("Exception while closing table " + id, e);
                }

            }
            tableIds.clear();
        }
    }

    @Override
    protected void postClose(Current current) {
        // no-op
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

    static String QUERY = "select o from OriginalFile o where o.mimetype = 'Repository'";

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

        IceMapper mapper = new IceMapper();
        List<OriginalFile> objs = (List<OriginalFile>) mapper
                .map((List<Filterable>) sf.executor.execute(current.ctx, sf.principal,
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
                        try {
                            if (server != null && server.getRepository() != null) {
                                holder.set(server);
                            }
                        } catch (Exception e) {
                            log.debug("Exception on getRepository: " + e);
                            holder.set(null);
                        }
                    }
                });
    }

    public TablePrx newTable(final long repo, String path, Current __current)
            throws ServerError {

        // Overriding repository logic for creation. As long as the
        // security system is still in charge, we need to have the files
        // being created for the proper user.
        final OriginalFile file = new OriginalFileI();
        RTime time = omero.rtypes.rtime(System.currentTimeMillis());
        file.setAtime(time);
        file.setMtime(time);
        file.setCtime(time);
        file.setMimetype(omero.rtypes.rstring("OMERO.tables"));
        file.setPath(omero.rtypes.rstring(path));
        file.setName(omero.rtypes.rstring(path));

        IObject obj = (IObject) sf.executor.execute(__current.ctx,
                sf.principal, new Executor.SimpleWork(this, "newTable", repo, path) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                try {
                    IObject obj = (IObject) new IceMapper().reverse(file);
                    return sf.getUpdateService().saveAndReturnObject(obj);
                } catch (Exception e) {
                    log.error(e.toString()); // slf4j migration: toString()
                    return null;
                }
            }

        });

        OriginalFile saved = (OriginalFile) new IceMapper().map(obj);
        if (saved == null) {
            throw new InternalException(null, null, "Failed to save file");
        }
        return openTable(saved, __current);

    }

    @SuppressWarnings("unchecked")
    public TablePrx openTable(final OriginalFile file, final Current __current)
            throws ServerError {

        // Now make sure the current user has permissions to do this
        if (file == null || file.getId() == null) {

            throw new ValidationException(null, null,
                    "file must be a managed instance.");

        }

        try {
            sf.executor.execute(__current.ctx, sf.principal,
                new Executor.SimpleWork(this,
                    "checkOriginalFilePermissions", file.getId().getValue()) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().get(
                            ome.model.core.OriginalFile.class,
                                file.getId().getValue());
                    }
                });
        } catch (Exception e) {
            IceMapper mapper = new IceMapper();
            ServerError ue = mapper.handleServerError(e, this.ctx);
            throw ue;
        }

        // Okay. All's valid.
        TablesPrx[] tables = registry.lookupTables();
        TablePrx tablePrx = (TablePrx) lookup(waitMillis,
                Arrays.<Ice.ObjectPrx> asList(tables),
                new RepeatTask<TablePrx>() {
                    public void requestService(Ice.ObjectPrx prx,
                            final ResultHolder holder) throws ServerError {
                       final TablesPrx server = TablesPrxHelper
                          .uncheckedCast(prx);

                       Map<String, String> ctx = new HashMap<String, String>();
                       ctx.put("omero.group", "-1");
                       server.begin_getTable(file, sf.proxy(), ctx,
                           new Ice.Callback() {
                               public void completed(Ice.AsyncResult r) {
                                   try {
                                       holder.set(server.end_getTable(r));
                                   } catch (Exception e) {
                                       holder.set(null);
                                   }
                               }});
                       }
                    });

        sf.allow(tablePrx);
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
        final ome.model.jobs.Job savedJob = saveJob(submittedJob, mapper, current);
        if (savedJob == null) {
            throw new ApiUsageException(null, null, "Could not submit job. ");
        }

        // Okay. All's valid.
        final Job job = (Job) mapper.map(savedJob);
        ResultHolder<ProcessorPrx> holder = new ResultHolder<ProcessorPrx>(seconds*1000);
        ProcessorCallbackI callback = new ProcessorCallbackI(sf, holder, job);
        ProcessorPrx server = callback.activateAndWait(current);

        // Nothing left to try
        if (server == null) {
            final int count = callback.getResponses();
            final String msg = String.format(
                    "No processor available! [%d response(s)]", count);
            updateJob(job.getId().getValue(), "Error", msg, current);
            throw new omero.NoProcessorAvailable(null, null, msg, count);
        }

        InteractiveProcessorI ip = new InteractiveProcessorI(sf.principal,
                sf.sessionManager, sf.executor, server, job, timeout,
                sf.control, paramsCache,
                new ParamsHelper(this, sf.getExecutor(), sf.getPrincipal()),
                helper, current);
        Ice.Identity procId = sessionedID("InteractiveProcessor");
        Ice.ObjectPrx rv = sf.registerServant(procId, new _InteractiveProcessorTie(ip));
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
            final IceMapper mapper, final Ice.Current current) {
        // First create the job with a status of WAITING.
        // The InteractiveProcessor will be responsible for its
        // further lifetime.
        final ome.model.jobs.Job savedJob = (ome.model.jobs.Job) sf.executor
                .execute(current.ctx, sf.principal, new Executor.SimpleWork(this,
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

    private void updateJob(final long id, final String status, final String message, final Ice.Current current) {
        sf.executor.execute(current.ctx, sf.principal, new Executor.SimpleWork(this, "updateJob") {
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

}
