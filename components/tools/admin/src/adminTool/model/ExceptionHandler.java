/*
 * adminTool.model.ExceptionHandler 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.model;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class ExceptionHandler {
    private static ExceptionHandler ref;

    public void catchException(Exception e) throws IAdminException,
            UnknownException, PermissionsException {
        if (e instanceof ome.conditions.ApiUsageException) {
            throw new IAdminException(e);
        }
        if (e instanceof ome.conditions.ValidationException) {
            throw new IAdminException(e);
        } else if (e instanceof javax.ejb.EJBAccessException) {
            throw new PermissionsException(e);
        } else if (e instanceof java.lang.SecurityException) {
            throw new PermissionsException(e);
        } else if (e instanceof ome.conditions.SecurityViolation) {
            throw new PermissionsException(e);
        } else {
            throw new UnknownException(e);
        }
    }

    private ExceptionHandler() {

    }

    public static ExceptionHandler get() {
        if (ref == null) {
            ref = new ExceptionHandler();
        }
        return ref;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}
