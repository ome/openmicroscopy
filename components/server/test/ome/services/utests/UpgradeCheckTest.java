/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.utests;

import java.util.ResourceBundle;

import junit.framework.TestCase;
import ome.services.util.UpgradeCheck;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.3
 */
public class UpgradeCheckTest extends TestCase {

    ResourceBundle bundle = ResourceBundle.getBundle("omero");
    UpgradeCheck check = new UpgradeCheck();

    @Test
    public void testNoActionOnNull() throws Exception {
        check.setPoll(1);
        check.setVersion(bundle.getString("omero.version"));
        check.setUrl(null);
        check.init();
    }

    @Test
    public void testNoActionOnEmpty() throws Exception {
        check.setPoll(1);
        check.setVersion(bundle.getString("omero.version"));
        check.setUrl("");
        check.init();
    }

    @Test
    public void testNoResponse() throws Exception {
        check.setPoll(1);
        check.setVersion(bundle.getString("omero.version"));
        check.setUrl(bundle.getString("omero.upgrades.url"));
        check.init();
    }

    @Test
    public void testWrongVersion() throws Exception {
        check.setPoll(1);
        check.setVersion("XXX" + bundle.getString("omero.version"));
        check.setUrl(bundle.getString("omero.upgrades.url"));
        check.init();
    }

    @Test
    public void tesBadUrl1() throws Exception {
        check.setPoll(1);
        check.setVersion("XXX" + bundle.getString("omero.version"));
        check.setUrl("http://foo");
        check.init();
    }

    @Test
    public void tesBadUrl2() throws Exception {
        check.setPoll(1);
        check.setVersion("XXX" + bundle.getString("omero.version"));
        check.setUrl("file:///dev/null"); // probably returns an ok.
        check.init();
    }

    @Test
    public void tesBadUrl3() throws Exception {
        check.setPoll(1);
        check.setVersion("XXX" + bundle.getString("omero.version"));
        check.setUrl("abcp");
        check.init();
    }

    @Test
    public void tesBadUrl4() throws Exception {
        check.setPoll(1);
        check.setVersion("XXX" + bundle.getString("omero.version"));
        check.setUrl("abc://bar");
        check.init();
    }

}