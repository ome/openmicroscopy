/*
/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

//Java imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;



//Third-party libraries
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.testing.OMEData;
import omero.ApiUsageException;
import omero.OptimisticLockException;
import omero.RInt;
import omero.ServerError;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;
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
	extends TestCase 
{

	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    private omero.client client;
    
    /** Helper reference to the <code>Service factory</code>. */
    private ServiceFactoryPrx factory;
    
    /** Reference to class used to create data object. */
    CreatePojosFixture2 fixture;

    //OMEData data;

    /** Helper reference to the <code>IContainer</code> service. */
    private IContainerPrx iContainer;

    /** Helper reference to the <code>IQuery</code> service. */
    private IQueryPrx iQuery;

    /** Helper reference to the <code>IUpdate</code> service. */
    private IUpdatePrx iUpdate;
    
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
        client = new omero.client();
        factory = client.createSession();
        iContainer = factory.getContainerService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();

        omero.client root = new omero.client();
        root.createSession("root", client.getProperty("omero.rootpass"));
        fixture = CreatePojosFixture2.withNewUser(root);
        fixture.createAllPojos();

        GROUP_FILTER = new ParametersI().grp(fixture.g.getId());
        OWNER_FILTER = new ParametersI().exp(fixture.e.getId());
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterMethod
    public void tearDown() 
    	throws Exception 
    {
    	client.closeSession();
    	factory = client.createSession();
        iContainer = factory.getContainerService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
    }

    /**
     * Test to create an image and make sure the version is correct.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken" })
    public void testVersionHandling() 
    	throws Exception
    {
        Image img = new ImageI();
        img.setName(rstring("version handling"));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        sent.setDescription(rstring("version handling update"));
        RInt version = sent.getVersion();

        // Version incremented
        Image sent2 = (Image) iUpdate.saveAndReturnObject(sent);
        RInt version2 = sent2.getVersion();
        assertTrue(version.getValue() != version2.getValue());

        // Resetting; should get error
        sent2.setVersion(version);
        CommentAnnotation iann = new CommentAnnotationI();
        iann.setTextValue( rstring(" version handling "));
        try {
            iUpdate.saveAndReturnObject(sent2);
            fail("Need optmistic lock exception.");
        } catch (OptimisticLockException e) {
            // ok.
        }

        // Fixing the change;
        // now it should work.
        sent2.setVersion( version2 );
        iUpdate.saveAndReturnObject(iann);

    }

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
     * Test to load container hierarchy with dataset specified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyDatasetSpecified() 
    	throws Exception 
    {
    	/*

        List ids = Arrays.asList(fixture.pu9990.getId().getValue(), 
        			fixture.pu9991.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, null);

        ParametersI po = new ParametersI().exp(rlong(0L));
        results = iContainer.loadContainerHierarchy(Project.class.getName(), null, po);
    	 */
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
    	/*

        List ids = Arrays.asList(fixture.pu9990.getId().getValue(), 
        			fixture.pu9991.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, null);

        ParametersI po = new ParametersI().exp(rlong(0L));
        results = iContainer.loadContainerHierarchy(Project.class.getName(), null, po);
    	 */
    }
    
    /**
     * Test to load container hierarchy, root node is <code>Project</code>.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyProjectAsRootWithOptions() 
    	throws Exception 
    {
    	/*

        List ids = Arrays.asList(fixture.pu9990.getId().getValue(), 
        			fixture.pu9991.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, null);

        ParametersI po = new ParametersI().exp(rlong(0L));
        results = iContainer.loadContainerHierarchy(Project.class.getName(), null, po);
    	 */
    }

    /**
     * Test to load container hierarchy, root node is <code>Screen</code>.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadContainerHierarchyScreenAsRootWithOptions() 
    	throws Exception 
    {
    	/*

        List ids = Arrays.asList(fixture.pu9990.getId().getValue(), 
        			fixture.pu9991.getId().getValue());
        List results = iContainer.loadContainerHierarchy(
        		Project.class.getName(), ids, null);

        ParametersI po = new ParametersI().exp(rlong(0L));
        results = iContainer.loadContainerHierarchy(Project.class.getName(), null, po);
    	 */
    }
    
    /**
     * Test to find hierarchies using the project as root node.
     * @throws ServerError Thrown if an error occurred.
     */
    @Test(groups = "EJBExceptions")
    public void testFindContainerHierarchiesProjectAsRoot() 
    	throws ServerError 
    {

    	/*TODO: rewrite test
        Parameters defaults = new ParametersI();
        List ids = fixture.getImageIds();
        List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, defaults);
        		*/
    }
    
    /**
     * Test to find hierarchies using the project as root node.
     * @throws ServerError Thrown if an error occurred.
     */
    @Test(groups = "EJBExceptions")
    public void testFindContainerHierarchiesDatasetAsRoot() 
    	throws ServerError 
    {

    	/* TODO: rewrite test
        Parameters empty = new ParametersI(new HashMap());

        List ids = fixture.getImageIds();
        try {
        	List results = iContainer.findContainerHierarchies(
            		Dataset.class.getName(), ids, empty);
            fail("Should fail");
        } catch (ApiUsageException e) {
            // ok.
        }
        */
    }

    /**
     * Test to retrieve the annotations linked to an image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testRetrieveCollectionForImage() 
    throws Exception 
    {
    	/*TODO: rewrite test
        Image i = (Image) iQuery.get(Image.class.getName(), 
        		fixture.iu5551.getId().getValue());
        i.unload();
        List<IObject> annotations = iContainer.retrieveCollection(i,
                ImageI.ANNOTATIONLINKS, null);
        assertTrue(annotations.size() > 0);
        */
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
        Project p = new ProjectI();
        p.setName(rstring("ui"));
        Dataset d = new DatasetI();
        d.setName(rstring("ui"));
        Image i = new ImageI();
        i.setName(rstring("ui"));
        p.linkDataset(d);
        d.linkImage(i);

        p = (Project) iContainer.createDataObject(p, null);

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
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testGetImagesByOwner() 
    	throws Exception
    {
        /*TODO: rewrite test
    	List ids = fixture.getProjectIds();

        List<Image> images = iContainer.getImages(Project.class.getName(), ids, 
        		OWNER_FILTER);
        assertFilterWorked(images, null, 100, fixture.e, null);
        */
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
    public void testFindContainerHierarchiesFilterByOwner() 
    	throws Exception
    {
    	/*TODO: rewrite test
    	List ids = fixture.getImageIds();
        try {
        	List results = iContainer.findContainerHierarchies(
            		Project.class.getName(), ids,
                    OWNER_FILTER);
            assertFilterWorked(results, null, 100, fixture.e, null);
            //but this shouldn't.
            Iterator i = results.iterator();
            while (i.hasNext()) {
                if (i.next() instanceof Image) {
                    i.remove();
                }
            }
            assertFilterWorked(results, null, 100, fixture.e, null);
        } catch (AssertionFailedError afe) {
        	// First assert may fail since the images aren't filtered
        }
        */
    }

    /**
     * Tests the finding of projects filtering by owners.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testFindContainerHierarchiesFilterByGroup() 
    	throws Exception
    {
    	/*TODO: rewrite test
    	List ids = fixture.getImageIds();
        try {
        	List results = iContainer.findContainerHierarchies(
            		Project.class.getName(), ids, GROUP_FILTER);
            assertFilterWorked(results, null, 100, null, fixture.g);
            Iterator i = results.iterator();
            while (i.hasNext()) {
                if (i.next() instanceof Image) {
                    i.remove();
                }
            }
            assertFilterWorked(results, null, 100, null, fixture.g);
        } catch (AssertionFailedError afe) {
        	// First assert may fail since the images aren't filtered
        }
        */
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
    	/*TODO: rewrite test
    	List<Long> ids = fixture.getProjectIds();
    	List results = iContainer.loadContainerHierarchy(Project.class.getName(), 
        		ids,  OWNER_FILTER);
        assertFilterWorked(results, null, 100, fixture.e, null);
        */
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
    	
    	/*TODO: rewrite Test
        List ids = fixture.getProjectIds();
        List results = iContainer.loadContainerHierarchy(Project.class.getName(), 
        		ids, GROUP_FILTER);
        assertFilterWorked(results, null, 100, null, fixture.g);
        */
    }

}
