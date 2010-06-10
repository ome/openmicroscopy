/*
 * integration.MetadataServiceTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Third-party libraries

import junit.framework.TestCase;

//Application-internal dependencies
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.sys.Parameters;
import pojos.FileAnnotationData;

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
        while (i.hasNext()) {
			o = i.next();
			if (o != null && o instanceof FileAnnotation) {
				r = (FileAnnotation) o;
				if (r.getId().getValue() == data.getId().getValue()) {
					assertTrue(r.getFile().getId().getValue() 
							== of.getId().getValue());
				}
					
			}
		}
    }
   
}
