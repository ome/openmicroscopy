/*
 * ome.conditions.RootException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * abstract superclass of all Omero exceptions. Only subclasses of this type
 * will be thrown by the server.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 */
public abstract class RootException extends RuntimeException {

    public RootException(String msg) {
        super(msg);
    }

}
