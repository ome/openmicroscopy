/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ome.conditions.InternalException;
import ome.io.nio.OriginalFilesService;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.services.delete.DeleteException;
import ome.services.delete.DeleteIds;
import ome.services.delete.DeleteSpec;
import ome.services.delete.DeleteSpecFactory;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.LockTimeout;
import omero.ServerError;
import omero.api.delete.DeleteCommand;
import omero.api.delete._DeleteHandleDisp;
import omero.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * Servant for the handle proxy from the IDelete service. This is also a
 * {@link Runnable} and is passed to a {@link ThreadPool} instance
 *
 * <pre>
 * Transitions:
 *
 *      +------------------o [FINISHED]
 *      |                        o
 *      |                        |
 * (CREATED) ---o READY o===o RUNNING o===o CANCELLING ---o [CANCELLED]
 *      |                                                        o
 *      |                                                        |
 *      +--------------------------------------------------------+
 *
 * </pre>
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.2.1
 * @see ome.api.IDelete
 */
public class DeleteHandleI extends _DeleteHandleDisp implements
        CloseableServant, Runnable {

    private static enum State {
        CREATED, READY, RUNNING, CANCELLING, CANCELLED, FINISHED;
    }

    private static class Report {

        DeleteCommand command;
        DeleteSpec spec;
        
        long start;
        int steps;
        long[] stepStarts;
        long[] stepStops;
        long stop;

        String warning;
        String error;
    }

    private static final long serialVersionUID = 1592043520935825L;

    private static final Log log = LogFactory.getLog(DeleteHandleI.class);

    /**
     * The identity of this servant, used during logging and similar operations.
     */
    private final Ice.Identity id;

    /**
     * The principal, i.e. the session information, about the current users
     * logins. This will be passed to the {@link #executor} instance for logging
     * in.
     */
    private final Principal principal;

    /**
     * Executor which will be used to provide access to the Hibernate
     * {@link Session} in a backgrond thread.
     */
    private final Executor executor;

    /**
     * {@link DeleteCommand} instances passed into this instance on creation. No
     * methods will modify this array, but they may be returned to the client.
     */
    private final DeleteCommand[] commands;

    /**
     * Map from index numbers to a {@link Report} instance. Messages, errors,
     * and similar from the {@link #run()} method will be stored for later
     * checking by the client.
     */
    private final ConcurrentHashMap<Integer, Report> reports = new ConcurrentHashMap<Integer, Report>();

    /**
     * Timeout in seconds that cancellation should wait.
     */
    private final int cancelTimeoutMs;

    /**
     * State-diagram location. This instance is also used as a lock around
     * certain critical sections.
     */
    private final AtomicReference<State> state = new AtomicReference<State>();

	private final ServiceFactoryI sf;


    /**
     * Create and
     *
     * @param id
     * @param sf
     * @param factory
     * @param commands
     * @param cancelTimeoutMs
     */
    public DeleteHandleI(final Ice.Identity id, final ServiceFactoryI sf,
            final DeleteSpecFactory factory, final DeleteCommand[] commands,
            int cancelTimeoutMs) {
        this.id = id;
        this.sf = sf;
        this.principal = sf.getPrincipal();
        this.executor = sf.getExecutor();
        this.cancelTimeoutMs = cancelTimeoutMs;
        this.state.set(State.CREATED);

        if (commands == null || commands.length == 0) {
            this.commands = new DeleteCommand[0];
            this.state.set(State.FINISHED);
        } else {
            this.commands = new DeleteCommand[commands.length];
            System.arraycopy(commands, 0, this.commands, 0, commands.length);

            for (int i = 0; i < commands.length; i++) {
                Integer idx = Integer.valueOf(i);
                Report report = new Report();
                reports.put(idx, report);

                report.command = commands[i];
                if (report.command == null) {
                    report.error = "Command is null";
                    continue;
                }

                try {
                    report.spec = factory.get(report.command.type);
                    if (report.spec == null) {
                        throw new NullPointerException(); // handled in catch
                    }
                } catch (Exception e) {
                    report.error = ("Specification not found: " + report.command.type);
                    continue;
                }

                try {
                    report.steps = report.spec.initialize(
                            report.command.id,
                            "",
                            report.command.options);
                    report.stepStarts = new long[report.steps];
                    report.stepStops = new long[report.steps];
                } catch (DeleteException de) {
                    report.error = ("Failed initialization: " + de.message);
                }

            }
        }
    }

    //
    // DeleteHandle. See documentation in slice definition.
    //

    public DeleteCommand[] commands(Current __current) throws ServerError {
        return commands;
    }

    public boolean finished(Current __current) throws ServerError {
        State s = state.get();
        if (State.FINISHED.equals(s) || State.CANCELLED.equals(s)) {
            return true;
        }
        return false;
    }

    public int errors(Current __current) throws ServerError {
        int errors = 0;
        for (Report report : reports.values()) {
            if (report.error != null) {
                errors++;
            }
        }
        return errors;
    }

    public List<String> report(Current __current) throws ServerError {
        List<String> rv = new ArrayList<String>(commands.length);
        for (int i = 0; i < commands.length; i++) {
            Integer idx = Integer.valueOf(i);
            Report report = reports.get(idx);
            if (report != null) {
                if (report.error != null) {
                    rv.add(report.error);
                } else {
                    rv.add(report.warning);
                }
            }
        }
        return rv;
    }

    public boolean cancel(Current __current) throws ServerError {

        // If we can successfully catch the CREATED or READY state,
        // then there's no reason to wait for anything else.
        if (state.compareAndSet(State.CREATED, State.CANCELLED) ||
                state.compareAndSet(State.READY, State.CANCELLED)) {
            return true;
        }

        long start = System.currentTimeMillis();
        while (cancelTimeoutMs >= (System.currentTimeMillis() - start)) {

            // This is the most important case. If things are running, then
            // we want to set "CANCELLING" as quickly as possible.
            if (state.compareAndSet(State.RUNNING, State.CANCELLING) ||
                    state.compareAndSet(State.CANCELLING, State.CANCELLING)) {

                try {
                    Thread.sleep(cancelTimeoutMs / 10);
                } catch (InterruptedException e) {
                    // Igoring the interruption since the while block
                    // will properly handle another iteration.
                }

            }

            // These are end states, so we'll just return.
            // See the transition states in the class javadoc
            if (state.compareAndSet(State.CANCELLED, State.CANCELLED)) {
                return true;

            } else if (state.compareAndSet(State.FINISHED, State.FINISHED)) {
                return false;
            }

        }

        // The only time that state gets set to CANCELLING is in the try
        // block above. If we've exited the while without switching to CANCELLED
        // then we've failed, and the value should be rolled back.
        //
        // If #run() noticed this before hand, then it would already have
        // moved from RUNNING to CANCELLED, and so the following statement would
        // return false, in which case we print out a warning so that in a later
        // version we can be more careful about retrying.
        if (!state.compareAndSet(State.CANCELLING, State.RUNNING)) {
            log.warn("Can't reset to RUNNING. State already changed.\n" +
			"This could be caused either by another thread having\n" +
			"already set the state back to RUNNING, or by the state\n" +
			"having changed to CANCELLED. In either case, it is safe\n" +
			"to throw the exception, and have the user recall cancel.");
        }

        LockTimeout lt = new LockTimeout();
        lt.backOff = 5000;
        lt.message = "timed out while waiting on CANCELLED state";
        lt.seconds = cancelTimeoutMs / 1000;
        throw lt;

    }

    //
    // CloseableServant. See documentation in interface.
    //

    public void close(Ice.Current current) throws ServerError {
        if (!finished(current)) {
            log.warn("Handle closed before finished! State=" + state.get());
        }
    }

    //
    // Runnable
    //

    /**
     *
     * NB: only executes if {@link #state} is {@link State#CREATED}.
     */
    public void run() {

        // If we're not in the created state, then do nothing
        // since something has gone wrong.
        if (!state.compareAndSet(State.CREATED, State.READY)) {
            return; // EARLY EXIT!
        }

        executor.execute(principal, new Executor.SimpleWork(this, "run",
                Ice.Util.identityToString(id), "size=" + commands.length) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                try {
                    doRun(session);
                    state.compareAndSet(State.READY, State.FINISHED);
                } catch (Cancel c) {
                    state.set(State.CANCELLED);
                    throw c;
                }
                return null;
            }
        });
        
        /*
         * If the delete has succeeded try to delete the associated files.
         */
        if (state.get() == State.FINISHED) {
        	deleteFiles();
        }
    }

    public void doRun(Session session) throws Cancel {
        for (int i = 0; i < commands.length; i++) {
            Integer idx = Integer.valueOf(i);
            Report report = reports.get(idx);

            // Handled during initialization.
            if (report.error != null) {
                continue;
            }

            report.start = System.currentTimeMillis();
            try {
                steps(session, report);
            } finally {
                report.stop = System.currentTimeMillis();
            }
        }
    }

    public void steps(Session session, Report report) throws Cancel {
        DeleteIds ids = null;
        for (int j = 0; j < report.steps; j++) {
            report.stepStarts[j] = System.currentTimeMillis();
            try {

                if (!state.compareAndSet(State.READY, State.RUNNING)) {
                    throw new Cancel("Not ready");
                }

                // Initialize on the first. Any exceptions should
                // cancel the whole process.
                if (ids == null) {
                    ids = new DeleteIds(session, report.spec);
                }
                report.warning = report.spec.delete(session, j, ids);
             } catch (DeleteException de) {
                report.error = de.message;
                if (de.cancel) {
                    Cancel cancel = new Cancel("Cancelled by DeleteException");
                    cancel.initCause(de);
                    throw cancel;
                }
            } catch (ConstraintViolationException cve) {
                report.error = "ConstraintViolation: " + cve.getConstraintName();
            } catch (Throwable t) {
                String msg = "Failure during DeleteHandle.steps :";
                report.error = (msg + t);
                log.error(msg, t);
                Cancel cancel = new Cancel("Cancelled by " + t.getClass().getName());
                cancel.initCause(t);
                throw cancel;
            } finally {
                report.stepStops[j] = System.currentTimeMillis();
                // If cancel was thrown, then this value will be overwritten
                // by the try/catch handler
                state.compareAndSet(State.RUNNING, State.READY);
            }
        }

    }
    
    /**
     * For each Report use the map of tables to deleted ids
     * to remove the files under Files, Pixels and Thumbnails
     * if the ids no longer exist in the db
     */
    public void deleteFiles() {
    	HashMap<String, List<Long>> idMap;
    	
     	for(Report report : reports.values()) {
       		log.info("specstring: " + report.spec.toString());
       		idMap = (HashMap<String, List<Long>>) report.spec.getTableIds();
            if (log.isDebugEnabled()) {
            	for(String table : idMap.keySet()) {
       				List<Long> ids = idMap.get(table);
       				log.debug("    Deleted " + table + ":" + ids.toString());
       			}
            }
       		if (idMap.containsKey("OriginalFile")) {
       			log.info("Trying to delete OriginalFiles : " + idMap.get("OriginalFile").toString());
       			List<Long> deletedIds = new ArrayList<Long>();
       			for(Long id : idMap.get("OriginalFile")) {
       				try {
       					ome.model.IObject obj = (ome.model.IObject) sf.getQueryService().find("OriginalFile", id);
       					if(obj == null) {
       						deletedIds.add(id);
       					} else {
       						log.info("OriginalFile " + id.toString() + " not deleted, still in database");
       					}
       				} catch (ServerError se) {
       					log.error("OriginalFile "  + id.toString() + " not deleted: ServerError: " + se.toString());
       				}
       			}
       			if(!deletedIds.isEmpty()) {
       				OriginalFilesService os = new OriginalFilesService("/OMERO");
       				os.removeFiles(deletedIds);
       			}
       		}
       		if (idMap.containsKey("Pixels")) {
       			log.info("Trying to delete Pixels : " + idMap.get("Pixels").toString());
       			List<Long> deletedIds = new ArrayList<Long>();
      			for(Long id : idMap.get("Pixels")) {
       				try {
       					ome.model.IObject obj = (ome.model.IObject) sf.getQueryService().find("Pixels", id);
       					if(obj == null) {
       						deletedIds.add(id);
       					} else {
       						log.info("Pixels " + id.toString() + " not deleted, still in database");
       					}
       				} catch (ServerError se) {
       					log.error("Pixels "  + id.toString() + " not deleted: ServerError: " + se.toString());
       				}
      			}
       			if(!deletedIds.isEmpty()) {
       				PixelsService ps = new PixelsService("/OMERO");
       				ps.removePixels(deletedIds);
       			}
       		}
       		if (idMap.containsKey("Thumbnail")) {
       			log.info("Trying to delete Thumbnails : " + idMap.get("Thumbnail").toString());
       			List<Long> deletedIds = new ArrayList<Long>();
       			for(Long id : idMap.get("Thumbnail")) {
       				try {
       					ome.model.IObject obj = (ome.model.IObject) sf.getQueryService().find("Thumbnail", id);
       					if(obj == null) {
       						deletedIds.add(id);
       					} else {
       						log.info("Thumbnail " + id.toString() + " not deleted, still in database");
       					}
       				} catch (ServerError se) {
       					log.error("Thumbnail "  + id.toString() + " not deleted: ServerError: " + se.toString());
       				}
       			}
       			if(!deletedIds.isEmpty()) {
       				ThumbnailService ts = new ThumbnailService("/OMERO");
       				ts.removeThumbnails(deletedIds);
       			}
      		}
       	}
    }

    /**
     * Signals that {@link DeleteHandleI#run()} has noticed that
     * {@link DeleteHandleI#state} wants a cancallation.
     */
    private static class Cancel extends InternalException {
        public Cancel(String message) {
            super(message);
        }

    };
}
