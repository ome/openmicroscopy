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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.RType;
import omero.ServerError;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Plate;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TextualAnnotationData;

/**
 * copied from client/test/ome/adapters/pojo/PojosServiceTest for the ticket
 * 1106 October, 2008
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
@Test(groups = { "client", "integration", "blitz" })
public class PojosServiceTest 
	extends AbstractTest//TestCase 
{

	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

    /** Reference to class used to create data object. */
    CreatePojosFixture2 fixture;

    /** Helper reference to the <code>IContainer</code> service. */
    private IContainerPrx iContainer;
    
    /** Used to filter by group. */
    private Parameters GROUP_FILTER;

    /** Used to filter by owner. */
    private Parameters OWNER_FILTER;

    // ~ Helpers
    // =========================================================================


    // TODO move to another class
    // now let's test all methods that use the filtering functionality
    // ===========================================================
  
    /**
     * Helper method to make sure the filter works.
     * 
     * @param results The values to handle.
     * @param min	  The minimum value or <code>null</code>.
     * @param max	  The maximum value or <code>null</code>.
     * @param e	 	  The experimenter the filter is using or <code>null</code>.
     * @param g	 	  The group the filter is using or <code>null</code>.
     */
    private void assertFilterWorked(List<?> results, Integer min, Integer max, 
    		Experimenter e, ExperimenterGroup g)
   {
        if (min != null) {
            assertTrue(results.size() > min);
        }
        if (max != null) {
            assertTrue(results.size() < max);
        }
        List<IObject> r = (List<IObject>) results;
        if (e != null) {
            for (IObject iobj : r) {
                assertEquals(e.getId().getValue(),
                        iobj.getDetails().getOwner().getId().getValue());
            }
        }
        if (g != null) {
            for (IObject iobj : r) {
                assertEquals(g.getId().getValue(),
                        iobj.getDetails().getGroup().getId().getValue());
            }
        }
    }
    
    /**
     * Creates a default image and returns it.
     * 
     * @return See above.
     */
    private ImageData simpleImageData()
    {
        // prepare data
        ImageData id = new ImageData();
        id.setName("My test image");
        id.setDescription("My test description");
        return id;
    }
    
    /**
     * Creates a default image and returns it.
     * 
     * @return See above.
     */
    private Image simpleImage() { return simpleImage(0); }
    
    /**
     * Creates a default dataset and returns it.
     * 
     * @return See above.
     */
    private DatasetData simpleDatasetData()
    {
        DatasetData dd = new DatasetData();
        dd.setName("t1");
        dd.setDescription("t1");
        return dd;
    }
    
    /**
     * Creates a default project and returns it.
     * 
     * @return See above.
     */
    private ProjectData simpleProjectData()
    {
        ProjectData data = new ProjectData();
        data.setName("project1");
        data.setDescription("project1");
        return data;
    }

    /**
     * Creates a default screen and returns it.
     * 
     * @return See above.
     */
    private ScreenData simpleScreenData()
    {
    	ScreenData data = new ScreenData();
        data.setName("screen1");
        data.setDescription("screen1");
        return data;
    }
    
    /**
     * Creates a default project and returns it.
     * 
     * @return See above.
     */
    private PlateData simplePlateData()
    {
    	PlateData data = new PlateData();
        data.setName("plate1");
        data.setDescription("plate1");
        return data;
    }
    
    /**
     * Creates an image, links it to a a new dataset and returns it.
     * 
     * @return See above.
     */
    private ImageData simpleImageDataWithDatasets()
    {
        DatasetData dd = simpleDatasetData();
        Set dss = new HashSet();
        dss.add(dd);
        ImageData id = simpleImageData();
        id.setDatasets(dss);
        return id;
    }

    /**
     * Makes sure that we have only one comment linked to the specified
     * dataset.
     * 
     * @param name The name of the dataset.
     * @param text The comment.
     * @throws ServerError Thrown if an error occurred while retrieving data.
     */
    private void assertUniqueAnnotationCreation(String name, String text) 
    	throws ServerError
    {
        // Test
        List ds = iQuery.findAllByString(Dataset.class.getName(), 
        		"name", name, true, null);
        List as = iQuery.findAllByString(CommentAnnotation.class.getName(), 
        		"textValue", text, true, null);

        assertTrue(ds.size() == 1);
        assertTrue(as.size() == 1);
    }
    
    /** 
     * Creates and saves an image with or without datasets.
     * 
     * @param withDataset Pass <code>true</code> to create an image with 
     *                    datasets, <code>false</code> otherwise.					  
     * @throws ServerError Thrown if an error occurred.
     */
    private Image saveImage(boolean withDataset) 
    	throws ServerError 
    {
        ImageData imgData = simpleImageDataWithDatasets();
        Image image = (Image) imgData.asIObject();
        image.setAcquisitionDate(rtime(0));
        image = (Image) iUpdate.saveAndReturnObject(image);
        assertTrue("It better have a dataset link",
        		image.sizeOfDatasetLinks() > 0);
        return image;
    }

    /**
     * Unlinks the datasets from the passed image.
     * 
     * @param img The image to unlink the object from.
     * @return The list of links.
     */
    private List unlinkImage(Image img)
    {
        List updated = img.linkedDatasetList();
        for (Object o : updated) {
            img.unlinkDataset((Dataset)o);
        }
        updated.add(img);
        return updated;
    }
    
    /**
     * Checks that the annotation is valid.
     * 
     * @param m The map to handle.
     */
    private void assertAnnotations(Map<Long, List<IObject>> m) 
    {
    	assertNotNull(m);
        Annotation ann = (Annotation) m.values().iterator().next().iterator()
                .next();
        assertNotNull(ann.getDetails().getOwner());
        assertTrue(ann.getDetails().getOwner().isLoaded());
        assertNotNull(ann.getDetails().getCreationEvent());
        assertTrue(ann.getDetails().getCreationEvent().isLoaded());
        // Annotations are immutable
        // assertNotNull(ann.getDetails().getUpdateEvent());
        // assertTrue(ann.getDetails().getUpdateEvent().isLoaded());

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
    	/*
        client = new omero.client();
        factory = client.createSession();
        iContainer = factory.getContainerService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();

        root = new omero.client();
        root.createSession("root", client.getProperty("omero.rootpass"));
        */
    	super.setUp();
    	iContainer = factory.getContainerService();
        fixture = CreatePojosFixture2.withNewUser(root);
        fixture.createAllPojos();

        GROUP_FILTER = new ParametersI().grp(fixture.g.getId());
        OWNER_FILTER = new ParametersI().exp(fixture.e.getId());
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
   
    /*
     *  @Override
    @AfterClass
    public void tearDown() 
    	throws Exception 
    {
        client.__del__();
        root.__del__();
    }*/

    /**
     * Test to delete an newly created image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateAndDeleteBasicImage() 
    	throws Exception 
    {
    	/*
        ImageData imgData = simpleImageData();
        Image img = (Image) imgData.asIObject();

        assertNull("Image doesn't have an id.", img.getId());
        
        img = (Image) iContainer.createDataObject(img, null);
        assertNotNull("Presto change-o, now it does.", img.getId());
        
        //Delete the image.
        iContainer.deleteDataObject(img, null);

        //Retrieve the image.
        img = (Image) iQuery.find(Image.class.getName(), 
        		img.getId().getValue());
        assertNull("we should have deleted it ", img);
        */
    }
    
    /**
     * Test to delete an newly created image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateAndDeleteBasicImageWithPixels() 
    	throws Exception 
    {
        //TODO

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
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
     * Test to the collection count method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCollectionCountForDataset() 
    	throws Exception 
    {
    	Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Image i = (Image) iUpdate.saveAndReturnObject(
    			simpleImage());
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

    /**
     * Test to count the annotation link to an image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCountAnnotationLinkedToImage() 
    	throws Exception 
    {
    	/*
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);
        */
    }

    /**
     * Test to count the annotation link to an image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCountAnnotationLinkedToDataset() 
    	throws Exception 
    {
    	/*
        Long id = fixture.dr7071.getId().getValue();
        Map m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() == 1);
        */

    }
    
    /**
     * Test to count the images linked to a dataset.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCountImageLinkedToDataset() 
    	throws Exception 
    {
    	/*
        Long id = fixture.dr7071.getId().getValue();
        Map m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() == 2);
        */
    }
    
    /**
     * Test to count the datasets linked to a project.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCountDatasetLinkedToProject() 
    	throws Exception 
    {
    	/*
        Long id = fixture.pr9091.getId().getValue();
        Map m = iContainer.getCollectionCount(Project.class.getName(),
                ProjectI.DATASETLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() == 2);
        */
    }

    /**
     * Test to retrieve the number of images within a given dataset.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testImagesCount() 
    	throws Exception
    {
       /*
        long id = fixture.du7770.getId().getValue();
        Dataset dataset = (Dataset) iContainer.loadContainerHierarchy(
        		Dataset.class.getName(),
                Collections.singletonList(id), null).iterator().next();
        
        // 7770 has not links
        //Test using the Pojo object
        DatasetData data = new DatasetData(dataset);
        Map<Long, Long> counts = data.getAnnotationsCounts();
        assertNotNull(counts);
        //assertNull(counts.get(self));
        
        
        //Retrieve dataset 7771
        id = fixture.du7771.getId().getValue();
        dataset = (Dataset) iContainer.loadContainerHierarchy(
        		Dataset.class.getName(),
                Collections.singletonList(id), null).iterator().next();
        data = new DatasetData(dataset);
        counts = data.getAnnotationsCounts();
        assertNotNull(counts);
        Entry entry;
        Iterator i = counts.entrySet().iterator();
        Long value;
        while (i.hasNext()) {
			entry = (Entry) i.next();
			value = (Long) entry.getValue();
			assertNotNull(value);
		}
		*/
    }

    // /
    // ========================================================================
    // / ~ Various bug-like checks
    // /
    // ========================================================================
   
    /**
     * Test to annotate a dataset with a comment.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testAnnotateDatasetCGLIBIssue() 
    	throws Exception 
    {

        // Setup: original is our in-memory, used every where object.
        Dataset original = new DatasetI();
        original.setName(rstring(" two rows "));
        original = (Dataset) iContainer.createDataObject(original, null);
        DatasetData annotatedObject = new DatasetData(original);
        Dataset annotated = (Dataset) 
        	iContainer.updateDataObject(annotatedObject.asIObject(), null);
        // Dataset m = new Dataset( original.getId(), false);
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setNs(rstring(""));
        annotation.setTextValue(rstring(" two rows content "));

        // CGLIB
        CommentAnnotation object = (CommentAnnotation) 
        	iContainer.createDataObject(annotation, null);
        DataObject returnedToUser = new TextualAnnotationData(object);

        // Now working but iPojos is still returning a CGLIB class.
        assertTrue(String.format("Class %s should equal class %s", object
                .getClass(), annotation.getClass()), object.getClass().equals(
                annotation.getClass()));
    }

    /**
     * Test to annotate a dataset with a comment annotation.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testAnnotateDatasetWithComment() 
    	throws Exception 
    {
    	/*
        String name = " two rows " + System.currentTimeMillis();
        String text = " two rows content " + System.currentTimeMillis();
        String desc = " new description " + System.currentTimeMillis();

        // Setup: original is our in-memory, used every where object.
        Dataset original = new DatasetI();
        original.setName(rstring (name));
        original = (Dataset) iUpdate.saveAndReturnObject(original);

        // No longer return these from create methods.
        assertNull(original.getAnnotationLinksCountPerOwner());
        
        original.setDescription(rstring(desc));

        //Create the comment
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setNs(rstring(""));
        annotation.setTextValue(rstring(text));
       
        // create the annotation
        annotation =  (CommentAnnotation)
        	iUpdate.saveAndReturnObject(annotation);
        // Link the annotation and the dataset
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setParent(original);
        link.setChild(annotation);
        // create the link
        link = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(link);
        assertTrue(link.getParent().getId().getValue() == 
        	original.getId().getValue());
        assertTrue(link.getChild().getId().getValue() == 
        	annotation.getId().getValue());
        	*/
    }

    /**
     * Tests the deletion of a comment annotation.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteAnnotation() 
    	throws Exception
    {
    	/*
        String string = "delete_annotation" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName(rstring(string));

        CommentAnnotation a = new CommentAnnotationI();
        a.setNs(rstring(""));
        a.setTextValue(rstring(string));
        d.linkAnnotation(a);

        d = (Dataset) iContainer.createDataObject(d, null);
        DatasetAnnotationLink al = d.copyAnnotationLinks().iterator()
                .next();
        a = (CommentAnnotation) al.getChild();

        iContainer.deleteDataObject(al, null);
        iContainer.deleteDataObject(a, null);

        Object o = iQuery.find(CommentAnnotation.class.getName(),
        		a.getId().getValue());
        assertNull(o);
        */
    }

    /**
     * Test to update an uploaded object using the <code>Pojo</code> object.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "version", "broken" })
    public void testUnloadedDataset() 
    	throws Exception 
    {
    	Image i = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
    	Image i1 = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
     * Links twice a dataset and an image. Only one link should be inserted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testDuplicateDatasetImageLink() 
    	throws Exception
    {
    	Image i1 = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
        /*TODO:rewrite test
    	List ids = fixture.getProjectIds();
        List<Image> images = iContainer.getImages(Project.class.getName(), ids, 
        		GROUP_FILTER);
        assertFilterWorked(images, null, 100, null, fixture.g);
        */
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
    	
    	
    	Image i = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage());
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
