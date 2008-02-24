/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.itests;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

public class UsageTest extends TestCase {

    @Test
    public void testClientClosedAutomatically() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:ice.config");
        File f2 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1, f2);
        client.closeOnDestroy();
    }

    @Test
    public void testClientClosedManually() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:ice.config");
        File f2 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1, f2);
        client.closeOnDestroy();
        client.close();
    }
}
