/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.ome.model.IProject;
import org.ome.model.LSID;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.logic.ServiceFactoryImpl;
import org.ome.model.Vocabulary;
;

/**
 * @author josh
 */
public class ContainerServiceTest extends TestCase {

	ContainerService cs;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ContainerServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ServiceFactory factory = new ServiceFactoryImpl();
		cs = factory.getContainerService();
	}
	
	public void testProjectsByExperimenter1() throws RemoteException, URISyntaxException{
		List l = cs.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size() > 0);
		
	}

	public void testProjectByLSID() throws RemoteException, URISyntaxException{
		List l = cs.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		IProject p = (IProject) l.get(0);
		//These should be equal!
		System.out.println(cs.retrieveProject(p.getLSID()));
		
		

	}
	
	
}
