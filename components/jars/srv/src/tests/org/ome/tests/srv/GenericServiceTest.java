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
import org.ome.interfaces.GenericService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.srv.db.jena.JenaAdministrationStore;
import org.ome.texen.Vocabulary;
import org.ome.texen.interfaces.IProject;
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
		ServiceFactory factory = TemporaryDBFactoryFactory.getServiceFactory();
		g = factory.getGenericService();
	}
	
	public void testGetLSObject() throws URISyntaxException{
		LSID l1 = new LSID("http://www.openmicroscopy.org/2005/OME.owl#proj1");
		ILSObject o1 = g.getLSObject(l1);
		assertTrue(o1!=null);

		ILSObject o2 = g.getLSObject(new LSID("lsid:this_does_not_exist"));
		assertTrue(o2==null);
		
	}
}
