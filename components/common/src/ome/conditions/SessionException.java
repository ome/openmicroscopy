/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

// Java imports
import javax.ejb.ApplicationException;

// Third-party libraries

// Application-internal dependencies

/**
 * User does not have permissions to perform given action.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@ApplicationException
public class SessionException extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = -4513364892739872987L;

    public SessionException(String msg) {
        super(msg);
    }

}
