/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.itests;

import java.io.File;

import junit.framework.TestCase;
import omero.RString;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

public class UsageTest extends TestCase {

    @Test
    public void testClientClosedAutomatically() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();
        client.getServiceFactory().closeOnDestroy();
    }

    @Test
    public void testClientClosedManually() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();
        client.getServiceFactory().closeOnDestroy();
        client.close();
    }

    @Test
    public void testUseSharedMemory() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();

        assertEquals(0, client.getInputKeys().size());
        client.setInput("a", new omero.RString("b"));
        assertEquals(1, client.getInputKeys().size());
        assertTrue(client.getInputKeys().contains("a"));
        assertEquals("b", ((RString) client.getInput("a")).val);

        client.close();
    }

}
