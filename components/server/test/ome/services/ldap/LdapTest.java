/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.logic.LdapImpl;
import ome.security.auth.LdapConfig;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Roles;
import ome.util.SqlAction;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Uses LDIF text files along with property files of good and bad user names to
 * test that the LDAP plugin is properly functioning.
 */
public class LdapTest extends MockObjectTestCase {

    public class Fixture {
        public ConfigurableApplicationContext ctx;
        File file;
        Mock role;
        Mock sql;
        LdapImpl ldap;
        LdapConfig config;
        LdapPasswordProvider provider;
        public LdapTemplate template;

        public void createUserWithGroup(LdapTest t, final String dn, String group) {
            role.expects(atLeastOnce()).method("createGroup")
                .with(t.eq(group), t.NULL, t.eq(false))
                .will(returnValue(101L));
            role.expects(once()).method("createExperimenter")
                .will(returnValue(101L));
            // sql.expects(once()).method("update") // FIXME
        }

        public boolean createUserFromLdap(String user, String string) {
            return ldap.createUserFromLdap(user, "password");
        }


        public EventContext login(String username, String group, String password) {
            // no-op here.
            return null;
        }

        public Object execute(Executor.Work work) {
            throw new RuntimeException("Only in subclasses!");
        }

        void close() {
            ctx.close();
        }

    }

    /**
     * Data provider which returns all "*.ldif" files in the directory
     * containing the class file of this test.
     */
    @DataProvider(name = "ldif_files")
    public Object[][] getLdifContexts() throws Exception {
        String name = getClass().getName();
        name = name.replaceAll("[.]", "//");
        name = "classpath:" + name + ".class";
        File file = ResourceUtils.getFile(name);
        File dir = file.getParentFile();
        Collection<?> coll = FileUtils.listFiles(dir, new String[] { "xml" },
                true);
        Object[][] files = new Object[coll.size()][];
        int count = 0;
        for (Object object : coll) {
            files[count] = new Object[] { object };
            count++;
        }
        return files;
    }

    /**
     * Runs the LDAP test suite against each of the given *.ldif files, by
     * attempting to login against an embedded ldap store with both the good
     * names and the bad names.
     */
    @Test(dataProvider = "ldif_files")
    @SuppressWarnings("unchecked")
    public void testLdiffFile(File file) throws Exception {

        Fixture fixture = createFixture(file);
        try {
            Map<String, List<String>> good = fixture.ctx.getBean("good", Map.class);
            Map<String, List<String>> bad = fixture.ctx.getBean("bad", Map.class);
            assertPasses(fixture, good);
            assertFails(fixture, bad);
        } finally {
            fixture.close();
        }

    }

    /**
     * old etc/omero.properties:
     * =====================
     * omero.ldap.config=false
     * omero.ldap.urls=ldap://localhost:389
     * omero.ldap.username=
     * omero.ldap.password=
     * omero.ldap.base=ou=example,o=com
     * omero.ldap.new_user_group=default
     * omero.ldap.groups=
     * omero.ldap.attributes=objectClass
     * omero.ldap.values=person
     * # for ssl connection on ldaps://localhost:636
     * omero.ldap.protocol=
     * omero.ldap.keyStore=
     * omero.ldap.keyStorePassword=
     * omero.ldap.trustStore=
     * omero.ldap.trustStorePassword=
     */
    protected Fixture createFixture(File ctxFile) throws Exception {

        Fixture fixture = new Fixture();
        fixture.ctx =new FileSystemXmlApplicationContext("file:" + ctxFile.getAbsolutePath());
        fixture.config = (LdapConfig) fixture.ctx.getBean("config");

        Map<String, LdapContextSource> sources =
            fixture.ctx.getBeansOfType(LdapContextSource.class);

        LdapContextSource source = sources.values().iterator().next();
        String[] urls = source.getUrls();
        assertEquals(1, urls.length);

        /*
        AuthenticationSource auth = source.getAuthenticationSource();
        SecureLdapContextSource secureSource =
            new SecureLdapContextSource(urls[0]);
        secureSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        secureSource.setBase("ou=People,dc=openmicroscopy,dc=org");
        secureSource.setUserDn(auth.getPrincipal());
        secureSource.setPassword(auth.getCredentials());
        secureSource.setProtocol("");
        secureSource.afterPropertiesSet();
        //secureSource.setKeyStore("");
        //secureSource.setKeyStorePassword("");
        //secureSource.setTrustPassword("");
        //secureSource.setTrustPassword("");
        */

        fixture.template = new LdapTemplate(source);

        fixture.role = mock(RoleProvider.class);
        RoleProvider provider = (RoleProvider) fixture.role.proxy();

        fixture.sql = mock(SqlAction.class);
        SqlAction sql = (SqlAction) fixture.sql.proxy();

        fixture.ldap = new LdapImpl(source, fixture.template,
                new Roles(), fixture.config, provider, sql);

        fixture.provider = new LdapPasswordProvider(
                new PasswordUtil(sql), fixture.ldap);
        return fixture;
    }

