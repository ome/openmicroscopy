/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import net.sf.acegisecurity.AccessDeniedException;

import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
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
	
	public void testGetNotMyImage() throws URISyntaxException{
		List l = is.queryImagesByExperimenter(new LSID(Vocabulary.NS+"Jason"));
		assertTrue("There should be one such image",l.size()>0);

		SpringTestHarness.setUserAuth();
		boolean accessDenied = false;
		try { 
		    is.retrieveImage(((IImage)l.get(0)).getLSID());
		} catch (AccessDeniedException ade){
		    accessDenied=true;
		}
		assertTrue("We shouldn't be able to see these",accessDenied);
		
		SpringTestHarness.setAdminAuth();
		try { 
		    is.retrieveImage(((IImage)l.get(0)).getLSID());
		} catch (AccessDeniedException ade){
		    fail("Access should not be denied as admin\n"+ade.getMessage());
		}

	}
	
	public void testGetImagesByProject() throws URISyntaxException{
		List l = is.queryImagesByProject(new LSID(Vocabulary.NS+"proj1"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			LSObject element = (LSObject) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size()>0);
	}
	
	public void testGetImagesByDataset() throws URISyntaxException{
		List l = is.queryImagesByDataset(new LSID(Vocabulary.NS+"ds1"));
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			LSObject element = (LSObject) iter.next();
			System.out.println(element);
		}
		assertTrue(l.size()>0);
	}
	
}
