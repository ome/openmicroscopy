/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import ome.services.icy.client.IceServiceFactory;
import ome.system.OmeroContext;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.constants.UPDATESERVICE;
import omero.constants.USERNAME;
import omero.model.Image;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import Ice.RouterPrx;

public class LoginTest extends IceTest {
        
    @Test
    public void testStart() throws Exception {

        Map<String, String> context = new HashMap<String, String>();
        context.put(USERNAME.value, "test");
        
        int status = 0;
        ic = null;
        Ice.InitializationData id = new Ice.InitializationData();
        id.defaultContext = context;
        id.properties = Ice.Util.createProperties();
        id.properties.setProperty("Ice.Default.Router","OMEROGlacier2/router:tcp -p 9998 -h 127.0.0.1");
        id.properties.setProperty("Ice.ACM.Client","0");
        id.properties.setProperty("Ice.MonitorConnections","60");
        id.properties.setProperty("Ice.RetryIntervals","-1");
        id.properties.setProperty("Ice.Warn.Connections","1");
        ic = Ice.Util.initialize(id);
        getSession(ic);
        checkUpdateService(ic);
    }

    @Test
    public void testWithContext() throws Exception {
        // Using non-static context to prevent "session exists" errors 
        OmeroContext ctx = OmeroContext.getContext(new Properties(),"OMERO.client");
        ic = (Ice.Communicator) ctx.getBean("Ice.Communicator");
        getSession(ic);
        checkUpdateService(ic);
        ctx.destroy();
    }

    @Test
    public void testWithSessionFactory() throws Exception {
        // Using non-static context to prevent "session exists" errors
        IceServiceFactory ice = new IceServiceFactory(null, null, null);
        ic = ice.getCommunicator();
        getSession(ice.getCommunicator());
        IUpdatePrx prx = checkUpdateService(ice.getCommunicator());
        ice.getContext().destroy();
    }
    
}
