/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Third-party libraries
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.model.annotations.AnnotationAnnotationLink;
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
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
	extends TestCase 
{

	/** Identifies the file annotation. */
	private static final String FILE_ANNOTATION = 
		"ome.model.annotations.FileAnnotation";
	
	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(MetadataServiceTest.class);

	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    private omero.client client;
    
    /**
     * A root-client object.
     */
    private omero.client root;

    /** Helper reference to the <code>Service factory</code>. */
    private ServiceFactoryPrx factory;
    
    
    /** Helper reference to the <code>IUpdate</code> service. */
    private IUpdatePrx iUpdate;

    /** Helper reference to the <code>IAdmin</code> service. */
    private IMetadataPrx iMetadata;
    
    /** Helper reference to the <code>IAdmin</code> service. */
    private IAdminPrx iAdmin;
    
    /**
     * Creates and returns an original file object.
     * @return
     */
    private OriginalFileI createOriginalFile()
    {
    	OriginalFileI oFile = new OriginalFileI();
		oFile.setName(omero.rtypes.rstring("of1"));
		oFile.setPath(omero.rtypes.rstring("/omero"));
		oFile.setSize(omero.rtypes.rlong(0));
		//Need to be modified
		oFile.setSha1(omero.rtypes.rstring("pending"));
		oFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
		return oFile;
    }
    
    /**
     * Creates a default image and returns it.
     *
     * @param time The acquisition time.
     * @return See above.
     */
    private Image simpleImage(long time)
    {
        // prepare data
        Image img = new ImageI();
        img.setName(rstring("image1"));
        img.setDescription(rstring("descriptionImage1"));
        img.setAcquisitionDate(rtime(time));
        return img;
    }
    
    
    /**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception
    {
        client = new omero.client();
        factory = client.createSession();
        iUpdate = factory.getUpdateService();
        iMetadata = factory.getMetadataService();
        iAdmin = factory.getAdminService();
        // administrator client
        String rootpass = client.getProperty("omero.rootpass");
        root = new omero.client(new String[]{"--omero.user=root",
                "--omero.pass=" + rootpass});
        root.createSession();
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception
    {
        client.__del__();
        root.__del__();
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
    	TagAnnotation annotation;
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
    
}
