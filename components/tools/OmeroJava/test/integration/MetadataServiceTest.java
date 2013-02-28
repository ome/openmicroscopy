/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.AcquisitionMode;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.ContrastMethod;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
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
import omero.model.Illumination;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.PixelsAnnotationLink;
import omero.model.PixelsAnnotationLinkI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAcquisitionI;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.DoubleAnnotationData;
import pojos.FileAnnotationData;
import pojos.InstrumentData;
import pojos.LightSourceData;
import pojos.LongAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.XMLAnnotationData;

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
	extends AbstractServerTest 
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
				assertEquals(faData.getFileID(), of.getId().getValue());
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
        
        List<Long> ids = new ArrayList<Long>();
        Parameters param = new Parameters();
        List<Long> nodes = new ArrayList<Long>();
        nodes.add(image.getId().getValue());
        Map<Long, List<IObject>> result = 
        	iMetadata.loadAnnotations(Image.class.getName(), nodes,
        			Arrays.asList(FILE_ANNOTATION), ids, 
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
				assertEquals(faData.getFileID(), of.getId().getValue());
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
			if (o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				count++;
				if (r.getId().getValue() == data.getId().getValue()) {
					assertEquals(r.getFile().getId().getValue(),
							of.getId().getValue());
					assertEquals(r.getFile().getName().getValue(),
							of.getName().getValue());
					assertEquals(r.getFile().getPath().getValue(),
							of.getPath().getValue());
				}
			}
		}
        assertTrue(count > 0);
        assertEquals(count, result.size());
        //Same thing but this time passing ome.model.annotations.FileAnnotation
        result = iMetadata.loadSpecifiedAnnotations(FILE_ANNOTATION,
        		include, exclude, param);
        assertNotNull(result);

        i = result.iterator();
        count = 0;
        while (i.hasNext()) {
        	o = i.next();
        	if (o != null && o instanceof FileAnnotation) {
        		r = (FileAnnotation) o;
        		count++;
        		if (r.getId().getValue() == data.getId().getValue()) {
        			assertEquals(r.getFile().getId().getValue(),
        					of.getId().getValue());
        			assertEquals(r.getFile().getName().getValue(),
        					of.getName().getValue());
        			assertEquals(r.getFile().getPath().getValue(),
        					of.getPath().getValue());
        		}
        	}
        }
        assertTrue(count > 0);
        assertEquals(count, result.size());
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
			if (o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				assertNotNull(r.getNs());
				assertEquals(ns, r.getNs().getValue());
			}
		}
        
        //now test the exclude condition
        include.clear();
        //List of name 
        exclude.add(ns);
        result = iMetadata.loadSpecifiedAnnotations(
        		FileAnnotation.class.getName(), 
        		include, exclude, param);
        assertNotNull(result);
        
        i = result.iterator();
        int count = 0;
        while (i.hasNext()) {
			o = i.next();
			if (o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				if (r.getNs() != null) {
					if (ns.equals(r.getNs().getValue())) count++;
				}
			}
		}
        assertEquals(count, 0);
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
    	assertEquals(result.size(), count);
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
    	assertEquals(result.size(), count);
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
    	assertEquals(result.size(), count);
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
    	assertEquals(result.size(), count);
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
    	assertEquals(result.size(), count);
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
    	iUpdate.saveAndReturnObject(link); 
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.noOrphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	TagAnnotationData data;
    	int count = 0;
    	String ns;
    	while (i.hasNext()) {
			data = new TagAnnotationData((TagAnnotation) i.next());
			ns = data.getNameSpace();
			if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				count++;
			}
		}
    	assertEquals(result.size(), count);
    }
    
    /**
     * Tests the retrieval of tag sets
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSetsAndOrphan() 
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
    	
    	tag = new TagAnnotationI();
    	tag.setTextValue(omero.rtypes.rstring("tag2"));
    	TagAnnotation orphaned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	
    	//save the link.
    	iUpdate.saveAndReturnObject(link); 
    	List<Long> tagsIds = new ArrayList<Long>();
    	tagsIds.add(orphaned.getId().getValue());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	TagAnnotationData data;
    	int count = 0;
    	int orphan = 0;
    	String ns;
    	while (i.hasNext()) {
			data = new TagAnnotationData((TagAnnotation) i.next());
			ns = data.getNameSpace();
			if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				count++;
			} else {
				if (tagsIds.contains(data.getId())) orphan++;
			}
		}
    	assertEquals(orphan, tagsIds.size());
    	assertTrue(count > 0);
    }
    
    /**
     * Tests the retrieval of tag sets. The tag set has a tag and a comment.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSets() 
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
    	iUpdate.saveAndReturnObject(link); 
    	
    	CommentAnnotation comment = new CommentAnnotationI();
    	comment.setTextValue(omero.rtypes.rstring("comment"));
    	comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
    	link = new AnnotationAnnotationLinkI();
    	link.setChild(comment);
    	link.setParent(tagSetReturned);
    	iUpdate.saveAndReturnObject(link); 
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	TagAnnotationData data;
    	int count = 0;
    	int orphan = 0;
    	String ns;
    	while (i.hasNext()) {
    		tag = (TagAnnotation) i.next();
			data = new TagAnnotationData(tag);
			ns = data.getNameSpace();
			if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				if (data.getId() == tagSetReturned.getId().getValue()) {
					assertEquals(tag.sizeOfAnnotationLinks(), 1);
					assertEquals(data.getTags().size(), 1);
					List l = tag.linkedAnnotationList();
					
					assertEquals(l.size(), 1);
					TagAnnotationData child = (TagAnnotationData) l.get(0);
					assertEquals(child.getId(), tagReturned.getId().getValue());
				}
			}
		}
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
        assertEquals(tagData.getDetails().getOwner().getId().getValue(), id2);
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
        assertEquals(result.size(), count);
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
        link.setChild((Annotation) tagData.proxy());
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
    	Map result = iMetadata.loadTagContent(
    			Arrays.asList(tagData.getId().getValue()), param);
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
    	assertEquals(nodes.size(), count);
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
    	List<Detector> detectors;
    	List<Filter> filters;
    	List<FilterSet> filterSets;
    	List<Objective> objectives;
    	List<LightSource> lights;
    	List<OTF> otfs;
    	Detector detector;
    	Filter filter;
    	FilterSet fs;
    	Objective objective;
    	OTF otf;
    	LightSource light;
    	Laser laser;
    	Iterator j; 
    	InstrumentData data;
    	for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
    		instrument = mmFactory.createInstrument(
    				ModelMockFactory.LIGHT_SOURCES[i]);
    		instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
    		data = new InstrumentData(instrument);
    		instrument = iMetadata.loadInstrument(
         			instrument.getId().getValue());
    		data = new InstrumentData(instrument);
    		assertTrue(instrument.sizeOfDetector() > 0);
    		assertTrue(instrument.sizeOfDichroic() > 0);
    		assertTrue(instrument.sizeOfFilter() > 0);
    		assertTrue(instrument.sizeOfFilterSet() > 0);
    		assertEquals(instrument.sizeOfLightSource(), 1);
    		assertTrue(instrument.sizeOfObjective() > 0);
    		assertTrue(instrument.sizeOfOtf() > 0);
    		
    		assertEquals(instrument.sizeOfDetector(),
    			data.getDetectors().size());
    		assertEquals(instrument.sizeOfDichroic(),
    			data.getDichroics().size());
    		assertEquals(instrument.sizeOfFilter(),
    			data.getFilters().size());
    		assertEquals(instrument.sizeOfFilterSet(),
    			data.getFilterSets().size());
    		assertEquals(instrument.sizeOfLightSource(),
    			data.getLightSources().size());
    		assertEquals(instrument.sizeOfObjective(),
    			data.getObjectives().size());
    		assertEquals(instrument.sizeOfOtf(),
    			data.getOTF().size());
    		
    		
    		detectors = instrument.copyDetector();
    		j = detectors.iterator();
    		while (j.hasNext()) {
    			detector = (Detector) j.next();
				assertNotNull(detector.getType());
			}
    		filters = instrument.copyFilter();
    		j = filters.iterator();
    		while (j.hasNext()) {
				filter = (Filter) j.next();
				assertNotNull(filter.getType());
				assertNotNull(filter.getTransmittanceRange());
			}
    		filterSets = instrument.copyFilterSet();
    		j = filterSets.iterator();
    		while (j.hasNext()) {
				fs = (FilterSet) j.next();
				//assertNotNull(fs.getDichroic());
			}
    		objectives = instrument.copyObjective();
    		j = objectives.iterator();
    		while (j.hasNext()) {
				objective = (Objective) j.next();
				assertNotNull(objective.getCorrection());
				assertNotNull(objective.getImmersion());
			}
    		otfs = instrument.copyOtf();
    		j = otfs.iterator();
    		while (j.hasNext()) {
				otf = (OTF) j.next();
				objective = otf.getObjective();
				assertNotNull(otf.getPixelsType());
				assertNotNull(otf.getFilterSet());
				assertNotNull(objective);
				assertNotNull(objective.getCorrection());
				assertNotNull(objective.getImmersion());
			}
    		lights = instrument.copyLightSource();
    		j = lights.iterator();
    		while (j.hasNext()) {
    			light = (LightSource) j.next();
				if (light instanceof Laser) {
					laser = (Laser) light;
					assertNotNull(laser.getType());
					assertNotNull(laser.getLaserMedium());
					assertNotNull(laser.getPulse());
				} else if (light instanceof Filament) {
					assertNotNull(((Filament) light).getType());
				} else if (light instanceof Arc) {
					assertNotNull(((Arc) light).getType());
				}
			}
    	} 	
    }

    /**
     * Tests the retrieval of an instrument light sources of different types.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrumentWithMultipleLightSources() 
    	throws Exception
    {
    	Instrument instrument = mmFactory.createInstrument(
				ModelMockFactory.LASER);
    	instrument.addLightSource(mmFactory.createFilament());
    	instrument.addLightSource(mmFactory.createArc());
		instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
		instrument = iMetadata.loadInstrument(instrument.getId().getValue());
		assertNotNull(instrument);
		List<LightSource> lights = instrument.copyLightSource();
		assertEquals(3, lights.size());
		Iterator<LightSource> i = lights.iterator();
		LightSource src;
		Laser laser;
		while (i.hasNext()) {
			src = i.next();
			if (src instanceof Laser) {
				laser = (Laser) src;
				assertNotNull(laser.getType());
				assertNotNull(laser.getLaserMedium());
				assertNotNull(laser.getPulse());
			} else if (src instanceof Filament) {
				assertNotNull(((Filament) src).getType());
			} else if (src instanceof Arc) {
				assertNotNull(((Arc) src).getType());
			}
		}
		
    }
    
    /**
     * Tests the retrieval of channel acquisition data.
     * One using an instrument with one laser, the second time with a laser
     * with a pump
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testLoadChannelAcquisitionData() 
    	throws Exception
    {
    	//create an instrument.
    	Boolean[] values = new Boolean[2];
    	values[0] = Boolean.valueOf(false);
    	values[1] = Boolean.valueOf(true);
    	for (int k = 0; k < values.length; k++) {
    		Image img = mmFactory.createImage();
        	img = (Image) iUpdate.saveAndReturnObject(img);
        	Pixels pixels = img.getPrimaryPixels();
        	long pixId = pixels.getId().getValue();
        	//method already tested in PixelsServiceTest
        	//make sure objects are loaded.
        	pixels = factory.getPixelsService().retrievePixDescription(pixId);
        	String pump = null;
        	if (values[k]) pump = ModelMockFactory.FILAMENT;
    		Instrument instrument = mmFactory.createInstrument(
        			ModelMockFactory.LASER, pump);
        	
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
        	Laser laser = (Laser) lasers.get(0);
        	
        	sql = "select d from Dichroic as d where d.instrument.id = :iid";
        	Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
        	sql = "select d from Objective as d where d.instrument.id = :iid";
        	Objective objective = (Objective) iQuery.findByQuery(sql, param);
        	
        	sql = "select d from OTF as d where d.instrument.id = :iid";
        	OTF otf = (OTF) iQuery.findByQuery(sql, param);
        	assertNotNull(otf);
        	LogicalChannel lc;
        	Channel channel;
        	ContrastMethod cm;
        	Illumination illumination;
        	AcquisitionMode mode;
        	List<IObject> types = factory.getPixelsService().getAllEnumerations(
        			ContrastMethod.class.getName());
        	cm = (ContrastMethod) types.get(0);
        	
        	types = factory.getPixelsService().getAllEnumerations(
        			Illumination.class.getName());
        	illumination = (Illumination) types.get(0);
        	types = factory.getPixelsService().getAllEnumerations(
        			AcquisitionMode.class.getName());
        	mode = (AcquisitionMode) types.get(0);
        	
        	List<Long> ids = new ArrayList<Long>();
        	for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
    			channel = pixels.getChannel(i);
    			lc = channel.getLogicalChannel();
    			lc.setContrastMethod(cm);
    			lc.setIllumination(illumination);
    			lc.setMode(mode);
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
        	assertEquals(channels.size(), pixels.getSizeC().getValue());
        	LogicalChannel loaded;
        	Iterator<LogicalChannel> j = channels.iterator();
        	LightSourceData l;
        	while (j.hasNext()) {
        		loaded = j.next();
        		assertNotNull(loaded);
            	ChannelAcquisitionData data = new ChannelAcquisitionData(loaded);
            	assertEquals(data.getDetector().getId(),
            		detector.getId().getValue());
            	assertEquals(data.getFilterSet().getId(),
            		filterSet.getId().getValue());
            	l = (LightSourceData) data.getLightSource();
            	assertEquals(l.getId(),laser.getId().getValue());
            	assertNotNull(l.getLaserMedium());
            	assertNotNull(l.getType());
            	if (values[k]) {
            		assertNotNull(((Laser) l.asIObject()).getPump());
            	}
            	assertNotNull(loaded.getDetectorSettings());
            	assertNotNull(loaded.getLightSourceSettings());
            	assertNotNull(loaded.getDetectorSettings().getBinning());
            	assertNotNull(loaded.getDetectorSettings().getDetector());
            	assertNotNull(loaded.getDetectorSettings().getDetector().getType());
            	assertNotNull(loaded.getLightPath());
            	assertEquals(data.getLightPath().getDichroic().getId(),
            			dichroic.getId().getValue());
            	assertNotNull(data.getContrastMethod());
            	assertNotNull(data.getIllumination());
            	assertNotNull(data.getMode());
            	//OTF support
            	
            	assertEquals(data.getOTF().getId(), otf.getId().getValue());
            	assertNotNull(loaded.getOtf());
            	assertEquals(loaded.getOtf().getId().getValue(),
            			otf.getId().getValue());
            	assertNotNull(loaded.getOtf().getFilterSet());
            	assertNotNull(loaded.getOtf().getObjective());
            	assertEquals(loaded.getOtf().getFilterSet().getId().getValue(),
            		filterSet.getId().getValue());
            	assertEquals(loaded.getOtf().getObjective().getId().getValue(),
            		objective.getId().getValue());
            	assertNotNull(loaded.getOtf().getPixelsType());
    		}
		}
    }

    /**
     * Tests the retrieval of tag sets. One with a tag, one without.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadEmptyTagSets() 
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
    	iUpdate.saveAndReturnObject(link); 
    	
    	tagSet = new TagAnnotationI();
    	tagSet.setTextValue(omero.rtypes.rstring("tagSet"));
    	tagSet.setNs(omero.rtypes.rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
    	TagAnnotation tagSetReturned_2 = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tagSet);
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	TagAnnotationData data;
    	String ns;
    	int count = 0;
    	while (i.hasNext()) {
    		tag = (TagAnnotation) i.next();
			data = new TagAnnotationData(tag);
			ns = data.getNameSpace();
			if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				if (data.getId() == tagSetReturned.getId().getValue()
						|| data.getId() == tagSetReturned_2.getId().getValue())
					count++;
			}
		}
    	assertEquals(count, 2);
    }
    
    /**
     * Tests the retrieval of tag sets with tags with a null ns and other 
     * with not null ns. The ns is not the tagset namespace
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadTagsNamepaceNullAndNotNull() 
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
    	iUpdate.saveAndReturnObject(link);
    	
    	List<Long> tagsIds = new ArrayList<Long>();
    	
    	tag = new TagAnnotationI();
    	tag.setTextValue(omero.rtypes.rstring("tag2"));
    	TagAnnotation orphaned = 
    		(TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	tagsIds.add(orphaned.getId().getValue());
    	
    	tag = new TagAnnotationI();
    	tag.setNs(omero.rtypes.rstring(""));
    	tag.setTextValue(omero.rtypes.rstring("tag2"));
    	orphaned = (TagAnnotation) iUpdate.saveAndReturnObject(tag);
    	tagsIds.add(orphaned.getId().getValue());
    	
    	ParametersI param = new ParametersI();
    	param.exp(omero.rtypes.rlong(self));
    	param.orphan(); //no tag loaded
    	
    	List<IObject> result = iMetadata.loadTagSets(param);
    	assertNotNull(result);
    	Iterator<IObject> i = result.iterator();
    	TagAnnotationData data;
    	int count = 0;
    	int orphan = 0;
    	String ns;
    	while (i.hasNext()) {
			data = new TagAnnotationData((TagAnnotation) i.next());
			ns = data.getNameSpace();
			if (ns != null) {
				if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
					if (tagSetReturned.getId().getValue() == data.getId())
						count++;
				}
			}
			if (tagsIds.contains(data.getId()))
				orphan++;
		}
    	assertEquals(orphan, tagsIds.size());
    	assertEquals(count, 1);
    }
    
    /**
     * Tests the creation of file annotation with an original file
     * and load it. Loads the annotation using the 
     * <code>loadSpecifiedAnnotations</code> method. Converts the file
     * annotation into its corresponding Pojo Object
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationConvertToPojo() 
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
        int count = 0;
        FileAnnotationData pojo;
        while (i.hasNext()) {
			o = i.next();
			if (o instanceof FileAnnotation) {
				pojo = new FileAnnotationData( (FileAnnotation) o);
				count++;
				if (pojo.getId() == data.getId().getValue()) {
					assertEquals(pojo.getFileID(), of.getId().getValue());
					assertEquals(pojo.getFileName(), of.getName().getValue());
					assertEquals(pojo.getFilePath(), of.getPath().getValue());
				}
			}
		}
        assertTrue(count > 0);
        assertEquals(count, result.size());
    }
    
    /**
     * Tests the retrieval of annotations with and without namespaces.
     * Exclude the annotation with a given name space.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationNS() 
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
		
		fa = new FileAnnotationI();
		fa.setFile(of);
		FileAnnotation data2 = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		assertNotNull(data2);
		
        Parameters param = new Parameters();
        
        //First test the include condition
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
        		FileAnnotation.class.getName(), 
        		new ArrayList<String>(), Arrays.asList(ns), param);
        assertNotNull(result);
       
        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        FileAnnotationData pojo;
        while (i.hasNext()) {
			o = i.next();
			pojo = new FileAnnotationData((FileAnnotation) o);
			if (data2.getId().getValue() == pojo.getId()) {
				assertEquals(pojo.getFileName(), of.getName().getValue());
				assertEquals(pojo.getFilePath(), of.getPath().getValue());
			}
		}
    }

	/**
	 * Tests the retrieval of a specified long annotation linked to images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToImages()
    	throws Exception
    {
    	Image img1 = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.simpleImage(0));
    	Image img2 = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.simpleImage(0));
    	
    	LongAnnotation data1 = new LongAnnotationI();
    	data1.setLongValue(omero.rtypes.rlong(1L));
    	data1 = (LongAnnotation) iUpdate.saveAndReturnObject(data1);
    	ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        
        LongAnnotation data2 = new LongAnnotationI();
        data2.setLongValue(omero.rtypes.rlong(1L));
        data2 = (LongAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        //Add a comment annotation
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(omero.rtypes.rstring("comment"));
        comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img2.proxy());
        l.setChild((Annotation) comment.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		LongAnnotation.class.getName(), include, exclude,
        		Image.class.getName(), Arrays.asList(img1.getId().getValue(),
        				img2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(img2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified comment annotation
     * linked to datasets. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToDatasets()
    	throws Exception
    {
    	String name = " 2&1 " + System.currentTimeMillis();
    	Dataset d1 = new DatasetI();
        d1.setName(rstring(name));
        d1 = (Dataset) iUpdate.saveAndReturnObject(d1);
    	
        Dataset d2 = new DatasetI();
        d2.setName(rstring(name));
        d2 = (Dataset) iUpdate.saveAndReturnObject(d2);
        
    	CommentAnnotation data1 = new CommentAnnotationI();
    	data1.setTextValue(omero.rtypes.rstring("1"));
    	data1 = (CommentAnnotation) iUpdate.saveAndReturnObject(data1);
    	DatasetAnnotationLink l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        CommentAnnotation data2 = new CommentAnnotationI();
    	data2.setTextValue(omero.rtypes.rstring("1"));
    	data2 = (CommentAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		CommentAnnotation.class.getName(), include, exclude,
        		Dataset.class.getName(), Arrays.asList(d1.getId().getValue(),
        				d2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified term annotation
     * linked to projects. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToProjects()
    	throws Exception
    {
    	String name = " 2&1 " + System.currentTimeMillis();
    	Project d1 = new ProjectI();
        d1.setName(rstring(name));
        d1 = (Project) iUpdate.saveAndReturnObject(d1);
    	
        Project d2 = new ProjectI();
        d2.setName(rstring(name));
        d2 = (Project) iUpdate.saveAndReturnObject(d2);
        
    	TermAnnotation data1 = new TermAnnotationI();
    	data1.setTermValue(omero.rtypes.rstring("Term 1"));
    	data1 = (TermAnnotation) iUpdate.saveAndReturnObject(data1);
    	ProjectAnnotationLink l = new ProjectAnnotationLinkI();
        l.setParent((Project) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        TermAnnotation data2 = new TermAnnotationI();
    	data2.setTermValue(omero.rtypes.rstring("Term 1"));
    	data2 = (TermAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ProjectAnnotationLinkI();
        l.setParent((Project) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new ProjectAnnotationLinkI();
    	l.setParent((Project) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		TermAnnotation.class.getName(), include, exclude,
        		Project.class.getName(), Arrays.asList(d1.getId().getValue(),
        				d2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified tag annotation
     * linked to screen. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToScreens()
    	throws Exception
    {
    	String name = " 2&1 " + System.currentTimeMillis();
    	Screen d1 = new ScreenI();
        d1.setName(rstring(name));
        d1 = (Screen) iUpdate.saveAndReturnObject(d1);
    	
        Screen d2 = new ScreenI();
        d2.setName(rstring(name));
        d2 = (Screen) iUpdate.saveAndReturnObject(d2);
        
    	TagAnnotation data1 = new TagAnnotationI();
    	data1.setTextValue(omero.rtypes.rstring("Tag 1"));
    	data1 = (TagAnnotation) iUpdate.saveAndReturnObject(data1);
    	ScreenAnnotationLink l = new ScreenAnnotationLinkI();
        l.setParent((Screen) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        TagAnnotation data2 = new TagAnnotationI();
    	data2.setTextValue(omero.rtypes.rstring("Tag 1"));
    	data2 = (TagAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ScreenAnnotationLinkI();
        l.setParent((Screen) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new ScreenAnnotationLinkI();
    	l.setParent((Screen) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		TagAnnotation.class.getName(), include, exclude,
        		Screen.class.getName(), Arrays.asList(d1.getId().getValue(),
        				d2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified boolean annotation
     * linked to plates. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToPlates()
    	throws Exception
    {
    	String name = " 2&1 " + System.currentTimeMillis();
    	Plate d1 = new PlateI();
        d1.setName(rstring(name));
        d1 = (Plate) iUpdate.saveAndReturnObject(d1);
    	
        Plate d2 = new PlateI();
        d2.setName(rstring(name));
        d2 = (Plate) iUpdate.saveAndReturnObject(d2);
        
    	BooleanAnnotation data1 = new BooleanAnnotationI();
    	data1.setBoolValue(omero.rtypes.rbool(true));
    	data1 = (BooleanAnnotation) iUpdate.saveAndReturnObject(data1);
    	PlateAnnotationLink l = new PlateAnnotationLinkI();
        l.setParent((Plate) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        BooleanAnnotation data2 = new BooleanAnnotationI();
        data1.setBoolValue(omero.rtypes.rbool(true));
    	data2 = (BooleanAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new PlateAnnotationLinkI();
        l.setParent((Plate) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new PlateAnnotationLinkI();
    	l.setParent((Plate) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		BooleanAnnotation.class.getName(), include, exclude,
        		Plate.class.getName(), Arrays.asList(d1.getId().getValue(),
        				d2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified xml annotation
     * linked to plates. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToPlateAcquisitions()
    	throws Exception
    {
    	String name = " 2&1 " + System.currentTimeMillis();
    	PlateAcquisition d1 = new PlateAcquisitionI();
        d1.setName(rstring(name));
        Plate p1 = new PlateI();
        p1.setName(rstring(name));
        d1.setPlate(p1);
        d1 = (PlateAcquisition) iUpdate.saveAndReturnObject(d1);
    	
        PlateAcquisition d2 = new PlateAcquisitionI();
        d2.setName(rstring(name));
        d2.setPlate(p1);
        d2 = (PlateAcquisition) iUpdate.saveAndReturnObject(d2);
        
    	XmlAnnotation data1 = new XmlAnnotationI();
    	data1.setTextValue(omero.rtypes.rstring("xml annotation"));
    	data1 = (XmlAnnotation) iUpdate.saveAndReturnObject(data1);
    	PlateAcquisitionAnnotationLink l = new PlateAcquisitionAnnotationLinkI();
        l.setParent((PlateAcquisition) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        XmlAnnotation data2 = new XmlAnnotationI();
        data1.setTextValue(omero.rtypes.rstring("xml annotation"));
    	data2 = (XmlAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new PlateAcquisitionAnnotationLinkI();
        l.setParent((PlateAcquisition) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new PlateAcquisitionAnnotationLinkI();
    	l.setParent((PlateAcquisition) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		XmlAnnotation.class.getName(), include, exclude,
        		PlateAcquisition.class.getName(),
        		Arrays.asList(d1.getId().getValue(), d2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified file annotation
     * linked to wells. All Types covered by other tests.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToWells()
    	throws Exception
    {
    	Plate p = (Plate) iUpdate.saveAndReturnObject(
        		mmFactory.createPlate(2, 1, 1, 1, false));
    	
    	ParametersI options = new ParametersI();
    	options.addLong("plateID", p.getId().getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("left outer join fetch well.wellSamples as ws ");
        sb.append("left outer join fetch ws.image as img ");
        sb.append("where pt.id = :plateID");
        List results = iQuery.findAllByQuery(sb.toString(), options);
        
        Well w1 = (Well) results.get(0);
        Well w2 = (Well) results.get(1);
        
        
        
    	FileAnnotation data1 = new FileAnnotationI();
    	data1 = (FileAnnotation) iUpdate.saveAndReturnObject(data1);
    	WellAnnotationLink l = new WellAnnotationLinkI();
        l.setParent((Well) w1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        FileAnnotation data2 = new FileAnnotationI();
    	data2 = (FileAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new WellAnnotationLinkI();
        l.setParent((Well) w2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        LongAnnotation c = new LongAnnotationI();
    	c.setLongValue(omero.rtypes.rlong(1L));
    	c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
    	l = new WellAnnotationLinkI();
    	l.setParent((Well) w2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		FileAnnotation.class.getName(), include, exclude,
        		Well.class.getName(),
        		Arrays.asList(w1.getId().getValue(), w2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(w1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(w2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of specified xml annotations linked to an image.
     * The one annotation has its ns set to <code>modulo</code> ns
     * the other one does not.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToImageWithModuloNS()
    	throws Exception
    {
    	Image img1 = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.simpleImage(0));
    	
    	XmlAnnotation data1 = new XmlAnnotationI();
    	data1.setTextValue(omero.rtypes.rstring("with modulo ns"));
    	data1.setNs(omero.rtypes.rstring(XMLAnnotationData.MODULO_NS));
    	data1 = (XmlAnnotation) iUpdate.saveAndReturnObject(data1);
    	ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        
        XmlAnnotation data2 = new XmlAnnotationI();
        data2.setTextValue(omero.rtypes.rstring("w/o modulo ns"));
        data2 = (XmlAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        Parameters param = new Parameters();
        List<String> include = Arrays.asList(XMLAnnotationData.MODULO_NS);
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		XmlAnnotation.class.getName(), include, exclude,
        		Image.class.getName(), Arrays.asList(img1.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 1);
        List<Annotation> result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        
        //now exclude ns
        include = new ArrayList<String>();
        exclude = Arrays.asList(XMLAnnotationData.MODULO_NS);
        
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		XmlAnnotation.class.getName(), include, exclude,
        		Image.class.getName(), Arrays.asList(img1.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 1);
        result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }
    
    /**
     * Tests the retrieval of a specified long annotation linked to pixels.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToPixels()
    	throws Exception
    {
    	Image img1 = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.createImage());
    	Image img2 = (Image) iUpdate.saveAndReturnObject(
       			mmFactory.createImage());
    	Pixels px1 = img1.getPrimaryPixels();
    	Pixels px2 = img2.getPrimaryPixels();
    	
    	LongAnnotation data1 = new LongAnnotationI();
    	data1.setLongValue(omero.rtypes.rlong(1L));
    	data1 = (LongAnnotation) iUpdate.saveAndReturnObject(data1);
    	PixelsAnnotationLink l = new PixelsAnnotationLinkI();
        l.setParent((Pixels) px1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);
        
        
        LongAnnotation data2 = new LongAnnotationI();
        data2.setLongValue(omero.rtypes.rlong(1L));
        data2 = (LongAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new PixelsAnnotationLinkI();
        l.setParent((Pixels) px2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        
        //Add a comment annotation
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(omero.rtypes.rstring("comment"));
        comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
        l = new PixelsAnnotationLinkI();
        l.setParent((Pixels) px2.proxy());
        l.setChild((Annotation) comment.proxy());
        iUpdate.saveAndReturnObject(l);
        
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        
        
        Map<Long, List<Annotation>> 
        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
        		LongAnnotation.class.getName(), include, exclude,
        		Pixels.class.getName(), Arrays.asList(px1.getId().getValue(),
        				px2.getId().getValue()),
        		param);
        
        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(px1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data1.getId().getValue());
        result = map.get(px2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(),
        		data2.getId().getValue());
    }

}
