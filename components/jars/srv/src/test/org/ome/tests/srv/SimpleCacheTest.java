/*
 * Created on Mar 1, 2005
*/
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.cache.Cache;
import org.ome.interfaces.ImageService;
import org.ome.interfaces.VersionService;
import org.ome.model.IImage;
import org.ome.model.Image;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;

/**
 * @author josh
 */
public class SimpleCacheTest extends BaseServiceTestCase{
  	
    	VersionService vs;
    	ImageService is;
    	Cache c;
    	
    	public static void main(String[] args) {
    		junit.textui.TestRunner.run(ImageServiceTest.class);
    	}
    	
    	/* (non-Javadoc)
    	 * @see junit.framework.TestCase#setUp()
    	 */
    	protected void setUp() throws Exception {
    		super.setUp();
    		is = (ImageService) SpringTestHarness.ctx.getBean("imageService");
    		vs = (VersionService) SpringTestHarness.ctx.getBean("versionService");
    		c = (Cache) SpringTestHarness.ctx.getBean("cache");
    	}
    	
    	public void testGetImageThenInCache() throws URISyntaxException {
    		IImage img = is.retrieveImage(new LSID(Vocabulary.NS+"img1"));
    		assertNotNull("Cache should be filled",c.get(img.getLSID()));
    		/* now update and cache should be empty */
    		img.setDescription("this is a new description/"+System.currentTimeMillis());
    		int before = vs.retrieveVersion(img.getLSID());
    		is.updateImage(img);
    		int after = vs.retrieveVersion(img.getLSID());
    		assertTrue("Version should be updated",after>before);
    	}
    	
}
