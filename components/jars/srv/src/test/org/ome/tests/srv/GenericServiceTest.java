/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;

import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.GenericService;
;

/**
 * @author josh
 */
public class GenericServiceTest extends BaseServiceTestCase {

	GenericService g;
	
	// TODO transaction tests, null LSIDs, catching class cast
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		g = (GenericService) SpringTestHarness.ctx.getBean("genericService");
	}
	
	public void testGetLSObject() throws URISyntaxException, RemoteException{
		LSID l1 = new LSID("http://www.openmicroscopy.org/2005/OME.owl#proj1");
		LSObject o1 = g.getLSObject(l1);
		assertTrue(o1!=null);

		LSObject o2 = g.getLSObject(new LSID("lsid:this_does_not_exist"));
		assertTrue(o2==null);
	}
	
// TODO make this test work
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
