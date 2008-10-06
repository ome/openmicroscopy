/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc.. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.testng.annotations.Test;

@Test(groups = { "integration", "blitz", "client" })
public class ClientConstructorsTest extends TestCase {

    public void testHostConstructor() throws Exception {
        omero.client c = new omero.client("localhost");
        c.createSession("root", "ome");
        c.closeSession();
        c.createSession("root", "ome");
        c.closeSession();
    }

    public void testInitializationDataConstructor() throws Exception {
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        id.properties.setProperty("omero.host", "localhost");
        id.properties.setProperty("omero.user", "root");
        id.properties.setProperty("omero.pass", "ome");
        omero.client c = new omero.client(id);
        c.createSession();
        c.closeSession();
        c.createSession();
        c.closeSession();
    }

    public void testMainArgsConstructor() throws Exception {
        String[] args = new String[] {"--omero.host=localhost","--omero.user=root", "--omero.pass=ome"};
        omero.client c = new omero.client(args);
        c.createSession();
        c.closeSession();
        c.createSession();
        c.closeSession();
    }
    
    public void testMapConstructor() throws Exception {
        Properties p = new Properties();
        p.put("omero.host","localhost");
        p.put("omero.user","root");
        p.put("omero.pass","ome");
        omero.client c = new omero.client(p);
        c.createSession();
        c.closeSession();
        c.createSession();
        c.closeSession();
    }
    
    public void testMainArgsGetsIcePrefix() throws Exception {
        String[] args = new String[] {"--Ice.MessageSizeMax=10","--omero.host=localhost","--omero.user=root", "--omero.pass=ome"};
        omero.client c = new omero.client(args);
        c.createSession();
        assertEquals("10", c.getProperty("Ice.MessageSizeMax"));
        c.closeSession();
    }

    
}
