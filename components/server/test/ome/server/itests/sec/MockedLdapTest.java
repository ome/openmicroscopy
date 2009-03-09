/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.List;

import ome.api.local.LocalLdap;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.jmock.Mock;
import org.springframework.ldap.core.LdapOperations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Based on {@link LdapTest}, this test uses a mocked ldap server via
 * {@link LdapOperations} to simplify testing.
 * 
 */
public class MockedLdapTest extends AbstractManagedContextTest {

    // Using local versions since we need to mock them.
    LocalLdap ldap;
    Mock mock;
    LdapOperations ops;

    @BeforeMethod
    public void setup() {
        mock = new Mock(LdapOperations.class);
        ops = (LdapOperations) mock.proxy();

        LdapImpl limpl = new LdapImpl(null, ops, jdbcTemplate, "default",
                "groups", "attributes", "values", true);
        ldap = limpl;

    }

    // ~ ILdap.searchAll
    // =========================================================================

    @Test
    public void testSearchAll() throws Exception {
        List<Experimenter> l = ldap.searchAll();
    }

    @Test
    public void testSearchDnInGroups() throws Exception {
        List<String> l = ldap.searchDnInGroups("group1",
                "cn=jsmith, ou=people, ou=example, o=com");
    }

    @Test
    public void testSearchByAttribute() throws Exception {
        List<Experimenter> exps = ldap.searchByAttribute("", "sn", "Smith");
    }

    @Test
    public void testSearchByAttributes() throws Exception {
        String[] attrs = ldap.getReqAttributes();
        String[] vals = ldap.getReqValues();

        List<Experimenter> exps = ldap.searchByAttributes("", attrs, vals);

        String dn = "cn=jsmith, ou=people"; // DN without base
        List<Experimenter> exps1 = ldap.searchByAttributes(dn, attrs, vals);

    }

    @Test
    public void testSearchByDN() throws Exception {
        String dn = "cn=jsmith, ou=people"; // DN without base
        Experimenter exp = ldap.searchByDN(dn);
    }

    @Test
    public void testFindDN() throws Exception {
        String dn = ldap.findDN("jsmith");

        // should be created 2 the same cns on the subtree.
        // should catch an exception
        try {
            ldap.findDN("jsmith");
        } catch (Exception e) {
            fail("Subtree should not contains two the same CNs");
        }
    }

    @Test
    public void testFindExp() throws Exception {
        Experimenter exp = ldap.findExperimenter("jsmith");

        // should be created 2 the same cns on the subtree.
        // should catch an exception
        try {
            ldap.findDN("jsmith");
        } catch (Exception e) {
            fail("Subtree should not contains two the same CNs");
        }
    }

    @Test
    public void testSearchAttributes() throws Exception {
        ldap.getReqAttributes();
    }

    @Test
    public void testValidatePassword() throws Exception {
        ldap.validatePassword("cn=jsmith, ou=people, ou=example, o=com",
                "passwd");
    }

    @Test
    public void testCreateUserFromLdap() throws Exception {
        ldap.createUserFromLdap("jmoore", "XXX");
    }

    @Test
    public void testGetReq() throws Exception {
        ldap.getSetting();
        ldap.getReqAttributes();
        ldap.getReqGroups();
        ldap.getReqValues();
    }

}