    protected void assertPasses(Fixture fixture, Map<String, List<String>> users) throws Exception {

        LdapImpl ldap = fixture.ldap;
        LdapTemplate template = fixture.template;

        for (String user : users.keySet()) {

            // addMemberOf(fixture, template, user);

            String dn = null;
            assertTrue(1 <= users.get(user).size());
            try {
                dn = ldap.findDN(user);
            } catch (ApiUsageException aue) {
                // This will be one of the major errors: when we can't find a user
                // that we expect to find. Adding a try/catch block for debugging
                // purposes.
                throw aue;
            }
            assertNotNull(dn);
            assertEquals(user, ldap.findExperimenter(user).getOmeName());
            fixture.createUserWithGroup(this, dn, users.get(user).get(0));
            assertTrue(fixture.createUserFromLdap(user, "password"));
            fixture.login(user, users.get(user).get(0), "password");

            // Check that proper dn is passed to setDN
            // Check password
            // Get list of groups
            // List all users and get back the target user
            // List groups and find the good ones
            //
        }
    }

    protected void assertFails(Fixture fixture, Map<String, List<String>> users) {
        LdapImpl ldap = fixture.ldap;
        for (String user : users.keySet()) {
            assertEquals(1, users.get(user).size());
            try {
                String dn = ldap.findDN(user);
                assertNotNull(dn);
                fixture.createUserWithGroup(this, dn, users.get(user).get(0));
                assertTrue(fixture.createUserFromLdap(user, "password"));
                fixture.login(user, users.get(user).get(0), "password");
                // Parsing afterwards to force an explosion to reproduce #2557
                assertEquals(user, ldap.findExperimenter(user).getOmeName());
                fail("user didn't fail");
            } catch (ValidationException e) {
                if (e.getMessage().equals("No group found for: cn=user,ou=attributeFilter")) {
                    // good. This is the expected result for #8357
                } else {
                    throw e; // This means that we couldn't insert.
                    // See the thread on case-senitivty in #2557
                }
            } catch (ApiUsageException e) {
                // if not a ValidationException, but otherwise an ApiUsageException,
                // then this will be the "Cannot find unique DN" which we are
                // looking for.
            } catch (SecurityViolation sv) {
                // e.g. User 466 is not a member of group 54 and cannot login
                // also good.
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void addMemberOf(Fixture fixture, LdapTemplate template, String user)
            throws NamingException {
        List<String> dns =
            template.search("", fixture.config.usernameFilter(user).encode(),
                new ContextMapper(){
                    public Object mapFromContext(Object arg0) {
                        DirContextAdapter ctx = (DirContextAdapter) arg0;
                        return ctx.getNameInNamespace();
                    }});
        assertEquals(dns.toString(), 1, dns.size());


        DistinguishedName name = new DistinguishedName(dns.get(0));
        DistinguishedName root = new DistinguishedName(template
                .getContextSource()
                .getReadOnlyContext()
                .getNameInNamespace());

        // Build a relative name
        for (int i = 0; i < root.size(); i++) {
            name.removeFirst();
        }

        DirContextOperations context = template.lookupContext(name);
        context.setAttributeValues("memberOf", new Object[]{"foo"});
        template.modifyAttributes(context);
    }
}
