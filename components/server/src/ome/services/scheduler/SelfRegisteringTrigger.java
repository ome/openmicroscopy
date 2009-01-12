/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.FatalBeanException;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

/**
 * Produces a <a href="http://www.opensymphony.com/quartz/Quartz</a>
 * {@link Scheduler} which automatically loads all the triggers it can find.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SelfRegisteringTrigger {

    public SelfRegisteringTrigger(Scheduler scheduler, Trigger trigger) {
        try {
            if (trigger instanceof JobDetailAwareTrigger) {
                scheduler.scheduleJob(((JobDetailAwareTrigger) trigger)
                        .getJobDetail(), trigger);
            } else {
                scheduler.scheduleJob(trigger);
            }
        } catch (SchedulerException se) {
            throw new FatalBeanException("Failed to self register trigger: "
                    + trigger, se);
        }
    }

}
