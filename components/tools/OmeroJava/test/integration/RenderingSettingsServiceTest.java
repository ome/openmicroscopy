/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IRenderingSettingsPrx;
import omero.model.ChannelBinding;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.RenderingDef;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Well;
import omero.model.WellSample;
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
     * Tests to set the default rendering settings for a set.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPixels() 
    	throws Exception 
    {
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Image.class.getName(), ids);
    	assertNotNull(v);
    	assertNotNull(v.size() == 1);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	
    	//create a dataset
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
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
    	assertNotNull(v.size() == 1);
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
    	//create a project.
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	//create a dataset

    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.link(p, d);
    	link = (ProjectDatasetLink) iUpdate.saveAndReturnObject(link);
    	
    	DatasetImageLink l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), image);
    	l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);
      
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	
    	
    	List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Project.class.getName(), ids);
    	ParametersI param;
    	String sql;
    	param = new ParametersI();
    	ids.clear();
    	ids.add(p.getId().getValue());
    	param.addIds(ids);
    	sql = "select pix from Pixels as pix " +
		"join fetch pix.image as i " +
		"join fetch pix.pixelsType " +
		"join fetch pix.channels as c " +
		"join fetch c.logicalChannel " +
		"join i.datasetLinks as dil " +
		"join dil.parent as d " +
		"left outer join d.projectLinks as pdl " +
		"left outer join pdl.parent as p " +
		"where p.id in (:ids)";
    	assertTrue(iQuery.findAllByQuery(sql, param).size() == 1);

    	assertNotNull(v);
    	assertNotNull(v.size() == 1);
    	param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
    
    /**
     * Tests to set the default rendering settings for a screen.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForScreen() 
    	throws Exception 
    {
    	Screen screen = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setChild(p);
    	link.setParent(screen);
    	iUpdate.saveAndReturnObject(link);
    	
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	Image image = well.getWellSample(0).getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(screen.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Screen.class.getName(), ids);
    	assertNotNull(v);
    	assertTrue(v.size() == 1);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
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
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Dataset
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Dataset.class.getName(), ids);
    	assertNotNull(v);
    	assertTrue(v.size() == 0);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    	Image image2 = mmFactory.createImage();
    	image2 = (Image) iUpdate.saveAndReturnObject(image2);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    	Image image2 = mmFactory.createImage();

    	image2 = (Image) iUpdate.saveAndReturnObject(image2);
    	//Create a dataset
    	
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
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
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image.getId().getValue());
    	//method already tested 
    	prx.resetDefaultsInSet(Image.class.getName(), ids);
    	 
    	//create a dataset
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	//Dataset
    	ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Dataset.class.getName(), 
    			ids);
    	assertNotNull(m);
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
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = mmFactory.createImage();

    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    	Image image2 = mmFactory.createImage();

    	image2 = (Image) iUpdate.saveAndReturnObject(image2);
    	//Create a dataset
    	//Link image and dataset
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	Project project = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	
    	ProjectDatasetLink pLink = new ProjectDatasetLinkI();
    	pLink.link(project, d);
    	iUpdate.saveAndReturnObject(pLink);

    	DatasetImageLink link = new DatasetImageLinkI();
    	link.link(new DatasetI(d.getId().getValue(), false), image2);
    	iUpdate.saveAndReturnObject(link);
    	
    	ids = new ArrayList<Long>();
    	ids.add(project.getId().getValue());
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
    	Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	
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
    	p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	results = loadWells(p.getId().getValue(), true);
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
     * Tests to apply the rendering settings to a plate acquisition.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testApplySettingsToSetForPlateAcquisition() 
    	throws Exception 
    {
    	Plate p = mmFactory.createPlate(1, 1, 1, 1, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	WellSample ws = well.getWellSample(0);
    	Image image = ws.getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(ws.getPlateAcquisition().getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(PlateAcquisition.class.getName(), ids);
    
    	//method already tested 
    	RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

    	//Create a second plate
    	p = mmFactory.createPlate(1, 1, 1, 1, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	results = loadWells(p.getId().getValue(), true);
    	well = (Well) results.get(0);
    	ws = well.getWellSample(0);
    	Image image2 = ws.getImage();
    	ids = new ArrayList<Long>();
    	ids.add(ws.getPlateAcquisition().getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, PlateAcquisition.class.getName(), ids);
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
     * Tests to apply the rendering settings to a screen.
     * Tests the <code>ApplySettingsToSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForScreen() 
    	throws Exception 
    {
    	Screen screen = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setChild(p);
    	link.setParent(screen);
    	link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);
    	screen = link.getParent();
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	
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
    	p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	
    	link = new ScreenPlateLinkI();
    	link.setChild(p);
    	link.setParent(screen);
    	link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);
    	
    	results = loadWells(p.getId().getValue(), true);
    	well = results.get(0);
    	Image image2 = well.getWellSample(0).getImage();
    	ids = new ArrayList<Long>();
    	ids.add(screen.getId().getValue());
    	Map<Boolean, List<Long>> m = 
    		prx.applySettingsToSet(id, Screen.class.getName(), ids);
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
    	Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	Image image = well.getWellSample(0).getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p.getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(Plate.class.getName(), ids);
    	assertNotNull(v);
    	assertTrue(v.size() == 1);
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertNotNull(values);
    	assertTrue(values.size() == 1);
    }
 
    /**
     * Tests to reset the default rendering settings to a plate acquisition.
     * Tests the <code>ResetDefaultInSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testResetDefaultInSetForPlateAcquisition() 
    	throws Exception 
    {
    	Plate p = mmFactory.createPlate(1, 1, 1, 1, true);
    	p = (Plate) iUpdate.saveAndReturnObject(p);
    	//load the well
    	List<Well> results = loadWells(p.getId().getValue(), true);
    	Well well = results.get(0);
    	WellSample ws = well.getWellSample(0);
    	Image image = ws.getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(ws.getPlateAcquisition().getId().getValue());
    	List<Long> v = prx.resetDefaultsInSet(PlateAcquisition.class.getName(),
    			ids);
    	assertNotNull(v);
    	assertTrue(v.size() == 1);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    			mmFactory.simpleDatasetData().asIObject());
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
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	List<Long> m = prx.resetMinMaxInSet(Dataset.class.getName(), ids);
    	assertTrue(m.size() == 0);
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
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
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
    			mmFactory.simpleDatasetData().asIObject());
    	
    	Project project = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	
    	ProjectDatasetLink pLink = new ProjectDatasetLinkI();
    	pLink.link(project, d);
    	iUpdate.saveAndReturnObject(pLink);

    	DatasetImageLink link = new DatasetImageLinkI();
    	link.link(new DatasetI(d.getId().getValue(), false), image);
    	iUpdate.saveAndReturnObject(link);
    	
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
    	Plate plate = mmFactory.createPlate(1, 1, 1, 0, true);
    	plate = (Plate) iUpdate.saveAndReturnObject(plate);
    	//load the well
    	List<Well> results = loadWells(plate.getId().getValue(), true);
    	Well well = results.get(0);
    	
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
    
    /**
     * Tests to apply the rendering settings to a plate.
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testResetMinMaxForSetForPlateAcquisition() 
    	throws Exception 
    {
    	Plate plate = mmFactory.createPlate(1, 1, 1, 1, true);
    	plate = (Plate) iUpdate.saveAndReturnObject(plate);
    	//load the well
    	List<Well> results = loadWells(plate.getId().getValue(), true);
    	Well well = results.get(0);
    	
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	WellSample ws = well.getWellSample(0);
    	Image image = ws.getImage();
    	Pixels pixels = image.getPrimaryPixels();
    	long id = pixels.getId().getValue();
    	//Image
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(ws.getPlateAcquisition().getId().getValue());
    	//method already tested 
    	 prx.resetDefaultsInSet(PlateAcquisition.class.getName(), ids);
    
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
    	
    	List<Long> m = prx.resetMinMaxInSet(PlateAcquisition.class.getName(), 
    			ids);
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
     * Tests to apply reset the min/max values for a screen.s
     * Tests the <code>ResetMinMaxForSet</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForScreen() 
    	throws Exception 
    {
    	Screen screen = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate plate = mmFactory.createPlate(1, 1, 1, 0, true);
    	plate = (Plate) iUpdate.saveAndReturnObject(plate);
    	
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setChild(plate);
    	link.setParent(screen);
    	link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);
    	screen = link.getParent();
    	//load the well
    	List<Well> results = loadWells(plate.getId().getValue(), true);
    	Well well = results.get(0);
    	
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
