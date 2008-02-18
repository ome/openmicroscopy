/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import java.util.ResourceBundle;

import junit.framework.TestCase;
import ome.system.UpgradeCheck;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.3
 */
public class UpgradeCheckTest extends TestCase {

    ResourceBundle bundle = ResourceBundle.getBundle("omero");
    String version = bundle.getString("omero.version");
    String url = bundle.getString("omero.upgrades.url");
    ome.system.UpgradeCheck check;

    @Test
    public void testNoActionOnNull() throws Exception {
        check = new UpgradeCheck(null, version, "test");
        check.run();
    }

    @Test
    public void testNoActionOnEmpty() throws Exception {
        check = new UpgradeCheck("", version, "test");
        check.run();
    }

    @Test
    public void testNoResponse() throws Exception {
        check = new UpgradeCheck(url, version, "test");
        check.run();
    }

    @Test
    public void testSlowResponse() throws Exception {
        check = new UpgradeCheck("http://127.0.0.1:8000", version, "test");
        check.run();
    }

    @Test
    public void testSlowResponse2() throws Exception {
        check = new UpgradeCheck("http://127.0.0.1:9998", version, "test");
        check.run();
    }

    @Test
    public void testBadIp() throws Exception {
        check = new UpgradeCheck("200.200.200.200", version, "test");
        check.run();
    }

    @Test
    public void testWrongVersion() throws Exception {
        check = new UpgradeCheck("200.200.200.200", "XXX" + version, "test");
        check.run();
    }

    @Test
    public void tesBadUrl1() throws Exception {
        check = new UpgradeCheck("http://foo", "XXX" + version, "test");
        check.run();
    }

    @Test
    public void tesBadUrl2() throws Exception {
        check = new UpgradeCheck("file://dev/null", "XXX" + version, "test");
        check.run();
    }

    @Test
    public void tesBadUrl3() throws Exception {
        check = new UpgradeCheck("abcp", "XXX" + version, "test");
        check.run();
    }

    @Test
    public void tesBadUrl4() throws Exception {
        check = new UpgradeCheck("abc://bar", "XXX" + version, "test");
        check.run();
    }

}
