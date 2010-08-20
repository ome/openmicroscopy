/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import static omero.rtypes.rdouble;
import static omero.rtypes.rint;

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
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
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
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StageLabel;
import omero.model.StatsInfo;
import omero.model.Well;
import omero.model.WellSample;
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
    public void testDeleteEmptyPlate() 
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
     * The boolean flag indicates to create or no plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeletePlate() 
    	throws Exception
    {
    	Boolean[] values = {Boolean.valueOf(false)};//, Boolean.valueOf(true)};
    	Boolean b;
    	Plate p;
    	List results;
    	PlateAcquisition pa = null;
    	StringBuilder sb;
    	Well well;
    	WellSample field;
    	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;
    	for (int i = 0; i < values.length; i++) {
			b = values[i];
			p = (Plate) iUpdate.saveAndReturnObject(
	    			createPlate(1, 1, 1, b));
			param = new ParametersI();
			param.addLong("plateID", p.getId().getValue());
			sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
	        sb.append("where pt.id = :plateID");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        
	        sb = new StringBuilder();
	        sb.append("select pa from PlateAcquisition as pa " +
	        		"where pa.plate.id = :plateID"); 
	        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
	        
	        j = results.iterator();
	        wellSampleIds = new ArrayList<Long>();
	        imageIds = new ArrayList<Long>();
	        while (j.hasNext()) {
				well = (Well) j.next();
				for (int k = 0; k < well.sizeOfWellSamples(); k++) {
					field = well.getWellSample(k);
					wellSampleIds.add(field.getId().getValue());
					assertNotNull(field.getImage());
					imageIds.add(field.getImage().getId().getValue());
				}
			}
	        //Now delete the plate
	        iDelete.deletePlate(p.getId().getValue());
	        
	        param = new ParametersI();
	        param.addId(p.getId().getValue());
	        sb = new StringBuilder();
	        //check the plate
	        sb.append("select p from Plate as p where p.id = :id");
	        assertNull(iQuery.findByQuery(sb.toString(), param));
	        
	        //check the well
	        param = new ParametersI();
	        param.addLong("plateID", p.getId().getValue());
	        sb = new StringBuilder();
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("where pt.id = :plateID");
			results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the well samples.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(wellSampleIds);
	        sb.append("select p from WellSample as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        
	        //check the image.
	        sb = new StringBuilder();
	        param = new ParametersI();
	        param.addIds(imageIds);
	        sb.append("select p from Image as p where p.id in (:ids)");
	        results = iQuery.findAllByQuery(sb.toString(), param);
	        assertTrue(results.size() == 0);
	        if (pa != null && b) {
	        	param = new ParametersI();
		        param.addId(pa.getId().getValue());
		        sb = new StringBuilder();
		        //check the plate
		        sb.append("select p from PlateAcquisition as p " +
		        		"where p.id = :id");
		        assertNull(iQuery.findByQuery(sb.toString(), param));
	        }
		}
    }

    /**
     * Tests to delete a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteDataset() 
    	throws Exception
    {
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Image image1 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Image image2 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	List<IObject> links = new ArrayList<IObject>();
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image1);
    	link.setParent(d);
    	links.add(link);
    	
    	link = new DatasetImageLinkI();
    	link.setChild(image2);
    	link.setParent(d);
    	links.add(link);
    	
    	iUpdate.saveAndReturnArray(links);
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image1.getId().getValue());
    	ids.add(image2.getId().getValue());
    	//new call to delete the dataset
    	
    	//Check if objects have been deleted
    	/*
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Image as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(d.getId().getValue());
    	sql = "select i from Dataset as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	*/
    }
    
    /**
     * Tests to delete a project containing a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteProject() 
    	throws Exception
    {
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			simpleProjectData().asIObject());
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			simpleDatasetData().asIObject());
    	Image image1 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Image image2 = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	List<IObject> links = new ArrayList<IObject>();
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild(image1);
    	link.setParent(d);
    	links.add(link);
    	
    	link = new DatasetImageLinkI();
    	link.setChild(image2);
    	link.setParent(d);
    	links.add(link);
    	
    	ProjectDatasetLink l = new ProjectDatasetLinkI();
    	l.setChild(d);
    	l.setParent(p);
    	links.add(l);
    	iUpdate.saveAndReturnArray(links);
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(image1.getId().getValue());
    	ids.add(image2.getId().getValue());
    	//new call to delete the project
    	
    	//Check if objects have been deleted
    	/*
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Image as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(d.getId().getValue());
    	sql = "select i from Dataset as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	
    	param = new ParametersI();
    	param.addId(p.getId().getValue());
    	sql = "select i from Project as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	*/
    }
    
    /**
     * Tests to delete a screen containing 2 plates, one w/o plate acquisition
     * and one with plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteScreen() 
    	throws Exception
    {
    	Screen screen = (Screen) iUpdate.saveAndReturnObject(
    			simpleScreenData().asIObject());
    	Plate p1 = (Plate) iUpdate.saveAndReturnObject(
    			createPlate(1, 1, 1, false)); //w/o plate acquisition
    	Plate p2 = (Plate) iUpdate.saveAndReturnObject(
    			createPlate(1, 1, 1, true)); //with plate acquisition
    	List<IObject> links = new ArrayList<IObject>();
    	ScreenPlateLink link = new ScreenPlateLinkI();
    	link.setChild(p1);
    	link.setParent(screen);
    	links.add(link);
    	link = new ScreenPlateLinkI();
    	link.setChild(p2);
    	link.setParent(screen);
    	links.add(link);
    	iUpdate.saveAndReturnArray(links);
    	
    	//Delete the screen
    	
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(p1.getId().getValue());
    	ids.add(p2.getId().getValue());
    	
    	//Check if the plates exist.
    	/*
    	ParametersI param = new ParametersI();
    	param.addIds(ids);
    	String sql = "select i from Plate as i where i.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	
    	param = new ParametersI();
    	param.addId(screen.getId().getValue());
    	sql = "select i from Screen as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));
    	*/
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
    public void testDeleteImageWithNonSharableAnnotations() 
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
    
    /**
     * Test to delete an image with ROis.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithROIs() 
    	throws Exception
    {
    	Image image = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Roi roi = new RoiI();
    	roi.setImage(image);
    	Rect rect;
    	Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    	for (int i = 0; i < 3; i++) {
    		rect = new RectI();
    		rect.setX(rdouble(10));
    		rect.setY(rdouble(10));
    		rect.setWidth(rdouble(10));
    		rect.setHeight(rdouble(10));
    		rect.setTheZ(rint(i));
    		rect.setTheT(rint(0));
    		serverROI.addShape(rect);
    	}
    	serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
    	List<Long> shapeIds = new ArrayList<Long>();
    	Shape shape;
    	for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
    		shape = serverROI.getShape(i);
    		shapeIds.add(shape.getId().getValue());
    	}
    	//Delete the image.
    	iDelete.deleteImage(image.getId().getValue(), true);
    	//check if the objects have been delete.
    	/*
    	ParametersI param = new ParametersI();
    	param.addId(serverROI.getId().getValue());
    	String sql = "select d from Roi as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));  
    	
    	//shapes
    	param = new ParametersI();
    	param.addIds(shapeIds);
    	sql = "select d from Shape as d where d.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	*/
    }
    
    /**
     * Test to deletes rois.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteROIs() 
    	throws Exception
    {
    	Image image = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
    	Roi roi = new RoiI();
    	roi.setImage(image);
    	Rect rect;
    	Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    	for (int i = 0; i < 3; i++) {
    		rect = new RectI();
    		rect.setX(rdouble(10));
    		rect.setY(rdouble(10));
    		rect.setWidth(rdouble(10));
    		rect.setHeight(rdouble(10));
    		rect.setTheZ(rint(i));
    		rect.setTheT(rint(0));
    		serverROI.addShape(rect);
    	}
    	serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
    	List<Long> shapeIds = new ArrayList<Long>();
    	Shape shape;
    	for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
    		shape = serverROI.getShape(i);
    		shapeIds.add(shape.getId().getValue());
    	}
    	
    	//delete the rois.
    	
    	//make sure we still have the image
    	/*
    	ParametersI param = new ParametersI();
    	param.addId(image.getId().getValue());
    	String sql = "select d from Image as d where d.id = :id";
    	assertNotNull(iQuery.findByQuery(sql, param));  
    	
    	//check if the objects have been delete.
    	ParametersI param = new ParametersI();
    	param.addId(serverROI.getId().getValue());
    	sql = "select d from Roi as d where d.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));  
    	
    	//shapes
    	param = new ParametersI();
    	param.addIds(shapeIds);
    	sql = "select d from Shape as d where d.id in (:ids)";
    	List results = iQuery.findAllByQuery(sql, param);
    	assertTrue(results.size() == 0);
    	*/
    }
    
}
