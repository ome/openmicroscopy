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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Filament;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightEmittingDiode;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
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
	extends AbstractTest 
{

	/** Identifies the file annotation. */
	private static final String FILE_ANNOTATION = 
		"ome.model.annotations.FileAnnotation";
	
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
				mmFactory.createOriginalFile());
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
				mmFactory.createOriginalFile());
		assertNotNull(of);

		FileAnnotationI fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data);
		//link the image 
		  //create an image and link the annotation
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
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
				mmFactory.createOriginalFile());
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
				mmFactory.createOriginalFile());
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
    	TagAnnotation tag = new TagAnnotationI();
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
    	CommentAnnotation comment = new CommentAnnotationI();
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
    	BooleanAnnotation bool = new BooleanAnnotationI();
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
    	LongAnnotation l = new LongAnnotationI();
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
    	DoubleAnnotation d = new DoubleAnnotationI();
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
    	TagAnnotation tagSet = new TagAnnotationI();
    	tagSet.setTextValue(omero.rtypes.rstring("tagSet"));
    	tagSet.setNs(omero.rtypes.rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
    	TagAnnotation tagSetReturned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tagSet);
    	//create a tag and link it to the tag set
    	TagAnnotation tag = new TagAnnotationI();
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
    	e1.setFirstName(omero.rtypes.rstring("integration"));
    	e1.setLastName(omero.rtypes.rstring("tester"));
    	Experimenter e2 = new ExperimenterI();
    	e2.setOmeName(omero.rtypes.rstring(uuid2));
    	e2.setFirstName(omero.rtypes.rstring("integration"));
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
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));
        IObject tagData = f.getUpdateService().saveAndReturnObject(tag);
        assertNotNull(tagData);
        //make sure we are not the owner of the tag.
        assertTrue(tagData.getDetails().getOwner().getId().getValue() == id2);
    	client.closeSession();
    	
        f = client.createSession(uuid, uuid);
        //Create an image.
        Image img = (Image) f.getUpdateService().saveAndReturnObject(
        		mmFactory.simpleImage(0));
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
    	TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));
        Annotation tagData = (Annotation) iUpdate.saveAndReturnObject(tag);
        Image img = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
        //Link the tag and the image.
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setChild(tagData);
        link.setParent(img);
        iUpdate.saveAndReturnObject(link);
        
        Project pData = (Project) iUpdate.saveAndReturnObject(
        		mmFactory.simpleProjectData().asIObject());
        ProjectAnnotationLinkI lp = new ProjectAnnotationLinkI();
        lp.setChild((Annotation) tagData.proxy());
        lp.setParent(pData);
        iUpdate.saveAndReturnObject(lp);
        
        Dataset dData = (Dataset) iUpdate.saveAndReturnObject(
        		mmFactory.simpleDatasetData().asIObject());
        DatasetAnnotationLinkI dp = new DatasetAnnotationLinkI();
        dp.setChild((Annotation) tagData.proxy());
        dp.setParent(dData);
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
     * Tests the retrieval of an instrument light sources of different types.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrument() 
    	throws Exception
    {
    	Instrument instrument;
    	Detector detector;
    	Dichroic dichroic;
    	Filter filter;
    	FilterSet filterSet;
    	OTF otf;
    	Laser laser = null;
    	Filament filament = null;
    	Arc arc = null;
    	LightEmittingDiode light = null;
    	Objective objective;
    	ParametersI param;
    	String sql;
    	IObject test;
    	List<IObject> result;
    	boolean instrumentFound = false;
    	boolean objectiveFound = false;
    	boolean detectorFound = false;
    	boolean filterFound = false;
    	boolean dichroicFound = false;
    	boolean lightFound = false;
    	boolean filterSetFound = false;
    	boolean otfFound = false;
    	Iterator<IObject> j;
    	IObject o;
    	for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
    		instrument = mmFactory.createInstrument(
    				ModelMockFactory.LIGHT_SOURCES[i]);
        	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        	assertNotNull(instrument);
    		param = new ParametersI();
        	param.addLong("iid", instrument.getId().getValue());
        	sql = "select d from Detector as d where d.instrument.id = :iid";
        	detector = (Detector) iQuery.findByQuery(sql, param);
            sql = "select d from Dichroic as d where d.instrument.id = :iid";
            dichroic = (Dichroic) iQuery.findByQuery(sql, param);
            sql = "select d from Filter as d where d.instrument.id = :iid";
            filter = (Filter) iQuery.findByQuery(sql, param);
            sql = "select d from Objective as d where d.instrument.id = :iid";
            objective = (Objective) iQuery.findByQuery(sql, param);
            sql = "select d from FilterSet as d where d.instrument.id = :iid";
            filterSet = (FilterSet) iQuery.findByQuery(sql, param);
            sql = "select d from OTF as d where d.instrument.id = :iid";
            otf = (OTF) iQuery.findByQuery(sql, param);
            if (ModelMockFactory.LASER.equals(
            		ModelMockFactory.LIGHT_SOURCES[i])) {
            	sql = "select d from Laser as d where d.instrument.id = :iid";
            	laser = (Laser) iQuery.findByQuery(sql, param);
            } else if (ModelMockFactory.FILAMENT.equals(
            		ModelMockFactory.LIGHT_SOURCES[i])) {
            	sql = "select d from Filament as d where d.instrument.id = :iid";
            	filament = (Filament) iQuery.findByQuery(sql, param);
            } else if (ModelMockFactory.ARC.equals(
            		ModelMockFactory.LIGHT_SOURCES[i])) {
            	sql = "select d from Arc as d where d.instrument.id = :iid";
            	arc = (Arc) iQuery.findByQuery(sql, param);
            } else if (ModelMockFactory.LIGHT_EMITTING_DIODE.equals(
            		ModelMockFactory.LIGHT_SOURCES[i])) {
            	sql = "select d from LightEmittingDiode as d where " +
            			"d.instrument.id = :iid";
            	light = (LightEmittingDiode) iQuery.findByQuery(sql, param);
            }

            result = iMetadata.loadInstrument(
        			instrument.getId().getValue());
        	assertNotNull(result);
        	assertTrue(result.size() > 0);
        	j = result.iterator();
        	while (j.hasNext()) {
    			o = j.next();
    			if (o instanceof Instrument) {
    				instrumentFound = true;
    				assertTrue(o.getId().getValue() == 
    					instrument.getId().getValue());
    			} else if (o instanceof Detector) {
    				detectorFound = true;
    				assertTrue(o.getId().getValue() == 
    					detector.getId().getValue());
    			} else if (o instanceof Filter) {
    				filterFound = true;
    				assertTrue(o.getId().getValue() == 
    					filter.getId().getValue());
    				filter = (Filter) o; 
    			} else if (o instanceof FilterSet) {
    				filterSetFound = true;
    				assertTrue(o.getId().getValue() == 
    					filterSet.getId().getValue());
    			} else if (o instanceof Dichroic) {
    				dichroicFound = true;
    				assertTrue(o.getId().getValue() == 
    					dichroic.getId().getValue());
    			} else if (o instanceof Objective) {
    				objectiveFound = true;
    				assertTrue(o.getId().getValue() == 
    					objective.getId().getValue());
    				objective = (Objective) o;
    			} else if (o instanceof Laser) {
    				assertNotNull(laser);
    				lightFound = true;
    				assertTrue(o.getId().getValue() == 
    					laser.getId().getValue());
    				laser = (Laser) o;
    			} else if (o instanceof Filament) {
    				assertNotNull(filament);
    				lightFound = true;
    				assertTrue(o.getId().getValue() == 
    					filament.getId().getValue());
    				filament = (Filament) o;
    			} else if (o instanceof Arc) {
    				assertNotNull(arc);
    				lightFound = true;
    				assertTrue(o.getId().getValue() == 
    					arc.getId().getValue());
    				arc = (Arc) o;
    			} else if (o instanceof LightEmittingDiode) {
    				assertNotNull(light);
    				lightFound = true;
    				assertTrue(o.getId().getValue() == 
    					light.getId().getValue());
    			} else if (o instanceof OTF) {
    				assertNotNull(otf);
    				otfFound = true;
    				assertTrue(o.getId().getValue() == 
    					otf.getId().getValue());
    			}
    		}
        	assertTrue(instrumentFound);
        	assertTrue(objectiveFound);
        	assertTrue(detectorFound);
        	assertTrue(filterFound);
        	assertTrue(dichroicFound);
        	assertTrue(lightFound);
        	assertTrue(filterSetFound);
        	//not yet implemented
        	//assertTrue(otfFound);
        	//make sure everything is loaded properly
        	//objective
        	assertNotNull(objective.getCorrection());
        	assertNotNull(objective.getImmersion());
        	//filter
        	assertNotNull(filter.getType());
        	assertNotNull(filter.getTransmittanceRange());
        	
        	//light source
        	if (laser != null) {
        		assertNotNull(laser.getType());
        		assertNotNull(laser.getLaserMedium());
        		assertNotNull(laser.getPulse());
        	}
        	if (filament != null) {
        		assertNotNull(filament.getType());
        	}
        	if (arc != null) {
        		assertNotNull(arc.getType());
        	}
        	if (otf != null) {
        		assertNotNull(otf.getFilterSet());
        		assertNotNull(otf.getObjective());
        		assertNotNull(otf.getPixelsType());
        		assertTrue(otf.getObjective().getId().getValue() == 
        			objective.getId().getValue());
        		assertTrue(otf.getFilterSet().getId().getValue() == 
        			filterSet.getId().getValue());
        	}
		}
    }
    
    /**
     * Tests the retrieval of an instrument several light sources.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrumentWithSimilarLightSources() 
    	throws Exception
    {
    	Instrument instrument;
    	String light;
    	instrument = mmFactory.createInstrument(
    			ModelMockFactory.LIGHT_SOURCES[0]);
    	for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
    		light = ModelMockFactory.LIGHT_SOURCES[i];
    		if (ModelMockFactory.LASER.equals(light))
        		instrument.addLightSource(mmFactory.createLaser());
        	else if (ModelMockFactory.FILAMENT.equals(light))
        		instrument.addLightSource(mmFactory.createFilament());
        	else if (ModelMockFactory.ARC.equals(light))
        		instrument.addLightSource(mmFactory.createArc());
        	else if (ModelMockFactory.LIGHT_EMITTING_DIODE.equals(light))
        		instrument.addLightSource(mmFactory.createLightEmittingDiode());
    	}
    	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
    	assertNotNull(instrument);
    	List<IObject> result = iMetadata.loadInstrument(
    			instrument.getId().getValue());
    	Iterator<IObject> i = result.iterator();
    	IObject object;
    	int count = 0;
    	while (i.hasNext()) {
    		object = i.next();
			if (object instanceof LightSource) count++;
		}
    	assertTrue(count == (ModelMockFactory.LIGHT_SOURCES.length+1));
    }
    
    /**
     * Tests the retrieval of channel acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadChannelAcquisitionData() 
    	throws Exception
    {
    	Image img = mmFactory.createImage();
    	img = (Image) iUpdate.saveAndReturnObject(img);
    	Pixels pixels = img.getPrimaryPixels();
    	long pixId = pixels.getId().getValue();
    	//method already tested in PixelsServiceTest
    	//make sure objects are loaded.
    	pixels = factory.getPixelsService().retrievePixDescription(pixId);
    	//create an instrument.
    	Instrument instrument = mmFactory.createInstrument(
    			ModelMockFactory.LASER);
    	//add second laser
    	instrument.addLightSource(mmFactory.createLaser());
    	instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
    	assertNotNull(instrument);
    	//retrieve the detector.
    	ParametersI param = new ParametersI();
    	param.addLong("iid", instrument.getId().getValue());
    	String sql = "select d from Detector as d where d.instrument.id = :iid";
    	Detector detector = (Detector) iQuery.findByQuery(sql, param);
    	sql = "select d from FilterSet as d where d.instrument.id = :iid";
    	FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
    	sql = "select d from Laser as d where d.instrument.id = :iid";
    	List<IObject> lasers =  iQuery.findAllByQuery(sql, param);
    	assertTrue(lasers.size() == 2);
    	Laser laser = (Laser) lasers.get(0);
    	
    	
    	sql = "select d from Dichroic as d where d.instrument.id = :iid";
    	Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
    	sql = "select d from Objective as d where d.instrument.id = :iid";
    	Objective objective = (Objective) iQuery.findByQuery(sql, param);
    	
    	sql = "select d from OTF as d where d.instrument.id = :iid";
    	OTF otf = (OTF) iQuery.findByQuery(sql, param);
    	LogicalChannel lc;
    	Channel channel;
    	List<Long> ids = new ArrayList<Long>();
    	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
			channel = pixels.getChannel(i);
			lc = channel.getLogicalChannel();
			lc.setOtf(otf);
	    	lc.setDetectorSettings(mmFactory.createDetectorSettings(detector));
	    	lc.setFilterSet(filterSet);
	    	lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
	    	lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
	    	lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
	    	assertNotNull(lc);
	    	ids.add(lc.getId().getValue());
		}
    	List<LogicalChannel> channels = iMetadata.loadChannelAcquisitionData(
    			ids);
    	assertTrue(channels.size() == pixels.getSizeC().getValue());
    	LogicalChannel loaded;
    	Iterator<LogicalChannel> j = channels.iterator();
    	while (j.hasNext()) {
    		loaded = j.next();
    		assertNotNull(loaded);
        	ChannelAcquisitionData data = new ChannelAcquisitionData(loaded);
        	assertTrue(data.getDetector().getId() == 
        		detector.getId().getValue());
        	assertTrue(data.getFilterSet().getId() == 
        		filterSet.getId().getValue());
        	assertTrue(data.getLightSource().getId() == 
        		laser.getId().getValue());
        	assertNotNull(loaded.getDetectorSettings());
        	assertNotNull(loaded.getLightSourceSettings());
        	assertNotNull(loaded.getDetectorSettings().getBinning());
        	assertNotNull(loaded.getDetectorSettings().getDetector());
        	assertNotNull(loaded.getDetectorSettings().getDetector().getType());
        	assertNotNull(loaded.getLightPath());
        	assertNotNull(data.getLightPath().getDichroic().getId() 
        			== dichroic.getId().getValue());
        	//OTF support
        	/*
        	assertTrue(data.getOTF().getId() == otf.getId().getValue());
        	assertNotNull(loaded.getOtf());
        	assertTrue(loaded.getOtf().getId().getValue() 
        			== otf.getId().getValue());
        	assertNotNull(loaded.getOtf().getFilterSet());
        	assertNotNull(loaded.getOtf().getObjective());
        	assertTrue(loaded.getOtf().getFilterSet().getId().getValue() ==
        		filterSet.getId().getValue());
        	assertTrue(loaded.getOtf().getObjective().getId().getValue() ==
        		objective.getId().getValue());
        		*/
		}
    }
    
}
