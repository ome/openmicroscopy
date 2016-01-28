/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.messages;

import java.util.EventObject;

import ome.system.OmeroContext;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;

/**
 * Message which can be published via
 * {@link OmeroContext#publishEvent(ApplicationEvent)} or
 * {@link OmeroContext#publishMessage(InternalMessage)}. It is currently
 * assumed that the Spring-configured {@link ApplicationEventMulticaster} will
 * publish the {@link InternalMessage} in the current {@link Thread}. If a
 * subclass can properly handle the threaded case, it should set
 * {@link #threadSafe} to true.
 * 
 * Since the the {@link OmeroContext#publishEvent(ApplicationEvent)} does not
 * allow for an exception, consumers of {@link InternalMessage} subclasses can
 * throw a {@link MessageException} which will properly handled by the
 * {@link OmeroContext#publishMessage(InternalMessage)} method. (Users of
 * {@link OmeroContext#publishEvent(ApplicationEvent)} will have to manually
 * unwrap the {@link MessageException}.
 * 
 * 
 * Note: this class may or may not be useful for the client-side, but it must be
 * in the common/ package for use by {@link ome.system.OmeroContext}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 * @see MessageException
 * @see OmeroContext
 */
public abstract class InternalMessage extends ApplicationEvent {

    protected boolean threadSafe = false;

    /**
     * Sole constructor which takes the "source" of this {@link EventObject}.
     * 
     * @param source
     * @see EventObject#EventObject(Object)
     */
    public InternalMessage(Object source) {
        super(source);
    }

    /**
     * Returns true if this message can safely be passed to another
     * {@link Thread}. The default {@link ApplicationEventMulticaster} executes
     * in the same {@link Thread}.
     */
    public boolean isThreadSafe() {
        return threadSafe;
    }

}
