/*
 * Created on Feb 27, 2005
*/
package org.ome.tests.client;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.interfaces.ContainerService;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class RMISpringTest extends TestCase {

    static {
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new SecurityManager());
		}
    }
    
    ContainerService cs;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RMISpringTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        cs = (ContainerService) SpringTestHarness.ctx.getBean("containerService");
    }

    public void testCS() {
        List l = cs.retrieveProjectsByExperimenter(null);
        System.out.println(l.get(0));
        
    }
    
}
