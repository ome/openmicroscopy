/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.ExporterPrx;
import omero.api.RawFileStorePrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsOriginalFileMapI;
import omero.sys.ParametersI;

/** 
 * Collections of tests for the <code>Exporter</code> service.
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
@Test(groups = { "client", "integration", "blitz" })
public class ExporterTest 
	extends AbstractTest
{
	
    /**
     * Tests to export an image as OME-TIFF. The image has an annotation
     * linked to it.
     * 
     * @throws Exception Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFFWithAnnotation() 
    	throws Exception 
    {
    	//First create an image
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	
    	//Need to have an annotation otherwise does not work
    	FileAnnotationI fa = new FileAnnotationI();
    	fa.setDescription(omero.rtypes.rstring("test"));
    	FileAnnotation a = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
    	ImageAnnotationLinkI l = new ImageAnnotationLinkI();
    	l.setChild(a);
    	l.setParent(new ImageI(image.getId().getValue(), false));
    	iUpdate.saveAndReturnObject(l);
    	OriginalFile f = mmFactory.createOriginalFile();
    	f = (OriginalFile) iUpdate.saveAndReturnObject(f);
    	
    	RawFileStorePrx svc = factory.createRawFileStore();
    	svc.setFileId(f.getId().getValue());
    	byte[] data = new byte[]{1};
    	svc.write(data, 0, data.length);
    	svc.close();
    	
    	ParametersI param = new ParametersI();
    	param.addId(f.getId().getValue());
    	f = (OriginalFile) iQuery.findByQuery(
    			"select i from OriginalFile i where i.id = :id", param);
    	//upload file, method tested in RawFileStore
    	
    	PixelsOriginalFileMapI m = new PixelsOriginalFileMapI();
    	m.setChild(new PixelsI(pixels.getId().getValue(), false));
    	m.setParent(f);
    	m = (PixelsOriginalFileMapI) iUpdate.saveAndReturnObject(m);
    	
    	//now export
    	ExporterPrx exporter = factory.createExporter();
    	exporter.addImage(image.getId().getValue());
    	long size = exporter.generateTiff();
    	assertTrue(size > 0);
    	//now read
    	byte[] values = exporter.read(0, (int) size);
    	assertNotNull(values);
    	assertTrue(values.length == size);
    	exporter.close();
    }
    
    /**
     * Tests to export an image as OME-TIFF. The image has an annotation
     * linked to it.
     * 
     * @throws Exception Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFFWithoutAnnotation() 
    	throws Exception 
    {
    	//First create an image
    	Image image = mmFactory.createImage();
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	
    	OriginalFile f = mmFactory.createOriginalFile();
    	f = (OriginalFile) iUpdate.saveAndReturnObject(f);
    	
    	RawFileStorePrx svc = factory.createRawFileStore();
    	svc.setFileId(f.getId().getValue());
    	byte[] data = new byte[]{1};
    	svc.write(data, 0, data.length);
    	svc.close();
    	
    	ParametersI param = new ParametersI();
    	param.addId(f.getId().getValue());
    	f = (OriginalFile) iQuery.findByQuery(
    			"select i from OriginalFile i where i.id = :id", param);
    	//upload file, method tested in RawFileStore
    	
    	PixelsOriginalFileMapI m = new PixelsOriginalFileMapI();
    	m.setChild(new PixelsI(pixels.getId().getValue(), false));
    	m.setParent(f);
    	m = (PixelsOriginalFileMapI) iUpdate.saveAndReturnObject(m);
    	
    	//now export
    	ExporterPrx exporter = factory.createExporter();
    	exporter.addImage(image.getId().getValue());
    	long size = exporter.generateTiff();
    	assertTrue(size > 0);
    	//now read
    	byte[] values = exporter.read(0, (int) size);
    	assertNotNull(values);
    	assertTrue(values.length == size);
    	exporter.close();
    }
    
}
