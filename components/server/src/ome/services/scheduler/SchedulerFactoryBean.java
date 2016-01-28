/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scheduler;

import java.util.HashMap;
import java.util.Map;

import ome.tools.spring.OnContextRefreshedEventListener;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

/**
 * Produces a <a href="http://www.opensymphony.com/quartz/Quartz</a>
 * {@link Scheduler} which automatically loads all the triggers it can find.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SchedulerFactoryBean extends
        org.springframework.scheduling.quartz.SchedulerFactoryBean implements
        ApplicationListener<ContextRefreshedEvent>,
        ApplicationContextAware {

    private final static Logger log = LoggerFactory
            .getLogger(SchedulerFactoryBean.class);

    private final Map<String, Trigger> triggers = new HashMap<String, Trigger>();

    /**
     * Already subclassing another class, so re-using the handler here
     * in a somewhat awkward to re-use the code.
     */
    private final OnContextRefreshedEventListener handler =
        new OnContextRefreshedEventListener(true, Integer.MAX_VALUE) {

            @Override
            public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
                handle(event);
            }
        };

    public void setApplicationContext(ApplicationContext ctx) {
        handler.setApplicationContext(ctx);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        handler.onApplicationEvent(event);
    }

    private void handle(ContextRefreshedEvent cre) {
        String[] names = cre.getApplicationContext().getBeanNamesForType(
                Trigger.class);
        for (String name : names) {
            if (triggers.containsKey(name)) {
                log.error("Scheduler already has trigger named: " + name);
                continue;
            }
            Trigger trigger = (Trigger) cre.getApplicationContext()
                    .getBean(name);
            registerTrigger(name, trigger);
        }
        restartIfNeeded();
    }

    /**
     * Registers a {@link JobDetailAwareTrigger}. A method like this should
     * really have protected visibility in the superclass.
     */
    protected void registerTrigger(String beanName, Trigger trigger) {
        try {
            Scheduler scheduler = (Scheduler) getObject();
            triggers.put(beanName, trigger);
            JobDetailAwareTrigger jdat = (JobDetailAwareTrigger) trigger;
            JobDetail job = jdat.getJobDetail();
            scheduler.addJob(job, false);
            scheduler.scheduleJob(trigger);
            log.debug(String.format("Registered trigger \"%s\": %s", beanName, trigger));
        } catch (SchedulerException se) {
            throw new RuntimeException(se);
        }
    }

    /**
     * Similar to the {@link #isRunning()} method, but properly handles the
     * situation where the {@link Scheduler} has been completely shutdown and
     * therefore must be replaced.
     */
    protected void restartIfNeeded() {
        if (!isRunning()) {
            try {
                start();
            } catch (SchedulingException se) {
                log.info("Replacing scheduler");
                try {
                    afterPropertiesSet();
                    if (!isRunning()) {
                        start();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to restart scheduler", e);
                }
            }
        }
    }
}
