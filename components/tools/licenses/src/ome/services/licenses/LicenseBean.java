/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore, josh.moore at gmx.de
 *
 *------------------------------------------------------------------------------
 */

package ome.services.licenses;

// Java imports
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

// Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ServiceInterface;
import ome.conditions.InternalException;
import ome.logic.AbstractLevel2Service;
import ome.logic.SimpleLifecycle;

/**
 * Implementation of the {@link ILicense} service interface. {@link LicenseBean}
 * primarily delegates to a {@link LicenseStore} instance which is created from
 * a hard-coded class name. This class name can be changed via the
 * tools/licenses/build.xml script.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 * @see ILicense
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(ILicense.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.services.licenses.ILicense")
@Local(ILicense.class)
@LocalBinding(jndiBinding = "omero/local/ome.services.licenses.ILicense")
@SecurityDomain("OmeroSecurity")
@Interceptors( { SimpleLifecycle.class })
public class LicenseBean extends AbstractLevel2Service implements LicenseStore {

    @Override
    protected final Class<? extends ServiceInterface> getServiceInterface() {
        return ILicense.class;
    }

    /**
     * Hard-coded class name for the {@link LicenseStore} implementation. This
     * value may have been changed via tools/licenses/build.xml
     */
    private final static String STORE_CLASS = "com.glencoesoftware.internal.licenses.Store";

    private final static LicenseStore STORE;

    // Now we'll try to create an instance of STORE_CLASS and assign it to the
    // STORE constant. If that doesn't work, we'll use a store that will always
    // fail ({@link InvalidStore}) because it's difficult to know if just
    // throwing an exception will prevent the server from starting up.
    static {
        try {
            Class storeClass = Class.forName(STORE_CLASS);
            STORE = (LicenseStore) storeClass.newInstance();
        } catch (Exception e) {
            throw new InternalException("Failed to create license store:"
                    + STORE_CLASS);
        }
    }

    // ~ Service methods
    // =========================================================================
    // All methods delegate to the global static STORE instance.

    /** See {@link ILicense#acquireLicense()} */
    @RolesAllowed("user")
    public byte[] acquireLicense() throws NoAvailableLicensesException {
        return STORE.acquireLicense();
    }

    /** See {@link ILicense#getAvailableLicenseCount()} */
    @RolesAllowed("user")
    public long getAvailableLicenseCount() {
        return STORE.getAvailableLicenseCount();
    }

    /** See {@link ILicense#getTotalLicenseCount()} */
    @RolesAllowed("user")
    public long getTotalLicenseCount() {
        return STORE.getTotalLicenseCount();
    }

    /** See {@link ILicense#getLicenseTimeout()} */
    @RolesAllowed("user")
    public long getLicenseTimeout() {
        return STORE.getLicenseTimeout();
    }

    /** See {@link ILicense#releaseLicense(byte[])} */
    @RolesAllowed("user")
    public boolean releaseLicense(byte[] token) throws InvalidLicenseException {
        return STORE.releaseLicense(token);
    }

    /** See {@link ILicense#resetLicenses()} */
    @RolesAllowed("system")
    public void resetLicenses() {
        STORE.resetLicenses();
    }

    // These methods are not visible to clients

    /** See {@link LicenseStore#isValid(byte[])} */
    public boolean isValid(byte[] token) {
        return STORE.isValid(token);
    }

    /** See {@link LicenseStore#enterValid(byte[])} */
    public void enterMethod(byte[] token) {
        STORE.enterMethod(token);
    }

    /** See {@link LicenseStore#exitMethod(byte[])} */
    public void exitMethod(byte[] token) {
        STORE.exitMethod(token);
    }
}
