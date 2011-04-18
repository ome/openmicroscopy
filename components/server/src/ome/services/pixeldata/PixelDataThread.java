/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import ome.io.messages.MissingPyramidMessage;
import ome.model.containers.Project;
import ome.model.core.Pixels;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
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
            PixelDataHandler handler) {
        this(manager, executor, handler, DEFAULT_PRINCIPAL);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler, Principal principal) {
        super(manager, executor, handler, principal);
        this.performProcessing = executor.getContext()
            .containsBean("pixelDataTrigger");
    }

    /**
     * Called by Spring on creation. Currently a no-op.
     */
    public void start() {
        log.info("Initializing PixelDataThread" +
                (performProcessing ? "" : " (create events only)"));
        sessionInit();
    }

    /**
     */
    @Override
    public void doRun() {
        if (performProcessing) {
            this.executor.execute(getPrincipal(), work);
        }
    }

    /**
     * Called by Spring on destruction.
     */
    public void stop() {
        log.info("Shutting down PixelDataThread");
    }

    public void onApplicationEvent(final MissingPyramidMessage mpm) {

        log.info("Received: " + mpm);
        // For the moment, we're going to assume that
        // a missing pyramid message will only be raised
        // if an Event is active.
        final CurrentDetails cd = executor.getContext().getBean(CurrentDetails.class);
        if (cd.size() <= 0) {
            return;
        }
        final EventContext ec = cd.getCurrentEventContext();
        Future<EventLog> future = this.executor.submit(new Callable<EventLog>(){
            public EventLog call() throws Exception {
                return makeEvent(ec, mpm);
            }});
        this.executor.get(future);
    }

    private EventLog makeEvent(final EventContext ec,
                               final MissingPyramidMessage mpm) {
        return (EventLog) this.executor.execute(getPrincipal(),
                    new Executor.SimpleWork(this, "createEvent") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                log.info("Creating PIXELDATA event for pixels id:"
                        + mpm.pixelsID);
                final EventLog el = new EventLog();
                final Event e = new Event();
                e.setExperimenter(
                        new Experimenter(ec.getCurrentUserId(), false));
                e.setExperimenterGroup(
                        new ExperimenterGroup(ec.getCurrentGroupId(), false));
                e.setSession(new ome.model.meta.Session(
                        ec.getCurrentSessionId(), false));
                e.setTime(new Timestamp(new Date().getTime()));
                e.setType(sf.getTypesService().getEnumeration(
                        EventType.class, ec.getCurrentEventType()));
                el.setAction("PIXELDATA");
                el.setEntityId(mpm.pixelsID);
                el.setEntityType(Pixels.class.getName());
                el.setEvent(e);
                return sf.getUpdateService().saveAndReturnObject(el);
            }
        });
    }
}