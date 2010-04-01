/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.grid;

import static omero.rtypes.rmap;
import static omero.rtypes.robject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ome.api.JobHandle;
import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.services.procs.Processor;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ApiUsageException;
import omero.RMap;
import omero.RType;
import omero.ServerError;
import omero.ValidationException;
import omero.model.Job;
import omero.model.OriginalFileI;
import omero.model.ParseJob;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import Glacier2.SessionControlPrx;
import Ice.Current;

/**
 * {@link Processor} implementation which delegates to an omero.grid.Processor
 * servant. Functions as a state machine. A single {@link ProcessPrx} can be
 * active at any given time. Once it's complete, then the {@link RMap results}
 * can be obtained, then a new {@link Job} can be submitted, until {@link #stop}
 * is set. Any other use throws an {@link ApiUsageException}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class InteractiveProcessorI extends _InteractiveProcessorDisp {

    private static Session UNINITIALIZED = new Session();

    private static Log log = LogFactory.getLog(InteractiveProcessorI.class);

    private final SessionManager mgr;

    private final ProcessorPrx prx;

    private final ParamsHelper helper;

    private final Executor ex;

    private final Job job;

    private final long scriptId;

    private final long timeout;

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Principal principal;

    private final SessionControlPrx control;

    private boolean detach = false;

    private boolean obtainResults = false;

    private boolean stop = false;

    private ProcessPrx currentProcess = null;

    private Session session;

    private JobParams params;

    /**
     * 
     * @param p
     * @param mgr
     * @param prx
     * @param job
     *            Unloaded {@link Job} instance, which will be used by
     *            {@link omero.grid.Processor} to reload the {@link Job}
     * @param timeout
     */
    public InteractiveProcessorI(Principal p, SessionManager mgr, Executor ex,
            ProcessorPrx prx, Job job, long timeout, SessionControlPrx control,
            ParamsHelper helper)
        throws ServerError {

        this.helper = helper;
        this.principal = p;
        this.ex = ex;
        this.mgr = mgr;
        this.prx = prx;
        this.job = job;
        this.timeout = timeout;
        this.control = control;
        this.session = UNINITIALIZED;

        // Loading values.
        scriptId = getScriptId(job);

    }

    public JobParams params(Current __current) throws ServerError {

        rwl.writeLock().lock();

        try {

            if (stop) {
                throw new ApiUsageException(null, null,
                        "This processor is stopped.");
            }

            // Setup new user session
            if (session == UNINITIALIZED) {
                session = newSession(__current);
            }

            if (params == null) {
                try {
                    if (job instanceof ParseJob) {
                        params = prx.parseJob(session.getUuid(), job);
                        if (params == null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Can't find params for " + scriptId);
                            OriginalFile file = loadFileOrNull("stderr");
                            if (file == null) {
                                sb.append(". No stderr");
                            } else {
                                sb.append(". Stderr is in file " + file.getId());
                                appendIfText(file, sb);
                            }
                            throw new omero.ValidationException(null, null,
                                    sb.toString());
                        }

                        helper.saveScriptParams(params, (ParseJob) job,
                                __current);
                    } else {
                        params = helper.getOrCreateParams(scriptId, __current);
                    }
                } catch (Throwable t) {
                    if (t instanceof ServerError) {
                        log.debug("Error while parsing job", t);
                        throw (ServerError) t;
                    } else {
                        omero.InternalException ie = new omero.InternalException();
                        IceMapper.fillServerError(ie, t);
                        throw ie;
                    }
                }
            }
            return params;

        } finally {
            rwl.writeLock().unlock();
        }

    }

    public ProcessPrx execute(RMap inputs, Current __current)
            throws ServerError {

        rwl.writeLock().lock();

        try {

            if (currentProcess != null) {
                throw new ApiUsageException(null, null,
                        "Process currently running.");
            }

            if (obtainResults) {
                throw new ApiUsageException(null, null,
                        "Please retrieve results.");
            }

            if (stop) {
                throw new ApiUsageException(null, null,
                        "This processor is stopped.");
            }

            // Setup new user session
            if (session == UNINITIALIZED) {
                session = newSession(__current);
            }

            // Setup environment
            if (inputs != null && inputs.getValue() != null) {
                IceMapper mapper = new IceMapper();
                for (String key : inputs.getValue().keySet()) {
                    Object v = mapper.fromRType(inputs.get(key));
                    mgr.setInput(session.getUuid(), key, v);
                }
            }

            // Execute
            try {
                final String uuid = session.getUuid();
                if (params == null) {
                    params = params(__current);
                }

                currentProcess = prx.processJob(uuid, params, job);

                // Have to add the process to the control, otherwise the
                // user won't be able to view it: ObjectNotExistException!
                // ticket:1522
                control.identities().add(
                        new Ice.Identity[]{currentProcess.ice_getIdentity()});

            } catch (omero.ValidationException ve) {
                failJob(ve);
                throw ve;
            } catch (ServerError se) {
                log.debug("Error while processing job", se);
                throw se;
            }

            if (currentProcess == null) {
                return null;
            }

            obtainResults = true;
            return currentProcess;

        } finally {
            rwl.writeLock().unlock();
        }

    }

    public RMap getResults(ProcessPrx proc, Current __current)
            throws ServerError {

        rwl.writeLock().lock();
        try {
            finishedOrThrow();

            // Gather output
            omero.RMap output = rmap(new HashMap<String, omero.RType>());
            Map<String, Object> env = mgr.outputEnvironment(session.getUuid());
            IceMapper mapper = new IceMapper();
            for (String key : env.keySet()) {
                RType rt = mapper.toRType(env.get(key));
                output.put(key, rt);
            }
            optionallyLoadFile(output.getValue(), "stdout");
            optionallyLoadFile(output.getValue(), "stderr");
            currentProcess = null;
            obtainResults = false;
            return output;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public long expires(Current __current) {
        return timeout;
    }

    public Job getJob(Current __current) {
        return job;
    }

    public boolean setDetach(boolean detach, Current __current) {
        rwl.writeLock().lock();
        try {
            boolean old = this.detach;
            this.detach = detach;
            return old;
        } finally {
            rwl.writeLock().unlock();
        }
    }
    
    /**
     * Cancels the current process, nulls the value, and returns immediately
     * if detach is false.
     */
    public void stop(Current __current) throws ServerError {
        rwl.writeLock().lock();
        
        if (stop) {
            return; // Already stopped.
        }
        
        try {
            
            if (detach) {
                if (currentProcess != null) {
                    log.info("Detaching from " + currentProcess);
                }
            } else {
                doStop();
            }

        stop = true;
            
        } finally {
            rwl.writeLock().unlock();
        }
    }

    protected void doStop() throws ServerError {
        // Then perform cleanup
        Exception pException = null;
        Exception sException = null;
        
        if (currentProcess != null) {
            try {
                ProcessPrx p = ProcessPrxHelper.uncheckedCast(
                        currentProcess.ice_oneway());
                p.shutdown();
                currentProcess = null;
            } catch (Exception ex) {
                log.warn("Failed to stop process", ex);
                pException = ex;
            }
        }
        
        if (session != null && session != UNINITIALIZED) {
            try {
                while (mgr.close(session.getUuid()) > 0);
                session = null;
            } catch (Exception ex) {
                log.warn("Failed to close session " + session.getUuid(), ex);
                sException = ex;
            }
        }
        
        if (pException != null || sException != null) {
            omero.InternalException ie = new omero.InternalException();
            StringBuilder sb = new StringBuilder();
            if (pException != null) {
                sb.append("Failed to shutdown process: " + pException.getMessage());
            }
            if (sException != null) {
                sb.append("Failed to close session: " + sException.getMessage());
            }
            ie.message = sb.toString();
            throw ie;
        }

    }
    
    // Helpers
    // =========================================================================

    private void finishedOrThrow() throws ServerError {
        if (currentProcess == null) {
            throw new ApiUsageException(null, null, "No current process.");
        } else if (currentProcess.poll() == null) {
            throw new ApiUsageException(null, null, "Process still running.");
        }
    }

    private final static String stdfile_query = "select file from Job job "
            + "join job.originalFileLinks links join links.child file "
            + "join fetch file.format where file.name = :name and job.id = :id";

    private OriginalFile loadFileOrNull(final String name) {
        return (OriginalFile) this.ex.execute(this.principal, new Executor.SimpleWork(this, "optionallyLoadFile") {
            @Transactional(readOnly=true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {

                return sf.getQueryService().findByQuery(
                        stdfile_query,
                        new Parameters().addId(job.getId().getValue())
                                .addString("name", name));
            }
        });
    }

    private void optionallyLoadFile(final Map<String, RType> val,
            final String name) {

        OriginalFile file = loadFileOrNull(name);
        if (file != null) {
            val.put(name,
            robject(new OriginalFileI(file.getId(), false)));
        }
    }

    private void appendIfText(final OriginalFile file, final StringBuilder sb) {
        if (file.getFormat().getValue().contains("text")) {
            this.ex.execute(this.principal, new Executor.SimpleWork(this, "appendIfText", file) {
                @Transactional(readOnly=true)
                public Object doWork(org.hibernate.Session session,
                        ServiceFactory sf) {
                    RawFileStore rfs = sf.createRawFileStore();
                    try {
                        rfs.setFileId(file.getId());
                        sb.append("\n\n---stderr---\n");
                        sb.append(new String(rfs.read(0, file.getSize().intValue())));
                    } finally {
                        rfs.close();
                    }
                    return null;
                }
            });
        }
    }

    private void failJob(final ValidationException ve) {
        this.ex.execute(this.principal, new Executor.SimpleWork(this, "failJob", job.getId().getValue()) {
            @Transactional(readOnly=false)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                JobHandle jh = sf.createJobHandle();
                try {
                    jh.attach(job.getId().getValue());
                    jh.setStatusAndMessage("Error", ve.message);
                } finally {
                    jh.close();
                }
                return null;
            }
        });
    }

    private Session newSession(Current __current) {
        EventContext ec = mgr.getEventContext(principal);
        Session newSession = mgr.createWithAgent(
                new Principal(ec.getCurrentUserName(),
                ec.getCurrentGroupName(), "Processing"), "OMERO.scripts");
        newSession.setTimeToIdle(0L);
        newSession.setTimeToLive(timeout);
        newSession = mgr.update(newSession, true);

        return newSession;
    }

    private String getScriptIdQuery = "select f from Job s "
            + "join s.originalFileLinks links "
            + "join links.child f "
            + "where s.id = :id and f.format.value = :fmt";

    private long getScriptId(final Job job) throws omero.ValidationException {
        final Parameters p = new Parameters();
        p.addId(job.getId().getValue());
        p.addString("fmt", "text/x-python");
        final OriginalFile f = (OriginalFile) this.ex.execute(this.principal,
                new Executor.SimpleWork(this, "getScriptId") {
                    @Transactional(readOnly = true)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                                getScriptIdQuery, p);
                    }
                });
        if (f == null) {
            throw new omero.ValidationException(null, null,
                    "No script for job :" + job.getId().getValue());
        }
        return f.getId();
    }

}
