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
import omero.model.Job;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

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

    private final Executor ex;

    private final Job job;

    private final long timeout;

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Principal principal;

    private boolean detach = false;
    
    private boolean obtainResults = false;

    private boolean stop = false;

    private ProcessPrx currentProcess = null;

    private Session session;

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
            ProcessorPrx prx, Job job, long timeout) {
        this.principal = p;
        this.ex = ex;
        this.mgr = mgr;
        this.prx = prx;
        this.job = job;
        this.timeout = timeout;
        this.session = UNINITIALIZED;
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

            try {
                return prx.parseJob(session.getUuid(), job);
            } catch (ServerError se) {
                log.debug("Error while parsing job", se);
                throw se;
            }

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
                for (String key : inputs.getValue().keySet()) {
                    mgr.setInput(session.getUuid(), key, inputs.get(key));
                }
            }

            // Execute
            try {
                currentProcess = prx.processJob(session.getUuid(), job);
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
     * Cancels the current process, nulls the value, and returns immediately.
     */
    public void stop(Current __current) {
        rwl.writeLock().lock();
        try {
            if (currentProcess != null) {
                if (!detach) {
                    currentProcess.cancel();
                } else {
                    log.info("Detaching from "+currentProcess);
                }
            }
            currentProcess = null;
            stop = true;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    // Helpers
    // =========================================================================

    private void finishedOrThrow() throws ApiUsageException {
        if (currentProcess == null) {
            throw new ApiUsageException(null, null, "No current process.");
        } else if (currentProcess.poll() == null) {
            throw new ApiUsageException(null, null, "Process still running.");
        }
    }

    private final static String stdfile_query = "select file from Job job "
            + "join job.originalFileLinks links " + "join links.child file "
            + "where file.name = :name and job.id = :id";

    private void optionallyLoadFile(final Map<String, RType> val,
            final String name) {
        this.ex.execute(this.principal, new Executor.SimpleWork(this, "optionallyLoadFile") {
            @Transactional(readOnly=true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {

                OriginalFile file = sf.getQueryService().findByQuery(
                        stdfile_query,
                        new Parameters().addId(job.getId().getValue())
                                .addString("name", name));
                if (file != null) {
                    val.put(name,
                            robject(new OriginalFileI(file.getId(), false)));
                }
                return null;
            }
        });
    }

    private Session newSession(Current __current) {
        EventContext ec = mgr.getEventContext(principal);
        Session newSession = mgr.create(new Principal(ec.getCurrentUserName(),
                ec.getCurrentGroupName(), "Processing"));
        newSession.setTimeToIdle(0L);
        newSession.setTimeToLive(timeout);
        newSession = mgr.update(newSession, true);

        return newSession;
    }
}
