/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.srv.db.jena.JenaAdministrationStore;
import org.ome.texen.Vocabulary;
import org.ome.texen.interfaces.IProject;
;

/**
 * @author josh
 */
public class AdminServiceTest extends TestCase {

	AdministrationService a;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AdminServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ServiceFactory factory = TemporaryDBFactoryFactory.getServiceFactory();
		a = factory.getAdministrationService();
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
		AdministrationService a = new org.ome.srv.db.jena.JenaAdministrationStore();
		List l = a.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			assertTrue(element instanceof ILSObject);
		}
	}
	
//	public void testLogicProjectsByExperimentsReturnsOnlyProjects(){
//		
//	}

	
}
