/*
 * Copyright 2010 Glencoe Software, Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jmock.MockObjectTestCase;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * Uses LDIF text files along with Spring XML files of good and bad user names
 * to test that the LDAP plugin is properly functioning.
 */
@Test(groups = "ldap")
public abstract class AbstractLdapTest
    extends MockObjectTestCase
{

    protected abstract Fixture createFixture(File file)
        throws Exception;

    /**
     * Loads a single Spring xml file from a directory in the same directory as
     * the current class with the given name. For example, if the current test
     * is LdapTest.class (in server/test/ome/services/ldap) then passing the
     * string "attributeFilter" will return the file
     * server/test/ome/services/ldap/attributeFilter/test.xml. The file need not
     * be named "test.xml" but must end with "xml".
     */
    public File getLdifContextFile(String dirName)
        throws Exception
    {
        String name = getClass().getName();
        name = name.replaceAll("[.]", "//");
        name = "classpath:" + name + ".class";
        File file = ResourceUtils.getFile(name);
        File dir = file.getParentFile();
        dir = new File(dir, dirName);
        Collection<?> coll = FileUtils.listFiles(dir, new String[] { "xml" },
            true);
        if (coll.size() != 1)
        {
            throw new IllegalArgumentException(
                "Expected exactly one XML file. Found: " + coll);
        }
        return (File) coll.iterator().next();
    }

    /**
     * Runs the LDAP test suite against each of the given *.ldif files, by
     * attempting to login against an embedded ldap store with both the good
     * names and the bad names.
     */
    protected void assertLdifFile(File file)
        throws Exception
    {

        Fixture fixture = createFixture(file);
        if (fixture == null)
        {
            // Skipping this fixture. Continue.
            // See LdapInitTest for an example of skippage.
            return;
        }

        try
        {
            fixture.assertGoodPasses();
            fixture.assertBadFails();
        }
        finally
        {
            fixture.close();
        }

    }
}
