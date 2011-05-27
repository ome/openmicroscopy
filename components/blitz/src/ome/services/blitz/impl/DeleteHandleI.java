/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ome.conditions.InternalException;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.PixelsService;
import ome.services.delete.DeleteStepFactory;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.LockTimeout;
import omero.ServerError;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteReport;
import omero.api.delete._DeleteHandleDisp;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * Servant for the handle proxy from the IDelete service. This is also a
 * {@link Runnable} and is passed to a ThreadPool instance
 *
 * <pre>
 * Transitions:
 *
 *      +------------------o [FINISHED]
 *      |                        o
 *      |                        |
 * (CREATED) ---o READY o===o RUNNING o===o CANCELLING ---o [CANCELLED]
 *      |           |                           o                o
 *      |           |---------------------------|                |
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


        private static class Report extends DeleteReport {
            private static final long serialVersionUID = 1L;
            GraphSpec spec;
            GraphState state;
        }


    private static final long serialVersionUID = 1592043520935825L;

    private static final Log log = LogFactory.getLog(DeleteHandleI.class);

    private static final List<String> fileTypeList = Collections.unmodifiableList(
            Arrays.asList( "OriginalFile", "Pixels", "Thumbnail"));
    
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
     * {@link Session} in a background thread.
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

    private final AbstractFileSystemService afs;

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
    public DeleteHandleI(final ApplicationContext ctx, final Ice.Identity id, final ServiceFactoryI sf,
            final AbstractFileSystemService afs, final DeleteCommand[] commands, int cancelTimeoutMs) {
        this.id = id;
        this.sf = sf;
        this.afs = afs;
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
                    report.spec = ctx.getBean(report.command.type, GraphSpec.class);
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
                } catch (GraphException de) {
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

    public DeleteReport[] report(Current __current) throws ServerError {
        DeleteReport[] rv = new DeleteReport[commands.length];
        for (int i = 0; i < commands.length; i++) {
            Integer idx = Integer.valueOf(i);
            rv[i] = reports.get(idx);
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
                    state.compareAndSet(State.READY, State.CANCELLING) ||
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
            log.warn("Can't reset to RUNNING. State already changed.\n"
                    + "This could be caused either by another thread having\n"
                    + "already set the state back to RUNNING, or by the state\n"
                    + "having changed to CANCELLED. In either case, it is safe\n"
                    + "to throw the exception, and have the user recall cancel.");
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
        sf.unregisterServant(id);
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

        StopWatch sw = new CommonsLogStopWatch();
        try {
            executor.execute(principal, new Executor.SimpleWork(this, "run",
                    Ice.Util.identityToString(id), "size=" + commands.length) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    try {
                        doRun(getSqlAction(), session);
                        state.compareAndSet(State.READY, State.FINISHED);
                    } catch (Cancel c) {
                        state.set(State.CANCELLED);
                        throw c; // Exception intended to rollback transaction
                    }
                    return null;
                }
            });

        } catch (Throwable t) {
            if (t instanceof Cancel) {
                log.debug("Delete cancelled by " + t.getCause());
            } else {
                log.debug("Delete rolled back by " + t.getCause());
            }
        } finally {
            sw.stop("omero.delete.tx");
        }

        // If the delete has succeeded try to delete the associated files.
        if (state.get() == State.FINISHED) {
            sw = new CommonsLogStopWatch();
            try {
                deleteFiles();
            } finally {
                sw.stop("omero.delete.binary");
            }
        }
    }

    public void doRun(SqlAction sql, Session session) throws Cancel {
        for (int i = 0; i < commands.length; i++) {
            Integer idx = Integer.valueOf(i);
            Report report = reports.get(idx);

            // Handled during initialization.
            if (report.error != null) {
                log.info("Initialization cancelled " + report.command.type
                        + ":" + report.command.id + " -- " + report.error);
                continue;
            } else {
                log.info("Deleting " + report.command.type
                        + ":" + report.command.id);
            }

            StopWatch sw = new CommonsLogStopWatch();
            try {
                steps(sql, session, report);
            } finally {
                sw.stop("omero.delete.command." + i);
                report.start = sw.getStartTime();
                report.stop = sw.getStartTime() + sw.getElapsedTime();
            }
        }
    }

    public void steps(SqlAction sql, Session session, Report report) throws Cancel {
        try {

            // Initialize. Any exceptions should cancel the process
            StopWatch sw = new CommonsLogStopWatch();
            DeleteStepFactory factory = new DeleteStepFactory(executor.getContext());
            report.state = new GraphState(factory, sql, session, report.spec);
            report.scheduledDeletes = report.state.getTotalFoundCount();
            if (report.scheduledDeletes == 0L) {
                report.warning = "Object missing.";
                return;
            }
            if (report.scheduledDeletes < Integer.MAX_VALUE) {
                report.stepStarts = new long[(int)report.scheduledDeletes];
                report.stepStops = new long[(int)report.scheduledDeletes];
            }
            sw.stop("omero.delete.ids." + report.scheduledDeletes);

            // Loop throw all steps
            report.warning = "";
            for (int j = 0; j < report.scheduledDeletes; j++) {
                sw = new CommonsLogStopWatch();
                try {
                    if (!state.compareAndSet(State.READY, State.RUNNING)) {
                        throw new Cancel("Not ready");
                    }
                    report.warning += report.state.execute(j);
                } finally {
                    sw.stop("omero.delete.step." + j);
                    if (report.scheduledDeletes < Integer.MAX_VALUE) {
                        report.stepStarts[j] = sw.getStartTime();
                        report.stepStops[j] = sw.getStartTime() + sw.getElapsedTime();
                    }
                    // If cancel was thrown, then this value will be overwritten
                    // by the try/catch handler
                    state.compareAndSet(State.RUNNING, State.READY);
                }
            }
            // If we reach this far, then the delete was successful, so save
            // the deleted id count.
            report.actualDeletes = report.state.getTotalProcessedCount();
        } catch (GraphException de) {
            report.error = de.message;
            Cancel cancel = new Cancel("Cancelled by DeleteException");
            cancel.initCause(de);
            throw cancel;
        } catch (ConstraintViolationException cve) {
            report.error = "ConstraintViolation: " + cve.getConstraintName();
            Cancel cancel = new Cancel("Cancelled by " + report.error);
            cancel.initCause(cve);
            throw cancel;
        } catch (Throwable t) {
            String msg = "Failure during DeleteHandle.steps :";
            report.error = (msg + t);
            log.error(msg, t);
            Cancel cancel = new Cancel("Cancelled by " + t.getClass().getName());
            cancel.initCause(t);
            throw cancel;
        }

    }

    /**
     * For each Report use the map of tables to deleted ids to remove the files
     * under Files, Pixels and Thumbnails if the ids no longer exist in the db.
     * Create a map of failed ids (not yet passed back to client).
      */
    private void deleteFiles() {

        File file;
        String filePath;

        HashMap<String, ArrayList<Long>> failedMap = new HashMap<String, ArrayList<Long>>();
        long bytesFailed;
        long filesFailed;
        
        for (Report report : reports.values()) {
            bytesFailed = 0;
            filesFailed = 0;
            for (String fileType : fileTypeList) {
                Set<Long> deletedIds = report.state.getProcessedIds(fileType);
                failedMap.put(fileType, new ArrayList<Long>());
                if (deletedIds != null && deletedIds.size() > 0) {
                    log.debug(String.format("Binary delete of %s for %s:%s: %s",
                            fileType, report.command.type, report.command.id,
                            deletedIds));
                    for (Long id : deletedIds) {
                        if (fileType.equals("OriginalFile")) {
                            filePath = afs.getFilesPath(id);
                            file = new File(filePath);
                        } else if (fileType.equals("Thumbnail")) {
                            filePath = afs.getThumbnailPath(id);
                            file = new File(filePath);
                        } else { // Pixels
                            filePath = afs.getPixelsPath(id);
                            file = new File(filePath);
                            // Try to remove a _pyramid file if it exists
                            File pyrFile = new File(filePath + PixelsService.PYRAMID_SUFFIX);
                            if(!deleteSingleFile(pyrFile)) {
                                failedMap.get(fileType).add(id);
                                filesFailed++;
                                bytesFailed += pyrFile.length();
                            }
                            File dir = file.getParentFile();
                            // Now any lock file
                            File lockFile = new File(dir, "." + id + PixelsService.PYRAMID_SUFFIX
                                    + BfPyramidPixelBuffer.PYR_LOCK_EXT);
                            if(!deleteSingleFile(lockFile)) {
                                failedMap.get(fileType).add(id);
                                filesFailed++;
                                bytesFailed += lockFile.length();
                            }
                            // Now any tmp files
                            FileFilter tmpFileFilter = new WildcardFileFilter("."
                                    + id + PixelsService.PYRAMID_SUFFIX + "*.tmp");
                            File[] tmpFiles = dir.listFiles(tmpFileFilter);
                            for (int i = 0; i < tmpFiles.length; i++) {
                                if(!deleteSingleFile(tmpFiles[i])) {
                                    failedMap.get(fileType).add(id);
                                    filesFailed++;
                                    bytesFailed += tmpFiles[i].length();
                                }
                            }
                        }
                        // Finally delete main file for any type.
                        if(!deleteSingleFile(file)) {
                            failedMap.get(fileType).add(id);
                            filesFailed++;
                            bytesFailed += file.length();
                        }                        
                    }
                }
            }

            report.undeletedFiles = new HashMap<String, long[]>();
            for (String key : failedMap.keySet()) {
                List<Long> ids = failedMap.get(key);
                long[] array = new long[ids.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = ids.get(i);
                }
                report.undeletedFiles.put(key, array);
            }
            if (filesFailed > 0) {
                String warning = "Warning: " + Long.toString(filesFailed) + " file(s) comprising "
                        + Long.toString(bytesFailed) + " bytes were not removed.";
                report.warning += warning;
                log.warn(warning);
            }
            if (log.isDebugEnabled()) {
                for (String table : failedMap.keySet()) {
                    log.debug("Failed to delete files : " + table + ":"
                            + failedMap.get(table).toString());
                }
            }
        }
    }
    
    /**
     * Helper to delete and log
     */
    private boolean deleteSingleFile(File file)
    {
        if (file.exists()) {
            if (file.delete()) {
                log.debug("DELETED: " + file.getAbsolutePath());
            } else {
                log.debug("Failed to delete " + file.getAbsolutePath());
                return false;
            }
        } else {
            log.debug("File " + file.getAbsolutePath() + " does not exist.");
        }
        return true;
    }

    /**
     * Signals that {@link DeleteHandleI#run()} has noticed that
     * {@link DeleteHandleI#state} wants a cancallation.
     */
    private static class Cancel extends InternalException {

        private static final long serialVersionUID = 1L;

        public Cancel(String message) {
            super(message);
        }

    };
}
