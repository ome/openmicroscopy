/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IRenderingSettingsPrx;
import omero.model.Dataset;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.RenderingDef;
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
    	DatasetImageLinkI l = new DatasetImageLinkI();
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
    	ProjectDatasetLinkI link = new ProjectDatasetLinkI();
    	link.setChild(d);
    	link.setParent(p);
    	iUpdate.saveAndReturnObject(link);
    	
    	DatasetImageLinkI l = new DatasetImageLinkI();
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
     * Tests to set the default rendering settings for a project.
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
    	DatasetImageLinkI l = new DatasetImageLinkI();
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
    	DatasetImageLinkI l = new DatasetImageLinkI();
    	l.setChild(image2);
    	l.setParent(d);
    	iUpdate.saveAndReturnObject(l);
    	
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	ProjectDatasetLinkI link = new ProjectDatasetLinkI();
    	link.setChild(d);
    	link.setParent(p);
    	iUpdate.saveAndReturnObject(link);
    	
    	ids = new ArrayList<Long>();
    	ids.add(d.getId().getValue());
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
    
}
