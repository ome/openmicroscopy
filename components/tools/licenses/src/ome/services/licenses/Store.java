/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

// Java import
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.security.SecuritySystem;

// Third-party libraries

// Application-internal dependencies

/**
 * Example {@link LicenseStore} implementation. NOT INTENDED FOR 
 * PRODUCTION USE. 
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 3.0-RC1
 */
public class Store implements LicenseStore {

    private final static Log log = LogFactory.getLog(Store.class);
    
    // Initialized variables.

    Random random = new Random();

    long totalLicenses = Long.MAX_VALUE;

    // Managed by resetLicenses()

    long usedLicenses;

    long timeout;
    
    Map<byte[], TokenInfo> tokenInfos;

    // ~ Ctors
    // =========================================================================

    public Store() {
        resetLicenses();
    }

    public Store(int count) {
        this();
        totalLicenses = count;
    }

    public Store(int total, int used) {
        this();
        totalLicenses = total;
        this.usedLicenses = used;
    }
    

    /** See {@link LicenseStore#setStaticSecuritySystem(SecuritySystem)} */
    public void setStaticSecuritySystem(SecuritySystem securitySystem) {
        // does nothing in this simple example
    }

    // ~ Local control methods
    // =========================================================================

    public void setLicenseCount(int count) {
        totalLicenses = count;
    }

    public void setUsedLicenses(int count) {
        usedLicenses = count;
    }

    public Map<byte[], TokenInfo> getTokens() {
        return tokenInfos;
    }

    // ~ Interface methods
    // =========================================================================

    public void enterMethod(byte[] token, LicensedPrincipal p) {
        TokenInfo tokenInfo = tokenInfos.get(token);
        if (tokenInfo == null) {
            throw new InvalidLicenseException("Can't enter method.");
        }
        tokenInfo.count++;
        tokenInfo.time = -1;
    }

    public void exitMethod(byte[] token, LicensedPrincipal p) {
        TokenInfo tokenInfo = tokenInfos.get(token);
        if (tokenInfo == null) {
            throw new InvalidLicenseException("Can't exit method.");
        }
        tokenInfo.count--;
        if (tokenInfo.count == 0) {
            tokenInfo.time = System.currentTimeMillis();
        }
    }

    public byte[] acquireLicense() {
        if (usedLicenses < totalLicenses) {
            byte[] token = new byte[4];
            random.nextBytes(token);
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.time = System.currentTimeMillis();
            tokenInfos.put(token, tokenInfo);
            usedLicenses++;
            log.info("Acquired license from example license store.");
            return token;
        }
        throw new NoAvailableLicensesException("Pool exhausted.");
    }

    public long getAvailableLicenseCount() {
        return totalLicenses - usedLicenses;
    }

    public long getTotalLicenseCount() {
        return totalLicenses;
    }
    
    public long getLicenseTimeout() {
        return timeout;
    }

    public boolean hasLicense(byte[] token) {
        boolean found = false;
        found = tokenInfos.containsKey(token);
        return found;
    }

    public boolean releaseLicense(byte[] token) {
        boolean valid = hasLicense(token);
        if (hasLicense(token)) {
            tokenInfos.remove(token);
            usedLicenses--;
            log.info("Released license to example license store.");
        }
        return valid;
    }

    public void resetLicenses() {
        usedLicenses = 0;
        timeout = -1L;
        tokenInfos = new HashMap<byte[], TokenInfo>();
    }

}
