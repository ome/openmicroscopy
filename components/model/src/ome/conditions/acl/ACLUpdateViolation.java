/*
 * ome.conditions.ACLUpdateViolation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions.acl;

/**
 * User does not have permissions to perform given action.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 */
public class ACLUpdateViolation extends ACLViolation {

    /**
     * 
     */
    private static final long serialVersionUID = -1940049248694216053L;

    public ACLUpdateViolation(Class klass, Long id, String msg) {
        super(klass, id, msg);
    }

}
