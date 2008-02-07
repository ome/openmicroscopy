/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.mule;

import java.text.DateFormat;
import java.util.Date;

import junit.framework.TestCase;
import ome.parameters.Parameters;
import ome.system.OmeroContext;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class BusUnitTest extends TestCase {


    @Test
    public void testExploringTheBus() throws Exception {
        Bus bus = new Bus();
        M msg = new M(new Parameters());
        final int count[] = new int[1];
        ApplicationListener al = new ApplicationListener() {
          public void onApplicationEvent(ApplicationEvent event) {
              count[0]++;
            }  
        };
        bus.addApplicationListener(al);
        bus.onApplicationEvent(msg);
        assertTrue(count[0] == 1);
        // removing and calling again
        bus.removeApplicationListener(al);
        bus.onApplicationEvent(msg);
        assertTrue(count[0] == 1);
        
        M msg2 = new M(new Parameters(),"vm://test");
        Adapter a = new Adapter();
        bus.registerAsyncListener(a, "vm://test");
        bus.onApplicationEvent(msg2);
        Thread.sleep(2*1000L);
        assertTrue(a.calls == 1);
        // removing and calling again.
        bus.unregisterAsyncListener("vm://test");
        bus.onApplicationEvent(msg2);
        Thread.sleep(2*1000L);
        assertTrue(a.calls == 1);
        
        
        /*
         * use case:
         * from blitz someone registers via:
         *   sessionFactoryI.registerMessageListener(messageListener); // or
         *   sessionFactoryI.registerAsyncMessageListener(messageListener, id);
         */ 
        
    
    }

    
}
