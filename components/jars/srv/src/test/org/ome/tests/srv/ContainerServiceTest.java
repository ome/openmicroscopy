/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.srv.db.jena.JenaAdministrationStore;
import org.ome.model.Vocabulary;
import org.ome.model.IProject;
;

/**
 * @author josh
 */
public class ContainerServiceTest extends TestCase {

	ContainerService a;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ContainerServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ServiceFactory factory = TemporaryDBFactoryFactory.getServiceFactory();
		a = factory.getContainerService();
	}
	
	public void testProjectsByExperimenter1() throws RemoteException, URISyntaxException{
		List l = a.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size() > 0);
	}
	
//	/** net/rmi implementation should return only I<Interface> implementations */
//	public void testNetProjectsByExperimenterReturnsOnlyProjects() throws RemoteException, URISyntaxException{
//		AdministrationService a = new org.ome.srv.net.rmi.RMIAdministrationFacade();
//		List l = a.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
//		for (Iterator iter = l.iterator(); iter.hasNext();) {
//			Object element = (Object) iter.next();
//			assertTrue(element instanceof IProject);
//		}
//	}
	
	/** db implementation should return only LSObjects */
	public void testDBProjectsByExperimenterReturnsOnlyLSObjects() throws RemoteException, URISyntaxException{
		ContainerService cs = new org.ome.srv.db.jena.JenaContainerStore();
		List l = cs.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			assertTrue(element instanceof LSObject);
		}
	}
	
//	public void testLogicProjectsByExperimentsReturnsOnlyProjects(){
//		
//	}

	
}
