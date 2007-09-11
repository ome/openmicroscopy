/*
 *   $Id: AdminTest.java 1395 2007-04-04 13:18:22Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.List;
import net.sf.ldaptemplate.support.DistinguishedName;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

public class LdapTest extends AbstractManagedContextTest {

	// ~ ILdap.searchAll
	// =========================================================================

	@Test
	public void testSearchAll() throws Exception {
		loginRoot();
		List<Experimenter> l = iLdap.searchAll();
		System.out.println(l.size());

	}

	@Test
	public void testSearchDnInGroups() throws Exception {
		loginRoot();

		List<String> l = iLdap.searchDnInGroups("member",
				"cn=jsmith, ou=example, ou=domain, o=com");
		for (String s : l) {
			System.out.println(s);
		}

	}

	@Test
	public void testSearchByAttribute() throws Exception {
		loginRoot();

		List<Experimenter> exps = iLdap.searchByAttribute(
				"", "sn", "Smith");
		for (Experimenter e : exps) {
			System.out.println(e.getOmeName());
		}

	}

	@Test
	public void testSearchByAttributes() throws Exception {
		loginRoot();

		String[] attrs = new String[2];
		attrs[0] = "objectClass";
		attrs[1] = "gidNumber";
		String[] vals = new String[2];
		vals[0] = "person";
		vals[1] = "1111";

		List<Experimenter> exps = iLdap.searchByAttributes(
				"", attrs, vals);
		System.out.println("size " + exps.size());
		for (Experimenter e : exps) {
			System.out.println(e.getOmeName());
		}

		String dn = "cn=jsmith,ou=example";
		List<Experimenter> exps1 = iLdap.searchByAttributes(dn, attrs, vals);
		System.out.println("size " + exps1.size());
		for (Experimenter e : exps1) {
			System.out.println(e.getOmeName());
		}

	}

	@Test
	public void testSearchByDN() throws Exception {
		loginRoot();

		String dn = "cn=atarkowska,ou=example";
		Experimenter exp = iLdap.searchByDN(dn);
		System.out.println("Experimenter: " + exp.getFirstName() + " "
				+ exp.getLastName() + ", " + exp.getOmeName() + " "
				+ exp.getEmail());

	}

	@Test
	public void testFindDN() throws Exception {
		loginRoot();

		String dn = iLdap.findDN("jsmith");
		System.out.println("DN: " + dn.toString());

		// should be created 2 the same cns on the subtree.
		// should catch an exception
		try {
			iLdap.findDN("jsmith");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchGroups() throws Exception {
		loginRoot();

	}

	@Test
	public void testSearchAttributes() throws Exception {
		loginRoot();

	}

	@Test
	public void testValidatePassword() throws Exception {
		loginRoot();
		System.out.println(iLdap.validatePassword("cn=jsmith,ou=example,ou=domain,o=com", "passwod"));
		
	}

	@Test
	public void testCreateUserFromLdap() throws Exception {
		loginRoot();
		System.out.println(iLdap.createUserFromLdap("jsmith", "passwd"));
		
	}

	
	@Test
	public void testGetReq() throws Exception {
		loginRoot();
		iLdap.getSetting();
		iLdap.getReqAttributes();
		iLdap.getReqGroups();
		iLdap.getReqValues();
	}
	
}
