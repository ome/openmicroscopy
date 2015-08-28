/*
 * ome.conditions.ResourceError
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Represents a incorrectible/unforseeable event within the server that lead to
 * a failure of a process.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 */
public class ResourceError extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = 773187967180983956L;

    public ResourceError(String msg) {
        super(msg);
    }

}
