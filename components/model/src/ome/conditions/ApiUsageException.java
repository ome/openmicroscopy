/*
 * ome.conditions.ApiUsageException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Alerts a user to a misuse of an Omero method call. This includes
 * {@link java.lang.IllegalArgumentException}-like and often
 * {@link java.lang.IllegalStateException}-like matters. The Api which is
 * broken may be declaratively checked with annotations and an interceptor or at
 * run-time with simple assertions.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 */
public class ApiUsageException extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = -8372725926525758661L;

    public ApiUsageException(String msg) {
        super(msg);
    }

}
