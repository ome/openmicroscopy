/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ome.api.ILdap;
import ome.logic.LdapImpl;
import ome.security.SecureLdapContextSource;
import ome.security.auth.PasswordProvider;
import ome.security.auth.RoleProvider;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
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
                false);
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
    public void testLdiffFile(File file) throws Exception {
        ApplicationContext ctx = createContext(file);
        Properties goodProps = (Properties) ctx.getBean("good");
        Properties badProps = (Properties) ctx.getBean("bad");
        Map<String, String[]> good = parse(goodProps);
        Map<String, String[]> bad = parse(badProps);
    }

    protected ApplicationContext createContext(File ldifFile) throws Exception {
        FileSystemXmlApplicationContext ctx =
            new FileSystemXmlApplicationContext("file:" + ldifFile.getAbsolutePath());
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
    protected ILdap makeService(ApplicationContext context) throws Exception {
        SecureLdapContextSource source = new  SecureLdapContextSource("url");
        LdapTemplate template = new LdapTemplate(source);
        Mock roleMock = mock(RoleProvider.class);
        RoleProvider roleProvider = (RoleProvider) roleMock.proxy();
        Mock passMock = mock(PasswordProvider.class);
        PasswordProvider passwordProvider = (PasswordProvider) passMock.proxy();

        LdapImpl ldap = new LdapImpl(roleProvider, passwordProvider, template,
                "new_user_group", "groups", "attributes", "values", true);
        return ldap;
    }

    protected void assertPasses(File file, Map<String, String[]> users) {
        fail();
    }

    protected void assertFails(File file, Map<String, String[]> users) {
        fail();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String[]> parse(Properties properties) throws Exception {

        Set<Object> names = properties.keySet();

        Map<String, String[]> rv = new HashMap<String, String[]>();
        for (Object key : names) {
            Object value = properties.get(key);
            rv.put(key.toString(), value.toString().split(","));
        }
        return rv;
    }

}
