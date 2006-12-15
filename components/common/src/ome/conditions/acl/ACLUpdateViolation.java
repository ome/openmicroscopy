/*
 * ome.conditions.ACLUpdateViolation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions.acl;

// Java imports
import javax.ejb.ApplicationException;

// Third-party libraries

// Application-internal dependencies

/**
 * User does not have permissions to perform given action.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.5
 */
@ApplicationException
public class ACLUpdateViolation extends ACLViolation {

    /**
     * 
     */
    private static final long serialVersionUID = -1940049248694216053L;

    public ACLUpdateViolation(Class klass, Long id, String msg) {
        super(klass, id, msg);
    }

}
