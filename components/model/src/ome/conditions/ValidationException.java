/*
 * ome.conditions.ValidationException
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
 * More specific {@link ome.conditions.ApiUsageException ApiUsageException}, in
 * that the specification of your data as outlined in the OME specification is
 * incorrect.
 * 
 * <p>
 * Examples include:
 * <ul>
 * <li>a {@link ome.model.containers.Project Project} name with invalid
 * characters</li>
 * <li>{@link ome.model.display.Color Color} values out-of-range</li>
 * <li>{@link ome.model.core.Image Image} linked to two distinct
 * {@link ome.model.containers.Category Categories} in a single
 * (mutually-exclusive) {@link ome.model.containers.CategoryGroup CategoryGroup}
 * </li>
 * </ul>
 * </p>
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
@ApplicationException
public class ValidationException extends ApiUsageException {

    /**
     * 
     */
    private static final long serialVersionUID = 8958921873970581811L;

    public ValidationException(String msg) {
        super(msg);
    }

}
