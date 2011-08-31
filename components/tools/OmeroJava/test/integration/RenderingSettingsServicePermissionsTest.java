/*
 * $Id$
 *
 *  Copyright 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import omero.api.IRenderingSettingsPrx;
import omero.model.ChannelBinding;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.sys.EventContext;

import org.testng.annotations.Test;


/** 
 * Tests copy/paste of rendering settings depending on the group's permissions.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class RenderingSettingsServicePermissionsTest 
	extends AbstractTest
{

	/**
     * Tests to apply the rendering settings to a collection of images
     * in a private group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageRW() 
    	throws Exception 
    {
    	EventContext ctx = newUserAndGroup("rw----");
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createBinaryImage();
    	Image image2 = createBinaryImage();
    	Image image3 = createBinaryImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//Generate settings for the 3 images.
    	prx.setOriginalSettingsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	long pix2 = image2.getPrimaryPixels().getId().getValue();
    	long pix3 = image3.getPrimaryPixels().getId().getValue();
    	ChannelBinding cb = def.getChannelBinding(0);
    	boolean b = cb.getActive().getValue();
    	cb.setActive(omero.rtypes.rbool(!b));
    	def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    	
    	ids.clear();
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//apply the settings of image1 to image2 and 3
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    	RenderingDef def2 = 
    		factory.getPixelsService().retrieveRndSettings(pix2);
    	RenderingDef def3 = 
    		factory.getPixelsService().retrieveRndSettings(pix3);
    	cb = def2.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	cb = def3.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	
    	//Now pass the original image too.
    	ids.add(image.getId().getValue());
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    }
    
	/**
     * Tests to apply the rendering settings to a collection of images
     * in a <code>RWR---</code> group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageRWR() 
    	throws Exception 
    {
    	EventContext ctx = newUserAndGroup("rwr---");
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createBinaryImage();
    	Image image2 = createBinaryImage();
    	Image image3 = createBinaryImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//Generate settings for the 3 images.
    	prx.setOriginalSettingsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	long pix2 = image2.getPrimaryPixels().getId().getValue();
    	long pix3 = image3.getPrimaryPixels().getId().getValue();
    	ChannelBinding cb = def.getChannelBinding(0);
    	boolean b = cb.getActive().getValue();
    	cb.setActive(omero.rtypes.rbool(!b));
    	def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    	
    	ids.clear();
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//apply the settings of image1 to image2 and 3
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    	RenderingDef def2 = 
    		factory.getPixelsService().retrieveRndSettings(pix2);
    	RenderingDef def3 = 
    		factory.getPixelsService().retrieveRndSettings(pix3);
    	cb = def2.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	cb = def3.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	
    	//Now pass the original image too.
    	ids.add(image.getId().getValue());
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images
     * in a <code>RWRW--</code> group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageRWRW() 
    	throws Exception 
    {
    	EventContext ctx = newUserAndGroup("rwrw--");
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createBinaryImage();
    	Image image2 = createBinaryImage();
    	Image image3 = createBinaryImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//Generate settings for the 3 images.
    	prx.setOriginalSettingsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	long pix2 = image2.getPrimaryPixels().getId().getValue();
    	long pix3 = image3.getPrimaryPixels().getId().getValue();
    	ChannelBinding cb = def.getChannelBinding(0);
    	boolean b = cb.getActive().getValue();
    	cb.setActive(omero.rtypes.rbool(!b));
    	def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    	
    	ids.clear();
    	ids.add(image2.getId().getValue());
    	ids.add(image3.getId().getValue());
    	//apply the settings of image1 to image2 and 3
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    	RenderingDef def2 = 
    		factory.getPixelsService().retrieveRndSettings(pix2);
    	RenderingDef def3 = 
    		factory.getPixelsService().retrieveRndSettings(pix3);
    	cb = def2.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	cb = def3.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	
    	//Now pass the original image too.
    	ids.add(image.getId().getValue());
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images
     * in a <code>RWR---</code> group. In that case the target image has been
     * viewed by another user to.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherRWR() 
    	throws Exception 
    {
    	EventContext ctx = newUserAndGroup("rwr---");
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createBinaryImage();
    	Image image2 = createBinaryImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	ids.add(image2.getId().getValue());
    	//Generate settings for the 3 images.
    	prx.setOriginalSettingsInSet(Image.class.getName(), ids);
    	disconnect();
    	EventContext ctx2 = newUserInGroup(ctx);
    	prx.setOriginalSettingsInSet(Image.class.getName(),
    			Arrays.asList(image2.getId().getValue()));
    	disconnect();
    	init(ctx);
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	long pix2 = image2.getPrimaryPixels().getId().getValue();
    	ChannelBinding cb = def.getChannelBinding(0);
    	boolean b = cb.getActive().getValue();
    	cb.setActive(omero.rtypes.rbool(!b));
    	def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    	
    	ids.clear();
    	ids.add(image2.getId().getValue());
    	//Change the settings of image 2
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    	RenderingDef def2 = 
    		factory.getPixelsService().retrieveRndSettings(pix2);
    	
    	cb = def2.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images
     * in a <code>RWR---</code> group. In that case the target image has been
     * viewed by another user to.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherRWR() 
    	throws Exception 
    {
    	EventContext ctx = newUserAndGroup("rwr---");
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createBinaryImage();
    	Image image2 = createBinaryImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	ids.add(image2.getId().getValue());
    	//Generate settings for the 3 images.
    	prx.setOriginalSettingsInSet(Image.class.getName(), ids);
    	disconnect();
    	EventContext ctx2 = newUserInGroup(ctx);
    	prx.setOriginalSettingsInSet(Image.class.getName(),
    			Arrays.asList(id));
    	disconnect();
    	init(ctx);
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	long pix2 = image2.getPrimaryPixels().getId().getValue();
    	ChannelBinding cb = def.getChannelBinding(0);
    	boolean b = cb.getActive().getValue();
    	cb.setActive(omero.rtypes.rbool(!b));
    	def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    	
    	ids.clear();
    	ids.add(image2.getId().getValue());
    	//Change the settings of image 2
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    	RenderingDef def2 = 
    		factory.getPixelsService().retrieveRndSettings(pix2);
    	
    	cb = def2.getChannelBinding(0);
    	assertEquals(cb.getActive().getValue(), !b);
    	ids.add(image.getId().getValue());
    	prx.applySettingsToSet(id, Image.class.getName(), ids);
    }
    
}
