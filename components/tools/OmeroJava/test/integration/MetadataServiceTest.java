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
import java.util.Map;
import java.util.UUID;


//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Correction;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.Detector;
import omero.model.DetectorI;
import omero.model.DetectorType;
import omero.model.Dichroic;
import omero.model.DichroicI;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Filter;
import omero.model.FilterI;
import omero.model.FilterType;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.Immersion;
import omero.model.Instrument;
import omero.model.InstrumentI;
import omero.model.Laser;
import omero.model.LaserI;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.MicroscopeI;
import omero.model.MicroscopeType;
import omero.model.Objective;
import omero.model.ObjectiveI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.PermissionsI;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectI;
import omero.model.Pulse;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TransmittanceRangeI;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.BooleanAnnotationData;
import pojos.DoubleAnnotationData;
import pojos.FileAnnotationData;
import pojos.LongAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * Collections of tests for the <code>IMetadata</code> service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MetadataServiceTest 
	extends AbstractTest//TestCase 
{

	/** Identifies the file annotation. */
	private static final String FILE_ANNOTATION = 
		"ome.model.annotations.FileAnnotation";
	
	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(MetadataServiceTest.class);

    /** Helper reference to the <code>IAdmin</code> service. */
    private IMetadataPrx iMetadata;
    
    /**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
        super.setUp();
        iMetadata = factory.getMetadataService();
    }
    
    /**
     * Tests the creation of file annotation with an original file
     * and load it. Loads the annotation using the <code>loadAnnotation</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadFileAnnotation() 
    	throws Exception
    {
		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				createOriginalFile());
		assertNotNull(of);
		
		FileAnnotationI fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
		
		List<Long> ids = new ArrayList<Long>();
		ids.add(data.getId().getValue());
		List<Annotation> annotations = iMetadata.loadAnnotation(ids);
		assertNotNull(annotations);
		Iterator<Annotation> i = annotations.iterator();
		Annotation annotation;
		FileAnnotationData faData;
		while (i.hasNext()) {
			annotation = i.next();
			if (annotation instanceof FileAnnotation) { //test creation of pojos
				faData = new FileAnnotationData((FileAnnotation) annotation);
				assertNotNull(faData);
				assertTrue(faData.getFileID() == of.getId().getValue());
			}
		}
    }

    /**
     * Tests the creation of file annotation with an original file
     * and load it. Loads the annotation using the <code>loadAnnotations</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadAnnotationsFileAnnotation() 
    	throws Exception
    {
		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				createOriginalFile());
		assertNotNull(of);

		FileAnnotationI fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
		//link the image 
		  //create an image and link the annotation
        Image image = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setParent(image);
        link.setChild(data);
        iUpdate.saveAndReturnObject(link);
        
        List<String> types = new ArrayList<String>();
        types.add(FILE_ANNOTATION);
        List<Long> ids = new ArrayList<Long>();
        Parameters param = new Parameters();
        List<Long> nodes = new ArrayList<Long>();
        nodes.add(image.getId().getValue());
        Map<Long, List<IObject>> result = 
        	iMetadata.loadAnnotations(Image.class.getName(), nodes, types, ids, 
        		param);
        assertNotNull(result);
        List<IObject> l = result.get(image.getId().getValue());
        assertNotNull(l);
        Iterator<IObject> i = l.iterator();
        IObject o;
        FileAnnotationData faData;
        while (i.hasNext()) {
			o = i.next();
			if (o instanceof FileAnnotation) {
				faData = new FileAnnotationData((FileAnnotation) o);
				assertNotNull(faData);
				assertTrue(faData.getFileID() == of.getId().getValue());
			}
		}
    }
    
    /**
     * Tests the creation of file annotation with an original file
     * and load it. Loads the annotation using the 
     * <code>loadSpecifiedAnnotations</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadSpecifiedAnnotationsFileAnnotation() 
    	throws Exception
    {
		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				createOriginalFile());
		assertNotNull(of);

		FileAnnotationI fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
		
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
        		FileAnnotation.class.getName(), 
        		include, exclude, param);
        assertNotNull(result);
       
        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        int count = 0;
        while (i.hasNext()) {
			o = i.next();
			if (o != null && o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				count++;
				if (r.getId().getValue() == data.getId().getValue()) {
					assertTrue(r.getFile().getId().getValue() 
							== of.getId().getValue());
				}
			}
		}
        assertTrue(count == result.size());
    }
    
    /**
     * Tests the retrieval of annotations using name space constraints.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationNsConditions() 
    	throws Exception
    {
		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				createOriginalFile());
		assertNotNull(of);

		String ns = "include";
		FileAnnotationI fa = new FileAnnotationI();
		fa.setFile(of);
		fa.setNs(omero.rtypes.rstring(ns));
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
		
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        include.add(ns);
        List<String> exclude = new ArrayList<String>();
        
        //First test the include condition
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
        		FileAnnotation.class.getName(), 
        		include, exclude, param);
        assertNotNull(result);
       
        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        while (i.hasNext()) {
			o = i.next();
			if (o != null && o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				assertNotNull(r.getNs());
				assertTrue(ns.equals(r.getNs().getValue()));
			}
		}
        
        //now test the exclude condition
        include.clear();
        exclude.add(ns);
        result = iMetadata.loadSpecifiedAnnotations(
        		FileAnnotation.class.getName(), 
        		include, exclude, param);
        assertNotNull(result);
        
        i = result.iterator();
        int count = 0;
        while (i.hasNext()) {
			o = i.next();
			if (o != null && o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				if (r.getNs() != null) {
					if (ns.equals(r.getNs().getValue())) count++;
				}
			}
		}
        assertTrue(count == 0);
    }
   
    /**
     * Tests the retrieval of annotations of different types i.e.
     * tag, comment, boolean, long and the conversion into the corresponding
     * <code>POJOS</code> object.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsVariousTypes() 
    	throws Exception
    {
    	TagAnnotationI tag = new TagAnnotationI();
    	tag.setTextValue(omero.rtypes.rstring("tag"));
    	TagAnnotation tagReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	Parameters param = new Parameters();
    	List<String> include = new ArrayList<String>();
    	List<String> exclude = new ArrayList<String>();

    	//First test the include condition
    	List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
    			TagAnnotation.class.getName(), include, exclude, param);
    	assertNotNull(result);

    	Iterator<Annotation> i = result.iterator();
    	int count = 0;
    	TagAnnotationData tagData = null;
    	Annotation annotation;
    	while (i.hasNext()) {
    		annotation = i.next();
    		if (annotation instanceof TagAnnotation) count++;
    		if (annotation.getId().getValue() == tagReturned.getId().getValue())
    			tagData = new TagAnnotationData(tagReturned);
    		
    	}
    	assertTrue(result.size() == count);
    	assertNotNull(tagData);
    	//comment
    	CommentAnnotationI comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment"));
    	CommentAnnotation commentReturned = 
    		(CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	result = iMetadata.loadSpecifiedAnnotations(
    			CommentAnnotation.class.getName(), include, exclude, param);
    	assertNotNull(result);
    	count = 0;
    	TextualAnnotationData commentData = null;
    	i = result.iterator();
    	while (i.hasNext()) {
    		annotation = i.next();
    		if (annotation instanceof CommentAnnotation) count++;
    		if (annotation.getId().getValue() == 
    			commentReturned.getId().getValue())
    			commentData = new TextualAnnotationData(commentReturned);
    	}
    	assertTrue(result.size() == count);
    	assertNotNull(commentData);
    	
    	//boolean
    	BooleanAnnotationI bool = new BooleanAnnotationI();
    	bool.setBoolValue(omero.rtypes.rbool(true));
    	BooleanAnnotation boolReturned = 
    		(BooleanAnnotation) iUpdate.saveAndReturnObject(bool);
    	result = iMetadata.loadSpecifiedAnnotations(
    			BooleanAnnotation.class.getName(), include, exclude, param);
    	assertNotNull(result);
    	count = 0;
    	BooleanAnnotationData boolData = null;
    	i = result.iterator();
    	while (i.hasNext()) {
    		annotation = i.next();
    		if (annotation instanceof BooleanAnnotation) count++;
    		if (annotation.getId().getValue() == 
    			boolReturned.getId().getValue())
    			boolData = new BooleanAnnotationData(boolReturned);
    	}
    	assertTrue(result.size() == count);
    	assertNotNull(boolData);
    	
    	//long
    	LongAnnotationI l = new LongAnnotationI();
    	l.setLongValue(omero.rtypes.rlong(1));
    	LongAnnotation lReturned = 
    		(LongAnnotation) iUpdate.saveAndReturnObject(l);
    	result = iMetadata.loadSpecifiedAnnotations(
    			LongAnnotation.class.getName(), include, exclude, param);
    	assertNotNull(result);
    	count = 0;
    	LongAnnotationData lData = null;
    	i = result.iterator();
    	while (i.hasNext()) {
    		annotation = i.next();
    		if (annotation instanceof LongAnnotation) count++;
    		if (annotation.getId().getValue() == 
    			lReturned.getId().getValue())
    			lData = new LongAnnotationData(lReturned);
    	}
    	assertTrue(result.size() == count);
    	assertNotNull(lData);
    	//double
    	DoubleAnnotationI d = new DoubleAnnotationI();
    	d.setDoubleValue(omero.rtypes.rdouble(1));
    	DoubleAnnotation dReturned = 
    		(DoubleAnnotation) iUpdate.saveAndReturnObject(d);
    	result = iMetadata.loadSpecifiedAnnotations(
    			DoubleAnnotation.class.getName(), include, exclude, param);
    	assertNotNull(result);
    	count = 0;
    	DoubleAnnotationData dData = null;
    	i = result.iterator();
    	while (i.hasNext()) {
    		annotation = i.next();
    		if (annotation instanceof DoubleAnnotation) count++;
    		if (annotation.getId().getValue() == 
    			dReturned.getId().getValue())
    			dData = new DoubleAnnotationData(dReturned);
    	}
    	assertTrue(result.size() == count);
    	assertNotNull(dData);
    }
    
    /**
     * Tests the retrieval of tag sets
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSetsNoOrphan() 
    	throws Exception
    {
    	long self = iAdmin.getEventContext().userId;
    	
    	//Create a tag set.
    	TagAnnotationI tagSet = new TagAnnotationI();
    	tagSet.setTextValue(omero.rtypes.rstring("tagSet"));
    	tagSet.setNs(omero.rtypes.rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
    	TagAnnotation tagSetReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tagSet);
    	//create a tag and link it to the tag set
    	TagAnnotationI tag = new TagAnnotationI();
    	tag.setTextValue(omero.rtypes.rstring("tag"));
    	TagAnnotation tagReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
    	link.setChild(tagReturned);
    	link.setParent(tagSetReturned);
    	//save the link.
    	AnnotationAnnotationLinkI linkReturned = 
    		(AnnotationAnnotationLinkI )iUpdate.saveAndReturnObject(link); 
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.noOrphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	IObject o;
    	int count = 0;
    	AnnotationAnnotationLinkI l;
    	while (i.hasNext()) {
			o = i.next();
			if (o instanceof AnnotationAnnotationLinkI) {
				l = (AnnotationAnnotationLinkI) o;
				if (l.getId().getValue() == linkReturned.getId().getValue()) {
					assertTrue(l.getChild().getId().getValue()
							== tagReturned.getId().getValue());
					assertTrue(l.getParent().getId().getValue()
							== tagSetReturned.getId().getValue());					
				}
				count++;
			}
		}
    	assertTrue(count == result.size());
    }
    
    /**
     * Tests the retrieval of annotations used but not owned.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadAnnotationsUsedNotOwned() 
    	throws Exception
    {
    	//
    	IAdminPrx svc = root.getSession().getAdminService();
    	String uuid = UUID.randomUUID().toString();
    	String uuid2 = UUID.randomUUID().toString();
    	Experimenter e1 = new ExperimenterI();
    	e1.setOmeName(omero.rtypes.rstring(uuid));
    	e1.setFirstName(omero.rtypes.rstring("integeration"));
    	e1.setLastName(omero.rtypes.rstring("tester"));
    	Experimenter e2 = new ExperimenterI();
    	e2.setOmeName(omero.rtypes.rstring(uuid2));
    	e2.setFirstName(omero.rtypes.rstring("integeration"));
    	e2.setLastName(omero.rtypes.rstring("tester"));
    	
    	ExperimenterGroup g = new ExperimenterGroupI();
    	g.setName(omero.rtypes.rstring(uuid));
    	g.getDetails().setPermissions(new PermissionsI("rwrw--"));
    	svc.createGroup(g);
    	long id1 = svc.createUser(e1, uuid);
    	long id2 = svc.createUser(e2, uuid);
    	client = new omero.client();
        ServiceFactoryPrx f = client.createSession(uuid2, uuid2);
    	//Create a tag annotation as another user.
        TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));
        IObject tagData = f.getUpdateService().saveAndReturnObject(tag);
        assertNotNull(tagData);
        //make sure we are not the owner of the tag.
        assertTrue(tagData.getDetails().getOwner().getId().getValue() == id2);
    	client.closeSession();
    	
        f = client.createSession(uuid, uuid);
        //Create an image.
       Image img = (Image) f.getUpdateService().saveAndReturnObject(
    		   simpleImage(0));
       //Link the tag and the image.
       ImageAnnotationLinkI link = new ImageAnnotationLinkI();
       link.setChild((Annotation) tagData);
       link.setParent(img);
       //Save the link
       f.getUpdateService().saveAndReturnObject(link);

       List<IObject> result = f.getMetadataService().loadAnnotationsUsedNotOwned(
    		   TagAnnotation.class.getName(), id1);
       assertTrue(result.size() > 0);
       Iterator<IObject> i = result.iterator();
       IObject o;
       int count = 0;
       boolean found = false;
       while (i.hasNext()) {
    	   o = i.next();
    	   if (o instanceof Annotation) { //make sure only retrieve annotations
    		   count++;
    		   if (o.getId().getValue() == tagData.getId().getValue())
    			   found = true;
    	   }
       }
       assertTrue(found);
       assertTrue(result.size() == count);
       client.closeSession();
    }
    
    /**
     * Tests the retrieval of object linked to a given tag.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadTagContent() 
    	throws Exception
    {
    	TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));
        Annotation tagData = (Annotation) iUpdate.saveAndReturnObject(tag);
    	
        Image img = (Image) iUpdate.saveAndReturnObject(simpleImage(0));
        //Link the tag and the image.
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setChild(tagData);
        link.setParent(img);
        iUpdate.saveAndReturnObject(link);
        
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("project1"));
        Project pData = (Project) iUpdate.saveAndReturnObject(p);
        ProjectAnnotationLinkI lp = new ProjectAnnotationLinkI();
        lp.setChild((Annotation) tagData.proxy());
        lp.setParent(p);
        iUpdate.saveAndReturnObject(lp);
        
        Dataset d = new DatasetI();
        d.setName(omero.rtypes.rstring("datatset"));
        Dataset dData = (Dataset) iUpdate.saveAndReturnObject(d);
        DatasetAnnotationLinkI dp = new DatasetAnnotationLinkI();
        dp.setChild((Annotation) tagData.proxy());
        dp.setParent(d);
        iUpdate.saveAndReturnObject(dp);
        
        long self = iAdmin.getEventContext().userId;
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(tagData.getId().getValue());
    	Map result = iMetadata.loadTagContent(ids, param);
    	assertNotNull(result);
    	List nodes = (List) result.get(tagData.getId().getValue());
    	assertNotNull(nodes);
    	Iterator<IObject> i = nodes.iterator();
    	IObject o;
    	int count = 0;
    	while (i.hasNext()) {
			o = i.next();
			if (o instanceof Image) {
				if (o.getId().getValue() == img.getId().getValue()) count++;
			} else if (o instanceof Dataset) {
				if (o.getId().getValue() == dData.getId().getValue()) count++;
			} else if (o instanceof Project) {
				if (o.getId().getValue() == pData.getId().getValue()) count++;
			}
		}
    	assertNotNull(count == result.size());
    }
    
    /**
     * Tests the retrieval of an instrument.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrument() 
    	throws Exception
    {
    	//retrieve the microscope type
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = svc.getAllEnumerations(
    			MicroscopeType.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	//create an instrument
    	InstrumentI instrument = new InstrumentI();
    	MicroscopeI microscope = new MicroscopeI();
    	microscope.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	microscope.setModel(omero.rtypes.rstring("model"));
    	microscope.setSerialNumber(omero.rtypes.rstring("number"));
    	microscope.setType((MicroscopeType) types.get(0));
    	instrument.setMicroscope(microscope);
    	Instrument instrumentReturned = (Instrument) 
    		iUpdate.saveAndReturnObject(instrument);
    	assertNotNull(instrumentReturned);
    	//Detector
    	types = svc.getAllEnumerations(DetectorType.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	DetectorI detector = new DetectorI();
    	detector.setAmplificationGain(omero.rtypes.rdouble(0));
    	detector.setGain(omero.rtypes.rdouble(1));
    	detector.setInstrument((Instrument) instrumentReturned.proxy());
    	detector.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	detector.setModel(omero.rtypes.rstring("model"));
    	detector.setSerialNumber(omero.rtypes.rstring("number"));
    	detector.setOffsetValue(omero.rtypes.rdouble(0));
    	detector.setType((DetectorType) types.get(0));
    	
    	Detector detectorReturned = 
    		(Detector) iUpdate.saveAndReturnObject(detector);
    	assertNotNull(detectorReturned);
    	
    	//Filter
    	types = svc.getAllEnumerations(FilterType.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	FilterI filter = new FilterI();
    	filter.setLotNumber(omero.rtypes.rstring("lot number"));
    	filter.setInstrument((Instrument) instrumentReturned.proxy());
    	filter.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	filter.setModel(omero.rtypes.rstring("model"));
    	filter.setType((FilterType) types.get(0));
    	TransmittanceRangeI transmittance = new TransmittanceRangeI();
    	transmittance.setCutIn(omero.rtypes.rint(500));
    	transmittance.setCutOut(omero.rtypes.rint(560));
    	filter.setTransmittanceRange(transmittance);
    	Filter filterReturned = (Filter) iUpdate.saveAndReturnObject(filter);
    	assertNotNull(filterReturned);
    	
    	//Dichroic
    	DichroicI dichroic = new DichroicI();
    	dichroic.setInstrument((Instrument) instrumentReturned.proxy());
    	dichroic.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	dichroic.setModel(omero.rtypes.rstring("model"));
    	dichroic.setLotNumber(omero.rtypes.rstring("lot number"));
    	
    	Dichroic dichroicReturned = 
    		(Dichroic) iUpdate.saveAndReturnObject(dichroic);
    	assertNotNull(dichroicReturned);
    	
    	//Objectives
    	ObjectiveI objective = new ObjectiveI();
    	objective.setInstrument((Instrument) instrumentReturned.proxy());
    	objective.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	objective.setModel(omero.rtypes.rstring("model"));
    	objective.setCalibratedMagnification(omero.rtypes.rdouble(1));
    	
    	//correction
    	types = svc.getAllEnumerations(Correction.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	objective.setCorrection((Correction) types.get(0));
    	//immersion
    	types = svc.getAllEnumerations(Immersion.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	objective.setImmersion((Immersion) types.get(0));
    	
    	objective.setIris(omero.rtypes.rbool(true));
    	objective.setLensNA(omero.rtypes.rdouble(0.5));
    	objective.setNominalMagnification(omero.rtypes.rint(1));
    	objective.setWorkingDistance(omero.rtypes.rdouble(1));
    	
    	Objective objectiveReturned = 
    		(Objective) iUpdate.saveAndReturnObject(objective);
    	assertNotNull(objectiveReturned);
    	
    	//light source
    	//laser
    	LaserI laser = new LaserI();
    	laser.setInstrument((Instrument) instrumentReturned.proxy());
    	laser.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	laser.setModel(omero.rtypes.rstring("model"));
    	laser.setFrequencyMultiplication(omero.rtypes.rint(1));
    	// type
    	types = svc.getAllEnumerations(LaserType.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	laser.setType((LaserType) types.get(0));
    	//laser medium
    	types = svc.getAllEnumerations(LaserMedium.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	laser.setLaserMedium((LaserMedium) types.get(0));
    	
    	//pulse
    	types = svc.getAllEnumerations(Pulse.class.getName());
    	assertNotNull(types);
    	assertTrue(types.size() > 0);
    	laser.setPulse((Pulse) types.get(0));
    	
    	laser.setFrequencyMultiplication(omero.rtypes.rint(0));
    	laser.setPockelCell(omero.rtypes.rbool(false));
    	laser.setPower(omero.rtypes.rdouble(0));
    	laser.setRepetitionRate(omero.rtypes.rdouble(1));
    	
    	Laser laserReturned = (Laser) iUpdate.saveAndReturnObject(laser);
    	assertNotNull(laserReturned);
    	
    	//Load the instrument
    	List<IObject> result = iMetadata.loadInstrument(
    			instrumentReturned.getId().getValue());
    	assertNotNull(result);
    	assertTrue(result.size() > 0);
    	Iterator<IObject> i = result.iterator();
    	IObject o;
    	boolean instrumentFound = false;
    	boolean objectiveFound = false;
    	boolean detectorFound = false;
    	boolean filterFound = false;
    	boolean dichroicFound = false;
    	boolean laserFound = false;
    	while (i.hasNext()) {
			o = i.next();
			if (o instanceof Instrument) {
				instrumentFound = true;
				assertTrue(o.getId().getValue() == 
					instrumentReturned.getId().getValue());
			} else if (o instanceof Detector) {
				detectorFound = true;
				assertTrue(o.getId().getValue() == 
					detectorReturned.getId().getValue());
			} else if (o instanceof Filter) {
				filterFound = true;
				assertTrue(o.getId().getValue() == 
					filterReturned.getId().getValue());
			} else if (o instanceof Dichroic) {
				dichroicFound = true;
				assertTrue(o.getId().getValue() == 
					dichroicReturned.getId().getValue());
			} else if (o instanceof Objective) {
				objectiveFound = true;
				assertTrue(o.getId().getValue() == 
					objectiveReturned.getId().getValue());
			} else if (o instanceof Laser) {
				laserFound = true;
				assertTrue(o.getId().getValue() == 
					laserReturned.getId().getValue());
			}
		}
    	assertTrue(instrumentFound);
    	assertTrue(objectiveFound);
    	assertTrue(detectorFound);
    	assertTrue(filterFound);
    	assertTrue(dichroicFound);
    	assertTrue(laserFound);
    }
    
    /**
     * Tests the retrieval of channel acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadChannelAcquisitionData() 
    	throws Exception
    {
    	
    }
    
}
