/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.ome.interfaces.ImageService;
import org.ome.interfaces.ServiceFactory;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;
import org.ome.srv.logic.ServiceFactoryImpl;
;

/**
 * @author josh
 */
public class ImageServiceTest extends TestCase {

	ImageService is;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ImageServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ServiceFactory factory = new ServiceFactoryImpl();
		is = factory.getImageService();
	}
	
	public void testGetImage() throws RemoteException, URISyntaxException{
		is.retrieveImagesByExperimenter(new LSID(Vocabulary.NS+"Josh"));
	}
	
}
