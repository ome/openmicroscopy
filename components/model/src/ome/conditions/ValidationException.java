/*
 * ome.conditions.ValidationException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * More specific {@link ome.conditions.ApiUsageException ApiUsageException}, in
 * that the specification of your data as outlined in the OME specification is
 * incorrect.
 * 
 * <p>
 * Examples include:
 * <ul>
 * <li>a {@link ome.model.containers.Project Project} name with invalid
 * characters</li>
 * </ul>
 * </p>
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public class ValidationException extends ApiUsageException {

    /**
     * 
     */
    private static final long serialVersionUID = 8958921873970581811L;

    public ValidationException(String msg) {
        super(msg);
    }

}
