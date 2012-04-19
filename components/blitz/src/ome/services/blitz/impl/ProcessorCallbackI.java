/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.impl;

import java.util.List;
import java.util.UUID;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * Callback used to lookup active processors via IceStorm.
 */
public class ProcessorCallbackI extends AbstractAmdServant
    implements _ProcessorCallbackOperations {

    private final static Log log = LogFactory.getLog(ProcessorCallbackI.class);

    private final Job job;

    private final ServiceFactoryI sf;

    private final ResultHolder<String> holder;

    /**
     * Simplified constructor used to see if any usermode processor is active
     * for either the current group or the current user. Currently uses a
     * hard-coded value of 5 seconds wait time. For more control, pass in a
     * {@link ResultHolder} instance.
     */
    public ProcessorCallbackI(ServiceFactoryI sf) {
        this(sf, new ResultHolder<String>(5 * 1000), null);
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
    public ProcessorCallbackI(ServiceFactoryI sf, ResultHolder<String> holder,
            Job job) {
        super(null, null);
        this.sf = sf;
        this.job = job;
        this.holder = holder;
    }

    /**
     * Generates a UUID-based {@link Ice.Identity} with the category of
     * {@link PROCESSORCALLBACK#value} and then calls
     * {@link #activateAndWait(Current, Ice.Identity).
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
     * @return
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
            String server = holder.get();
            Ice.ObjectPrx p = sf.adapter.getCommunicator()
                    .stringToProxy(server);
            return ProcessorPrxHelper.uncheckedCast(p);
        } finally {
            sf.unregisterServant(acceptId);
        }
    }

    /**
     * Callback method called by the remote processor instance.
     */
    public void isAccepted(boolean accepted, String sessionUuid,
            String procConn, Current __current) {

        String reason = "because false returned";

        if (accepted) {
            log.debug(String.format(
                    "Processor with session %s returned %s accepted",
                    sessionUuid, procConn, accepted));
            try {
                EventContext procEc = sf.sessionManager
                        .getEventContext(new Principal(sessionUuid));
                EventContext ec = sf.getEventContext(__current);
                if (procEc.isCurrentUserAdmin()
                        || procEc.getCurrentUserId().equals(
                                ec.getCurrentUserId())) {
                    this.holder.set(procConn);
                } else {
                    reason = "since disallowed";
                }
            } catch (Exception e) {
                reason = "due to exception: " + e.getMessage();
            }
        }

        log.debug(String.format("Processor with session %s rejected %s",
                sessionUuid, reason));
        this.holder.set(null);

    }

    /**
     * Callback method which should not be called for this instance.
     */
    public void responseRunning(List<Long> jobIds, Current __current) {
        log.error("responseRunning should not have been called");
    }

}
