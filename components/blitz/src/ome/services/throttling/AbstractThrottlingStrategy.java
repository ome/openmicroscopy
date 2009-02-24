/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import ome.conditions.OverUsageException;
import ome.services.messages.stats.AbstractStatsMessage;
import ome.services.messages.stats.ObjectsReadStatsMessage;
import ome.services.messages.stats.ObjectsWrittenStatsMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Throttling implementation which uses the calling server {@link Thread} for
 * execution. This mimics the behavior of the pre-AMD blitz.
 */
public abstract class AbstractThrottlingStrategy implements ThrottlingStrategy {

    protected final Log log = LogFactory.getLog(getClass());

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ObjectsReadStatsMessage) {
            ObjectsReadStatsMessage read = (ObjectsReadStatsMessage) event;
            handle(read, read.getObjectsRead() + " objects read.");
        } else if (event instanceof ObjectsWrittenStatsMessage) {
            ObjectsWrittenStatsMessage written = (ObjectsWrittenStatsMessage) event;
            handle(written, written.getObjectsWritten() + " objects written.");
        }
    }

    private void handle(AbstractStatsMessage event, String msg) {
        if (event.isHard()) {
            throw new OverUsageException(String.format(
                    "Aborting execution: Reason = \"%s\"", msg));
        } else {
            log.info("Blocking for 5 seconds: " + msg);
            // Allow one second to pass before continuing
            while (System.currentTimeMillis() < event.getTimestamp() + 5000L) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    // ok
                }
            }
        }
    }

}
