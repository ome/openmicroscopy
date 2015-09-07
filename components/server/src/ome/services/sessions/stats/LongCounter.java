/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import ome.util.messages.InternalMessage;

/**
 * Counter object which increments an internal long by some integer value,
 * and according to some strategy publishes an {@link InternalMessage} subclass.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public abstract class LongCounter implements ApplicationEventPublisherAware {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private ApplicationEventPublisher publisher;
    
    private int interval = 0;
    
    private long last = 0;
    
    private final Object mutex = new Object();
    
    protected long count = 0;
    
    public LongCounter(int interval) {
        this.interval = interval;
    }

    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void increment(int incr) {
        synchronized (mutex) {
            count = count + incr;
            if (count >= (last+interval)) {
                last = count;
                InternalMessage message = message();
                try {
                    log.info("Publishing "+ message);
                    publisher.publishEvent(message);
                } catch (Throwable t) {
                    log.error(message + " produced an error: "+t);
                }
            }
        }
    }
 
    /**
     * 
     * @return
     */
    protected abstract InternalMessage message();

}
