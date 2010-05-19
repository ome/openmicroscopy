/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

//Java imports
import java.util.Arrays;
import java.util.List;


//Third-party libraries
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectI;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.sys.ParametersI;
import pojos.ProjectData;


/** 
 * Collections of tests for the <code>IUpdate</code> service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class UpdateServiceTest 
	extends TestCase 
{

	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(UpdateServiceTest.class);

	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    private omero.client client;
    
    /** Helper reference to the <code>Service factory</code>. */
    private ServiceFactoryPrx factory;
    
    /** Helper reference to the <code>IQuery</code> service. */
    private IQueryPrx iQuery;
    
    /** Helper reference to the <code>IUpdate</code> service. */
    private IUpdatePrx iUpdate;
     
	/**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception 
    {
        client = new omero.client();
        factory = client.createSession();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
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
        // This also calls: factory.destroy();
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLinks() 
    	throws Exception
    {
    	Image img = new ImageI();
        img.setName(rstring("duplinks"));
        img.setAcquisitionDate( rtime(0) );

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks"));

        img.linkDataset(ds);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ds = img.linkedDatasetList().get(0);

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnArray</code> method.
     * Note that the dataset has to be before the image in the list.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLink() 
    	throws Exception 
    {
    	Image img = new ImageI();
        img.setName(rstring("duplinks"));
        img.setAcquisitionDate(rtime(0));

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks"));

        img.linkDataset(ds);

        List<IObject> retVal = iUpdate.saveAndReturnArray(
        		Arrays.asList(ds, img));
        img = (Image) retVal.get(1);
        ds = (Dataset) retVal.get(0);

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateProjectDatasetLinkg() 
    	throws Exception 
    {
    	String name = "TEST:" + System.currentTimeMillis();

        // Save Project.
        Project p = new ProjectI();
        p.setName(rstring(name));
        p = (Project) iUpdate.saveAndReturnObject(p);

        // Check only one
        List list = iQuery.findAllByString(Project.class.getName(),
        		"name", name, true, null);
        assertTrue(list.size() == 1);
        assertEquals(((Project) list.get(0)).getId().getValue(),
        		 p.getId().getValue());

        // Update it.
        ProjectData pd = new ProjectData(p);
        pd.setDescription("....testnodups....");
        Project send = (Project) pd.asIObject();
        assertEquals(p.getId().getValue(), pd.getId());
        assertEquals(send.getId().getValue(), pd.getId());

        Project result = (Project) iUpdate.saveAndReturnObject(send);
        ProjectData test = new ProjectData(result);
        assertEquals(test.getId(), p.getId().getValue());

        // Check again.
        List list2 = iQuery.findAllByString(Project.class.getName(), 
        		"name", name, true,  null);
        assertTrue(list2.size() == 1);
        assertEquals(((Project) list.get(0)).getId().getValue(),
                ((Project) list2.get(0)).getId().getValue());
    }
    
    /**
     * Test to make sure that the version does not increase after an update.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken", "ticket:118" })
    public void tesVersionNotIncreasingAfterUpdate()
            throws Exception 
    {
        CommentAnnotation ann = new CommentAnnotationI();
        Image img = new ImageI();

        img.setName( rstring("version_test") );
        img.setAcquisitionDate( rtime(0) );
        ann.setTextValue( rstring("version_test") );
        img.linkAnnotation(ann);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ann = (CommentAnnotation) img.linkedAnnotationList().get(0);

        assertNotNull(img.getId());
        assertNotNull(ann.getId());

        int origVersion = img.getVersion().getValue();
        // No longer exists int orig_ann_version = ann.getVersion().intValue();

        ann.setTextValue(rstring("updated version_test"));

        ann = (CommentAnnotation) iUpdate.saveAndReturnObject(ann);
        img = (Image) iQuery.get(Image.class.getName(), img.getId().getValue()); 

        // No longer existsint new_ann_version = ann.getVersion().intValue();
        int newVersion = img.getVersion().getValue();

        assertFalse(ann.getTextValue().getValue().contains("updated"));
        assertTrue(origVersion == newVersion);
    }
    
    /**
     * Test to make sure that the version number does not increase 
     * when invoking the <code>SaveAndReturnObject</code> on an Object 
     * not modified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken", "ticket:118" })
    public void testVersionNotIncreasingOnUnmodifiedObject() 
    	throws Exception 
    {
        Image img = new ImageI();
        img.setName(rstring("no vers. increment")) ;
        img.setAcquisitionDate( rtime(0) );
        img = (Image) iUpdate.saveAndReturnObject(img);

        Image test = (Image) iUpdate.saveAndReturnObject(img);

        fail("must move details correction to the merge event listener "
                + "or version will always be incremented. ");

        assertTrue(img.getVersion().equals(test.getVersion()));
    }
    
}
