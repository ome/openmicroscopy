/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Calls {@link System#setProperty(String, String)} for the "javax.net.ssl"
 * properties needed to configure a trust store and a keystore for the Java
 * process.
 */
public class KeyAndTrustStoreConfiguration implements InitializingBean {

    private final static Logger log = LoggerFactory.getLogger("omero.security");

    private static final String JAVAX_NET_SSL_KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    private static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore";
    private static final String JAVAX_NET_SSL_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    private static final String JAVAX_NET_SSL_TRUST_STORE = "javax.net.ssl.trustStore";
    private String keyStore = null;
    private String keyStorePassword = null;
    private String trustStore = null;
    private String trustStorePassword = null;

    public void afterPropertiesSet() throws Exception {

        String oldTrustStore = System
                .getProperty(JAVAX_NET_SSL_TRUST_STORE, "");
        String oldKeyStore = System.getProperty(JAVAX_NET_SSL_KEY_STORE, "");

        if (oldTrustStore != null) {
            if (oldTrustStore.equals(trustStore)) {
                log.debug("Found duplicate trust store: " + oldTrustStore);
            } else if (oldTrustStore.length() > 0) {
                log.warn("Overwriting existing trust store: " + oldTrustStore);
            }
        }
        System.setProperty(JAVAX_NET_SSL_TRUST_STORE, trustStore);
        System.setProperty(JAVAX_NET_SSL_TRUST_STORE_PASSWORD,
                trustStorePassword);

        if (oldKeyStore != null) {
            if (oldKeyStore.equals(keyStore)) {
                log.debug("Found duplicate trust store: " + oldKeyStore);
            } else if (oldKeyStore.length() > 0) {
                log.warn("Overwriting existing key store: " + oldKeyStore);
            }
        }
        System.setProperty(JAVAX_NET_SSL_KEY_STORE, keyStore);
        System.setProperty(JAVAX_NET_SSL_KEY_STORE_PASSWORD, keyStorePassword);
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public void setTrustStorePassword(String password) {
        this.trustStorePassword = password;
    }

    public void setKeyStorePassword(String password) {
        this.keyStorePassword = password;
    }

}
