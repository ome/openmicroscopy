/*
 * ome.conditions.ACLCreateViolation
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
public class ACLCreateViolation extends ACLViolation {

    /**
     * 
     */
    private static final long serialVersionUID = -1836014147577135921L;

    public ACLCreateViolation(Class klass, Long id, String msg) {
        super(klass, id, msg);
    }

}
