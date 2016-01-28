/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.messages;

import ome.system.OmeroContext;

/**
 * Message which wraps a {@link Throwable} instance since
 * {@link OmeroContext#publishEvent(org.springframework.context.ApplicationEvent)}
 * cannot throw a checked exception.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class MessageException extends RuntimeException {

    private static final long serialVersionUID = 5563094828794438400L;

    protected final Throwable t;

    /**
     * Create an instance. The passed in {@link Throwable} is available via
     * {@link #getException()}.
     * 
     * @param msg
     * @param throwable
     */
    public MessageException(String msg, Throwable throwable) {
        super(msg, throwable);
        t = throwable;
    }

    /**
     * Get the exception which this instance wraps.
     * @return the {@link Throwable exception}
     */
    public Throwable getException() {
        return t;
    }

}
