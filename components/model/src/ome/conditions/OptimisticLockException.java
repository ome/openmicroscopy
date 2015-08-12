/*
 * ome.conditions.ptimisticException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Signifies that another user has updated or deleted a given object, more
 * specifically a query of the form : "&lt;action&gt; where id = ? and version = ?"
 * applied to no rows.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 */
public class OptimisticLockException extends ApiUsageException {

    /**
     * 
     */
    private static final long serialVersionUID = 4632580711597252540L;

    public OptimisticLockException(String msg) {
        super(msg);
    }

}
