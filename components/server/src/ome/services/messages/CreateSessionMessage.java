/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

/**
 * Published after a successful session creation. If an exception is throw, this
 * will be propagated back to the caller and the transaction will be rolled
 * back.
 */
public class CreateSessionMessage extends AbstractSessionMessage {

    private static final long serialVersionUID = 6132548299119420025L;

    public CreateSessionMessage(Object source, String sessionId) {
        super(source, sessionId);
    }

}