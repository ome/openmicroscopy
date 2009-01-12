/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.List;

import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalLdap;
import ome.conditions.ApiUsageException;
import ome.logic.AdminImpl;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.security.ACLVoter;
import ome.server.itests.AbstractManagedContextTest;

import org.hibernate.SessionFactory;
import org.jmock.Mock;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
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
    LocalAdmin admin;
    LocalLdap ldap;
    Mock mock;
    LdapOperations ops;

    @BeforeMethod
    public void setup() {
        mock = new Mock(LdapOperations.class);
        ops = (LdapOperations) mock.proxy();

        LdapImpl limpl = new LdapImpl();
        limpl.setLdapTemplate(ops);
        limpl.setJdbcTemplate(jdbcTemplate);
        limpl.setConfig(true);
        limpl.setGroups("groups");
        limpl.setAttributes("attributes");
        limpl.setValues("values");
        // TODO Need to fix circular dependency on admin

        AdminImpl aimpl = new AdminImpl();
        aimpl.setJdbcTemplate(this.jdbcTemplate);
        aimpl.setSecuritySystem(this.securitySystem);
        aimpl.setSessionFactory((SessionFactory) applicationContext
                .getBean("sessionFactory"));
        aimpl.setMailSender(null);
        aimpl.setTemplateMessage(null);
        aimpl.setAclVoter((ACLVoter) applicationContext.getBean("aclVoter"));
        aimpl.setLdapService(limpl);
        limpl.setAdminService(aimpl);

        ldap = limpl;
        List<String> list = (List<String>) applicationContext
                .getBean("statelessInterceptors");
        ProxyFactoryBean factory = new ProxyFactoryBean();
        factory.setBeanFactory(applicationContext);
        factory.setInterceptorNames(list.toArray(new String[] {}));
        factory.setInterfaces(new Class[] { LocalAdmin.class });
        factory.setTarget(aimpl);

        admin = (LocalAdmin) factory.getObject();
        ProxyFactory factory2 = new ProxyFactory();
        factory2.setInterfaces(new Class[] { LocalAdmin.class });
        factory2.addAdvice(this.loginAop);
        factory2.setTarget(admin);
        admin = (LocalAdmin) factory2.getProxy();
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
        Experimenter exp = null;
        try {
            exp = admin.lookupExperimenter("jmoore");
        } catch (ApiUsageException e) {
            ldap.createUserFromLdap("jmoore", "XXX");
        }

        assertTrue("Experimenter exist, for test please try set another one.",
                exp != null);

    }

    @Test
    public void testGetReq() throws Exception {
        ldap.getSetting();
        ldap.getReqAttributes();
        ldap.getReqGroups();
        ldap.getReqValues();
    }

}