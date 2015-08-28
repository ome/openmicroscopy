/*
 * ome.conditions.RootException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Catchall for unknown server exceptions. This most likely represents a bug in
 * the server-code and should be reported to the Omero team.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 */
public class InternalException extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = -2866954105868569561L;

    public InternalException(String msg) {
        super(msg);
    }

}
