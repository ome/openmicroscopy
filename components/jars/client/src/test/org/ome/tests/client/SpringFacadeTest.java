/*
 * Created on Feb 27, 2005
*/
package org.ome.tests.client;

import java.util.HashSet;

import org.ome.omero.interfaces.HierarchyBrowsing;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class SpringFacadeTest extends TestCase {

    HierarchyBrowsing srv;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringFacadeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        srv = (HierarchyBrowsing) SpringTestHarness.ctx.getBean("hierarchyBrowsingFacade");
    }

    public void test1() {
        try {
            srv.findPDIHierarchies(new HashSet());
        } catch (Throwable t){
            t.printStackTrace();
            throw new RuntimeException(t);
        }
        
    }
    
}
