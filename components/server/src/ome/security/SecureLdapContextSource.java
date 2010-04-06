/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.Hashtable;

import javax.naming.Context;

import org.springframework.security.ldap.DefaultSpringSecurityContextSource;


public class SecureLdapContextSource extends DefaultSpringSecurityContextSource {
    
    private String keyStore = null;
    private String keyStorePassword = null;
    private String trustStore = null;
    private String trustStorePassword = null;
    private String protocol = null;
    
    public SecureLdapContextSource(String url) {
        super(url);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        
        if(protocol.compareTo("")!=0) {
            Hashtable<String, String> env = super.getAuthenticatedEnv(userDn, password);
                    
            //specify use of ssl
            env.put(Context.SECURITY_PROTOCOL, protocol);
            
            //set the environment
            super.setupAuthenticatedEnvironment(env, userDn, password);
            
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        }
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
    
    public void setProtocol(String prot) {
        this.protocol = prot;
    }
    
}
