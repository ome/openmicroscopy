/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.*;
import junit.framework.TestCase;
import omero.RString;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Various uses of the {@link omero.client} object.
 * All configuration comes from the ICE_CONFIG
 * environment variable.
 */
public class ClientUsageTest extends TestCase {

	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    private omero.client refClient;
    
	/**
     * Initializes the various services.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception 
    {
    	refClient = new omero.client();
    	refClient.createSession();
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterMethod
    public void tearDown() 
    	throws Exception 
    {
    	refClient.closeSession();
    	refClient.getSession().destroy();
    }
    
    @Test
    public void testClientClosedAutomatically() throws Exception {
        omero.client client = new omero.client();
        client.createSession();
        client.getSession().closeOnDestroy();
    }

    @Test
    public void testClientClosedManually() throws Exception {
        omero.client client = new omero.client();
        client.createSession();
        client.getSession().closeOnDestroy();
        client.closeSession();
    }

    @Test
    public void testUseSharedMemory() throws Exception {
        omero.client client = new omero.client();
        client.createSession();

        assertEquals(0, client.getInputKeys().size());
        client.setInput("a", rstring("b"));
        assertEquals(1, client.getInputKeys().size());
        assertTrue(client.getInputKeys().contains("a"));
        assertEquals("b", ((RString) client.getInput("a")).getValue());

        client.closeSession();
    }

    @Test
    public void testCreateInsecureClientTicket2099() throws Exception {
        omero.client secure = new omero.client();
        assertTrue(secure.isSecure());
        try {
            secure.createSession().getAdminService().getEventContext();
            omero.client insecure = secure.createClient(false);
            try {
                insecure.getSession().getAdminService().getEventContext();
                assertFalse(insecure.isSecure());
            } finally {
                insecure.closeSession();
            }
        } finally {
            secure.closeSession();
        }
    }

}
