/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.system.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example {@link LicenseStore} implementation. NOT INTENDED FOR PRODUCTION USE.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta1
 */
public class Store implements LicenseStore {

    private final static Log log = LogFactory.getLog(Store.class);

    // Initialized variables.

    Random random = new Random();

    long totalLicenses = Long.MAX_VALUE;

    // Managed by resetLicenses()

    long usedLicenses;

    long timeout;

    Map<Bytes, TokenInfo> tokenInfos;

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

    /** See {@link LicenseStore#setSessionManager(SessionManager)  */
    public void setSessionManager(SessionManager sessionManager) {
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

    public TokenInfo getToken(byte[] token) {
        return tokenInfos.get(new Bytes(token));
    }

    // ~ Interface methods
    // =========================================================================

    public void enterMethod(byte[] token, Principal p) {
        TokenInfo tokenInfo = tokenInfos.get(new Bytes(token));
        if (tokenInfo == null) {
            throw new InvalidLicenseException("Can't enter method.");
        }
        tokenInfo.count++;
        tokenInfo.time = -1;
    }

    public void exitMethod(byte[] token, Principal p) {
        TokenInfo tokenInfo = tokenInfos.get(new Bytes(token));
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
            tokenInfos.put(new Bytes(token), tokenInfo);
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
        if (token != null) {
            found = tokenInfos.containsKey(new Bytes(token));
        }
        return found;
    }

    public boolean releaseLicense(byte[] token) {
        boolean valid = hasLicense(token);
        if (hasLicense(token)) {
            tokenInfos.remove(new Bytes(token));
            usedLicenses--;
            log.info("Released license to example license store.");
        }
        return valid;
    }

    public void resetLicenses() {
        usedLicenses = 0;
        timeout = -1L;
        tokenInfos = new HashMap<Bytes, TokenInfo>();
    }

    private static class Bytes {

        private final byte[] b;

        public Bytes(byte[] bytes) {
            assert bytes != null;
            b = bytes;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Bytes)) {
                return false;
            } else if (obj == this) {
                return true;
            }

            Bytes other = (Bytes) obj;
            for (int i = 0; i < b.length; i++) {
                if (b[i] != other.b[i]) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 13;
            for (int i = 0; i < b.length; i++) {
                hash = 7 * b[i] + hash;
            }
            return hash;
        }

        @Override
        public String toString() {
            return Arrays.toString(b);
        }

    }
}
