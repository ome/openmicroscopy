/*
 * integration.ImporterTest 
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

//Third-party libraries
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.xml.model.OME;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.BooleanAnnotation;
import omero.model.CommentAnnotation;
import omero.model.IObject;
import omero.model.LongAnnotation;
import omero.model.Pixels;
import omero.model.Roi;
import omero.model.Shape;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.sys.ParametersI;

/** 
 * Collection of tests to import images.
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
@Test(groups = {"import", "integration"})
public class ImporterTest 
	extends AbstractTest
{

	/** The default width of an image. */
	private static final int WIDTH = 100;
	
	/** The default height of an image. */
	private static final int HEIGHT = 100;
	
	/** The basic formats tested. */
	private static final String[] FORMATS = {"jpeg", "png"};
	
	/** The OME-XML format. */
	private static final String OME_FORMAT = "ome";
	
	/** The collection of files that have to be deleted. */
	private List<File> files;
	
	/** Reference to the importer store. */
	private OMEROMetadataStoreClient importer;

	/**
	 * Creates a basic buffered image.
	 * 
	 * @return See above.
	 */
	private BufferedImage createBasicImage()
	{
		return new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	}
	
	/**
	 * Creates an image file of the specified format.
	 * 
	 * @param file The file where to write the image.
	 * @param format One of the follow types: jpeg, png.
	 * @throws Exception Thrown if an error occurred while encoding the image.
	 */
	private void createImageFile(File file, String format)
		throws Exception
	{
		Iterator writers = ImageIO.getImageWritersByFormatName(format);
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(file);
        writer.setOutput(ios);
        writer.write(createBasicImage());
        ios.close();
	}
	
	/**
	 * Imports the specified OME-XML file and returns the pixels set
	 * if successfully imported.
	 * 
	 * @param file The file to import.
	 * @param format The format of the file to import.
	 * @return 
	 * @throws Exception Thrown if an error occurred while encoding the image.
	 */
	private List<Pixels> importFile(File file, String format)
		throws Throwable
	{
		ImportLibrary importLibrary = new ImportLibrary(importer, 
				new OMEROWrapper(new ImportConfig()));
		List<Pixels> pixels = null;
		//try to import the file
		pixels = importLibrary.importImage(file, 0, 0, 1, format, null, 
				false, true, null, null);
		assertNotNull(pixels);
		assertTrue(pixels.size() > 0);
		return pixels;
	}
	
	/**
	 * Overridden to initialize the list.
	 * @see AbstractTest#setUp()
	 */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
    	super.setUp();
    	files = new ArrayList<File>();
    	importer = new OMEROMetadataStoreClient();
    	importer.initialize(factory);
    }
    
	/**
	 * Overridden to delete the files.
	 * @see AbstractTest#tearDown()
	 */
    @Override
    @AfterClass
    public void tearDown() 
    	throws Exception
    {
    	Iterator<File> i = files.iterator();
    	while (i.hasNext()) {
			i.next().delete();
		}
    	files.clear();
    }
    
    /**
     * Tests the import of a <code>JPEG</code>, <code>PNG</code>
     * @throws Exception Thrown if an error occurred.
     */
    @Test
	public void testImportGraphicsImages()
		throws Exception
	{
		File f;
		List<String> failures = new ArrayList<String>();
		for (int i = 0; i < FORMATS.length; i++) {
			f = File.createTempFile("testImportGraphicsImages"+FORMATS[i], 
					"."+FORMATS[i]);
			createImageFile(f, FORMATS[i]);
			files.add(f);
			try {
				importFile(f, FORMATS[i]);
			} catch (Throwable e) {
				failures.add(FORMATS[i]);
			}
		}
		if (failures.size() > 0) {
			Iterator<String> j = failures.iterator();
			String s = "";
			while (j.hasNext()) {
				s += j.next();
				s += " ";
			}
			fail("Cannot import the following formats:"+s);
		}
		assertTrue("File Imported", failures.size() == 0);
			
	}
	
	/**
     * Tests the import of an OME-XML file with one image.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportSimpleImage()
		throws Exception
	{
		File f = File.createTempFile("testImportSimpleImage", "."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createImage(), false);
		try {
			importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
	}
	
	/**
     * Tests the import of an OME-XML file with an annotated image.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportAnnotatedImage()
		throws Exception
	{
		File f = File.createTempFile("testImportAnnotatedImage", "."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createAnnotatedImage(), false);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
		Pixels p = pixels.get(0);
		long id = p.getImage().getId().getValue();
		String sql = "select l from ImageAnnotationLink as l ";
		sql += "left outer join fetch l.parent as p";
		sql += "where p.id = :id";
		ParametersI param = new ParametersI();
		param.addId(id);
		List<IObject> l = iQuery.findAllByQuery(sql, param);
		int numberOfAnnotations = 5;
		assertTrue(l.size() >= numberOfAnnotations); //always companion file.
		int count = 0;
		Iterator<IObject> i = l.iterator();
		IObject object;
		while (i.hasNext()) {
			object = i.next();
			if (object instanceof CommentAnnotation) count++;
			else if (object instanceof TagAnnotation) count++;
			else if (object instanceof TermAnnotation) count++;
			else if (object instanceof BooleanAnnotation) count++;
			else if (object instanceof LongAnnotation) count++;
		}
		assertTrue(count == numberOfAnnotations);
	}
	
	/**
     * Tests the import of an OME-XML file with an image with acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportImageWithAcquisitionData()
		throws Exception
	{
		File f = File.createTempFile("testImportImageWithAcquisitionData", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createImageWithAcquisitionData(), true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
		Pixels p = pixels.get(0);
		long id = p.getImage().getId().getValue();
		//load the image and make we have everything
	}
	
	/**
     * Tests the import of an OME-XML file with an image with ROI.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportImageWithROI()
		throws Exception
	{
		File f = File.createTempFile("testImportImageWithROI", "."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createImageWithROI(), true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
		Pixels p = pixels.get(0);
		long id = p.getImage().getId().getValue();
		//load the image and make the ROI
		//Method tested in ROIServiceTest
		IRoiPrx svc = factory.getRoiService();
		RoiResult r = svc.findByImage(id, new RoiOptions());
		assertNotNull(r);
		List<Roi> rois = r.rois;
		assertNotNull(rois);
		assertTrue(rois.size() == 1);
		Iterator<Roi> i = rois.iterator();
		Roi roi;
		List<Shape> shapes;
		while (i.hasNext()) {
			roi = i.next();
			shapes = roi.copyShapes();
			assertNotNull(shapes);
			assertTrue(shapes.size() == XMLMockObjects.SHAPES.length);
		}
	}
	
	/**
     * Tests the import of an OME-XML file with a fully populated plate.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportPlate()
		throws Exception
	{
		File f = File.createTempFile("testImportPlate", "."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createPlate(), true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import the plate", e);
		}
	}
	
	/**
     * Tests the import of an OME-XML file with a fully populated plate
     * with a plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = false)
	public void testImportPlateWithPlateAcquisition()
		throws Exception
	{
		File f = File.createTempFile("testImportPlateWithPlateAcquisition", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createPlateWithPlateAcquistion(), true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import the plate", e);
		}
	}
	
}
