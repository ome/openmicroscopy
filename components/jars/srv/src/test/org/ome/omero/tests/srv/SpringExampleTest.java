/*
 * Created on Feb 26, 2005
*/
package org.ome.tests.srv;

import java.util.Iterator;
import java.util.List;

import org.ome.interfaces.ContainerService;
import org.ome.model.LSObject;


/**
 * @author josh
 */
public class SpringExampleTest extends BaseServiceTestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringExampleTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println(SpringTestHarness.ctx.getBeanDefinitionNames());
    }

    public void testSimple() {
        ContainerService cs = (ContainerService) SpringTestHarness.ctx.getBean("containerService");
        List l = cs.retrieveProjectsByExperimenter(null);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            LSObject element = (LSObject) iter.next();
            System.out.println(element);
        }
    }
    
    
}
