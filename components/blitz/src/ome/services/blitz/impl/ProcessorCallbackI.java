/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.impl;

import java.util.List;

import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.ResultHolder;
import ome.system.EventContext;
import ome.system.Principal;
import omero.ServerError;
import omero.constants.topics.PROCESSORACCEPTS;
import omero.grid.ProcessorCallbackPrx;
import omero.grid.ProcessorCallbackPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid._ProcessorCallbackDisp;
import omero.model.Job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

public class ProcessorCallbackI extends _ProcessorCallbackDisp {

    private final static Log log = LogFactory.getLog(ProcessorCallbackI.class);

    private final Job job;

    private final ServiceFactoryI sf;

    private final ResultHolder<String> holder;

    private final EventContext ec;

    public ProcessorCallbackI(ServiceFactoryI sf, ResultHolder<String> holder, Job job) {
        this.sf = sf;
        this.job = job;
        this.holder = holder;
        this.ec = sf.sessionManager.getEventContext(sf.principal);
    }

    public ProcessorPrx activateAndWait(Ice.Current current, Ice.Identity acceptId) throws ServerError {

        Ice.ObjectPrx prx = sf.registerServant(acceptId, this);

        try {
            prx = sf.adapter.createDirectProxy(acceptId);
            ProcessorCallbackPrx cbPrx =
                    ProcessorCallbackPrxHelper.uncheckedCast(prx);

            TopicManager.TopicMessage msg = new TopicManager.TopicMessage(this,
                    PROCESSORACCEPTS.value,
                    new ProcessorPrxHelper(),
                    "willAccept",
                    new omero.model.ExperimenterI(ec.getCurrentUserId(), false),
                    new omero.model.ExperimenterGroupI(ec.getCurrentGroupId(), false),
                    this.job,
                    cbPrx);
            sf.topicManager.onApplicationEvent(msg);
            String server = holder.get();
            Ice.ObjectPrx p = sf.adapter.getCommunicator().stringToProxy(server);
            return ProcessorPrxHelper.uncheckedCast(p);
        } finally {
            sf.unregisterServant(acceptId);
        }
    }

    public void isAccepted(boolean accepted, String sessionUuid, String procConn, Current __current) {

        String reason = "because false returned";

        if (accepted) {
            log.debug(String.format(
                    "Processor with session %s returned %s accepted",
                    sessionUuid, procConn, accepted));
            try {
                EventContext procEc = sf.sessionManager.getEventContext(new Principal(sessionUuid));
                if (procEc.isCurrentUserAdmin() ||
                        procEc.getCurrentUserId().equals(ec.getCurrentUserId())) {
                    this.holder.set(procConn);
                } else {
                    reason = "since disallowed";
                }
            } catch (Exception e) {
                reason = "due to exception: " + e.getMessage();
            }
        }

        log.debug(String.format(
                "Processor with session %s rejected %s",
                sessionUuid, reason));
        this.holder.set(null);

    }

    public void responseRunning(List<Long> jobIds, Current __current) {
        log.error("responseRunning should not have been called");
    }

}