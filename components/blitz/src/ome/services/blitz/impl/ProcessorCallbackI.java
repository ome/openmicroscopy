/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.ResultHolder;
import ome.system.EventContext;
import ome.system.Principal;

import omero.ServerError;
import omero.constants.categories.PROCESSORCALLBACK;
import omero.constants.topics.PROCESSORACCEPTS;
import omero.grid.ProcessorCallbackPrx;
import omero.grid.ProcessorCallbackPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid._ProcessorCallbackOperations;
import omero.grid._ProcessorCallbackTie;
import omero.model.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

/**
 * Callback used to lookup active processors via IceStorm.
 */
public class ProcessorCallbackI extends AbstractAmdServant
    implements _ProcessorCallbackOperations {

    private final static Logger log = LoggerFactory.getLogger(ProcessorCallbackI.class);

    private final Job job;

    private final ServiceFactoryI sf;

    private final ResultHolder<ProcessorPrx> holder;

    private final AtomicInteger responses = new AtomicInteger(0);

    /**
     * Simplified constructor used to see if any usermode processor is active
     * for either the current group or the current user. Currently uses a
     * hard-coded value of 5 seconds wait time. For more control, pass in a
     * {@link ResultHolder} instance.
     */
    public ProcessorCallbackI(ServiceFactoryI sf) {
        this(sf, new ResultHolder<ProcessorPrx>(5 * 1000), null);
    }

    /**
     * Primary constructor. Asks processors if they will accept the given job
     * for the current user and the current group.
     *
     * @param sf
     *            Cannot be null.
     * @param holder
     *            Cannot be null.
     * @param job
     *            Can be null.
     */
    public ProcessorCallbackI(ServiceFactoryI sf, ResultHolder<ProcessorPrx> holder,
            Job job) {
        super(null, null);
        this.sf = sf;
        this.job = job;
        this.holder = holder;
    }

    /**
     * Return the number of times this instance has been called in a thread
     * safe manner.
     * @return See above.
     */
    public int getResponses() {
        return responses.get();
    }

    /**
     * Generates a UUID-based {@link Ice.Identity} with the category of
     * {@link PROCESSORCALLBACK#value} and then calls
     * {@link #activateAndWait(Current, Ice.Identity)}.
     * @return See above.
     */
    public ProcessorPrx activateAndWait(Ice.Current current) throws ServerError {
        Ice.Identity acceptId = new Ice.Identity();
        acceptId.name = UUID.randomUUID().toString();
        acceptId.category = PROCESSORCALLBACK.value;
        return activateAndWait(current, acceptId);
    }

    /**
     * Primary method which adds this instance to IceStorm, waits for a response
     * from any active processor services, and finally unregister itself before
     * returning the first processor instance which responded.

     * @param current
     * @param acceptId
     * @return See above.
     * @throws ServerError
     */
    public ProcessorPrx activateAndWait(Ice.Current current,
            Ice.Identity acceptId) throws ServerError {
        Ice.ObjectPrx prx = sf.registerServant(acceptId,
                new _ProcessorCallbackTie(this));

        try {
            prx = sf.adapter.createDirectProxy(acceptId);
            ProcessorCallbackPrx cbPrx = ProcessorCallbackPrxHelper
                    .uncheckedCast(prx);

            EventContext ec = sf.getEventContext(current);

            TopicManager.TopicMessage msg = new TopicManager.TopicMessage(this,
                    PROCESSORACCEPTS.value, new ProcessorPrxHelper(),
                    "willAccept", new omero.model.ExperimenterI(ec
                            .getCurrentUserId(), false),
                    new omero.model.ExperimenterGroupI(ec
                            .getCurrentGroupId(), false),
                            this.job, cbPrx);
            sf.topicManager.onApplicationEvent(msg);
            return holder.get();
        } finally {
            sf.unregisterServant(acceptId);
        }
    }

    /**
     * Callback method called by the remote processor instance.
     * See ticket 8266 for reasons this method should not be used.
     */
    @Deprecated
    public void isAccepted(boolean accepted, String sessionUuid,
            String procConn, Current __current) {
        isProxyAccepted(accepted, sessionUuid, ProcessorPrxHelper.checkedCast(
            sf.adapter.getCommunicator().stringToProxy(procConn)), __current);
    }

    /**
     * Callback method called by the remote processor instance.
     */
    public void isProxyAccepted(boolean accepted, String sessionUuid,
            ProcessorPrx procProxy, Current __current) {

        responses.incrementAndGet();
        Exception exc = null;
        String reason = "because false returned";

        if (accepted) {
            String procLog = sf.adapter.getCommunicator().proxyToString(procProxy);
            log.debug(String.format(
                    "Processor with session %s returned %s accepted",
                    sessionUuid, procLog, accepted));
            try {
                EventContext procEc = sf.sessionManager
                        .getEventContext(new Principal(sessionUuid));
                EventContext ec = sf.getEventContext(__current);
                if (procEc.isCurrentUserAdmin()
                        || procEc.getCurrentUserId().equals(
                                ec.getCurrentUserId())) {
                    this.holder.set(ProcessorPrxHelper.checkedCast(procProxy));
                    return;  // EARLY EXIT
                } else {
                    reason = "since disallowed";
                }
            } catch (Ice.ObjectNotExistException onee) {
                exc = onee;
                reason = "due to ObjectNotExistException: " + procLog;
            } catch (Exception e) {
                exc = e;
                reason = "due to exception: " + e.getMessage();
            }
        }

        String msg = String.format("Processor with session %s rejected %s",
                sessionUuid, reason);
        if (exc != null) {
            log.warn(msg, exc);
        } else {
            log.debug(msg);
        }
        this.holder.set(null);

    }

    /**
     * Callback method which should not be called for this instance.
     */
    public void responseRunning(List<Long> jobIds, Current __current) {
        log.error("responseRunning should not have been called");
    }

}
