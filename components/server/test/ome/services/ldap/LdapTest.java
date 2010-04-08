/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ome.logic.LdapImpl;
import ome.security.auth.LdapConfig;
import ome.security.auth.RoleProvider;
import ome.system.Roles;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
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

    /**
     * Data provider which returns all "*.ldif" files in the directory
     * containing the class file of this test.
     */
    @DataProvider(name = "ldif_files")
    public Object[][] getLdifContexts() throws Exception {
        String name = LdapTest.class.getName();
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
        ConfigurableApplicationContext ctx = createContext(file);
        try {
            LdapImpl ldap = configureLdap(ctx);
            Map<String, List<String>> good = ctx.getBean("good", Map.class);
            Map<String, List<String>> bad = ctx.getBean("bad", Map.class);
            assertPasses(ldap, good);
            assertFails(ldap, bad);
        } finally {
            ctx.close();
        }
    }

    protected ConfigurableApplicationContext createContext(File ctxFile) throws Exception {
        FileSystemXmlApplicationContext ctx =
            new FileSystemXmlApplicationContext("file:" + ctxFile.getAbsolutePath());
        return ctx;
    }

    /**
     * etc/omero.properties:
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
    protected LdapImpl configureLdap(ApplicationContext context) throws Exception {

        LdapConfig config = (LdapConfig) context.getBean("config");

        Map<String, LdapContextSource> sources =
            context.getBeansOfType(LdapContextSource.class);

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

        LdapTemplate template = new LdapTemplate(source);

        Mock mock = mock(RoleProvider.class);
        RoleProvider provider = (RoleProvider) mock.proxy();

        LdapImpl ldap = new LdapImpl(source, template,
                new Roles(), config, provider, null);
        return ldap;
    }

    protected void assertPasses(LdapImpl ldap, Map<String, List<String>> users) {
        for (String user : users.keySet()) {
            ldap.findExperimenter(user);
        }
    }

    protected void assertFails(LdapImpl ldap, Map<String, List<String>> users) {
        for (String user : users.keySet()) {
            try {
                ldap.findExperimenter(user);
                fail();
            } catch (Exception e) {
                // good
            }
        }
    }

}
