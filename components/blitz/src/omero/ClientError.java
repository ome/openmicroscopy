/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero;

import ome.conditions.RootException;

/**
 * abstract superclass of all Omero exceptions. Only subclasses of this type
 * will be thrown by the server.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ClientError extends RootException {

    public ClientError(String msg) {
        super(msg);
    }

}
