/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;


// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local extensions of {@link ILicense} for validating 
 * license tokens and tracking license timeouts.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
public interface LicenseStore extends ILicense {

    void enterMethod(byte[] token);
    void exitMethod(byte[] token);    
    boolean isValid(byte[] token);
    
}
