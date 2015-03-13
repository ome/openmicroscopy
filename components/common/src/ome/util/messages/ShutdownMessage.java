/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.messages;

/**
 * Published when the server is shutting down.
 */
public class ShutdownMessage extends InternalMessage {

    private static final long serialVersionUID = 1293480142039840025L;

    public ShutdownMessage(Object source) {
        super(source);
    }

}
