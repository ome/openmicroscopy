/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client;

/**
 * Client-side only exception which is thrown when no
 * connection can be made to the server, or the server
 * throws any unexpected exception <em>stack</em>.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class OutOfService extends RuntimeException {

    public OutOfService(String msg) {
        super(msg);
    }

    public OutOfService(String msg, Throwable cause) {
        super(msg, cause);
    }
}
