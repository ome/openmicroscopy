/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.ome.model.IExperimenter;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ContainerService;
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
		ServiceFactory factory = new ServiceFactoryImpl();
		a = factory.getAdministrationService();
	}
	
	public void testExperimenter1() throws RemoteException, URISyntaxException{
		IExperimenter l = (IExperimenter) a.getExperimenter(new LSID(Vocabulary.NS+"Josh"));
		System.out.println(l);
		assertTrue(l.getFirstName()!=null);
	}
	
}
