/*
/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.RType;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.Instrument;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.PermissionsI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAnnotationLinkI;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.StageLabel;
import static omero.rtypes.rlong;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.PlateAcquisitionData;
import pojos.ProjectData;
import pojos.ScreenData;

/**
 * Collections of tests for the <code>IContainer</code> service.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
@Test(groups = { "client", "integration", "blitz" })
public class PojosServiceTest 
	extends AbstractTest
{

    /** Reference to class used to create data object. */
    CreatePojosFixture2 fixture;

    /** Helper reference to the <code>IContainer</code> service. */
    private IContainerPrx iContainer;

    /**
     * Makes sure that the pixels set is loaded.
     * 
     * @param pixels The pixels to handle.
     * @throws Exception Thrown if an error occurred.
     */
    private void checkPixels(PixelsData pixels)
    	throws Exception 
    {
    	assertNotNull(pixels);
		assertNotNull(pixels.getPixelSizeX());
        assertNotNull(pixels.getPixelSizeY());
        assertNotNull(pixels.getPixelSizeZ());
        assertNotNull(pixels.getPixelType());
        assertNotNull(pixels.getImage());
        assertNotNull(pixels.getOwner());
        assertNotNull(pixels.getSizeC());
        assertNotNull(pixels.getSizeT());
        assertNotNull(pixels.getSizeZ());
        assertNotNull(pixels.getSizeY());
        assertNotNull(pixels.getSizeX());
    }
    
    /**
     * Initializes the various services.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception 
    {   
    	super.setUp();
    	iContainer = factory.getContainerService();
        fixture = CreatePojosFixture2.withNewUser(root);
        fixture.createAllPojos();
    }

    /**
     * Test to load container hierarchy with project specified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyProjectSpecified() 
    	throws Exception 
    {
    	//first create a project
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	//link the 2
    	ProjectDatasetLink link = new ProjectDatasetLinkI();
    	link.setParent(p);
    	link.setChild(d);
    	iUpdate.saveAndReturnObject(link);
    	//
    	Parameters param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        ProjectData project;
        Set<DatasetData> datasets;
        Iterator<DatasetData> j;
        DatasetData dataset;
        while (i.hasNext()) {
			project = new  ProjectData((Project) i.next());
			assertTrue(project.getId() == p.getId().getValue());
			datasets = project.getDatasets();
			assertTrue(datasets.size() == 1);
			j = datasets.iterator();
			while (j.hasNext()) {
				dataset = j.next();
				assertTrue(dataset.getId() == d.getId().getValue());
			}
		}
    }
    
    /**
     * Test to load container hierarchy with screen specified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyScreenSpecified() 
    	throws Exception 
    {
    	//first create a project
    	Screen p = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate d = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.simplePlateData().asIObject());
    	
    	//link the 2
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setParent(p);
    	link.setChild(d);
    	iUpdate.saveAndReturnObject(link);
    	//
    	Parameters param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Screen.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        ScreenData screen;
        Set<PlateData> plates;
        Iterator<PlateData> j;
        PlateData plate;
        while (i.hasNext()) {
			screen = new  ScreenData((Screen) i.next());
			assertTrue(screen.getId() == p.getId().getValue());
			plates = screen.getPlates();
			assertTrue(plates.size() == 1);
			j = plates.iterator();
			while (j.hasNext()) {
				plate = j.next();
				assertTrue(plate.getId() == d.getId().getValue());
			}
		}
    }
    
    /**
     * Test to load container hierarchy with screen specified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyScreenWithPlateAndPlateAcquisitionSpecified() 
    	throws Exception 
    {
    	//first create a project
    	Screen p = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate d = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.simplePlateData().asIObject());
    	PlateAcquisition pa = (PlateAcquisition) 
    		mmFactory.simplePlateAcquisitionData().asIObject();
    	pa.setPlate(d);
    	pa = (PlateAcquisition) iUpdate.saveAndReturnObject(pa);
    	
    	//link the 2
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setParent(p);
    	link.setChild(d);
    	iUpdate.saveAndReturnObject(link);
    	
    	Parameters param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Screen.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        ScreenData screen;
        Set<PlateData> plates;
        Iterator<PlateData> j;
        PlateData plate;
        while (i.hasNext()) {
			screen = new  ScreenData((Screen) i.next());
			assertTrue(screen.getId() == p.getId().getValue());
			plates = screen.getPlates();
			assertTrue(plates.size() == 1);
			j = plates.iterator();
			while (j.hasNext()) {
				plate = j.next();
				assertTrue(plate.getId() == d.getId().getValue());
			}
		}
    }
    
    /**
     * Test to load container hierarchy with no project specified, no orphan
     * loaded
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyNoProjectSpecified() 
    	throws Exception 
    {
    	//first create a project
    	long self = factory.getAdminService().getEventContext().userId;
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Project p2 = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), new ArrayList(), param);
        assertTrue(results.size() > 0);
        Iterator i = results.iterator();
        int count = 0;
        IObject object;
        while (i.hasNext()) {
        	object = (IObject) i.next();
			if (!(object instanceof Project)) {
				count++;
			}
		}
        assertTrue(count == 0);
    }

    /**
     * Test to load container hierarchy with no screen specified, no orphan
     * loaded
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyNoScreenSpecified() 
    	throws Exception 
    {
    	//first create a screen
    	long self = factory.getAdminService().getEventContext().userId;
    	Screen p = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Screen p2 = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
        List results = iContainer.loadContainerHierarchy(
        		Screen.class.getName(), new ArrayList(), param);
        assertTrue(results.size() > 0);
        Iterator i = results.iterator();
        int count = 0;
        IObject object;
        while (i.hasNext()) {
        	object = (IObject) i.next();
			if (!(object instanceof Screen)) {
				count++;
			}
		}
        assertTrue(count == 0);
    }
    
    /**
     * Test to load container hierarchy with project specified, with orphan
     * loaded
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyNoProjectSpecifiedWithOrphan() 
    	throws Exception 
    {
    	//first create a project
    	long self = factory.getAdminService().getEventContext().userId;
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Project p2 = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan();
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), new ArrayList(), param);
        assertTrue(results.size() > 0);
        Iterator i = results.iterator();
        IObject object; 
        int value = 0;
        while (i.hasNext()) {
        	object = (IObject) i.next();
			if (object instanceof Dataset) {
				if (object.getId().getValue() == d.getId().getValue()) {
					value++;
				}
			}
		}
        assertTrue(value == 1);
    }
    
    /**
     * Test to load container hierarchy with project specified, with orphan
     * loaded
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyNoScreenSpecifiedWithOrphan() 
    	throws Exception 
    {
    	//first create a project
    	long self = factory.getAdminService().getEventContext().userId;
    	Screen p = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate d = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.simplePlateData().asIObject());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan();
        List results = iContainer.loadContainerHierarchy(
        		Screen.class.getName(), new ArrayList(), param);
        assertTrue(results.size() > 0);
        Iterator i = results.iterator();
        IObject object; 
        int value = 0;
        while (i.hasNext()) {
        	object = (IObject) i.next();
			if (object instanceof Plate) {
				if (object.getId().getValue() == d.getId().getValue()) {
					value++;
				}
			}
		}
        assertTrue(value == 1);
    }
    
    /**
     * Test to load container hierarchy with dataset specified
     * and loads the images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyDatasetSpecifiedAndLeaves() 
    	throws Exception 
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	//link the 2
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(img);
    	iUpdate.saveAndReturnObject(link);
    	ParametersI param = new ParametersI();
    	param.leaves();
        List<Long> ids = new ArrayList<Long>();
        ids.add(d.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Dataset.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        DatasetData dataset;
        Set<ImageData> images;
        Iterator<ImageData> j;
        ImageData image;
        while (i.hasNext()) {
			dataset = new  DatasetData((Dataset) i.next());
			if (dataset.getId() == d.getId().getValue()) {
				images = dataset.getImages();
				assertTrue(images.size() == 1);
				j = images.iterator();
				while (j.hasNext()) {
					image = j.next();
					assertNotNull(
							image.asImage().getDetails().getUpdateEvent());
					assertTrue(
					image.asImage().getDetails().getUpdateEvent().isLoaded());
					assertTrue(image.getId() == img.getId().getValue());
				}
			} 
		}
    }
    
    /**
     * Test to load container hierarchy with dataset specified
     * and loads the images. We then make sure that the default pixels
     * are loaded.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = {"ticket:401", "ticket:221"})
    public void testLoadContainerHierarchyDatasetSpecifiedAndLeavesWithDefaultPixels() 
    	throws Exception 
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image 
    	img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
    	//link the 2
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(img);
    	iUpdate.saveAndReturnObject(link);
    	ParametersI param = new ParametersI();
    	param.leaves();
        List<Long> ids = new ArrayList<Long>();
        ids.add(d.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Dataset.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        DatasetData dataset;
        Set<ImageData> images;
        Iterator<ImageData> j;
        ImageData image;
        PixelsData pixelsData;
        while (i.hasNext()) {
			dataset = new  DatasetData((Dataset) i.next());
			if (dataset.getId() == d.getId().getValue()) {
				images = dataset.getImages();
				assertTrue(images.size() == 1);
				j = images.iterator();
				while (j.hasNext()) {
					image = j.next();
					checkPixels(image.getDefaultPixels());
				}
			} 
		}
    }
    
    /**
     * Test to the collection count method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCollectionCountForDataset() 
    	throws Exception 
    {
    	Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	//link the d and i
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d1);
    	link.setChild(i);
    	iUpdate.saveAndReturnObject(link);
    	Parameters p = new ParametersI();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(d1.getId().getValue());
    	ids.add(d2.getId().getValue());
    	Map m = iContainer.getCollectionCount(Dataset.class.getName(), 
    			DatasetData.IMAGE_LINKS, ids, p);
    	Long v = (Long) m.get(d1.getId().getValue());
    	assertTrue(v.longValue() == 1);
    	v = (Long) m.get(d2.getId().getValue());
    	assertTrue(v.longValue() == 0);
    }
    
    /**
     * Tests the retrieval of images filtering by owners.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByOwner() 
    	throws Exception
    {
    	Image i1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(i1);
    	iUpdate.saveAndReturnObject(link);
    	ParametersI param = new ParametersI();
    	List<Long> ids = new ArrayList<Long>(1);
    	ids.add(d.getId().getValue());
    	List<Image> images = iContainer.getImages(Dataset.class.getName(), ids, 
    			param);
    	assertTrue(images.size() > 0);
    	Iterator<Image> i = images.iterator();
    	Image img;
    	int count = 0;
    	while (i.hasNext()) {
			img = i.next();
			if (img.getId().getValue() == i1.getId().getValue())
				count++;
				
		}
    	assertTrue(count == 1);
    	param = new ParametersI();
    	param.exp(rlong(fixture.e.getId().getValue()));
    	images = iContainer.getImages(Dataset.class.getName(), ids, 
    			param);
    	assertTrue(images.size() == 0);
    }

    /**
     * Tests the retrieval of images filtering by owners. Those images
     * will have a pixels set.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByOwnerWithPixels() 
    	throws Exception
    {
    	Image i1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.createImage());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(i1);
    	iUpdate.saveAndReturnObject(link);
    	ParametersI param = new ParametersI();
    	List<Long> ids = new ArrayList<Long>(1);
    	ids.add(d.getId().getValue());
    	List<Image> images = iContainer.getImages(Dataset.class.getName(), ids, 
    			param);
    	assertTrue(images.size() > 0);
    	Iterator<Image> i = images.iterator();
    	Image img;
    	int count = 0;
    	PixelsData pixelsData;
    	while (i.hasNext()) {
			img = i.next();
			if (img.getId().getValue() == i1.getId().getValue()) {
				pixelsData = new PixelsData(img.getPixels(0));
				checkPixels(pixelsData);
				count++;
			}	
		}
    	assertTrue(count == 1);
    	param = new ParametersI();
    	param.exp(rlong(fixture.e.getId().getValue()));
    	images = iContainer.getImages(Dataset.class.getName(), ids, 
    			param);
    	assertTrue(images.size() == 0);
    }

    /**
     * Links twice a dataset and an image. Only one link should be inserted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testDuplicateDatasetImageLink() 
    	throws Exception
    {
    	Image i1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(i1);
    	iUpdate.saveAndReturnObject(link);
    	link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(i1);
    	try {
    		iUpdate.saveAndReturnObject(link);
    		fail("Should not be able to insert twice.");
		} catch (Exception e) {
		}
    	String sql = "select link from DatasetImageLink as link where " +
		"link.parent.id = :parentID and link.child.id = :childID";

		ParametersI param = new ParametersI();
		param.map = new HashMap<String, RType>();
		param.map.put("parentID", d.getId());
		param.map.put("childID", i1.getId());
    	List l = iQuery.findAllByQuery(sql, param);
    	assertTrue(l.size() == 1);
    }
    
    /**
     * Tests the retrieval of images filtering by groups.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByGroup() 
    	throws Exception
    {
    	//Create 2 groups and add a user 
    	String uuid1 = UUID.randomUUID().toString();
		ExperimenterGroup g1 = new ExperimenterGroupI();
		g1.setName(omero.rtypes.rstring(uuid1));
		g1.getDetails().setPermissions(new PermissionsI("rw----"));
		
		String uuid2 = UUID.randomUUID().toString();
		ExperimenterGroup g2 = new ExperimenterGroupI();
		g2.setName(omero.rtypes.rstring(uuid2));
		g2.getDetails().setPermissions(new PermissionsI("rw----"));
			
		IAdminPrx svc = root.getSession().getAdminService();
		IQueryPrx query = root.getSession().getQueryService();
		long id1 = svc.createGroup(g1);
		long id2 = svc.createGroup(g2);
		
		ParametersI p = new ParametersI();
		p.addId(id1);
		
		ExperimenterGroup eg1 = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		p = new ParametersI();
		p.addId(id2);
		
		ExperimenterGroup eg2 = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		Experimenter e = new ExperimenterI();
		e.setOmeName(omero.rtypes.rstring(uuid1));
		e.setFirstName(omero.rtypes.rstring("user"));
		e.setLastName(omero.rtypes.rstring("user"));
		
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		//method tested elsewhere
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg1);
		groups.add(eg2);
		groups.add(userGroup);
		
		svc.createExperimenter(e, eg1, groups);
		
		omero.client client = new omero.client();
        ServiceFactoryPrx f = client.createSession(uuid1, uuid1);
		//add an image.
        IUpdatePrx update = f.getUpdateService();
        Dataset d = (Dataset) update.saveAndReturnObject(
        		mmFactory.simpleDatasetData().asIObject());
        long d1 = d.getId().getValue();
        
        Image image1 = (Image) update.saveAndReturnObject(
        		mmFactory.simpleImage(0));
    	//link the 2
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(image1);
    	update.saveAndReturnObject(link);
        
        
        
       //Change the security context
        client.getSession().setSecurityContext(
        		new ExperimenterGroupI(id2, false));
		//add an image.
        d = (Dataset) update.saveAndReturnObject(
        		mmFactory.simpleDatasetData().asIObject());
        long d2 = d.getId().getValue();
        Image image2 = (Image) 
        	f.getUpdateService().saveAndReturnObject(
        			mmFactory.simpleImage(0));
        link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(image2);
    	f.getUpdateService().saveAndReturnObject(link);
    	List<Long> ids = new ArrayList<Long>();
		ids.add(d1);
		ids.add(d2);
		List<Image> images = f.getContainerService().getImages(
				Dataset.class.getName(), ids, p);
		assertNotNull(images);
		assertTrue(images.size() == 1);
		Iterator<Image> i = images.iterator();
		
		//Should only retrieve images from group2 
		while (i.hasNext()) {
			assertTrue(i.next().getId().getValue() == image2.getId().getValue());
		}
		
		client.getSession().setSecurityContext(
        		new ExperimenterGroupI(id1, false));
		images = f.getContainerService().getImages(
				Dataset.class.getName(), ids, p);
		assertNotNull(images);
		assertTrue(images.size() == 1);
		i = images.iterator();
		
		//Should only retrieve images from group2 
		while (i.hasNext()) {
			assertTrue(i.next().getId().getValue() == image1.getId().getValue());
		}
    }
    
    /**
     * Tests the finding of projects filtering by owners.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testFindContainerHierarchiesProjectAsRootFilterByOwner() 
    	throws Exception
    {
    	long id =  fixture.e.getId().getValue();
    	ParametersI param = new ParametersI();
    	param.leaves();
    	param.exp(omero.rtypes.rlong(id));
    	
    	
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	//link dataset and image
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(i);
    	iUpdate.saveAndReturnObject(link);
    	//link project and dataset
    	ProjectDatasetLink l = new ProjectDatasetLinkI();
    	l.setParent(p);
    	l.setChild(d);
    	iUpdate.saveAndReturnObject(l);
    	
    	List<Long> ids = new ArrayList<Long>(1);
    	ids.add(i.getId().getValue());
    	//Should have one project.
    	List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, param);
    	assertTrue(results.size() == 1);
    	Image pp = (Image) results.get(0);
    	assertTrue(pp.getId().getValue() == i.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a projects filtering by owners.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testLoadContainerHierarchyFilterByOwner() 
    	throws Exception
    {
    	long id =  fixture.e.getId().getValue();
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(id));
    	
    	List<Long> ids = fixture.getProjectIds();
    	List results = iContainer.loadContainerHierarchy(
    			Project.class.getName(), 
        		new ArrayList<Long>(),  param);
       Iterator i = results.iterator();
       IObject object;
       int value = 0;
       while (i.hasNext()) {
    	   object = (IObject) i.next();
    	   if (p.getId().getValue() == object.getId().getValue())
    		   value++;
       }
       assertTrue(value == 0);
    }
    
    /**
     * Tests the retrieval of a projects filtering by groups.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testLoadContainerHierarchyFilterByGroup() 
    	throws Exception
    {
    	long id =  fixture.g.getId().getValue();
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	ParametersI param = new ParametersI();
    	param.grp(omero.rtypes.rlong(id));
    	
    	List<Long> ids = fixture.getProjectIds();
    	List results = iContainer.loadContainerHierarchy(
    			Project.class.getName(), 
        		new ArrayList<Long>(),  param);
       Iterator i = results.iterator();
       IObject object;
       int value = 0;
       while (i.hasNext()) {
    	   object = (IObject) i.next();
    	   if (p.getId().getValue() == object.getId().getValue())
    		   value++;
       }
       assertTrue(value == 0);
    }

    /**
     * Tests the retrieval of images created during a given period.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByOptions() 
    	throws Exception
    {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
				gc.get(Calendar.MONTH), 
				gc.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    	long startTime = gc.getTime().getTime();
    	ParametersI po = new ParametersI();
		po.leaves();
		po.startTime(omero.rtypes.rtime(startTime-1));
		Image i = (Image) iUpdate.saveAndReturnObject(
				mmFactory.simpleImage(startTime));
		
		List result = iContainer.getImagesByOptions(po);
		assertTrue(result.size() > 0);
		Iterator j = result.iterator();
		int count = 0;
		IObject object;
		Image img;
		int value = 0;
		while (j.hasNext()) {
			object = (IObject) j.next();
			if (object instanceof Image) {
				img = (Image) object;
				if (img.getId().getValue() == i.getId().getValue())
					value++;
					
				count++;
			}
		}
		assertTrue(result.size() == count);
		assertTrue(value == 1);
		//
		gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
				gc.get(Calendar.MONTH), 
				gc.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		startTime = gc.getTime().getTime();
		po = new ParametersI();
		po.leaves();
		po.startTime(omero.rtypes.rtime(startTime));
		result = iContainer.getImagesByOptions(po);
		assertTrue(result.size() == 0);
    } 

    /**
     * Test to load container hierarchy with dataset as root and no leaves
     * flag turned on.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:907")
    public void testLoadContainerHierarchyDatasetLeavesNotLoaded() 
    	throws Exception 
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(0));
    	//link the 2
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent(d);
    	link.setChild(img);
    	iUpdate.saveAndReturnObject(link);
    	ParametersI param = new ParametersI();
    	param.noLeaves();
        List<Long> ids = new ArrayList<Long>();
        List results = iContainer.loadContainerHierarchy(
        		Dataset.class.getName(), ids, param);
        assertTrue(results.size() > 0);
        Iterator i = results.iterator();
        DatasetData dataset;
        Set<ImageData> images;
        Iterator<ImageData> j;
        ImageData image;
        while (i.hasNext()) {
			dataset = new  DatasetData((Dataset) i.next());
			if (dataset.getId() == d.getId().getValue()) {
				images = dataset.getImages();
				assertNull(images);
			} 
		}
        
        //now check if the image is correctly loaded
        param = new ParametersI();
        param.leaves();
        results = iContainer.loadContainerHierarchy(
        		Dataset.class.getName(), ids, param);
        assertTrue(results.size() > 0);
        i = results.iterator();
        while (i.hasNext()) {
			dataset = new  DatasetData((Dataset) i.next());
			if (dataset.getId() == d.getId().getValue()) {
				images = dataset.getImages();
				assertTrue(images.size() > 0);
			} 
		}
    }
    
    /**
     * Test to load an image with its acquisition data. This method
     * invoked the <code>getImages</code>.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadImageWithAcquisitionData() 
    	throws Exception 
    {
    	//First create an image 
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	//create an instrument
    	Instrument instrument = (Instrument) iUpdate.saveAndReturnObject(
    			mmFactory.createInstrument(ModelMockFactory.LASER));
    	ParametersI param = new ParametersI();
    	param.addLong("iid", instrument.getId().getValue());
    	String sql = "select d from Objective as d where d.instrument.id = :iid";
    	Objective objective = (Objective) iQuery.findByQuery(sql, param);
    	//create so settings.
    	ObjectiveSettings settings = (ObjectiveSettings) 
    		iUpdate.saveAndReturnObject(
    				mmFactory.createObjectiveSettings(objective));
    	assertNotNull(settings);
    	image.setObjectiveSettings(settings);
    	StageLabel label = (StageLabel)
    		iUpdate.saveAndReturnObject(mmFactory.createStageLabel());
    	image.setStageLabel(label);
    	ImagingEnvironment env = (ImagingEnvironment)
			iUpdate.saveAndReturnObject(mmFactory.createImageEnvironment());
    	image.setImagingEnvironment(env);
    	iUpdate.saveAndReturnObject(image);
    	ParametersI po = new ParametersI();
		po.acquisitionData();
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(image.getId().getValue());
		List results = iContainer.getImages(Image.class.getName(), ids, param);
		assertNotNull(results);
		assertTrue(results.size() == 1);
		//Check if acquisition data are loaded.
		Image test = (Image) results.get(0);
		assertNotNull(test);
		assertTrue(test.getId().getValue() == image.getId().getValue());
		assertNotNull(test.getObjectiveSettings());
		assertNotNull(test.getImagingEnvironment());
		assertNotNull(test.getStageLabel());
		assertTrue(test.getObjectiveSettings().getId().getValue() == 
			settings.getId().getValue());
		assertTrue(test.getImagingEnvironment().getId().getValue() == 
			env.getId().getValue());
		assertTrue(test.getStageLabel().getId().getValue() == 
			label.getId().getValue());
    }
    
    /**
     * Test to load container hierarchy and make sure the annotations are
     * counted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testLoadContainerHierarchyProjectDatasetWithAnnotations() 
    	throws Exception 
    {
    	//first create a project
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	CommentAnnotation comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment Project"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	//attach comment to Project
    	ProjectAnnotationLinkI pal = new ProjectAnnotationLinkI();
    	pal.setParent((Project) p.proxy());
    	pal.setChild(comment);
    	iUpdate.saveAndReturnObject(pal);
    	comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment Dataset"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	//attach comment to Project
    	DatasetAnnotationLinkI dal = new DatasetAnnotationLinkI();
    	dal.setParent((Dataset) d.proxy());
    	dal.setChild(comment);
    	iUpdate.saveAndReturnObject(dal);
    	
    	//link the 2
    	ProjectDatasetLink link = new ProjectDatasetLinkI();
    	link.setParent((Project) p.proxy());
    	link.setChild((Dataset) d.proxy());
    	iUpdate.saveAndReturnObject(link);
    	Parameters param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        ProjectData project;
        Set<DatasetData> datasets;
        Iterator<DatasetData> j;
        DatasetData dataset;
        Map<Long, Long> count;
        Entry entry;
        Iterator k;
        while (i.hasNext()) {
        	//use pojos
			project = new ProjectData((Project) i.next());
			count = project.getAnnotationsCounts();
			assertEquals(1, count.size());
			datasets = project.getDatasets();
			k = count.entrySet().iterator();
			while (k.hasNext()) {
				entry = (Entry) k.next();
				assertEquals(((Long) entry.getValue()).longValue(), 1);
				
			}
			//assertTrue(count.containsKey(ctx.userId));
			//one annotation to project.
			//assertEquals(((Long) count.get(ctx.userId)).longValue(), 1);
			j = datasets.iterator();
			while (j.hasNext()) {
				dataset = j.next();
				count = dataset.getAnnotationsCounts();
				assertEquals(1, count.size());
				k = count.entrySet().iterator();
				while (k.hasNext()) {
					entry = (Entry) k.next();
					assertEquals(((Long) entry.getValue()).longValue(), 1);
					
				}
			}
		}
    }
    
    /**
     * Test to load container hierarchy and make sure the annotations are
     * counted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testLoadContainerHierarchyScreenPlatePlateAcquisitionWithAnnotations() 
    	throws Exception 
    {
    	//first create a Screen/Plate/PlateAcquisition
    	Screen s = (Screen) iUpdate.saveAndReturnObject(
    			mmFactory.simpleScreenData().asIObject());
    	Plate p = (Plate) iUpdate.saveAndReturnObject(
    			mmFactory.simplePlateData().asIObject());
    	PlateAcquisition a = (PlateAcquisition) 
    		mmFactory.simplePlateAcquisitionData().asIObject();
    	a.setPlate(p);
    	a = (PlateAcquisition) iUpdate.saveAndReturnObject(a);

        //Now create and attach comments to each
    	CommentAnnotation comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment Screen"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	//attach comment to Screen
    	ScreenAnnotationLinkI sal = new ScreenAnnotationLinkI();
    	sal.setParent((Screen) s.proxy());
    	sal.setChild(comment);
    	iUpdate.saveAndReturnObject(sal);

    	comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment Plate"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	//attach comment to Plate
    	PlateAnnotationLinkI pal = new PlateAnnotationLinkI();
    	pal.setParent((Plate) p.proxy());
    	pal.setChild(comment);
    	iUpdate.saveAndReturnObject(pal);
    	
    	comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment PlateAcquisition"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	//attach comment to Plate
    	PlateAcquisitionAnnotationLinkI aal = new PlateAcquisitionAnnotationLinkI();
    	aal.setParent((PlateAcquisition) a.proxy());
    	aal.setChild(comment);
    	iUpdate.saveAndReturnObject(aal);
    	
    	//link the Screen and Plate
    	ScreenPlateLink splink = new ScreenPlateLinkI();
    	splink.setParent((Screen) s.proxy());
    	splink.setChild((Plate) p.proxy());
    	iUpdate.saveAndReturnObject(splink);

    	Parameters param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(s.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Screen.class.getName(), ids, param);
        assertTrue(results.size() == 1);
        Iterator i = results.iterator();
        ScreenData screen;
        Set<PlateData> plates;
        Set<PlateAcquisitionData> plateAcquisitions;
        Iterator<PlateData> j;
        Iterator<PlateAcquisitionData> l;
        PlateData plate;
        PlateAcquisitionData plateAcquisition;
        Map<Long, Long> count;
        Entry entry;
        Iterator k;
        while (i.hasNext()) {
        	//use pojos
			screen = new ScreenData((Screen) i.next());
			count = screen.getAnnotationsCounts();
			assertEquals(1, count.size());
			k = count.entrySet().iterator();
			while (k.hasNext()) {
				entry = (Entry) k.next();
				assertEquals(((Long) entry.getValue()).longValue(), 1);
				
			}
			plates = screen.getPlates();
			j = plates.iterator();
			while (j.hasNext()) {
				plate = j.next();
				count = plate.getAnnotationsCounts();
				assertEquals(1, count.size());
				k = count.entrySet().iterator();
				while (k.hasNext()) {
					entry = (Entry) k.next();
					assertEquals(((Long) entry.getValue()).longValue(), 1);
					
				}
			    plateAcquisitions = plate.getPlateAcquisitions();
				l = plateAcquisitions.iterator();
			    while (l.hasNext()) {
				    plateAcquisition = l.next();
				    count = plateAcquisition.getAnnotationsCounts();
				    assertEquals(1, count.size());
				    k = count.entrySet().iterator();
				    while (k.hasNext()) {
					    entry = (Entry) k.next();
					    assertEquals(((Long) entry.getValue()).longValue(), 1);
					}
				}
			}
		}
    }

    /**
     * Test to find the P/D the specified images are in.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testFindContainerHierarchyFromProject() 
    	throws Exception 
    {
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(10));
    	//link the project and the dataset
    	ProjectDatasetLink l = new ProjectDatasetLinkI();
    	l.setParent((Project) p.proxy());
    	l.setChild((Dataset) d.proxy());
    	iUpdate.saveAndReturnObject(l);
    	
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent((Dataset) d.proxy());
    	link.setChild((Image) i.proxy());
    	iUpdate.saveAndReturnObject(link);
    	
    	
    	
    	ParametersI param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(i.getId().getValue());
        List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, param);
        assertEquals(results.size(), 1);
        Project found = (Project) results.get(0);
        assertEquals(found.getId().getValue(), p.getId().getValue());
        ProjectData project = new ProjectData(found);
        Set<DatasetData> datasets = project.getDatasets();
        assertEquals(datasets.size(), 1);
        Iterator<DatasetData> j = datasets.iterator();
        DatasetData dataset;
        Set<ImageData> images;
        Iterator<ImageData> k;
        while (j.hasNext()) {
			dataset = j.next();
			assertEquals(dataset.getId(), d.getId().getValue());
			images = dataset.getImages();
			k = images.iterator();
			while (k.hasNext()) {
				assertEquals(k.next().getId(), i.getId().getValue());
			}
		}
    }
    
    /**
     * Test to find the P/D the specified images are in.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testFindContainerHierarchyFromDataset() 
    	throws Exception 
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(10));
    	//link the project and the dataset
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setParent((Dataset) d.proxy());
    	link.setChild((Image) i.proxy());
    	iUpdate.saveAndReturnObject(link);
    	
    	
    	
    	ParametersI param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(i.getId().getValue());
        List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, param);
        assertEquals(results.size(), 1);
        Dataset found = (Dataset) results.get(0);
        assertEquals(found.getId().getValue(), d.getId().getValue());

        DatasetData dataset = new DatasetData(found);
        Set<ImageData> images = dataset.getImages();
        Iterator<ImageData> k = images.iterator();
		while (k.hasNext()) {
			assertEquals(k.next().getId(), i.getId().getValue());
		}
    }

    /**
     * Test to find the P/D the specified images are in.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testFindContainerHierarchyFromImage() 
    	throws Exception 
    {
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(10));
    	
    	ParametersI param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(i.getId().getValue());
        List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, param);
        assertEquals(results.size(), 1);
        Image found = (Image) results.get(0);
        assertEquals(found.getId().getValue(), i.getId().getValue());
    }

    /**
     * Test to find the P/D the specified images are in.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testFindContainerHierarchyWrongType() 
    	throws Exception 
    {
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage(10));
    	
    	ParametersI param = new ParametersI();
        List<Long> ids = new ArrayList<Long>();
        ids.add(i.getId().getValue());
        try {
        	iContainer.findContainerHierarchies(
            		Dataset.class.getName(), ids, param);
        	fail("Only Project type is supported.");
		} catch (Exception e) {}
    }
}
