/*
/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

//Java imports
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.RType;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import static omero.rtypes.rlong;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
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

	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

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
    			simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	
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
			if (project.getId() == p.getId().getValue()) {
				datasets = project.getDatasets();
				assertTrue(datasets.size() == 1);
				j = datasets.iterator();
				while (j.hasNext()) {
					dataset = j.next();
					assertTrue(dataset.getId() == d.getId().getValue());
				}
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
    			simpleScreenData().asIObject());
    	Plate d = (Plate) iUpdate.saveAndReturnObject(
    			simplePlateData().asIObject());
    	
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
			if (screen.getId() == p.getId().getValue()) {
				plates = screen.getPlates();
				assertTrue(plates.size() == 1);
				j = plates.iterator();
				while (j.hasNext()) {
					plate = j.next();
					assertTrue(plate.getId() == d.getId().getValue());
				}
			} 
		}
    }
    
    /**
     * Test to load container hierarchy with project specified, no orphan
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
    			simpleProjectData().asIObject());
    	Project p2 = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	
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
     * Test to load container hierarchy with screen specified, no orphan
     * loaded
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyNoScreenSpecified() 
    	throws Exception 
    {
    	//first create a project
    	long self = factory.getAdminService().getEventContext().userId;
    	Screen p = (Screen) iUpdate.saveAndReturnObject(
    			simpleScreenData().asIObject());
    	Screen p2 = (Screen) iUpdate.saveAndReturnObject(
    			simpleScreenData().asIObject());
    	
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
    			simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Project p2 = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	
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
    			simpleScreenData().asIObject());
    	Plate d = (Plate) iUpdate.saveAndReturnObject(
    			simplePlateData().asIObject());
    	
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
    			simpleDatasetData().asIObject());
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
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
    			simpleDatasetData().asIObject());
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Pixels pixels = createPixels();
    	img.addPixels(pixels);
    	img = (Image) iUpdate.saveAndReturnObject(img);
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
    public void testCollectionCountForDataset() 
    	throws Exception 
    {
    	Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			simpleImage(0));
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
    
    

    @Test(groups = "EJBExceptions")
    public void testCountingApiExceptions() throws Exception{

    	/*
        List ids = Collections.singletonList(new Long(1));

        // Does not exist
        try {
            iContainer.getCollectionCount("DoesNotExist", "meNeither", ids, 
            		null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Missing plural on dataset
        try {
            iContainer.getCollectionCount("ome.model.containers.Project",
                    "dataset", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Null ids
        try {
            iContainer.getCollectionCount("ome.model.containers.Project",
                    "datasets", null, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Poorly formed
        try {
            iContainer.getCollectionCount("hackers.rock!!!", "", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty Class string
        try {
            iContainer.getCollectionCount("", "datasets", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty Class string
        try {
            iContainer.getCollectionCount(null, "datasets", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty property string
        try {
            iContainer.getCollectionCount("ome.model.core.Image", "", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Null property string
        try {
            iContainer.getCollectionCount("ome.model.core.Image", null, ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }
*/
    }

    // /
    // ========================================================================
    // / ~ Various bug-like checks
    // /
    // ========================================================================
    /**
     * Test to update an uploaded object using the <code>Pojo</code> object.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "version", "broken" })
    public void testUnloadedDataset() 
    	throws Exception 
    {
    	Image i = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
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
    	
        p = (Project) iContainer.loadContainerHierarchy(
                Project.class.getName(), 
                Collections.singletonList(p.getId().getValue()), 
                null).iterator().next();
        ProjectData pData = new ProjectData(p);
        
        DatasetData dData = pData.getDatasets().iterator().next();
        pData.setDescription("new value:ui");

        iContainer.updateDataObject(pData.asIObject(), null);
        try {
        	dData.getName();
            fail(" this should blow up ");
        } catch (Exception e) { // TODO which exception?
            // good.
        }
    }

    /**
     * Tests the retrieval of images filtering by owners.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByOwner() 
    	throws Exception
    {
    	Image i1 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
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
    	Image i1 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Pixels pixels = createPixels();
    	i1.addPixels(pixels);
    	i1 = (Image) iUpdate.saveAndReturnObject(i1);
    	
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
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
    	Image i1 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
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
    			simpleDatasetData().asIObject());
        long d1 = d.getId().getValue();
        
        Image image1 = (Image) update.saveAndReturnObject(simpleImage(0));
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
    			simpleDatasetData().asIObject());
        long d2 = d.getId().getValue();
        Image image2 = (Image) 
        	f.getUpdateService().saveAndReturnObject(simpleImage(0));
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
    @Test(groups = "ticket:318")
    public void testFindContainerHierarchiesProjectAsRootFilterByOwner() 
    	throws Exception
    {
    	long id =  fixture.e.getId().getValue();
    	ParametersI param = new ParametersI();
    	param.leaves();
    	param.exp(omero.rtypes.rlong(id));
    	
    	
    	Image i = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
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
    	try {
    		Project pp = (Project) results.get(0);
        	assertTrue(pp.getId().getValue() == p.getId().getValue());
        	//Should return a project not an image.
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
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
    			simpleProjectData().asIObject());
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
    			simpleProjectData().asIObject());
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
		Image i = (Image) iUpdate.saveAndReturnObject(simpleImage(startTime));
		
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
    			simpleDatasetData().asIObject());
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
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
				assertTrue(images.size() == 1);
			} 
		}
    }
    
}
