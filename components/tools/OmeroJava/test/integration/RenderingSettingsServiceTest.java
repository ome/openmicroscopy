/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IRenderingSettingsPrx;
import omero.model.ChannelBinding;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.RenderingDef;
import omero.model.Well;
import omero.sys.ParametersI;

/** 
 * Collections of tests for the <code>RenderingSettingsService</code> service.
 * 
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
public class RenderingSettingsServiceTest 
	extends AbstractTest
{
	
	/**
	 * Loads the wells.
	 * 
	 * @param plateID The identifier of the plate.
	 * @return See above.
	 */
	private List<IObject> loadWells(long plateID)
		throws Exception 
	{
		StringBuilder sb = new StringBuilder();
		ParametersI param = new ParametersI();
		param.addLong("plateID", plateID);
		sb.append("select well from Well as well ");
		sb.append("left outer join fetch well.plate as pt ");
		sb.append("left outer join fetch well.wellSamples as ws ");
		sb.append("left outer join fetch ws.image as img ");
		sb.append("left outer join fetch img.pixels as pix ");
        sb.append("left outer join fetch pix.pixelsType as pt ");
        sb.append("where well.plate.id = :plateID");
        return iQuery.findAllByQuery(sb.toString(), param);
	}
	
    /**
     * Tests to set the default rendering settings for a set.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPixels() 
    	throws Exception 
    {
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Pixels first
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(pixels.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Pixels.class.getName(), ids);
    	assertNotNull(v);
    	//check if we have settings now.
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    	
    	//Image
    	ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	v = prx.resetDefaultsInSet(Image.class.getName(), ids);
    	assertNotNull(v);
    	values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
    
    /**
     * Tests to set the default rendering settings for a set.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForImage() 
    	throws Exception 
    {
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Image.class.getName(), ids);
    	assertNotNull(v);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
    
    /**
     * Tests to set the default rendering settings for a set.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForDataset() 
    	throws Exception 
    {
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	
    	//create a dataset
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.setChild(image);
    	l.setParent(d);
    	iUpdate.saveAndReturnObject(l);
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Dataset
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Dataset.class.getName(), ids);
    	assertNotNull(v);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
    
    /**
     * Tests to set the default rendering settings for a project.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForProject() 
    	throws Exception 
    {
    	/*
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	
    	//create a dataset

    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());

    	//create a project.
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	ProjectDatasetLink link = new ProjectDatasetLinkI();
    	link.setChild(d);
    	link.setParent(p);
    	iUpdate.saveAndReturnObject(link);
    	
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.setChild(image);
    	l.setParent(d);
    	iUpdate.saveAndReturnObject(l);
    	    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();

    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Project.class.getName(), ids);
    	assertNotNull(v);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    	*/
    }
    
    /**
     * Tests to set the default rendering settings for a empty dataset.
     * Tests the <code>resetDefaultsInSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForEmptyDataset() 
    	throws Exception 
    {
    	//create a dataset
    	/*
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Dataset
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Dataset.class.getName(), ids);
    	assertNotNull(v);
    	assertTrue(v.size() == 0);
    	*/
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImage() 
    	throws Exception 
    {
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	//Create a second image.
    	Image image2 = createImage();
    	ids = new ArrayList<Long>();
    	ids.add(image2.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Image.class.getName(), ids);
    	assertNotNull(m);
    	List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
    	List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
    	assertNotNull(success);
    	assertNotNull(failure);
    	assertTrue(success.size() == 1);
    	assertTrue(failure.size() == 0);
    	id = success.get(0); //image id.
    	assertTrue(id == image2.getId().getValue());
    	RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
    			image2.getPrimaryPixels().getId().getValue());
    	compareRenderingDef(def, def2);
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images within
     * a dataset.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForDataset() 
    	throws Exception 
    {
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	//Create a second image.
    	Image image2 = createImage();
    	//Create a dataset
    	
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.setChild(image2);
    	l.setParent(d);
    	iUpdate.saveAndReturnObject(l);
    	
    	ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Dataset.class.getName(), ids);
    	assertNotNull(m);
    	List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
    	List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
    	assertNotNull(success);
    	assertNotNull(failure);
    	assertTrue(success.size() == 1);
    	assertTrue(failure.size() == 0);
    	id = success.get(0); //image id.
    	assertTrue(id == image2.getId().getValue());
    	RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
    			image2.getPrimaryPixels().getId().getValue());
    	compareRenderingDef(def, def2);
    }
    
    /**
     * Tests to apply the rendering settings to an empty dataset.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForEmptyDataset() 
    	throws Exception 
    {
    	
    }
    /**
     * Tests to apply the rendering settings to a collection of images contained
     * in a project.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForProject() 
    	throws Exception 
    {
    	/*
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	//Create a second image.
    	Image image2 = createImage();
    	//Create a dataset
    	
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.setChild(image2);
    	l.setParent(d);
    	iUpdate.saveAndReturnObject(l);
    	
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	ProjectDatasetLink link = new ProjectDatasetLinkI();
    	link.setChild(d);
    	link.setParent(p);
    	iUpdate.saveAndReturnObject(link);
    	
    	ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Project.class.getName(), ids);
    	assertNotNull(m);
    	List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
    	List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
    	assertNotNull(success);
    	assertNotNull(failure);
    	assertTrue(success.size() == 1);
    	assertTrue(failure.size() == 0);
    	id = success.get(0); //image id.
    	assertTrue(id == image2.getId().getValue());
    	RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
    			image2.getPrimaryPixels().getId().getValue());
    	compareRenderingDef(def, def2);
    	*/
    }
    
    /**
     * Tests to apply the rendering settings to a plate.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForPlate() 
    	throws Exception 
    {
    	Plate p = createPlate(1, 1, 1, false, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<IObject> results = loadWells(p.getId().getValue());
    	Well well = (Well) results.get(0);
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = well.getWellSample(0).getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Plate.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	
    	
    	
    	//Create a second plate
    	p = createPlate(1, 1, 1, false, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	results = loadWells(p.getId().getValue());
    	well = (Well) results.get(0);
    	Image image2 = well.getWellSample(0).getImage();
    	ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Plate.class.getName(), ids);
    	assertNotNull(m);
    	List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
    	List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
    	assertNotNull(success);
    	assertNotNull(failure);
    	assertTrue(success.size() == 1);
    	assertTrue(failure.size() == 0);
    	id = success.get(0); //image id.
    	assertTrue(id == image2.getId().getValue());
    	RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
    			image2.getPrimaryPixels().getId().getValue());
    	compareRenderingDef(def, def2);
    }
    
    /**
     * Tests to reset the default rendering settings to a plate.
     * Tests the <code>ResetDefaultInSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPlate() 
    	throws Exception 
    {
    	Plate p = createPlate(1, 1, 1, false, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<IObject> results = loadWells(p.getId().getValue());
    	Well well = (Well) results.get(0);
    	Image image = well.getWellSample(0).getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Plate.class.getName(), ids);
    	assertNotNull(v);
    	assertTrue(v.size() > 0);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
 
    /**
     * Tests to apply the rendering settings to a collection of images.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForImage() 
    	throws Exception 
    {
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	
    	//Modified the settings.
    	ChannelBinding channel;
    	List<Point> list = new ArrayList<Point>();
    	
    	Point p;
    	List<IObject> toUpdate = new ArrayList<IObject>();
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(0);
			p = new Point();
			p.setLocation(channel.getInputStart().getValue(), 
					channel.getInputEnd().getValue());
			list.add(p);
			channel.setInputStart(omero.rtypes.rdouble(1));
			channel.setInputEnd(omero.rtypes.rdouble(2));
			toUpdate.add(channel);
		}
    	iUpdate.saveAndReturnArray(toUpdate);
    	List<Long> m = prx.resetMinMaxInSet(Image.class.getName(), ids);
    	assertNotNull(m);
    	assertTrue(m.size() == 1);
    	def = factory.getPixelsService().retrieveRndSettings(id);
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(i);
			p = list.get(i);
			assertTrue(channel.getInputStart().getValue() == p.getX());
			assertTrue(channel.getInputEnd().getValue() == p.getY());
		}
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForDataset() 
    	throws Exception 
    {
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	
    	//Modified the settings.
    	ChannelBinding channel;
    	List<Point> list = new ArrayList<Point>();
    	
    	Point p;
    	List<IObject> toUpdate = new ArrayList<IObject>();
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(0);
			p = new Point();
			p.setLocation(channel.getInputStart().getValue(), 
					channel.getInputEnd().getValue());
			list.add(p);
			channel.setInputStart(omero.rtypes.rdouble(1));
			channel.setInputEnd(omero.rtypes.rdouble(2));
			toUpdate.add(channel);
		}
    	iUpdate.saveAndReturnArray(toUpdate);
    	//Link image and dataset
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image);
    	link.setParent(d);
    	iUpdate.saveAndReturnObject(link);
    	ids.clear();
    	ids.add(d.getId().getValue());
    	List<Long> m = prx.resetMinMaxInSet(Dataset.class.getName(), ids);
    	assertNotNull(m);
    	assertTrue(m.size() == 1);
    	def = factory.getPixelsService().retrieveRndSettings(id);
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(i);
			p = list.get(i);
			assertTrue(channel.getInputStart().getValue() == p.getX());
			assertTrue(channel.getInputEnd().getValue() == p.getY());
		}
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForEmptyDataset() 
    	throws Exception 
    {
    	/*
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	List<Long> m = prx.resetMinMaxInSet(Dataset.class.getName(), ids);
    	*/
    }
    
    /**
     * Tests to apply the rendering settings to a collection of images.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForProject() 
    	throws Exception 
    {
    	/*
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Image.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	
    	//Modified the settings.
    	ChannelBinding channel;
    	List<Point> list = new ArrayList<Point>();
    	
    	Point p;
    	List<IObject> toUpdate = new ArrayList<IObject>();
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(0);
			p = new Point();
			p.setLocation(channel.getInputStart().getValue(), 
					channel.getInputEnd().getValue());
			list.add(p);
			channel.setInputStart(omero.rtypes.rdouble(1));
			channel.setInputEnd(omero.rtypes.rdouble(2));
			toUpdate.add(channel);
		}
    	iUpdate.saveAndReturnArray(toUpdate);
    	//Link image and dataset
    	List<IObject> links = new ArrayList<IObject>();
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image);
    	link.setParent(d);
    	links.add(link);
    	
    	Project project = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	ProjectDatasetLink pLink = new ProjectDatasetLinkI();
    	pLink.setChild(d);
    	pLink.setParent(project);
    	links.add(pLink);
    	
    	iUpdate.saveAndReturnArray(links);
    	ids.clear();
    	ids.add(project.getId().getValue());
    	List<Long> m = prx.resetMinMaxInSet(Project.class.getName(), ids);
    	assertNotNull(m);
    	assertTrue(m.size() == 1);
    	def = factory.getPixelsService().retrieveRndSettings(id);
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(i);
			p = list.get(i);
			assertTrue(channel.getInputStart().getValue() == p.getX());
			assertTrue(channel.getInputEnd().getValue() == p.getY());
		}
		*/
    }

    /**
     * Tests to apply the rendering settings to a plate.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForPlate() 
    	throws Exception 
    {
    	Plate plate = createPlate(1, 1, 1, false, true);
    	plate = (Plate) iUpdate.saveAndReturnObject(plate);
    	//load the well
    	List<IObject> results = loadWells(plate.getId().getValue());
    	Well well = (Well) results.get(0);
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = well.getWellSample(0).getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(plate.getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(Plate.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
    	//Modified the settings.
    	ChannelBinding channel;
    	List<Point> list = new ArrayList<Point>();
    	
    	Point p;
    	List<IObject> toUpdate = new ArrayList<IObject>();
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(0);
			p = new Point();
			p.setLocation(channel.getInputStart().getValue(), 
					channel.getInputEnd().getValue());
			list.add(p);
			channel.setInputStart(omero.rtypes.rdouble(1));
			channel.setInputEnd(omero.rtypes.rdouble(2));
			toUpdate.add(channel);
		}
    	iUpdate.saveAndReturnArray(toUpdate);
    	
    	List<Long> m = prx.resetMinMaxInSet(Plate.class.getName(), ids);
    	assertNotNull(m);
    	assertTrue(m.size() == 1);
    	def = factory.getPixelsService().retrieveRndSettings(id);
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = def.getChannelBinding(i);
			p = list.get(i);
			assertTrue(channel.getInputStart().getValue() == p.getX());
			assertTrue(channel.getInputEnd().getValue() == p.getY());
		}
    }
    
}
