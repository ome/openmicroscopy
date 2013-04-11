/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * {@link ApplicationListener} which only listens for
 * {@link ContextRefreshedEvent} <em>and</em> only responds to the first one.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.0
 */
public abstract class OnContextRefreshedEventListener implements
        ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private/* final */ApplicationContext ctx;

    private final boolean handleOthers;

    private final int limit;

    private final AtomicInteger count = new AtomicInteger(0);

    public OnContextRefreshedEventListener() {
        this(false, 1);
    }

    public OnContextRefreshedEventListener(boolean handleOthers, int limit) {
        this.handleOthers = handleOthers;
        this.limit = limit;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.ctx = applicationContext;
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() != ctx && !handleOthers) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring other application context refresh: " + ctx);
            }
            return;
        }

        int current = count.incrementAndGet();
        if (current > limit) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring refresh beyond limit: " + current);
            }
            return;
        }
        handleContextRefreshedEvent(event);
    }

    public abstract void handleContextRefreshedEvent(ContextRefreshedEvent event);

}
