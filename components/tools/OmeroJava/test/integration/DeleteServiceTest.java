/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IDeletePrx;
import omero.model.Channel;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.StatsInfo;
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
    public void testDeleteBasicImage() 
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
     * Test to delete an image with pixels, channels, logical channels 
     * and statistics.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImage() 
    	throws Exception
    {
    	Image img = createImage();
    	Pixels pixels = img.getPrimaryPixels();
    	long pixId = pixels.getId().getValue();
    	//method already tested, make sure objects are loaded.
    	pixels = factory.getPixelsService().retrievePixDescription(pixId);
    	//channels.
    	long id = img.getId().getValue();
    	
    	List<Long> channels = new ArrayList<Long>();
    	List<Long> logicalChannels = new ArrayList<Long>();
    	List<Long> infos = new ArrayList<Long>();
    	Channel channel;
    	LogicalChannel lc;
    	StatsInfo info;
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = pixels.getChannel(i);
			assertNotNull(channel);
			channels.add(channel.getId().getValue());
			lc = channel.getLogicalChannel();
			assertNotNull(lc);
			logicalChannels.add(lc.getId().getValue());
			info = channel.getStatsInfo();
			assertNotNull(info);
			infos.add(info.getId().getValue());
		}
    	
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
    	Iterator<Long> i = channels.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb.append("select i from Channels i ");
	    	sb.append("where i.id = :id");
		}
    	i = infos.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb.append("select i from StatsInfo i ");
	    	sb.append("where i.id = :id");
		}
    	i = logicalChannels.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb.append("select i from LogicalChannel i ");
	    	sb.append("where i.id = :id");
		}
    }

}
