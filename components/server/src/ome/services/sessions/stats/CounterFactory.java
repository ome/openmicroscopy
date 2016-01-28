/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Factory for creating counter objects. Passed to other Spring beans to prevent
 * constant context lookups.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class CounterFactory implements ApplicationEventPublisherAware {

    protected ApplicationEventPublisher publisher;

    protected int objectsReadHardLimit = Integer.MAX_VALUE;

    protected int objectsWrittenHardLimit = Integer.MAX_VALUE;

    protected int methodHardLimit = Integer.MAX_VALUE;

    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void setObjectsReadHardLimit(int objectsReadHardLimit) {
        this.objectsReadHardLimit = objectsReadHardLimit;
    }

    public void setObjectsWrittenHardLimit(int objectsWrittenHardLimit) {
        this.objectsWrittenHardLimit = objectsWrittenHardLimit;
    }

    public void setMethodHardLimit(int methodHardLimit) {
        this.methodHardLimit = methodHardLimit;
    }

    public SessionStats createStats() {
        ObjectsReadCounter read = new ObjectsReadCounter(objectsReadHardLimit);
        read.setApplicationEventPublisher(publisher);
        ObjectsWrittenCounter written = new ObjectsWrittenCounter(
                objectsWrittenHardLimit);
        written.setApplicationEventPublisher(publisher);
        MethodCounter methods = new MethodCounter(methodHardLimit);
        methods.setApplicationEventPublisher(publisher);
        return new SimpleSessionStats(read, written, methods);
    }

}
