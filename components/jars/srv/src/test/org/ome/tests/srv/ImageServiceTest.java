/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.interfaces.ImageService;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.Vocabulary;
;

/**
 * @author josh
 */
public class ImageServiceTest extends BaseServiceTestCase {

	ImageService is;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ImageServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		is = (ImageService) SpringTestHarness.ctx.getBean("imageService");
	}
	
	public void testGetImage() throws RemoteException, URISyntaxException{
		is.retrieveImagesByExperimenter(new LSID(Vocabulary.NS+"Josh"));
	}
	
	public void testGetImagesByProject() throws RemoteException, URISyntaxException{
		List l = is.retrieveImagesByProject(new LSID(Vocabulary.NS+"proj1"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			LSObject element = (LSObject) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size()>0);
	}
	
	public void testGetImagesByDataset() throws RemoteException, URISyntaxException{
		List l = is.retrieveImagesByDataset(new LSID(Vocabulary.NS+"ds1"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			LSObject element = (LSObject) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size()>0);
	}
	
}
