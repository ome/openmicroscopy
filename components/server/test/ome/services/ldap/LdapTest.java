/*
 *   Copyright 2010 - 2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.security.auth.LdapConfig;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.util.SqlAction;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.MockObjectSupportTestCase;
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
 * test that the LDAP API is properly functioning.
 */
@Test(groups = "ldap")
public class LdapTest extends MockObjectTestCase {

    public class Fixture {
        public ConfigurableApplicationContext ctx;
        File file;
        Mock role;
        Mock sql;
        Mock queryMock;
        LocalQuery query;
        LdapImpl ldap;
        LdapConfig config;
        LdapPasswordProvider provider;
        public LdapTemplate template;
        public OmeroContext applicationContext;
        boolean ignoreCaseLookup;

        public void createUserWithGroup(LdapTest t, final String dn,
                String group) {
            role.expects(atLeastOnce())
                    .method("createGroup")
                    .with(t.eq(group), MockObjectSupportTestCase.NULL,
                            t.eq(false)).will(returnValue(101L));
            role.expects(once()).method("createExperimenter")
                    .will(returnValue(101L));
        }

        public Experimenter createUser(String user) {
            return ldap.createUser(user);
        }

        public Experimenter createUser(String user, String string,
                boolean checkPassword) {
            return ldap.createUser(user, "password", checkPassword);
        }

        public Experimenter findExperimenter(String username) {
            return ldap.findExperimenter(username);
        }

        public void setDN(Long experimenterID, String dn) {
            ldap.setDN(experimenterID, dn);
        }

        public List<Experimenter> discover() {
            return ldap.discover();
        }

        public EventContext login(String username, String group, String password) {
            return null;
        }

        public Object execute(Executor.Work work) {
            throw new RuntimeException("Only in subclasses!");
        }

        void close() {
            if (ignoreCaseLookup) {
                applicationContext.getBean("atomicIgnoreCase",
                        AtomicBoolean.class).set(false);
                ignoreCaseLookup = false;
            }
            ctx.close();
        }
    }

    /**
     * Data provider which returns an array of XML files located under the root
     * of the directory containing the class file of this test.
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
     * Runs the LDAP test suite against each of the given LDIF files, by
     * attempting to login against an embedded LDAP store with both the good
     * names and the bad names.
     */
    @Test(dataProvider = "ldif_files")
    @SuppressWarnings("unchecked")
    public void testLdiffFile(File file) throws Exception {
        Fixture fixture = createFixture(file);
        try {
            Map<String, List<String>> good = fixture.ctx.getBean("good",
                    Map.class);
            Map<String, List<String>> bad = fixture.ctx.getBean("bad",
                    Map.class);
            assertPasses(fixture, good);
            assertFails(fixture, bad);
            assertCreateUserFromLdap(fixture, good);
            if (!good.isEmpty()) {
                assertDiscover(fixture, good);
            }
        } finally {
            fixture.close();
        }
    }

    protected Fixture createFixture(File ctxFile) throws Exception {
        Fixture fixture = new Fixture();
        fixture.ctx = new FileSystemXmlApplicationContext("file:"
                + ctxFile.getAbsolutePath());
        fixture.config = (LdapConfig) fixture.ctx.getBean("config");

        Map<String, LdapContextSource> sources = fixture.ctx
                .getBeansOfType(LdapContextSource.class);

        LdapContextSource source = sources.values().iterator().next();
        String[] urls = source.getUrls();
        assertEquals(1, urls.length);

        fixture.template = new LdapTemplate(source);

        fixture.role = mock(RoleProvider.class);
        RoleProvider provider = (RoleProvider) fixture.role.proxy();

        fixture.sql = mock(SqlAction.class);
        SqlAction sql = (SqlAction) fixture.sql.proxy();
        
        fixture.queryMock = mock(LocalQuery.class);
        fixture.query = (LocalQuery) fixture.queryMock.proxy();
        fixture.queryMock.expects(once()).method("findByString").will(
                returnValue(null));

        fixture.ldap = new LdapImpl(source, fixture.template, new Roles(),
                fixture.config, provider, sql);
        fixture.ldap.setQueryService(fixture.query);

        fixture.provider = new LdapPasswordProvider(new PasswordUtil(sql),
                fixture.ldap);

        return fixture;
    }

