/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.model.enums.Family;
import omero.RLong;
import omero.api.IPixelsPrx;
import omero.api.RenderingEnginePrx;
import omero.model.ChannelBinding;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.model.RenderingModel;
import omero.sys.ParametersI;

/** 
 * Collection of tests for the <code>RenderingEngine</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class RenderingEngineTest
	extends AbstractTest
{
	
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	private Image createImage()
		throws Exception
	{
		IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = 
    		svc.getAllEnumerations(PixelsType.class.getName());
    	List<Integer> channels = new ArrayList<Integer>();
    	for (int i = 0; i < DEFAULT_CHANNELS_NUMBER; i++) {
			channels.add(i);
		}
    	RLong id = svc.createImage(SIZE_X, SIXE_Y, SIXE_Z, SIXE_T, channels, 
    			(PixelsType) types.get(1),
    			"test", "");
    	//Retrieve the image.
    	ParametersI param = new ParametersI();
    	param.addId(id.getValue());
    	Image img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	return (Image) iUpdate.saveAndReturnObject(img);
	}
	
	/**
	 * Tests the creation of the rendering engine for a given pixels set w/o
	 * looking up for rendering settings.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testCreateRenderingEngineNoSettings()
		throws Exception
	{
		Pixels pixels = createPixels();
		Image i = pixels.getImage();
        i = (Image) iUpdate.saveAndReturnObject(i);
        pixels = i.getPrimaryPixels();
		RenderingEnginePrx svc = factory.createRenderingEngine();
		try {
			svc.lookupPixels(pixels.getId().getValue());
			svc.load();
			fail("We should not have been able to load it.");
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Tests the creation of the rendering engine for a given pixels set when
	 * looking up for rendering settings.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testCreateRenderingEngine()
		throws Exception
	{
		Image image = createImage();
		RenderingEnginePrx svc = factory.createRenderingEngine();
		Pixels pixels = image.getPrimaryPixels();
		long id = pixels.getId().getValue();
		svc.lookupPixels(id);
		svc.resetDefaults();
		svc.lookupRenderingDef(id);
		svc.load();
		svc.close();
	}
	
	/**
	 * Tests the retrieval of the rendering settings data using the rendering 
	 * engine.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testRenderingEngineGetters()
		throws Exception
	{
		Image image = createImage();
		RenderingEnginePrx re = factory.createRenderingEngine();
		Pixels pixels = image.getPrimaryPixels();
		long id = pixels.getId().getValue();
		re.lookupPixels(id);
		re.resetDefaults();
		re.lookupRenderingDef(id);
		re.load();
		//retrieve the rendering def
		RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
		assertTrue(def.getDefaultZ().getValue() == re.getDefaultZ());
		assertTrue(def.getDefaultT().getValue() == re.getDefaultT());
		assertTrue(def.getModel().getValue().getValue().equals( 
				re.getModel().getValue().getValue()));
		QuantumDef q1 = def.getQuantization();
		QuantumDef q2 = re.getQuantumDef();
		assertNotNull(q1);
		assertNotNull(q2);
		assertTrue(q1.getBitResolution().getValue() == 
			q2.getBitResolution().getValue());
		assertTrue(q1.getCdStart().getValue() == 
			q2.getCdStart().getValue());
		assertTrue(q1.getCdEnd().getValue() == 
			q2.getCdEnd().getValue());
		List<ChannelBinding> channels1 = def.copyWaveRendering();
		assertNotNull(channels1);
		Iterator<ChannelBinding> i = channels1.iterator();
		ChannelBinding c1;
		int index = 0;
		int[] rgba;
		while (i.hasNext()) {
			c1 = i.next();
			rgba = re.getRGBA(index);
			assertTrue(c1.getRed().getValue() == rgba[0]);
			assertTrue(c1.getGreen().getValue() == rgba[1]);
			assertTrue(c1.getBlue().getValue() == rgba[2]);
			assertTrue(c1.getAlpha().getValue() == rgba[3]);
			assertTrue(c1.getCoefficient().getValue() 
					== re.getChannelCurveCoefficient(index));
			assertTrue(c1.getFamily().getValue().getValue().equals(
					re.getChannelFamily(index).getValue().getValue()));
			assertTrue(c1.getInputStart().getValue() == 
				re.getChannelWindowStart(index));
			assertTrue(c1.getInputEnd().getValue() == 
				re.getChannelWindowEnd(index));
			Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
			Boolean b2 = Boolean.valueOf(re.isActive(index));
			assertTrue(b1.equals(b2));
			b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
			b2 = Boolean.valueOf(re.getChannelNoiseReduction(index));
			assertTrue(b1.equals(b2));
		}
		re.close();
	}
	
	/**
	 * Tests to modify the rendering settings using the rendering engine
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testRenderingEngineSetters()
		throws Exception
	{
		Image image = createImage();
		RenderingEnginePrx re = factory.createRenderingEngine();
		Pixels pixels = image.getPrimaryPixels();
		long id = pixels.getId().getValue();
		re.lookupPixels(id);
		re.resetDefaults();
		re.lookupRenderingDef(id);
		re.load();
		RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
		int v = def.getDefaultT().getValue()+1;
		re.setDefaultT(v);
		assertTrue(re.getDefaultT() == v);
		v = def.getDefaultZ().getValue()+1;
		re.setDefaultZ(v);
		assertTrue(re.getDefaultZ() == v);
		
		//tested in PixelsService
		IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> families = svc.getAllEnumerations(Family.class.getName());
    	List<IObject> models = svc.getAllEnumerations(
    			RenderingModel.class.getName());
    	RenderingModel model = def.getModel();
    	Iterator<IObject> i;
    	RenderingModel m;
    	i = models.iterator();
    	while (i.hasNext()) {
			m = (RenderingModel) i.next();
			if (m.getId().getValue() != model.getId().getValue()) {
				model = m;	
				break;
			}
		}
    	re.setModel(model);
    	assertTrue(re.getModel().getId().getValue() == model.getId().getValue());
		re.close();
	}
	
	
}
