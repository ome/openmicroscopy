/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import ome.api.IDelete;

/**
 * Exception which will be thrown by activities within {@link DeleteSpec}
 * implementations. This is a caught exception so that internal API consumers
 * will have to handle any issues any create the proper remote exceptions for
 * clients.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteException extends Exception {

    private static final long serialVersionUID = 1L;

    public final String message;

    public DeleteException(String msg) {
        this.message = msg;
    }
}
