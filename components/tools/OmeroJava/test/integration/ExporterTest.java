/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.List;

import omero.RInt;
import omero.RLong;
import omero.api.ExporterPrx;
import omero.api.IPixelsPrx;
import omero.api.RawFileStorePrx;
import omero.model.Details;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PixelsOriginalFileMapI;
import omero.model.PixelsType;
import omero.sys.ParametersI;

//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies

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
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	private Image createImage()
		throws Exception
	{
		IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = 
    		svc.getAllEnumerations(PixelsType.class.getName());
    	List<Integer> channels = new ArrayList<Integer>();
    	for (int i = 0; i < 1; i++) {
			channels.add(i);
		}
    	
    	RLong id = svc.createImage(1, 1, 1, 1, channels, 
    			(PixelsType) types.get(1), "test", "");
    	//Retrieve the image.
    	ParametersI param = new ParametersI();
    	param.addId(id.getValue());
    	Image img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	return (Image) iUpdate.saveAndReturnObject(img);
	}
	
    /**
     * Tests to export an image as OME-TIFF.
     * 
     * @throws Exception Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFF() 
    	throws Exception 
    {
    	//First create an image
    	Image image = createImage();
    	
    	//Need to have an annotation otherwise does not work
    	FileAnnotationI fa = new FileAnnotationI();
    	fa.setDescription(omero.rtypes.rstring("test"));
    	FileAnnotation a = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
    	ImageAnnotationLinkI l = new ImageAnnotationLinkI();
    	l.setChild(a);
    	l.setParent(image);
    	iUpdate.saveAndReturnObject(l);
    	Pixels pixels = image.getPrimaryPixels();
    	OriginalFile f = createOriginalFile();
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
    	param = new ParametersI();
    	param.addId(pixels.getId().getValue());
    	pixels = (Pixels) iQuery.findByQuery(
    			"select i from Pixels i where i.id = :id", param);
    	//upload file, method tested in RawFileStore
    	
    	PixelsOriginalFileMapI m = new PixelsOriginalFileMapI();
    	m.setChild(pixels);
    	m.setParent(f);
    	m = (PixelsOriginalFileMapI) iUpdate.saveAndReturnObject(m);
    	
    	image = m.getChild().getImage();
    	
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
