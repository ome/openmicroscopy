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
import ome.api.ServiceInterface;

/**
 * (CLIENT) Provides methods for acquiring and releasing server licenses.
 * For this service to be active, it must be compiled into the 
 * server jar file via server/build.xml and the 
 * omero.hard-wired.interceptor configuration property.
 *
 * Each {@link LicenseStore} implementation will behave
 * differently, but in general it is intended that users will
 * have to acquire a license (represented by a byte-array 
 * license token) which must be presented on all method calls
 * to non-{@link ILicense} services. This is enforced by 
 * {@link LicenseWiring}, the {@link ome.logic.HardWiredInterceptor}
 * implementation which will be compiled in. 
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 3.0-RC1
 * @see LicenseStore
 * @see LicenseWiring
 */
public interface ILicense extends ServiceInterface {

    /**
     * Returns the number of licenses which can currently be
     * {@link #acquireLicense() acquired}. There is no guarantee
     * that the number will remain constant before the next 
     * call, and therefore clients should be prepared to handle 
     * {@link LicenseException exceptions} during acquisition. 
     *
     * No guarantee is given on the interpretation of licenses
     * used. Refer to the {@link LicenseStore} implementation 
     * for more details. For example, a power-user may consume
     * more than one license per acquisition.
     */
    long getAvailableLicenseCount();

    /**
     * Returns the total number of licenses which the server 
     * has. This number is <em>relatively</em> constant, but
     * can be changed via a call to {@link #resetLicenses()}
     */
    long getTotalLicenseCount();

    /**
     * Returns the timeout per license as interpreted by the
     * {@link LicenseStore} implementation. Please refer there
     * for more information.
     */
    long getLicenseTimeout();

    /**
     * Reserves a license for the current user. The token returned
     * must likely be presented on each subsequent method call if
     * the {@link LicenseWiring} interceptor is in effect. 
     * 
     * Licenses may timeout depending on the {@link LicenseStore}
     * implementation. Clients should be ready to handle
     * {@link LicenseException exceptions} on any method call.
     *
     * Licenses should also be {@link #releaseLicense(byte[]) released}
     * when possible so that other users can access the server.
     * 
     * @returns Non-null byte-array. There is no guarantee of 
     * what the contents and/or length of the array will be.
     */
    byte[] acquireLicense();

    /**
     * Frees a license for re-reservation via {@link #acquireLicense()}.
     * If the license is invalid, no exception is thrown, but rather a
     * <code>false</code> value returned.
     */
    boolean releaseLicense(byte[] token);

    /**
     * Administrative method to clear all active licenses and
     * possibly to re-initialize the {@link LicenseStore}. This
     * may include a change to the internal settings such as
     * {@link #getTotalLicenseCount() license count} or 
     * {@link #getLicenseTimeout() timeout}.
     */
    void resetLicenses();
    
}
