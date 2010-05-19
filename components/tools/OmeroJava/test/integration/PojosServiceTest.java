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

    OMEData data;

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
        OmeroContext test = new OmeroContext(new String[]{
                "classpath:ome/config.xml",
                "classpath:ome/testing/data.xml"});
        data = (OMEData) test.getBean("data");
	*/
        
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
        // This also calls factory.destroy();
    }
    
    /**
     * Tests that the experimenter with login name <code>root</code> is
     * in the database, and makes sure it is converted into an 
     * <code>Pojo</code> object.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testExperimenterConvertion() 
    	throws Exception {
        List l = iQuery.findAllByExample(new ExperimenterI(), null);
        assertTrue("Root has to exist.", l.size() > 0);
        Experimenter exp = (Experimenter) l.get(0);
        assertNotNull("Must have an id", exp.getId());
        assertNotNull("And a login name", exp.getOmeName());

        // Now let's try to map it.
        ExperimenterData expData = new ExperimenterData(exp);
        assertNotNull("And something should still be there", expData);
        assertTrue("And it should have an id", expData.getId() > -1);
        assertNotNull("And login name", expData.getUserName());
    }

    /**
     * Test to create an image and <code>Pojo</code> representation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateImage() 
    	throws Exception
    {
        ImageData imgData = simpleImageData();
        Image img = (Image) iContainer.createDataObject(imgData.asIObject(), 
        			null);
        assertNotNull("We should get something back", img);
        assertNotNull("Should have an id", img.getId());
        assertTrue(imgData.getName().equals(img.getName().getValue()));
        Image img2 = (Image) iQuery.get(Image.class.getName(), 
        		img.getId().getValue());
        assertNotNull("And we should be able to find it again.", img2);
    }

    /**
     * Test to create an image with datasets, convert it to <code>Pojo</code>
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateImageWithDatasets() throws Exception {
        Image image = saveImage(true);
        List l = image.linkedDatasetList();
        assertNotNull(l);
        assertTrue(l.size() == 1);
        
        Dataset ds = (Dataset) l.get(0);
        long id = ds.getId().getValue();

        // another copy
        Image img2 = (Image) iQuery.findAllByQuery(
                "select i from Image i "
                        + "left outer join fetch i.datasetLinks "
                        + "where i.id = :id",
                new ParametersI().addId(image.getId())).get(0);
        //Convert the image into a Pojo
        ImageData data = new ImageData(img2);
        Set<DatasetData> datasets = data.getDatasets();
        assertTrue("It better have a dataset link too", datasets.size() == 1);
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        while (i.hasNext()) {
        	dataset = i.next();
        	assertEquals("And the ids have to be the same", id, dataset.getId());
		}
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
     * Test to link a dataset and an image.
     * 
     *  @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLinkDatasetAndImage() 
    	throws Exception 
    {
        ImageData imgData = simpleImageData();
        DatasetData dsData = simpleDatasetData();
        DatasetImageLink link = new DatasetImageLinkI();
        link.link(dsData.asDataset(), imgData.asImage());

        IObject test = iContainer.link(
        		Arrays.<IObject>asList(link) , null).get(0);
        assertNotNull("ILink should be there", test);

    }
    
    /**
     * Test to link a dataset and an image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLinkProjectAndDataset() 
    	throws Exception 
    {
        ProjectData p = simpleProjectData();
        DatasetData d = simpleDatasetData();

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.link(p.asProject(), d.asDataset());

        IObject test = iContainer.link(
        		Arrays.<IObject>asList(link) , null).get(0);
        assertNotNull("ILink should be there", test);
    }

    /**
     * Test to unlink datasets and images.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkDatasetAndImage() 
    	throws Exception {

        // Method 1:
        Image img = saveImage(true);
        List updated = unlinkImage(img);
        iUpdate.saveCollection(updated);

        // Make sure it's not linked.
        List list = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 2:
        img = saveImage(true);
        updated = unlinkImage(img);
        iContainer.updateDataObjects(updated, null);

        List list2 = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 3:
        img = saveImage(true);
        Dataset target = img.linkedDatasetList().get(0);
        // For querying
        DatasetImageLink dslink = img.findDatasetImageLink(target).iterator()
                .next();

        img.unlinkDataset(target);
        img = (Image) iContainer.updateDataObject(img, null);

        IObject test = iQuery.find(DatasetImageLink.class.getName(), 
        		dslink.getId().getValue());
        assertNull(test);

        // Method 4;
        Dataset d = new DatasetI();
        d.setName(rstring("unlinking"));
        Project p = new ProjectI();
        p.setName(rstring("unlinking"));
        p = (Project) iContainer.createDataObject(p, null);
        d = (Dataset) iContainer.createDataObject(d, null);

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d);
    }

    /**
     * Test to unlink projects and datasets. 
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkProjectAndDataset() 
    	throws Exception 
    {

       //TODO
    }
    
    /**
     * Test to unlink datasets and images from just one side.
     * @throws Exception
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkDatasetAndImageFromJustOneSide() 
    	throws Exception 
    {
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
    }

    /**
     * Test to unlink projects and datasets from just one side.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkProjectAndDatasetFromJustOneSide() 
    	throws Exception 
    {
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
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

        Parameters defaults = new ParametersI();

        List ids = data.getMax("Image.ids", 2);
        List results = iContainer.findContainerHierarchies(
        		Project.class.getName(), ids, defaults);
    }
    
    /**
     * Test to find hierarchies using the project as root node.
     * @throws ServerError Thrown if an error occurred.
     */
    @Test(groups = "EJBExceptions")
    public void testFindContainerHierarchiesDatasetAsRoot() 
    	throws ServerError 
    {

        Parameters empty = new ParametersI(new HashMap());

        List ids = data.getMax("Image.ids", 2);
        try {
        	List results = iContainer.findContainerHierarchies(
            		Dataset.class.getName(), ids, empty);
            fail("Should fail");
        } catch (ApiUsageException e) {
            // ok.
        }
    }

    /**
     * Test to retrieve the annotations linked to an image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testRetrieveCollectionForImage() 
    throws Exception 
    {
        Image i = (Image) iQuery.get(Image.class.getName(), 
        		fixture.iu5551.getId().getValue());
        i.unload();
        List<IObject> annotations = iContainer.retrieveCollection(i,
                ImageI.ANNOTATIONLINKS, null);
        assertTrue(annotations.size() > 0);
    }

    @Test(groups = "EJBExceptions")
    public void testCountingApiExceptions() throws Exception{

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
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

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
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

    }
    
    /**
     * Test to count the annotation link to an image.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCountAnnotationLinkedToProject() 
    	throws Exception 
    {
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

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
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);
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
        Long id = fixture.iu5551.getId().getValue();
        Map m = iContainer.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iContainer.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);
    }
        
    /*
    @Test
    public void test_getImages() throws Exception {
        List ids = data.getMax("Project.ids", 2);
        List images = iContainer.getImages(Project.class.getName(), ids, null);
    }

    @Test(groups = "EJBExceptions")
    public void test_getUserImages() throws Exception {
        try {
            List results = iContainer.getUserImages(null);
            fail("APIUsage: experimenter/group option must be set.");
        } catch (ApiUsageException e) {
            // ok.
        }

        results = iContainer.getUserImages(new ParametersI().exp(fixture.e.getId())
                .map());
        assertTrue(results.size() > 0);

    }
*/
    //
    // Misc
    //

    /**
     * Move to another class
     */
    @Test(groups = { "broken", "ticket:334" })
    public void testAndForTheFunOfItLetsGetTheREWorking() throws Exception {

    	/*
        Pixels pix = (Pixels) iQuery.findAll(Pixels.class.getName(), null).get(0);
        IPixelsPrx pixDB = factory.getPixelsService();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(pix.getId().getValue());
        re.load();

        omero.romio.PlaneDef pd = new omero.romio.PlaneDef();
        pd.slice = omero.romio.XY.value;
        pd.z = 0;
        pd.t = 0;
        re.render(pd);
        */

    }

    /**
     * Test to retrieve the number of images within a given dataset.
     * 
     * @throws Exception Thrown if an error occurred.s
     */
    @Test
    public void testImagesCount() 
    	throws Exception
    {
        long self = factory.getAdminService().getEventContext().userId;

        long id = fixture.du7770.getId().getValue();
        Dataset dataset = (Dataset) iContainer.loadContainerHierarchy(
        		Dataset.class.getName(),
                Collections.singletonList(id), null).iterator().next();
        
        // 7770 has not links
        //Test using the Pojo object
        DatasetData data = new DatasetData(dataset);
        Map<Long, Long> counts = data.getAnnotationsCounts();
        assertNotNull(counts);
        assertNull(counts.get(self));
        
        
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
    }

    // /
    // ========================================================================
    // / ~ Various bug-like checks
    // /
    // ========================================================================

   


    /**
     * Test to handle duplicate links
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDuplicateProjectDatasetLink() 
    	throws Exception 
    {

        String string = "duplinksagain" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName(rstring(string));

        Project p = new ProjectI();
        p.setName(rstring(string));

        d.linkProject(p);
        d = (Dataset) iContainer.createDataObject(d, null);
        List<Project> orig = d.linkedProjectList();
        Set orig_ids = new HashSet();
        for (Project pr : orig) {
            orig_ids.add(pr.getId().getValue());
        }

        DatasetData dd = new DatasetData(d);
        Dataset toSend = dd.asDataset();

        Dataset updated = (Dataset) iContainer.updateDataObject(toSend, null);

        List<Project> updt = updated.linkedProjectList();
        Set updt_ids = new HashSet();
        for (Project pr : updt) {
            updt_ids.add(pr.getId().getValue());
        }

        if (log.isDebugEnabled()) {
            log.debug(orig_ids);
            log.debug(updt_ids);
        }

        assertTrue(updt_ids.containsAll(orig_ids));
        assertTrue(orig_ids.containsAll(updt_ids));
    }
   
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
        long self = factory.getAdminService().getEventContext().userId;

        String name = " two rows " + System.currentTimeMillis();
        String text = " two rows content " + System.currentTimeMillis();
        String desc = " new description " + System.currentTimeMillis();

        // Setup: original is our in-memory, used every where object.
        Dataset original = new DatasetI();
        original.setName(rstring (name));
        original = (Dataset) iContainer.createDataObject(original, null);

        // No longer return these from create methods.
        assertNull(original.getAnnotationLinksCountPerOwner());
        // assertNull(original.getAnnotationLinksCountPerOwner().get(self));

        original.setDescription(rstring(desc));

        //Create the comment
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setNs(rstring(""));
        annotation.setTextValue(rstring(text));
        original.linkAnnotation(annotation);

        original = (Dataset) iContainer.createDataObject(original, null);
        
        //Make sure we have at one annotation
        List l = original.linkedAnnotationList();
        assertNotNull(l);
        assertTrue(l.size() == 1);
        annotation = (CommentAnnotation) l.get(0);

        assertUniqueAnnotationCreation(name, text);

        //Dataset test = (Dataset) iQuery.get(Dataset.class.getName(), original.getId().getValue());
        //assertTrue(desc.equals(test.getDescription()));

        // createDataObjects no longer does counts
        // assertNotNull(original.getAnnotationLinksCountPerOwner());
        // assertNotNull(original.getAnnotationLinksCountPerOwner().get(self));
        // assertTrue(original.getAnnotationLinksCountPerOwner().get(self) >
        // 0L);

    }

    /**
     * Test to create a project and link datasets to it.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateProjectAndLinkDatasets() 
    	throws Exception 
    {
        String name = " 2&1 " + System.currentTimeMillis();
        Project p = new ProjectI();
        p.setName(rstring(name));

        p = (Project) iContainer.createDataObject(p, null);

        Dataset d1 = new DatasetI();
        d1.setName(rstring(name));
        d1 = (Dataset) iContainer.createDataObject(d1, null);

        Dataset d2 = new DatasetI();
        d2.setName(rstring(name));
        d2 = (Dataset) iContainer.createDataObject(d2, null);

        ProjectDatasetLink l1 = new ProjectDatasetLinkI();
        ProjectDatasetLink l2 = new ProjectDatasetLinkI();

        l1.setParent(p);
        l1.setChild(d1);

        l2.setParent(p);
        l2.setChild(d2);

        p.addProjectDatasetLinkToBoth(l1, true);
        p.addProjectDatasetLinkToBoth(l2, true);

        p = (Project) iContainer.updateDataObject(p, null);

        //Check the conversion of Project to ProjectData
        ProjectData pData = new ProjectData(p);
        Set<DatasetData> datasets = pData.getDatasets();
        //We should have 2 datasets
        assertTrue(datasets.size() == 2);
        int count = 0;
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        while (i.hasNext()) {
        	dataset = i.next();
			if (dataset.getId() == d1.getId().getValue() ||
					dataset.getId() == d2.getId().getValue()) count++;
		}
        assertTrue(count == 2);
        
        
        Iterator it = p.copyDatasetLinks().iterator();
        while (it.hasNext()) {
            ProjectDatasetLink link = (ProjectDatasetLink) it.next();
            if (link.getChild().getId().getValue() == d1.getId().getValue()) {
                l1 = link;
                d1 = link.getChild();
            } else if (link.getChild().getId().getValue()
            		== d2.getId().getValue()) {
                l2 = link;
                d2 = link.getChild();
            } else {
                fail("Links aren't set up propertly");
            }
        }

        /* Use another test for that
        d1.setDescription( rstring(name) );

        Dataset test = (Dataset) iContainer.updateDataObject(d1, null);

        ProjectDatasetLink link1 = (ProjectDatasetLink) iQuery.get(ProjectDatasetLink.class.getName(), l1
                .getId().getValue());

        assertNotNull(link1);
        assertEquals(link1.getParent().getId().getValue(), p.getId().getValue()); 
        assertEquals(link1.getChild().getId().getValue(), d1.getId().getValue());

        ProjectDatasetLink link2 = (ProjectDatasetLink) iQuery.get(ProjectDatasetLink.class.getName(), l2
                .getId().getValue());

        assertNotNull(link2);
        assertEquals(link2.getParent().getId().getValue(), p.getId().getValue());
        assertEquals(link2.getChild().getId().getValue(), d2.getId().getValue());
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
    }
    
    /**
     * Tests to update a textual annotation.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUpdateTextualAnnotation() 
    	throws Exception 
    {
        DataObject annotatedObject;
        AnnotationData data;

        Dataset d = new DatasetI();
        d.setName(rstring("update_annotation"));
        d = (Dataset) iContainer.createDataObject(d, null);
        annotatedObject = new DatasetData(d);

        data = new TextualAnnotationData("update_annotation");
       
        IObject updated = iContainer.updateDataObject(
        		annotatedObject.asIObject(), null);

        DatasetAnnotationLink link = 
        	((Dataset) updated).linkAnnotation(data.asAnnotation());
        link = (DatasetAnnotationLink) iContainer.updateDataObject(link, null);
        link.getChild().unload();

        DataObject toReturn = 
        	new TextualAnnotationData((CommentAnnotation) link.getChild());
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

    @Test(groups = "ticket:318")
    public void testFilters_getUserImages() throws Exception {

        // nothing should throw an exception
        try {
            iContainer.getUserImages(null);
            fail();
        } catch (ApiUsageException api) {
            // ok
        }

        // TODO MOVE TO FIXTURE
        // First we'll need to create an image from the user but not in the
        // group,
        // and from the group but not the user
        // Image i1 = new Image(); i.setName("user not group");
        // i.getDetails().setOwner(fixture.e);
        // i.getDetails().setGroup(new ExperimenterGroup(1L));
        // i =
        // Image i2 = new Image(); i.

        // just filtering for the user should get us everything
        List<Image> imgs = iContainer.getUserImages(OWNER_FILTER);
        assertFilterWorked(imgs, 0, null, fixture.e, null);

        // now for groups
        imgs = iContainer.getUserImages(GROUP_FILTER);
        assertFilterWorked(imgs, 0, null, null, fixture.g);

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
        // there are about 6 projects in our fixture
    	List ids = data.getMax("Project.ids", 100);

        List<Image> images = iContainer.getImages(Project.class.getName(), ids, 
        		OWNER_FILTER);
        assertFilterWorked(images, null, 100, fixture.e, null);
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
        // there are about 6 projects in our fixture
    	List ids = data.getMax("Project.ids", 100);
        List<Image> images = iContainer.getImages(Project.class.getName(), ids, 
        		GROUP_FILTER);
        assertFilterWorked(images, null, 100, null, fixture.g);
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
    	List ids = data.getMax("Image.ids", 100);
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
    	List ids = data.getMax("Image.ids", 100);
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
    }
    
    /**
     * Tests the retrieval of a projects filtering by owners.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testloadContainerHierarchyFilterByOwner() 
    	throws Exception
    {
    	List ids = data.getMax("Project.ids", 2);
    	List results = iContainer.loadContainerHierarchy(Project.class.getName(), 
        		ids,  OWNER_FILTER);
        assertFilterWorked(results, null, 100, fixture.e, null);
    }
    
    /**
     * Tests the retrieval of a projects filtering by groups.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:318")
    public void testloadContainerHierarchyFilterByGroup() 
    	throws Exception
    {
        List ids = data.getMax("Project.ids", 2);
        List results = iContainer.loadContainerHierarchy(Project.class.getName(), 
        		ids, GROUP_FILTER);
        assertFilterWorked(results, null, 100, null, fixture.g);
    }

    /**
     * Tests the creation of a project without datasets.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyProject() 
    	throws Exception
    {
        ProjectData data = new ProjectData();
        data.setName("name");
        Project p = (Project) iContainer.createDataObject(data.asIObject(), 
        		null);
        assertTrue(p.getDetails().getGroup().sizeOfGroupExperimenterMap() != 0);
        assertTrue(p.getDetails().getOwner().sizeOfGroupExperimenterMap() != 0);
    }

}
