/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * No connections are currently available for the database. Wait and try again.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class DatabaseBusyException extends ConcurrencyException {

    /**
     * 
     */
    private static final long serialVersionUID = 8958921873970581811L;

    public DatabaseBusyException(String msg, long backOffInMilliseconds) {
        super(msg, backOffInMilliseconds);
    }

}
