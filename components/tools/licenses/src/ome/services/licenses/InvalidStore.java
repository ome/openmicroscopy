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
 * NullObject which is used when the {@link LicenseStore} class specified
 * in {@link LicenseBean} is not found.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
public class InvalidStore implements LicenseStore {

    public void enterMethod(byte[] token) {
        throw new LicenseException("Invalid license store.");
    }

    public void exitMethod(byte[] token) {
        throw new LicenseException("Invalid license store.");
    }

    public byte[] acquireLicense() {
        throw new LicenseException("Invalid license store.");
    }

    public long getAvailableLicenseCount() {
        throw new LicenseException("Invalid license store.");
    }

    public long getTotalLicenseCount() {
        throw new LicenseException("Invalid license store.");
    }

    public long getLicenseTimeout() {
        throw new LicenseException("Invalid license store.");
    }

    public boolean isValid(byte[] token) {
        throw new LicenseException("Invalid license store.");
    }
    
    public boolean releaseLicense(byte[] token) {
        throw new LicenseException("Invalid license store.");
    }
    
    public void resetLicenses() {
        throw new LicenseException("Invalid license store.");
    }    
    
}
