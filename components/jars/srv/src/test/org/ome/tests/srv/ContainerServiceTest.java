/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.model.IProject;
import org.ome.model.LSID;
import org.ome.interfaces.ContainerService;
import org.ome.model.Vocabulary;
;

/**
 * @author josh
 */
public class ContainerServiceTest extends BaseServiceTestCase {

	ContainerService cs;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ContainerServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cs = (ContainerService) SpringTestHarness.ctx.getBean("containerService");
	}
	
	public void testUpdateProject() throws RemoteException, URISyntaxException{

	    // Let's set the description on a project
	    
	    List l1 = cs.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		assertTrue("List should contain an element",l1.size()>0);
		IProject p1 = (IProject) l1.get(0);
		
		String oldDesc = p1.getDescription();
		p1.setDescription("This is my new Description>>"+System.currentTimeMillis());
		cs.updateProject(p1);
		
		IProject p2 = cs.retrieveProject(p1.getLSID());
		System.err.println(p2);
		assertTrue("The description should have changed",oldDesc!=p2.getDescription());
		
		// Now for an immutable project
		
		List l2 = cs.retrieveProjectsByExperimenter(null); // This should return all projects
		assertTrue("List should have all the projects in it",l2.size()>0);
		IProject p3 = null;
		for (Iterator i = l2.iterator(); i.hasNext();) {
            IProject element = (IProject) i.next();
            if (null!=element.getImmutable()&&element.getImmutable().booleanValue()){
                p3=element;
            }
        }
		assertTrue("There should be one immutable project",null!=p3);
		p3.setDescription("This description should never get set"+System.currentTimeMillis());
		boolean exceptionThrown = false;
		try {
		    cs.updateProject(p3);
		} catch (Exception e){
		    exceptionThrown=true;
		}
		assertTrue("An exception should be thrown on updating an immutable",exceptionThrown);
		
	}
	

	public void testProjectsByExperimenter1() throws RemoteException, URISyntaxException{
		List l = cs.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = iter.next();
			System.out.println(element);
		}
		assertTrue(l.size() > 0);
		
	}
	
	
}
