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
public class MuleUnitTest extends TestCase {

    OmeroContext ctx;

    ApplicationEvent event;

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @BeforeClass
    public void adaptSetUp() throws Exception {
        super.setUp();
        ctx = new OmeroContext("ome/server/utests/mule/MuleUnitTest.xml");
    }

    @AfterClass
    public void adaptTearDown() throws Exception {
        ctx.close();
        super.tearDown();
    }

    // =========================================================================

    @Test
    public void testSimplePublish() throws Exception {
        event = new InternalMsg(this);
        ctx.publishEvent(event);
        MuleListener listener = (MuleListener) ctx.getBean("simpleListener");
        assertTrue(listener.check);
    }

    @Test
    public void testSimpleUMO() throws Exception {
        event = new ExternalMsg("hi","vm://simpleUMO");
        ctx.publishEvent(event);
        Thread.sleep(1500L);
        Service service = (Service) ctx.getBean("umoService");
        assertTrue(service.check);
    }

    @Test
    public void testSimpleUMOWithMapping() throws Exception {
        event = new ExternalMsg("hi","endpoint.vm.simpleUMO");
        ctx.publishEvent(event);
        Thread.sleep(1500L);
        Service service = (Service) ctx.getBean("umoService");
        assertTrue(service.check);
    }

    @Test
    public void testExploringTheBus() throws Exception {
        Bus bus = new Bus();
        M msg = new M(new Parameters());
        final boolean t[] = new boolean[1];
        ApplicationListener al = new ApplicationListener() {
          public void onApplicationEvent(ApplicationEvent event) {
              t[0] = true;
            }  
        };
        bus.addApplicationListener(al);
        bus.onApplicationEvent(msg);
        assertTrue(t[0]);
        
        /*
         * use case:
         * from blitz someone registers via:
         *   sessionFactoryI.registerMessageListener(messageListener); // or
         *   sessionFactoryI.registerAsyncMessageListener(messageListener, id);
         */ 
        
    
    }

    
}

// ~ Helper classes
// =========================================================================

class Service implements Callable {
    boolean check = false;
    public Object onCall(UMOEventContext arg0) throws Exception {
        check = true;
        return null;
    }

}

class MuleListener implements ApplicationListener {
    boolean check = false;
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof InternalMsg) {
            check = true;
        }
    }

}

class XmppService {
    public String doReply (String req) {
       String reply = "I can't understand you";

       if (req.equalsIgnoreCase("date")) {
          Date now = new Date();
          DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.FULL);
          reply = dateFormatter.format(now);
          return reply;
       }

       else if (req.equalsIgnoreCase("time")) {
          Date now = new Date();
          DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
          reply = timeFormatter.format(now);
          return reply;
       }

       else if (req.equalsIgnoreCase("datetime")) {
          Date now = new Date();
          reply = now.toString();
          return reply;
       }

      else return reply;
    }
};
