/*
 * Created on Feb 27, 2005
*/
package org.ome.tests.client;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.ImageService;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class SpringFacadeTest extends TestCase {

    AdministrationService as;
    ContainerService cs;
    ImageService is;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringFacadeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        as = (AdministrationService) SpringTestHarness.ctx.getBean("administrationFacade");
        cs = (ContainerService) SpringTestHarness.ctx.getBean("containerFacade");
        is = (ImageService) SpringTestHarness.ctx.getBean("imageFacade");
    }

    public void testCS() throws RemoteException, URISyntaxException{
        LSID lsid = new LSID(Vocabulary.NS+"Josh");
        List l = cs.retrieveProjectsByExperimenter(lsid);
        System.out.println(l.get(0));
        
        l = is.queryImagesByProject(null);
        System.out.println(l.get(0));
        
        as.createExperimenter();
        
    }
    
}
