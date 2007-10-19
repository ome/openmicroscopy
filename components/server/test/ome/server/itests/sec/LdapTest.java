/*
 *   $Id: AdminTest.java 1395 2007-04-04 13:18:22Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

public class LdapTest extends AbstractManagedContextTest {

	// ~ ILdap.searchAll
	// =========================================================================

	@Test
	public void testSearchAll() throws Exception {
		if (iLdap.getSetting()) {
			List<Experimenter> l = iLdap.searchAll();
			System.err.println(l.size());
		}
	}

	@Test
	public void testSearchDnInGroups() throws Exception {
		if (iLdap.getSetting()) {
			List<String> l = iLdap.searchDnInGroups("group1",
					"cn=jsmith, ou=people, ou=example, o=com");
			System.err.println(l.size());
		}
	}

	@Test
	public void testSearchByAttribute() throws Exception {
		if (iLdap.getSetting()) {
			List<Experimenter> exps = iLdap
					.searchByAttribute("", "sn", "Smith");
			System.err.println(exps.size());
		}
	}

	@Test
	public void testSearchByAttributes() throws Exception {
		if (iLdap.getSetting()) {
			String[] attrs = iLdap.getReqAttributes();
			String[] vals = iLdap.getReqValues();

			List<Experimenter> exps = iLdap.searchByAttributes("", attrs, vals);
			System.out.println("size " + exps.size());

			String dn = "cn=jsmith, ou=people"; //DN without base
			List<Experimenter> exps1 = iLdap
					.searchByAttributes(dn, attrs, vals);
			System.out.println("size " + exps1.size());

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
			System.out.println("DN: " + dn.toString());

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
	public void testSearchAttributes() throws Exception {
		if (iLdap.getSetting()) {
			iLdap.getReqAttributes();
		}

	}

	@Test
	public void testValidatePassword() throws Exception {
		if (iLdap.getSetting()) {
			System.err.println(iLdap.validatePassword(
					"cn=jsmith, ou=people, ou=example, o=com", "passwd"));
		}
	}

	@Test
	public void testCreateUserFromLdap() throws Exception {
		if(iLdap.getSetting()) {
			Experimenter exp = null;
			try {
				exp = iAdmin.lookupExperimenter("jsmith");
			} catch (ApiUsageException e) {
				iLdap.createUserFromLdap("jsmith", "passwd");
			}
			
			if(exp!=null) 
				System.err.println("Experimenter exist, for test please try set another one.");
			
		}
	}

	@Test
	public void testGetReq() throws Exception {
		if(iLdap.getSetting()) {
			iLdap.getSetting();
			iLdap.getReqAttributes();
			iLdap.getReqGroups();
			iLdap.getReqValues();
		}
	}

}
