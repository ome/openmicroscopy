/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.Hashtable;
import javax.naming.Context;
import org.springframework.ldap.core.support.LdapContextSource;


public class SecureLdapContextSource extends LdapContextSource {
    
    private String keyStore = null;
    private String keyStorePassword = null;
    private String trustStore = null;
    private String trustStorePassword = null;
    private String protocol = null;
    
    public SecureLdapContextSource() {
        super();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        
        if(protocol.compareTo("")!=0) {
            Hashtable<String, String> env = super.getAuthenticatedEnv();
                    
            //specify use of ssl
            env.put(Context.SECURITY_PROTOCOL, protocol);
            
            //set the environment
            super.setupAuthenticatedEnvironment(env);
            
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
