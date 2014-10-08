/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import junit.framework.TestCase;
import ome.system.OmeroContext;
import ome.system.UpgradeCheck;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.3
 */
public class UpgradeCheckTest extends TestCase {

    OmeroContext ctx = new OmeroContext(new String[]{"classpath:ome/config.xml"});
    String url = ctx.getProperty("omero.upgrades.url");
    String version = ctx.getProperty("omero.version");
    ome.system.UpgradeCheck check;

    @Test
    public void testNoActionOnNull() throws Exception {
        check = new UpgradeCheck(null, version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertFalse(check.isExceptionThrown());
    }

    @Test
    public void testNoActionOnEmpty() throws Exception {
        check = new UpgradeCheck("", version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertFalse(check.isExceptionThrown());

    }

    @Test
    public void testSlowResponse() throws Exception {
        check = new UpgradeCheck("http://127.0.0.1:8000", version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test
    public void testSlowResponse2() throws Exception {
        check = new UpgradeCheck("http://127.0.0.1:9998", version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());
    }

    @Test
    public void testBadIp() throws Exception {
        check = new UpgradeCheck("200.200.200.200", version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test
    public void testWrongVersion() throws Exception {
        check = new UpgradeCheck("200.200.200.200", "XYZ" + version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test(enabled = false)
    public void testBadUrl1() throws Exception {
        check = new UpgradeCheck("http://foo", "XYZ" + version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test
    public void testBadUrl2() throws Exception {
        check = new UpgradeCheck("file://dev/null", "XYZ" + version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test
    public void testBadUrl3() throws Exception {
        check = new UpgradeCheck("abcp", "XYZ" + version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

    @Test
    public void testBadUrl4() throws Exception {
        check = new UpgradeCheck("abc://bar", "XYZ" + version, "test");
        check.run();
        assertFalse(check.isUpgradeNeeded());
        assertTrue(check.isExceptionThrown());

    }

}
