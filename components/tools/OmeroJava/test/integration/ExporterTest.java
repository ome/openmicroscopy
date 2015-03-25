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
import integration.PermissionsTestAll.TestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import loci.common.Constants;
import loci.common.RandomAccessInputStream;
import loci.common.RandomAccessOutputStream;
import loci.formats.tiff.TiffParser;
import loci.formats.tiff.TiffSaver;
import ome.specification.OmeValidator;
import ome.specification.SchemaResolver;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    /** The catalog file to find. */
    private static final String CATALOG = "ome-transforms.xml";

    /** The conversion file to find.*/
    private static final String UNITS_CONVERSION = "units-conversion.xsl";

    /** The <i>name</i> attribute. */
    private static final String CURRENT = "current";

    /** The <i>schema</i> attribute. */
    private static final String SCHEMA = "schema";

    /** The <i>target</i> name. */
    private static final String TARGET = "target";

    /** The <i>transform</i> name. */
    private static final String TRANSFORM = "transform";

    /** The <i>source</i> node. */
    private static final String SOURCE = "source";

    /** The <i>file</i> attribute. */
    private static final String FILE = "file";

    /** Flag indicating to create an image using XML mock and import it.*/
    private static final int IMAGE = 0;

    /** Flag indicating to create a simple image.*/
    private static final int SIMPLE_IMAGE = 1;

    /**
     * Flag indicating to create an image with ROI using XML mock and
     * import it.
     */
    private static final int IMAGE_ROI = 2;

    /**
     * Flag indicating to create an image with annotated acquisition data
     * using XML mock and import it.
     */
    private static final int IMAGE_ANNOTATED_DATA = 2;

    /** The various transforms read from the configuration file.*/
    private Map<String, List<String>> downgrades;

    /** The collection of transforms to perform upgrade.*/
    private Map<String, List<String>> upgrades;

    /** The current schema.*/
    private String currentSchema;

    /**
     * Validates the specified input.
     *
     * @param input
     *            The input to validate
     * @throws Exception
     *             Thrown if an error occurred during the validation
     */
    private void validate(File input) throws Exception {
        OmeValidator validator = new OmeValidator();
        validator.parseFile(input);
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
        Iterator<InputStream> i = transforms.iterator();
        File output;
        InputStream in = null;
        OutputStream out = null;
        Resolver resolver = null;
        while (i.hasNext()) {
            stream = i.next();
            try {
                factory = TransformerFactory.newInstance();
                resolver = new Resolver();
                factory.setURIResolver(resolver);
                output = File.createTempFile(
                        RandomStringUtils.random(100, false, true),
                        "."+ OME_XML);
                output.deleteOnExit();
                Source src = new StreamSource(stream);
                Templates template = factory.newTemplates(src);
                transformer = template.newTransformer();
                transformer.setParameter(OutputKeys.ENCODING, Constants.ENCODING);
                out = new FileOutputStream(output);
                in = new FileInputStream(inputXML);
                transformer.transform(new StreamSource(in),
                        new StreamResult(out));
                inputXML = output;
            } catch (Exception e) {
                throw new Exception("Cannot apply transform", e);
            } finally {
                if (stream != null) stream.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (resolver != null) resolver.close();
            }
        }
        File f = File.createTempFile(
                RandomStringUtils.random(100, false, true), "."+ OME_XML);
        FileUtils.copyFile(inputXML, f);
        return f;
    }

    /**
     * Creates an image to export.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private Image createSimpleImageToExport() throws Exception {
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
     * Creates an image to export.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private Image createImageWithROIToExport() throws Exception {
      //create an import and image
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                "."+ OME_XML);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImageWithROI(), true);
        List<Pixels> pix = null;
        try {
            // method tested in ImporterTest
            pix = importFile(f, OME_XML, true);
            return pix.get(0).getImage();
        } catch (Throwable e) {
            throw new Exception("Cannot create image to import", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Creates an image to export.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private Image createImageWithAnnotatedDataToExport() throws Exception {
        //create an import and image
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                "."+ OME_XML);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImageWithAnnotatedAcquisitionData(), true);
        List<Pixels> pix = null;
        try {
            // method tested in ImporterTest
            pix = importFile(f, OME_XML, true);
            return pix.get(0).getImage();
        } catch (Throwable e) {
            throw new Exception("Cannot create image to import", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Creates an image to export.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private Image createImageToExport() throws Exception {
        //create an import and image
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                "." + OME_XML);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImageWithAcquisitionData(), true);
        List<Pixels> pix = null;
        try {
            // method tested in ImporterTest
            pix = importFile(f, OME_XML, true);
            return pix.get(0).getImage();
        } catch (Throwable e) {
            throw new Exception("Cannot create image to import", e);
        } finally {
            if (f != null) f.delete();
        }
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
        upgrades = new HashMap<String, List<String>>();
        downgrades = currentSchema();
    }

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
        downgrades.clear();
        upgrades.clear();
    }

    /**
     * Tests to export an image as OME-XML.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMEXML() throws Exception {
        // First create an image
        Image image = createImageToExport();

        // now export
        ExporterPrx exporter = factory.createExporter();
        exporter.addImage(image.getId().getValue());
        long size = exporter.generateXml();
        assertTrue(size > 0);
        // now read
        byte[] values = exporter.read(0, (int) size);
        assertNotNull(values);
        assertEquals(values.length, size);
        exporter.close();
    }

    /**
     * Tests to export an image as OME-XML. The image has an annotation linked
     * to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMEXMLWithAnnotation() throws Exception {
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
        long size = exporter.generateXml();
        assertTrue(size > 0);
        // now read
        byte[] values = exporter.read(0, (int) size);
        assertNotNull(values);
        assertEquals(values.length, size);
        exporter.close();
    }

    /**
     * Tests to export an image as OME-TIFF. The image has an annotation linked
     * to it. Downgrade the image.
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
     * Tests to export an image as OME-TIFF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testExportAsOMETIFF() throws Exception {
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
     * Generates an <code>OME-XML</code> file.
     * 
     * @param index The type of image to import. One of the constants defined
     *              by this class.
     * @return The exporter file.
     * @throws Exception Thrown if an error occurred.
     */
    private File createImageFile(int index)
            throws Exception
    {
        // First create an image
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                "." + OME_XML);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        if (index == IMAGE_ROI) {
            writer.writeFile(f, xml.createImageWithROI(), true);
        } else if (index == IMAGE_ANNOTATED_DATA) {
            writer.writeFile(f, xml.createImageWithAnnotatedAcquisitionData(),
                    true);
        } else {
            writer.writeFile(f, xml.createImageWithAcquisitionData(), true);
        }
        return f;
    }

    /**
     * Exports the file with the specified extension either
     * <code>OME-XML</code> or <code>OME-TIFF</code>.
     *
     * @param extension The extension to use.
     * @param index The type of image to import. One of the constants defined
     *              by this class.
     * @return The exporter file.
     * @throws Exception Thrown if an error occurred.
     */
    private File export(String extension, int index)
            throws Exception
    {
        // First create an image
        Image image = null;
        if (index == SIMPLE_IMAGE) {
            image = createSimpleImageToExport();
        } else if (index == IMAGE_ROI) {
            image = createImageWithROIToExport();
        } else {
            image = createImageToExport();
        }
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                "."+ extension);
        FileOutputStream stream = new FileOutputStream(f);
        ExporterPrx store = null;
        try {
            try {
                store = factory.createExporter();
                store.addImage(image.getId().getValue());
                long size;
                if (OME_TIFF.equals(extension)) {
                    size = store.generateTiff();
                } else {
                    size = store.generateXml();
                }
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
        return f;
    }

    /**
     * Parse the target node.
     *
     * @param node The node to parse.
     * @param sheets Hosts the result.
     */
    private void parseTarget(Element node, Map<String, List<String>> sheets)
    {
        Node attribute;
        NamedNodeMap map;
        NodeList transforms;
        map = node.getAttributes();
        String schema = null;
        List<String> list = null;
        for (int j = 0; j < map.getLength(); j++) {
            attribute = map.item(j);
            schema = attribute.getNodeValue();
            transforms = node.getElementsByTagName(TRANSFORM);
            list = new ArrayList<String>();
            for (int i = 0; i < transforms.getLength(); i++) {
                Node a;
                NamedNodeMap m = transforms.item(i).getAttributes();
                for (int k = 0; k < m.getLength(); k++) {
                    attribute = m.item(k);
                    if (FILE.equals(attribute.getNodeName()))
                        list.add(attribute.getNodeValue());
                }
            }
        }
        if (StringUtils.isNotBlank(schema) && CollectionUtils.isNotEmpty(list)) {
            sheets.put(schema, list);
        }
    }

    /**
     * Extracts the value of the current schema.
     *
     * @param schema The current value.
     * @throws Exception Thrown when an error occurred while parsing the file.
     */
    private Map<String, List<String>> extractCurrentSchema(String schema,
            Document document)
        throws Exception
    {
        NodeList list = document.getElementsByTagName(SOURCE);
        Element n;
        Node attribute;
        NamedNodeMap map;
        NodeList t;
        Map<String, List<String>> transforms =
                new HashMap<String, List<String>>();
        Map<String, List<String>> umap;
        for (int i = 0; i < list.getLength(); ++i) {
            n = (Element) list.item(i);
            map = n.getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                attribute = map.item(j);
                if (SCHEMA.equals(attribute.getNodeName())) {
                    if (schema.equals(attribute.getNodeValue())) {
                        t = n.getElementsByTagName(TARGET);
                        for (int k = 0; k < t.getLength(); k++) {
                            parseTarget((Element) t.item(k), transforms);
                        }
                    } else {
                        NodeList nl = n.getElementsByTagName("upgrades");
                        umap = new HashMap<String, List<String>>();
                        String src = attribute.getNodeValue();
                        for (int k = 0; k < nl.getLength(); k++) {
                            Element node = (Element) nl.item(k);
                            NodeList tt = node.getElementsByTagName(TARGET);
                            for (int l = 0; l < tt.getLength(); l++) {
                                parseTarget((Element) tt.item(l), umap);
                            }
                            //parse the map
                            Iterator<Entry<String, List<String>>> kk = 
                                    umap.entrySet().iterator();
                            Entry<String, List<String>> e;
                            List<String> vl;
                            Iterator<String> jj;
                            while (kk.hasNext()) {
                                e = kk.next();
                                if (e.getKey().equals(schema)) {
                                    upgrades.put(src, e.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        return transforms;
    }

    /**
     * Reads the current schema.
     *
     * @return See above.
     * @throws Exception Thrown if an error occurred while reading.
     */
    private Map<String, List<String>> currentSchema() throws Exception
    {
        InputStream stream = getStream(CATALOG);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(stream);
            currentSchema = doc.getDocumentElement().getAttribute(CURRENT);
            if (StringUtils.isBlank(currentSchema))
                throw new Exception("No schema specified.");
            return extractCurrentSchema(currentSchema, doc);
        } catch (Exception e) {
            throw new Exception("Unable to parse the catalog.", e);
        } finally {
            if (stream != null) stream.close();
        }
    }

    /**
     * Prepares elements used to perform downgrade or upgrade.
     *
     * @param values The map to create the transform from.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    private Object[][] createList(Map<String, List<String>> values)
            throws Exception
    {
        List<Target> targets = new ArrayList<Target>();
        Object[][] data = null;
        List<String> l;
        Iterator<String> j;
        Entry<String, List<String>> e;
        Iterator<Entry<String, List<String>>> i = values.entrySet().iterator();
        while (i.hasNext()) {
            e = i.next();
            l = e.getValue();
            List<InputStream> streams = new ArrayList<InputStream>();
            j = l.iterator();
            while (j.hasNext()) {
                streams.add(getStream(j.next()));
            }
            targets.add(new Target(streams, e.getKey()));
        }
        int index = 0;
        Iterator<Target> k = targets.iterator();
        data = new Object[targets.size()][1];
        while (k.hasNext()) {
            data[index][0] = k.next();
            index++;
        }
        return data;
    }
    /**
     * Creates the transformations.
     * @return Object[][] data.
     */
    @DataProvider(name = "createTransform")
    public Object[][] createTransform() throws Exception {
        return createList(downgrades);
    }

    /**
     * Creates the upgrade transformation.
     * @return Object[][] data.
     */
    @DataProvider(name = "createUpgrade")
    public Object[][] createUpgrade() throws Exception {
        return createList(upgrades);
    }

    /**
     * Retrieve the input stream.
     *
     * @param name The name of the stream.
     * @return See above.
     */
    private InputStream getStream(String name)
    {
        return this.getClass().getResourceAsStream("/transforms/"+name);
    }

    /**
     * Returns the list of transformations to generate the file to upgrade.
     *
     * @param target The schema to start from for the upgrade.
     * @return See above.
     */
    private List<InputStream> retrieveDowngrade(String target)
    {
        List<String> list = downgrades.get(target);
        if (CollectionUtils.isEmpty(list)) return null;
        List<InputStream> streams = new ArrayList<InputStream>();
        Iterator<String> j = list.iterator();
        while (j.hasNext()) {
            streams.add(getStream(j.next()));
        }
        return streams;
    }

    /**
     * Test the downgrade of an image with acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testDowngradeImageWithAcquisition(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = createImageFile(IMAGE);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }

    /**
     * Test the downgrade of an image with ROi.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testDowngradeImageWithROI(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = createImageFile(IMAGE_ROI);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }

    /**
     * Test the downgrade of an image with annotated acquisition data 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testDowngradeImageWithAnnotatedAcquisitionData(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = createImageFile(IMAGE_ANNOTATED_DATA);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }
    
    /**
     * Test if an image built with current schema validates.
     * @throws Exception Thrown if an error occurred.
     */
    public void testValidateImageWithAcquisition() throws Exception {
        File f = null;
        try {
            f = createImageFile(IMAGE);
            validate(f);
        } catch (Throwable e) {
            throw new Exception("Cannot validate the image: ", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Test if an image with ROI built with current schema validates.
     * @throws Exception Thrown if an error occurred.
     */
    public void testValidateImageWithROI() throws Exception {
        File f = null;
        try {
            f = createImageFile(IMAGE_ROI);
            validate(f);
        } catch (Throwable e) {
            throw new Exception("Cannot validate the image: ", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Test if an image with annotated acquisition data built with current
     * schema validates.
     * @throws Exception Thrown if an error occurred.
     */
    public void testValidateImageWithAnnotatedAcquisition() throws Exception {
        File f = null;
        try {
            f = createImageFile(IMAGE_ANNOTATED_DATA);
            validate(f);
        } catch (Throwable e) {
            throw new Exception("Cannot validate the image: ", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Test the export of an image as OME-XML. Downgrade it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testExportAsOMEXMLDowngrade(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = export(OME_XML, IMAGE);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }

    /**
     * Test the export of an image as OME-XML. Downgrade it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testExportAsOMEXMLDowngradeSimpleImage(Target target)
            throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = export(OME_XML, SIMPLE_IMAGE);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }

    /**
     * Test the export of an image with ROI as OME-XML. Downgrade it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testExportAsOMEXMLDowngradeImageWithROI(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = export(OME_XML, IMAGE_ROI);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed);
            //import the file
            importFile(transformed, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
        }
    }

    /**
     * Tests to export an image as OME-TIFF and downgrade it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test(dataProvider = "createTransform")
    public void testExportAsOMETIFFDowngrade(Target target) throws Exception {
        File f = null;
        File transformed = null;
        File inputXML = null;
        RandomAccessInputStream in = null;
        RandomAccessOutputStream out = null;
        RandomAccessOutputStream tiffOutput = null;
        File tiffXML = null;
        try {
            f = export(OME_TIFF, IMAGE);
            //extract XML and copy to tmp file
            String path = f.getAbsolutePath();
            TiffParser parser = new TiffParser(path);
            inputXML = File.createTempFile(RandomStringUtils.random(100, false,
                    true),"." + OME_XML);
            FileUtils.writeStringToFile(inputXML, parser.getComment(),
                    Constants.ENCODING);
            //transform XML
            transformed = applyTransforms(inputXML, target.getTransforms());
            validate(transformed);
            String comment = FileUtils.readFileToString(transformed,
                    Constants.ENCODING);

            tiffOutput = new RandomAccessOutputStream(path);
            TiffSaver saver = new TiffSaver(tiffOutput, path);
            saver.setBigTiff(parser.isBigTiff());
            in = new RandomAccessInputStream(path);
            saver.overwriteComment(in, comment);
            tiffOutput.close();
            //import the file
            importFile(f, OME_TIFF);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (inputXML != null) inputXML.delete();
            if (in != null) in.close();
            if (tiffXML != null) tiffXML.delete();
        }
    }

    /**
     * Test the upgrade of an image with acquisition data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createUpgrade")
    public void testUpgradeImageWithAcquisition(Target target) throws Exception {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE); //2015 image
            List<InputStream> transforms = retrieveDowngrade(target.getSource());
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file.
            upgraded = applyTransforms(transformed, target.getTransforms());
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot upgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    /**
     * Test the upgrade of an image with ROI.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createUpgrade")
    public void testUpgradeImageWithROI(Target target) throws Exception {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE_ROI); //2015 image
            List<InputStream> transforms = retrieveDowngrade(target.getSource());
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file.
            upgraded = applyTransforms(transformed, target.getTransforms());
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot upgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    /**
     * Test the upgrade of an image with annotated acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createUpgrade")
    public void testUpgradeImageWithAnnotatedAcquisition(Target target) throws Exception {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE_ANNOTATED_DATA); //2015 image
            List<InputStream> transforms = retrieveDowngrade(target.getSource());
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file.
            upgraded = applyTransforms(transformed, target.getTransforms());
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot upgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    // Path not in ome-transforms but stylesheets are available
    /**
     * Test the upgrade of an image from 2003-FC to 2008-09.
     * @throws Exception Thrown if an error occurred.
     */
    public void testUpgradeImage2003FCto200809() throws Exception
    {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE); //2015 image
            List<InputStream> transforms = retrieveDowngrade("2003-FC");
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file to 2008-09
            List<InputStream> upgrades = new ArrayList<InputStream>();
            upgrades.add(getStream("2003-FC-to-2008-09.xsl"));
            upgraded = applyTransforms(transformed, upgrades);
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot transform image to 2008-09 ", e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    /**
     * Test the upgrade of an image from 2007-06 to 2008-02.
     * @throws Exception Thrown if an error occurred.
     */
    public void testUpgradeImage200706to200802() throws Exception
    {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE); //2015 image
            List<InputStream> transforms = retrieveDowngrade("2007-06");
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file to 2008-02
            List<InputStream> upgrades = new ArrayList<InputStream>();
            upgrades.add(getStream("2007-06-to-2008-02.xsl"));
            upgraded = applyTransforms(transformed, upgrades);
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot transform image to 2008-02", e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    /**
     * Test the upgrade of an image from 2007-06 to 2008-09.
     * @throws Exception Thrown if an error occurred.
     */
    public void testUpgradeImage200706to200809() throws Exception
    {
        File f = null;
        File transformed = null;
        File upgraded = null;
        try {
            f = createImageFile(IMAGE); //2015 image
            List<InputStream> transforms = retrieveDowngrade("2007-06");
            //Create file to upgrade
            transformed = applyTransforms(f, transforms);
            //now upgrade the file to 2008-09
            List<InputStream> upgrades = new ArrayList<InputStream>();
            upgrades.add(getStream("2007-06-to-2008-09.xsl"));
            upgraded = applyTransforms(transformed, upgrades);
            //validate the file
            validate(upgraded);
            //import the file
            importFile(upgraded, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot transform image to 2008-09", e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (upgraded != null) upgraded.delete();
        }
    }

    class Target {

        /** The transforms to apply.*/
        private List<InputStream> transforms;

        /** The source schema.*/
        private String source;

        /**
         * Creates a new instance.
         *
         * @param transforms The transforms to apply.
         * @param source The source schema.
         */
        Target(List<InputStream> transforms, String source)
        {
            this.transforms = transforms;
            this.source = source;
        }

        /**
         * Returns the transforms to apply.
         *
         * @return See above.
         */
        List<InputStream> getTransforms() { return transforms; }

        /**
         * Returns the source schema.
         *
         * @return See above.
         */
        String getSource() { return source; }

    }

    class Resolver implements URIResolver {

        /** The stream.*/
        private InputStream stream;

        /** Close the input stream if not <code>null</code>.*/
        public void close()
            throws Exception
        {
            if (stream != null) stream.close();
        }

        @Override
        public Source resolve(String href, String base)
                throws TransformerException {
            stream = getStream(UNITS_CONVERSION);
            return new StreamSource(stream);
        }
    }
}
