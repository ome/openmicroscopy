/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import ome.conditions.InternalException;
import ome.io.messages.MissingPyramidMessage;
import ome.model.core.Pixels;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public class PixelDataThread extends ExecutionThread implements ApplicationListener<MissingPyramidMessage> {

    private final static Log log = LogFactory.getLog(PixelDataThread.class);

    private final static Principal DEFAULT_PRINCIPAL = new Principal("root",
            "system", "Task");

    private final static int DEFAULT_THREADS = 1;

    /** Server session UUID */
    private final String uuid;

    /** Number of threads that should be used for processing **/
    private final int numThreads;

    /**
     * Whether this thread should perform actual processing or simply add a
     * PIXELDATA {@link EventLog}. For the moment, this is determined based
     * on whether "pixelDataTrigger" is defined. If yes, then this is the
     * standalone pixel data processor using the ome/services/pixeldata.xml
     * Spring configuration. Otherwise, it's the main blitz process.
     */
    private final boolean performProcessing;

    /**
     * Uses default {@link Principal} for processing
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler, String uuid) {
        this(manager, executor, handler, DEFAULT_PRINCIPAL, uuid, DEFAULT_THREADS);
    }

    /**
     * Uses default {@link Principal} for processing
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler, String uuid, int numThreads) {
        this(manager, executor, handler, DEFAULT_PRINCIPAL, uuid, numThreads);
    }

    /**
     * Calculates {@link #performProcessing} based on the existence of the
     * "pixelDataTrigger" and passes all parameters to
     * {@link #PixelDataThread(boolean, SessionManager, Executor, PixelDataHandler, Principal, String, int) the main ctor}.
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler, Principal principal, String uuid,
            int numThreads) {
        this(executor.getContext().containsBean("pixelDataTrigger"),
            manager, executor, handler, principal, uuid, numThreads);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public PixelDataThread(boolean performProcessing,
            SessionManager manager, Executor executor,
            PixelDataHandler handler, Principal principal, String uuid,
            int numThreads) {
        super(manager, executor, handler, principal);
        this.performProcessing = performProcessing;
        this.uuid = uuid;
        this.numThreads = numThreads;
    }

    /**
     * Called by Spring on creation. Currently a no-op.
     */
    public void start() {
        StringBuilder sb = new StringBuilder();
        sb.append("Initializing PixelDataThread");
        if (performProcessing) {
            sb.append(String.format(" (threads=%s)", numThreads));
        } else {
            sb.append(" (create events only)");
        }
        log.info(sb.toString());
    }

    /**
     * Retry the work unit in case {@link ome.util.SqlAction#setCurrentEventLog(long, String)} used by
     * {@link ome.services.eventlogs.PersistentEventLogLoader} throws an unexpected constraint violation
     * as reported in Trac ticket #10181.
     */
    private Object retriableExecute() {
        try {
            return this.executor.execute(getPrincipal(), this.work);
        } catch (DataIntegrityViolationException e) {
            final Object result = this.executor.execute(getPrincipal(), this.work);
            log.warn("work unit threw exception, succeeded on second try", e);
            return result;
        }
    }

    @Override
    public void doRun() {
        if (performProcessing) {

            // Single-threaded simplification
            if (numThreads == 1) {
                retriableExecute();
                return;
            }

            final ExecutorCompletionService<Object> ecs =
                new ExecutorCompletionService<Object>(executor.getService());

            for (int i = 0; i < numThreads; i++) {
                ecs.submit(new Callable<Object>(){
                    @Override
                    public Object call() {
                        return retriableExecute();
                    }
                });
            }

            try {
                for (int i = 0; i < numThreads; i++) {
                    Future<Object> future = ecs.take();
                    try {
                        future.get();
                    } catch (ExecutionException ee) {
                        onExecutionException(ee);
                    }
                }
            } catch (InterruptedException ie) {
                log.fatal("Interrupted exception during multiple thread handling." +
				"Other threads may not have been successfully completed.",
                    ie);
            }
        }
    }

    /**
     * Basic handling just logs at ERROR level. Subclasses (especially for
     * testing) can do more.
     */
    protected void onExecutionException(ExecutionException ee) {
        log.error("ExceptionException!", ee.getCause());
    }

    /**
     * Called by Spring on destruction.
     */
    public void stop() {
        log.info("Shutting down PixelDataThread");
    }

    public void onApplicationEvent(final MissingPyramidMessage mpm) {

        log.info("Received: " + mpm);
        // #5232. If this is called without an active event, then throw
        // an exception since a call to Executor should wrap whatever the
        // invoker is doing.
        final CurrentDetails cd = executor.getContext().getBean(CurrentDetails.class);
        if (cd.size() <= 0) {
            throw new InternalException("Not logged in.");
        }
        final EventContext ec = cd.getCurrentEventContext();
        if (null == ec.getCurrentUserId()) {
            throw new InternalException("No user! Must be wrapped by call to Executor?");
        }

        Future<EventLog> future = this.executor.submit(cd.getContext(),
                new Callable<EventLog>(){
            public EventLog call() throws Exception {
                return makeEvent(ec, mpm);
            }});
        this.executor.get(future);
    }

    private EventLog makeEvent(final EventContext ec,
                               final MissingPyramidMessage mpm) {

        final Principal p = new Principal(uuid);
        final Map<String, String> callContext = new HashMap<String, String>();

        // First call is with -1 in order to find the pixels group.
        // TODO: this could equally be done with sqlAction.
        callContext.put("omero.group", "-1");
        final Long groupID = (Long) this.executor.execute(callContext, p,
                new Executor.SimpleWork(this, "getGroupId") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                final ExperimenterGroup group = sf.getQueryService().findByQuery(
                        "select p.details.group from Pixels p where p.id = :id",
                        new ome.parameters.Parameters().addId(mpm.pixelsID));
                return group.getId();
            }
        });

        // Reset to prevent "Not intended for copying" errors
        callContext.put("omero.group", groupID.toString());
        return (EventLog) this.executor.execute(callContext, p,
                new Executor.SimpleWork(this, "createEvent") {
        @Transactional(readOnly = false)
        public Object doWork(Session session, ServiceFactory sf) {
            log.info("Creating PIXELDATA event for pixels id:"
                    + mpm.pixelsID);

                // Load objects
                final EventType type = sf.getTypesService().getEnumeration(
                        EventType.class, ec.getCurrentEventType());
                final EventLog el = new EventLog();
                final Event e = new Event();
                e.setExperimenter(
                        new Experimenter(ec.getCurrentUserId(), false));
                e.setExperimenterGroup(new ExperimenterGroup(groupID, false));
                e.setSession(new ome.model.meta.Session(
                        ec.getCurrentSessionId(), false));
                e.setTime(new Timestamp(new Date().getTime()));
                e.setType(type);
                el.setAction("PIXELDATA");
                el.setEntityId(mpm.pixelsID);
                el.setEntityType(Pixels.class.getName());
                el.setEvent(e);
                return sf.getUpdateService().saveAndReturnObject(el);
            }
        });
    }
}
