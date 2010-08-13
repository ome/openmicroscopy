/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IDeletePrx;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;
import omero.sys.ParametersI;

/** 
 * Collections of tests for the <code>Delete</code> service.
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
public class DeleteServiceTest 
	extends AbstractTest
{

    /** Helper reference to the <code>IDelete</code> service. */
    private IDeletePrx iDelete;
    
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
    	iDelete = factory.getDeleteService();
    }
    
    /**
     * Test to delete an image w/o pixels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImage() 
    	throws Exception
    {
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	assertNotNull(img);
    	long id = img.getId().getValue();
    	iDelete.deleteImage(id, false); //do not force.
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Image i ");
    	sb.append("where i.id = :id");
    	img = (Image) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    }
    
    /**
     * Test to delete a simple plate i.e. w/o wells or acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeletePlate() 
    	throws Exception
    {
    	Plate p = (Plate) iUpdate.saveAndReturnObject(
    			simplePlateData().asIObject());
    	assertNotNull(p);
    	long id = p.getId().getValue();
    	iDelete.deletePlate(id);
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Plate i ");
    	sb.append("where i.id = :id");
    	p = (Plate) iQuery.findByQuery(sb.toString(), param);
    	assertNull(p);
    }
    
    /**
     * Test to delete populated plate.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteFullPlate() 
    	throws Exception
    {
    	Plate p = (Plate) iUpdate.saveAndReturnObject(
    			simplePlateData().asIObject());
    	assertNotNull(p);
    	long id = p.getId().getValue();
    	iDelete.deletePlate(id);
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Plate i ");
    	sb.append("where i.id = :id");
    	p = (Plate) iQuery.findByQuery(sb.toString(), param);
    	assertNull(p);
    }
    
    
    /**
     * Test to delete an image w/o pixels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithPixels() 
    	throws Exception
    {
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	assertNotNull(img);
    	Pixels pixels = createPixels();
    	img.addPixels(pixels);
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	long id = img.getId().getValue();
    	assertNotNull(img.getPixels(0));
    	long pixId = img.getPixels(0).getId().getValue();
    	iDelete.deleteImage(id, false); //do not force.
    	ParametersI param = new ParametersI();
    	param.addId(id);

    	StringBuilder sb = new StringBuilder();
    	sb.append("select i from Image i ");
    	sb.append("where i.id = :id");
    	img = (Image) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    	sb = new StringBuilder();
    	param = new ParametersI();
    	param.addId(pixId);
    	sb.append("select i from Pixels i ");
    	sb.append("where i.id = :id");
    	pixels = (Pixels) iQuery.findByQuery(sb.toString(), param);
    	assertNull(img);
    }
    
    /**
     * Tests the <code>checkImageDelete</code> method.  
     * Returns the list of object than can prevent the delete.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCheckImageDelete()
    	throws Exception
    {
    	Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	assertNotNull(img);
    }
    
}
