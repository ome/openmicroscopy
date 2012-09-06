/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;

import org.testng.annotations.Test;

/**
 * Unit tests which do not require a database connection but DO require a
 * working ApacheDS setup (i.e. the embedded LDAP server).
 */
@Test
public class LdapUnitTest extends AbstractLdapTest {

    /**
     * 
     * @param ctxFile
     * @return
     * @throws Exception
     */
    protected Fixture createFixture(File ctxFile) throws Exception {
        return new UnitFixture(ctxFile);
    }

    //
    // The following tests are all hard-coded to point at the sub-directories
    // of the ome.services.ldap test package. If any new directories are added,
    // they should manually be added here UNLESS they should only be run by
    // subclasses of LdapUnitTest (like LdapIntegrationTest).
    //

    public void testAttributeFilter() throws Exception {
        File file = getLdifContextFile("attributeFilter"); 
        assertLdifFile(file);
    }

    public void testBcrt() throws Exception {
        File file = getLdifContextFile("bcrt"); 
        assertLdifFile(file);
    }

    public void testCaseSensitive() throws Exception {
        File file = getLdifContextFile("caseSensitive"); 
        assertLdifFile(file);
    }

    public void testcnonly() throws Exception {
        File file = getLdifContextFile("cnonly"); 
        assertLdifFile(file);
    }

    public void testMultipleGroups() throws Exception {
        File file = getLdifContextFile("multipleGroups"); 
        assertLdifFile(file);
    }

    public void testNonCn() throws Exception {
        File file = getLdifContextFile("nonCn"); 
        assertLdifFile(file);
    }

    public void testOu1() throws Exception {
        File file = getLdifContextFile("ou1"); 
        assertLdifFile(file);
    }

    public void testPosix() throws Exception {
        File file = getLdifContextFile("posix"); 
        assertLdifFile(file);
    }

    public void testPosixNoGroup() throws Exception {
        File file = getLdifContextFile("posixNoGroup"); 
        assertLdifFile(file);
    }

    public void testPosixWithOu() throws Exception {
        File file = getLdifContextFile("posixWithOu"); 
        assertLdifFile(file);
    }

    public void test1() throws Exception {
        File file = getLdifContextFile("test1"); 
        assertLdifFile(file);
    }
    
    public void testAttrGroup() throws Exception {
        File file = getLdifContextFile("testAttrGroup"); 
        assertLdifFile(file);
    }
    
    public void testOuGroup() throws Exception {
        File file = getLdifContextFile("testOuGroup"); 
        assertLdifFile(file);
    }
    
    public void testQueryGroup() throws Exception {
        File file = getLdifContextFile("testQueryGroup"); 
        assertLdifFile(file);
    }
    
    public void testWeird() throws Exception {
        File file = getLdifContextFile("weird"); 
        assertLdifFile(file);
    }
}
