/*
 * $Id$
 *
 *   Copyright 2006-2015 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import loci.common.RandomAccessInputStream;
import loci.formats.tiff.TiffParser;
import loci.formats.tiff.TiffSaver;
import ome.specification.OmeValidator;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>Exporter</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ExporterTest extends AbstractServerTest {

    /** The ome-tiff extension. */
    private static final String OME_TIFF = "ome.tiff";

    /** Possible file extension. */
    public static final String OME_XML = "ome.xml";

    /** Maximum size of pixels read at once. */
    private static final int INC = 262144;

    /** The collection of files that have to be deleted. */
    private List<File> files;

    /**
     * Validates the specified input.
     *
     * @param input
     *            The input to validate
     * @param schemas
     *            The schemas to use.
     * @throws Exception
     *             Thrown if an error occurred during the validation
     */
    private void validate(File input, StreamSource[] schemas) throws Exception {
        OmeValidator theValidator = new OmeValidator();
        theValidator.validateFile(input, schemas);
    }

    /**
     * Applies the transforms to the specified XML file.
     *
     * @param inputXML
     *            The file to transforms.
     * @param transforms
     *            The collection of transforms.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred during the transformations.
     */
    private File applyTransforms(File inputXML, List<InputStream> transforms)
            throws Exception {
        TransformerFactory factory;
        Transformer transformer;
        InputStream stream;
        InputStream in = null;
        OutputStream out = null;
        File output;
        Iterator<InputStream> i = transforms.iterator();
        while (i.hasNext()) {
            factory = TransformerFactory.newInstance();
            stream = i.next();
            output = File.createTempFile(RandomStringUtils.random(10), "."
                    + OME_XML);
            transformer = factory.newTransformer(new StreamSource(stream));
            out = new FileOutputStream(output);
            in = new FileInputStream(inputXML);
            transformer.transform(new StreamSource(in), new StreamResult(out));
            files.add(output);
            inputXML = output;
            stream.close();
            out.close();
            in.close();
        }
        return inputXML;
    }

    /**
     * Creates an image to export.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private Image createImageToExport() throws Exception {
        // First create an image
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        OriginalFile f = mmFactory.createOriginalFile();
        f = (OriginalFile) iUpdate.saveAndReturnObject(f);

        RawFileStorePrx svc = factory.createRawFileStore();
        svc.setFileId(f.getId().getValue());
        byte[] data = new byte[] { 1 };
        svc.write(data, 0, data.length);
        svc.close();

        ParametersI param = new ParametersI();
        param.addId(f.getId().getValue());
        f = (OriginalFile) iQuery.findByQuery(
                "select i from OriginalFile i where i.id = :id", param);
        // upload file, method tested in RawFileStore

        PixelsOriginalFileMapI m = new PixelsOriginalFileMapI();
        m.setChild(new PixelsI(pixels.getId().getValue(), false));
        m.setParent(f);
        m = (PixelsOriginalFileMapI) iUpdate.saveAndReturnObject(m);
        return image;
    }

    /**
     * Overridden to initialize the list.
     *
     * @see AbstractServerTest#setUp()
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        files = new ArrayList<File>();
    }

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
        Iterator<File> i = files.iterator();
        while (i.hasNext()) {
            i.next().delete();
        }
        files.clear();
    }

    /**
     * Tests to export an image as OME-TIFF. The image has an annotation linked
     * to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFFWithAnnotation() throws Exception {
        // First create an image
        Image image = createImageToExport();

        // Need to have an annotation otherwise does not work
        FileAnnotationI fa = new FileAnnotationI();
        fa.setDescription(omero.rtypes.rstring("test"));
        FileAnnotation a = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLinkI l = new ImageAnnotationLinkI();
        l.setChild(a);
        l.setParent(new ImageI(image.getId().getValue(), false));
        iUpdate.saveAndReturnObject(l);

        // now export
        ExporterPrx exporter = factory.createExporter();
        exporter.addImage(image.getId().getValue());
        long size = exporter.generateTiff();
        assertTrue(size > 0);
        // now read
        byte[] values = exporter.read(0, (int) size);
        assertNotNull(values);
        assertEquals(values.length, size);
        exporter.close();
    }

    /**
     * Tests to export an image as OME-TIFF. The image has an annotation linked
     * to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFFWithoutAnnotation() throws Exception {
        // First create an image
        Image image = createImageToExport();

        // now export
        ExporterPrx exporter = factory.createExporter();
        exporter.addImage(image.getId().getValue());
        long size = exporter.generateTiff();
        assertTrue(size > 0);
        // now read
        byte[] values = exporter.read(0, (int) size);
        assertNotNull(values);
        assertEquals(values.length, size);
        exporter.close();
    }

    /**
     * Tests to export an image as OME-TIFF. The image has an annotation linked
     * to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test(groups = "broken")
    public void testExportAsOMETIFFDowngrade() throws Exception {
        // First create an image
        Image image = createImageToExport();
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_TIFF);
        FileOutputStream stream = new FileOutputStream(f);
        ExporterPrx store = null;
        try {
            try {
                store = factory.createExporter();
                store.addImage(image.getId().getValue());
                long size = store.generateTiff();
                long offset = 0;
                try {
                    for (offset = 0; (offset + INC) < size;) {
                        stream.write(store.read(offset, INC));
                        offset += INC;
                    }
                } finally {
                    stream.write(store.read(offset, (int) (size - offset)));
                    stream.close();
                }
            } catch (Exception e) {
                if (stream != null)
                    stream.close();
                if (f != null)
                    f.delete();
                throw e;
            }
        } finally {
            try {
                if (store != null)
                    store.close();
            } catch (Exception e) {
                throw e;
            }
        }
        StreamSource[] schemas = new StreamSource[1];

        schemas[0] = new StreamSource(this.getClass().getResourceAsStream(
                "/Released-Schema/2010-06/ome.xsd"));

        InputStream sheet = this.getClass().getResourceAsStream(
                "/Xslt/2011-06-to-2010-06.xsl");
        List<InputStream> transforms = Arrays.asList(sheet);

        File downgraded = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_TIFF);
        File inputXML = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_XML);
        files.add(inputXML);
        files.add(downgraded);
        FileUtils.copyFile(f, downgraded);
        // Extract the xml.
        String c = new TiffParser(f.getAbsolutePath()).getComment();
        FileUtils.writeStringToFile(inputXML, c);

        // Apply the transforms to downgrade the file.
        inputXML = applyTransforms(inputXML, transforms);
        //
        String path = downgraded.getAbsolutePath();
        TiffSaver saver = new TiffSaver(path);
        RandomAccessInputStream ra = new RandomAccessInputStream(path);
        saver.overwriteComment(ra, FileUtils.readFileToString(inputXML));
        ra.close();

        // validate schema
        File downgradedXML = File.createTempFile(RandomStringUtils.random(10),
                "." + OME_XML);
        c = new TiffParser(path).getComment();
        FileUtils.writeStringToFile(downgradedXML, c);
        validate(downgradedXML, schemas);
        files.add(downgradedXML);
        try {
            List<Pixels> pixels = importFile(downgraded, OME_TIFF);
            // Add checks.
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
    }

}