    protected void assertPasses(Fixture fixture, Map<String, List<String>> users)
            throws Exception {
        LdapImpl ldap = fixture.ldap;

        for (String user : users.keySet()) {
            String dn = null;
            assertTrue(1 <= users.get(user).size());
            try {
                dn = ldap.findDN(user);
            } catch (ApiUsageException aue) {
                // This will be one of the major errors: when we can't find a
                // user that we expect to find. Adding a try/catch block for
                // debugging purposes.
                throw aue;
            }

            assertNotNull(dn);
            assertEquals(fixture.ignoreCaseLookup ? user.toLowerCase() : user,
                    ldap.findExperimenter(user).getOmeName());
            fixture.createUserWithGroup(this, dn, users.get(user).get(0));
            assertNotNull(fixture.createUser(user, "password", true));
            fixture.login(fixture.ignoreCaseLookup ? user.toLowerCase() : user,
                    users.get(user).get(0), "password");
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
                assertNotNull(fixture.createUser(user, "password", true));
                fixture.login(user, users.get(user).get(0), "password");
                // Parsing afterwards to force an explosion to reproduce #2557
                assertEquals(user, ldap.findExperimenter(user).getOmeName());
                fail("user didn't fail");
            } catch (ValidationException e) {
                if (e.getMessage().equals(
                        "No group found for: cn=user,ou=attributeFilter")) {
                    // Good. This is the expected result for #8357
                } else {
                    throw e;
                    // This means that we couldn't insert.
                    // See the thread on case-sensitivity in #2557
                }
            } catch (ApiUsageException e) {
                // If not a ValidationException, but otherwise an
                // ApiUsageException, then this will be the
                // "Cannot find unique DN" which we are looking for.
            } catch (SecurityViolation sv) {
                // e.g. User 466 is not a member of group 54 and cannot login
                // Also good.
            }
        }
    }

    protected void assertCreateUserFromLdap(Fixture fixture,
            Map<String, List<String>> users) {
        LdapImpl ldap = fixture.ldap;
        for (String user : users.keySet()) {
            String dn = null;
            try {
                dn = ldap.findDN(user);
            } catch (ApiUsageException aue) {
                throw aue;
            }

            assertNotNull(dn);
            assertEquals(fixture.ignoreCaseLookup ? user.toLowerCase() : user,
                    ldap.findExperimenter(user).getOmeName());
            fixture.createUserWithGroup(this, dn, users.get(user).get(0));
            assertNotNull(fixture.createUser(user));
            try {
                fixture.createUser("nonExistingUserShouldNotBeCreated");
            } catch (ApiUsageException aue) {
                // Expected
                continue;
            }
            fail("This user shouldn't have been created!");
        }
    }

    protected void assertDiscover(Fixture fixture,
            Map<String, List<String>> users) {
        for (String user : users.keySet()) {
            Experimenter experimenter = fixture.findExperimenter(user);
            assertNotNull(experimenter);

            fixture.setDN(experimenter.getId(), null);
            List<Experimenter> discoveredExperimenters = fixture.discover();
            if (!discoveredExperimenters.isEmpty()) {
                boolean discovered = false;
                for (Experimenter e : discoveredExperimenters) {
                    if (experimenter.getId().equals(e.getId())) {
                        discovered = true;
                        break;
                    }
                }
                assertTrue(discovered);
            }
            fixture.setDN(experimenter.getId(), "dn");
        }
    }

    @SuppressWarnings("unchecked")
    protected void addMemberOf(Fixture fixture, LdapTemplate template,
            String user) throws NamingException {
        List<String> dns = template.search("",
                fixture.config.usernameFilter(user).encode(),
                new ContextMapper() {
                    public Object mapFromContext(Object arg0) {
                        DirContextAdapter ctx = (DirContextAdapter) arg0;
                        return ctx.getNameInNamespace();
                    }
                });
        assertEquals(dns.toString(), 1, dns.size());

        DistinguishedName name = new DistinguishedName(dns.get(0));
        DistinguishedName root = new DistinguishedName(template
                .getContextSource().getReadOnlyContext().getNameInNamespace());

        // Build a relative name
        for (int i = 0; i < root.size(); i++) {
            name.removeFirst();
        }

        DirContextOperations context = template.lookupContext(name);
        context.setAttributeValues("memberOf", new Object[] { "foo" });
        template.modifyAttributes(context);
    }
}
