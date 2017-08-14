/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import ome.security.SecuritySystem;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.util.BlitzConfiguration;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
public class BlitzConfigurationTest extends MockObjectTestCase {

    Ring ring;
    BlitzConfiguration config;
    SessionManager sm;
    SecuritySystem ss;
    Executor ex;
    Mock m_sm, m_ss, m_ex;
    
    @BeforeClass(groups = "integration")
    public void setup() throws Exception {
        ring = new Ring("uuid", null, null, null);
    }
    
    @Test(groups = "integration")
    public void testCreation() throws Exception {
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        id.properties.setProperty("BlitzAdapter.Endpoints", "default -h 127.0.0.1");
        config = new BlitzConfiguration(id, ring, sm, ss, ex, 10000);
    }
    
    @Test(groups = "integration")
    public void testCreationAndDestruction() throws Exception {
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        id.properties.setProperty("BlitzAdapter.Endpoints", "default -h 127.0.0.1");
        config = new BlitzConfiguration(id, ring, sm, ss, ex, 10000);
        config.destroy();
    }

}
