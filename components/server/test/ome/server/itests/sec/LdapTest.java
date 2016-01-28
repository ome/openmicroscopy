/*
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.sec;

import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.ldap.core.LdapOperations;
import org.testng.annotations.Test;

public class LdapTest extends AbstractManagedContextTest {

	// ~ ILdap.searchAll
	// =========================================================================

	@Test
	public void testSearchAll() throws Exception {
		if (iLdap.getSetting()) {
            List<Experimenter> l = iLdap.searchAll();
		}
        
	}

	@Test
	public void testSearchDnInGroups() throws Exception {
		if (iLdap.getSetting()) {
			List<String> l = iLdap.searchDnInGroups("group1",
					"cn=jsmith, ou=people, ou=example, o=com");
		}
	}

	@Test
	public void testSearchByAttribute() throws Exception {
		if (iLdap.getSetting()) {
			List<Experimenter> exps = iLdap
					.searchByAttribute("", "sn", "Smith");
		}
	}

	@Test
	public void testSearchByAttributes() throws Exception {
		if (iLdap.getSetting()) {
			String[] attrs = new String[0];
			String[] vals = new String[0];

			List<Experimenter> exps = iLdap.searchByAttributes("", attrs, vals);

			String dn = "cn=jsmith, ou=people"; //DN without base
			List<Experimenter> exps1 = iLdap
					.searchByAttributes(dn, attrs, vals);

		}
	}

	@Test
	public void testSearchByDN() throws Exception {
		if (iLdap.getSetting()) {
			String dn = "cn=jsmith, ou=people"; //DN without base
			Experimenter exp = iLdap.searchByDN(dn);
			System.out.println("Experimenter: " + exp.getFirstName() + " "
					+ exp.getLastName() + ", " + exp.getOmeName() + " "
					+ exp.getEmail());
		}
	}

	@Test
	public void testFindDN() throws Exception {
		if (iLdap.getSetting()) {
			String dn = iLdap.findDN("jsmith");
			
			// should be created 2 the same cns on the subtree.
			// should catch an exception
			try {
				iLdap.findDN("jsmith");
			} catch (Exception e) {
				System.err.println("Subtree should not contains two the same CNs");
				e.printStackTrace();
			}
		}
	}
    
    @Test
    public void testFindExp() throws Exception {
        if (iLdap.getSetting()) {
            Experimenter exp = iLdap.findExperimenter("jsmith");
            
            // should be created 2 the same cns on the subtree.
            // should catch an exception
            try {
                iLdap.findDN("jsmith");
            } catch (Exception e) {
                System.err.println("Subtree should not contains two the same CNs");
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testCreateUserFromLdap() throws Exception {
        if(iLdap.getSetting()) {
            Experimenter exp = null;
            try {
                exp = iAdmin.lookupExperimenter("jmoore");
            } catch (ApiUsageException e) {
                // iLdap.createUserFromLdap("jmoore", "XXX");
                fail();
            }
            
            if(exp!=null) 
                System.err.println("Experimenter exist, for test please try set another one.");
            
        }
    }
    
	@Test
	public void testGetReq() throws Exception {
		if(iLdap.getSetting()) {
			iLdap.getSetting();
		}
	}

	// Helpers
	// =========================================================================
	
	LdapOperations ops() {
	    ProxyFactory factory = new ProxyFactory(new Class[]{LdapOperations.class});
	    factory.addAdvice(new MethodInterceptor(){

            public Object invoke(MethodInvocation arg0) throws Throwable {
                throw new UnsupportedOperationException();
            }});
	    return (LdapOperations) factory.getProxy();
	}
	
}
