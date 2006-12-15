/*
 * ome.conditions.ptimisticException
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
 * Signifies that another user has updated or deleted a given object, more
 * specifically a query of the form : "&lt;action&gt; where id = ? and version = ?"
 * applied to no rows.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
@ApplicationException
public class OptimisticLockException extends ApiUsageException {

    public OptimisticLockException(String msg) {
        super(msg);
    }

}
