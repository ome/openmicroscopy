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
import org.ome.model.Project;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.srv.db.jena.JenaAdministrationStore;
import org.ome.srv.logic.ServiceFactoryImpl;
import org.ome.model.Vocabulary;
import org.ome.model.IProject;
;

/**
 * @author josh
 */
public class GenericServiceTest extends TestCase {

	GenericService g;
	
	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ServiceFactory factory = new ServiceFactoryImpl();
		g = factory.getGenericService();
	}
	
	public void testGetLSObject() throws URISyntaxException, RemoteException{
		LSID l1 = new LSID("http://www.openmicroscopy.org/2005/OME.owl#proj1");
		LSObject o1 = g.getLSObject(l1);
		assertTrue(o1!=null);

		LSObject o2 = g.getLSObject(new LSID("lsid:this_does_not_exist"));
		assertTrue(o2==null);
	}
	
// TODO
//	public void testAllObjectsByType() throws URISyntaxException, RemoteException{
//		IProject proj = new Project(new LSID("EMPTY"));
//		LSID lsid = new LSID(proj.getURI());
//
//		List l1 = g.getLSObjectsByLSIDType(lsid);
//		assertTrue(null!=l1 && l1.size() > 0);
//		
//		List l2 = g.getLSObjectsByClassType(proj.getClass());
//		assertTrue(null!=l2 && l2.size() > 0);
//		
//		assertEquals(l1.size(),l2.size());
//		
//	}
	
}
