/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

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
import omero.ApiUsageException;
import omero.ServerError;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.ProcessorPrx;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.TablePrx;
import omero.grid.TablesPrx;
import omero.grid._GridServicesOperations;
import omero.model.Job;
import omero.model.JobStatus;
import omero.model.JobStatusI;
import omero.model.OriginalFile;
import omero.model.Repository;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * Implementation of the GridServices interface.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.1
 * @see ome.grid.GridServices
 */
public class GridServicesI extends AbstractAmdServant implements
        _GridServicesOperations, BlitzOnly, ServiceFactoryAware {

    private final TopicManager topicManager;

    private final Registry registry;

    private ServiceFactoryI sf;

    public GridServicesI(BlitzExecutor be, TopicManager topicManager,
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

    public RepositoryMap acquireRepositories(Current current)
            throws ServerError {
        // Possibly need to throttle the numbers of acquisitions per time.
        // Need to keep up with closing
        // might need to cache the found repositories.

        InternalRepositoryPrx[] repos = registry.lookupRepositories();

        RepositoryMap map = new RepositoryMap();
        map.descriptions = new ArrayList<Repository>();
        map.proxies = new ArrayList<RepositoryPrx>();

        for (InternalRepositoryPrx i : repos) {
            if (i == null) {
                continue;
            }
            Repository desc = i.getDescription();
            RepositoryPrx proxy = i.getProxy();
            map.descriptions.add(desc);
            map.proxies.add(proxy);
        }

        return map;
    }

    public TablePrx acquireTable(OriginalFile file, Current __current)
            throws ServerError {
        return null;
    }

    @SuppressWarnings("unchecked")
    public TablePrx acquireWritableTable(final OriginalFile file, int seconds,
            Current __current) throws ServerError {

        checkAcquisitionWait(seconds);

        // Now make sure the current user has permissions to do this
        if (file == null) {

            return null;

        } else if (file.getId() != null) {

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

        } else {

            // Overwrites
            omero.RTime creation = omero.rtypes.rtime(System
                    .currentTimeMillis());
            file.setCtime(creation);
            file.setAtime(creation);
            file.setMtime(creation);
            file.setSha1(omero.rtypes.rstring("DIR"));
            file.setFormat(new omero.model.FormatI());
            file.getFormat().setValue(omero.rtypes.rstring("OMERO.tables"));
            file.setRepository(null);
            file.setPath(omero.rtypes.rstring("TBD"));
            file.setSize(omero.rtypes.rlong(0));
            IceMapper mapper = new IceMapper();
            final ome.model.core.OriginalFile f = (ome.model.core.OriginalFile) mapper
                    .reverse(file);
            Long id = (Long) sf.executor.execute(sf.principal,
                    new Executor.SimpleWork(this, "saveNewOriginalFile", file
                            .getName().getValue()) {
                        @Transactional(readOnly = false)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return sf.getUpdateService().saveAndReturnObject(f)
                                    .getId();
                        }
                    });
            file.setId(omero.rtypes.rlong(id));
            file.unload();

        }

        // Okay. All's valid.
        TablesPrx[] tables = registry.lookupTables();
        TablePrx tablePrx = (TablePrx) repeatLookup(Arrays.asList(tables),
                seconds, new RepeatTask<TablesPrx, TablePrx>() {
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
