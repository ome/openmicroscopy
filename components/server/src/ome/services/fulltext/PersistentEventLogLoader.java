/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.meta.EventLog;
import ome.services.eventlogs.EventLogFailure;
import ome.services.eventlogs.EventLogLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * {@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Reseting that saved value would restart indexing.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class PersistentEventLogLoader extends ome.services.eventlogs.PersistentEventLogLoader {

    private final static Logger log = LoggerFactory
            .getLogger(PersistentEventLogLoader.class);

    /**
     * Called when the configuration database does not contain a valid
     * current_id. Used to index all the data which does not have an EventLog.
     */
    @Override
    public void initialize() {
        for (Class<IEnum> cls : types.getEnumerationTypes()) {
            for (IEnum e : queryService.findAll(cls, null)) {
                addEventLog(cls, e.getId());
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof EventLogFailure) {
            EventLogFailure failure = (EventLogFailure) event;
            if (failure.wasSource(this)) {
                String msg = "FullTextIndexer stuck! "
                    + "Failed to index EventLog: " + failure.log;
                log.error(msg, failure.throwable);
                rollback(failure.log);
                throw new InternalException(msg);
            }
        } else {
            super.onApplicationEvent(event);
        }
    }

}
