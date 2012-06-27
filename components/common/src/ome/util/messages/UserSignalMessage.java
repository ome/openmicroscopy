/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.messages;

/**
 * Published if the Posix USR1 or USR2 signal is received.
 */
public class UserSignalMessage extends InternalMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    final public int signal;

    public UserSignalMessage(Object source, int signal) {
        super(source);
        this.signal = signal;
    }

}
