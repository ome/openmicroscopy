/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

/**
 * Exception which will be thrown by activities within {@link GraphSpec}
 * implementations. This is a caught exception so that internal API consumers
 * will have to handle any issues and create the proper remote exceptions for
 * clients.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public class GraphException extends Exception {

    private static final long serialVersionUID = 1L;

    public final String message;

    public GraphException(String msg) {
        this.message = msg;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("(message=");
        sb.append(message);
        sb.append(')');
        return sb.toString();
    }
}
