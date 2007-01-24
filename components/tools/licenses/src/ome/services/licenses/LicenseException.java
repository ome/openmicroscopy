/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.licenses;

// Java imports
import javax.ejb.ApplicationException;

// Third-party libraries

// Application-internal dependencies
import ome.conditions.RootException;

/**
 * Abstract superclass of all License exceptions.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@ApplicationException
public class LicenseException extends RootException {

    private static final long serialVersionUID = -9204313277710608446L;

    public LicenseException(String msg) {
        super(msg);
    }

}
