/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import org.springframework.context.ApplicationListener;

import ome.util.messages.InternalMessage;

/**
 * {@link InternalMessage} implementations which carry relate to some
 * {@link ome.model.meta.Session} bound event.
 * These messages are <em>not</em> thread-safe
 * and so will be called within the same {@link Thread} as the publisher. This
 * means {@link ApplicationListener listeners} have a chance to throw an
 * exception and cancel the related event.
 * 
 * @see ome.services.sessions.SessionManager
 */
public abstract class AbstractSessionMessage extends InternalMessage {

    String id;

    public AbstractSessionMessage(Object source, String sessionId) {
        super(source);
        this.id = sessionId;
    }

    public String getSessionId() {
        return this.id;
    }

    @Override
    public final boolean isThreadSafe() {
        return false;
    }

}