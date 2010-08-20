/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IDeletePrx;
import omero.api.IRenderingSettingsPrx;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImagingEnvironment;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LogicalChannel;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.StageLabel;
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
     * Test to delete a populated plate.
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
    	//method already tested in PixelsServiceTest
    	//make sure objects are loaded.
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
	    	sb = new StringBuilder();
	    	sb.append("select i from Channel i ");
	    	sb.append("where i.id = :id");
	    	channel = (Channel) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(channel);
		}
    	i = infos.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb = new StringBuilder();
	    	sb.append("select i from StatsInfo i ");
	    	sb.append("where i.id = :id");
	    	info = (StatsInfo) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(info);
		}
    	i = logicalChannels.iterator();
    	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
	    	param.addId(id);
	    	sb = new StringBuilder();
	    	sb.append("select i from LogicalChannel i ");
	    	sb.append("where i.id = :id");
	    	lc = (LogicalChannel) iQuery.findByQuery(sb.toString(), param);
	    	assertNull(lc);
		}
    }

    /**
     * Test to delete an image with rendering settings.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithRenderingSettings() 
    	throws Exception
    {
    	Image image = createImage();
    	Pixels pixels = image.getPrimaryPixels();
    	//method already tested in RenderingSettingsServiceTest
    	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(pixels.getId().getValue());
    	prx.resetDefaultsInSet(Pixels.class.getName(), ids);
    	//check if we have settings now.
    	ParametersI param = new ParametersI();
    	param.addLong("pid", pixels.getId().getValue());
    	String sql = "select rdef from RenderingDef as rdef " +
    			"where rdef.pixels.id = :pid";
    	List<IObject> settings = iQuery.findAllByQuery(sql, param);
    	//now delete the image
    	assertTrue(settings.size() > 0);
    	iDelete.deleteImage(image.getId().getValue(), false); //do not force.
    	//check if the settings have been deleted.
    	Iterator<IObject> i = settings.iterator();
    	IObject o;
    	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			o = iQuery.findByQuery(sql, param);
			assertNull(o);
		}
    }
    
    /**
     * Test to delete an image with annotations that cannot be shared
     * e.g. boolean, comments.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageNonSharableAnnotations() 
    	throws Exception
    {
    	Image img = createImage();
    	long imageId = img.getId().getValue();
    	CommentAnnotation annotation = new CommentAnnotationI();
    	annotation.setTextValue(omero.rtypes.rstring("comment"));
    	annotation = (CommentAnnotation) iUpdate.saveAndReturnObject(annotation);
    	long annotationId = annotation.getId().getValue();
    	ImageAnnotationLink link = new ImageAnnotationLinkI();
    	link.setChild(annotation);
    	link.setParent(img);
    	link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
    	iDelete.deleteImage(imageId, true);
    	//check the annotation linked has been removed.
    	ParametersI param = new ParametersI();
    	param.addId(link.getId().getValue());
    	String sql = "select l from ImageAnnotationLink as l where l.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	//annotation should be deleted but not in this version
    	/*
    	param.addId(annotationId);
    	sql = "select l from Annotation as l where l.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	*/
    }
    
    /**
     * Test to delete an image with acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithAcquisitionData() 
    	throws Exception
    {
    	Image img = createImage();
    	Pixels pixels = img.getPrimaryPixels();
    	long pixId = pixels.getId().getValue();
    	//method already tested in PixelsServiceTest
    	//make sure objects are loaded.
    	pixels = factory.getPixelsService().retrievePixDescription(pixId);
    	//create an instrument.
    	Instrument instrument = createInstrument(LASER);
    	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
    	assertNotNull(instrument);

    	//retrieve the elements we need for the settings.
    	//retrieve the detector.
    	ParametersI param = new ParametersI();
    	param.addLong("iid", instrument.getId().getValue());
    	String sql = "select d from Detector as d where d.instrument.id = :iid";
    	Detector detector = (Detector) iQuery.findByQuery(sql, param);
    	sql = "select d from FilterSet as d where d.instrument.id = :iid";
    	FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
    	sql = "select d from Laser as d where d.instrument.id = :iid";
    	Laser laser = (Laser) iQuery.findByQuery(sql, param);
    	sql = "select d from Dichroic as d where d.instrument.id = :iid";
    	Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
    	sql = "select d from OTF as d where d.instrument.id = :iid";
    	OTF otf = (OTF) iQuery.findByQuery(sql, param);
    	sql = "select d from Objective as d where d.instrument.id = :iid";
    	Objective objective = (Objective) iQuery.findByQuery(sql, param);
    	
    	img.setImagingEnvironment(createImageEnvironment());
    	img.setObjectiveSettings(createObjectiveSettings(objective));
    	img.setStageLabel(createStageLabel());
    	iUpdate.saveAndReturnObject(img);
    	param = new ParametersI();
    	param.acquisitionData();
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(img.getId().getValue());
    	//method already tested in PojosService test
    	List results = factory.getContainerService().getImages(
    			Image.class.getName(), ids, param);
    	img = (Image) results.get(0);
    	ObjectiveSettings settings = img.getObjectiveSettings();
    	StageLabel label = img.getStageLabel();
    	ImagingEnvironment env = img.getImagingEnvironment();
    	
    	LogicalChannel lc;
    	Channel channel;
    	ids = new ArrayList<Long>();
    	long detectorSettingsID = 0;
    	long lightSourceSettingsID = 0;
    	long ligthPathID = 0;
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = pixels.getChannel(i);
			lc = channel.getLogicalChannel();
			lc.setOtf(otf);
	    	lc.setDetectorSettings(createDetectorSettings(detector));
	    	lc.setFilterSet(filterSet);
	    	lc.setLightSourceSettings(createLightSettings(laser));
	    	lc.setLightPath(createLightPath(null, dichroic, null));
	    	lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
	    	assertNotNull(lc);
	    	ids.add(lc.getId().getValue());
	    	detectorSettingsID = lc.getDetectorSettings().getId().getValue();
	    	lightSourceSettingsID = 
	    		lc.getLightSourceSettings().getId().getValue();
	    	ligthPathID = lc.getLightPath().getId().getValue();
		}
    	
    	//Now we try to delete the image.
    	iDelete.deleteImage(img.getId().getValue(), true);
    	//Follow the section with acquisition data.
    	//Now check if the settings are still there.
    	/*
    	param = new ParametersI();
    	param.addId(detectorSettingsID);
    	sql = "select d from DetectorSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(lightSourceSettingsID);
    	sql = "select d from LightSourceSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(ligthPathID);
    	sql = "select d from LightPath as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	//instrument
    	param.addId(instrument.getId().getValue());
    	sql = "select d from Instrument as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	param.addId(detector.getId().getValue());
    	sql = "select d from Detector as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(otf.getId().getValue());
    	sql = "select d from OTF as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(objective.getId().getValue());
    	sql = "select d from Objective as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(dichroic.getId().getValue());
    	sql = "select d from Dichroic as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(filterSet.getId().getValue());
    	sql = "select d from FilterSet as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(laser.getId().getValue());
    	sql = "select d from Laser as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(settings.getId().getValue());
    	sql = "select d from ObjectiveSettings as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(env.getId().getValue());
    	sql = "select d from ImagingEnvironment as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param.addId(env.getId().getValue());
    	sql = "select d from StageLabel as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	*/
    }
    
}
