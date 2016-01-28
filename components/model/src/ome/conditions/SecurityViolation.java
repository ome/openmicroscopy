/*
 * ome.conditions.SecurityViolation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * User does not have permissions to perform given action.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 */
public class SecurityViolation extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = -4513363960541699377L;

    public SecurityViolation(String msg) {
        super(msg);
    }

}
