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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import loci.common.RandomAccessInputStream;
import loci.common.RandomAccessOutputStream;
import loci.formats.tiff.TiffParser;
import loci.formats.tiff.TiffSaver;
import ome.specification.OmeValidator;
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
    private static String CATALOG = "/transforms/ome-transforms.xml";

    /** The conversion file to find. */
    private static String UNITS_CONVERSION = "/transforms/units-conversion.xsl";

    /** The <i>name</i> attribute. */
    private static String CURRENT = "current";

    /** The <i>schema</i> attribute. */
    private static String SCHEMA = "schema";

    /** The <i>target</i> name. */
    private static String TARGET = "target";

    /** The <i>transform</i> name. */
    private static String TRANSFORM = "transform";

    /** The <i>source</i> node. */
    private static String SOURCE = "source";

    /** The <i>file</i> attribute. */
    private static String FILE = "file";

    /** The collection of files that have to be deleted. */
    private List<File> files;

    /** The various transforms read from the configuration file.*/
    private Map<String, List<String>> sheets;

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
        try {
            while (i.hasNext()) {
                stream = i.next();
                factory = TransformerFactory.newInstance();
                factory.setURIResolver(new Resolver());
                Source src = new StreamSource(stream);
                Templates template = factory.newTemplates(src);
                transformer = template.newTransformer();
                output = File.createTempFile(RandomStringUtils.random(10), "."
                        + OME_XML);
                out = new FileOutputStream(output);
                in = new FileInputStream(inputXML);
                transformer.transform(new StreamSource(in), new StreamResult(out));
                files.add(output);
                inputXML = output;
                stream.close();
                out.close();
                in.close();
            }
        } catch (Exception e) {
            throw new Exception("Cannot apply transform", e);
        }
        File f = File.createTempFile(RandomStringUtils.random(10), "."+ OME_XML);
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
    private Image createImageToExport() throws Exception {
        // First create an image
        /*
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
        */
        //create an import and image
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_XML);
        files.add(f);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pix = null;
        try {
            pix = importFile(f, OME_XML, true);
            return pix.get(0).getImage();
        } catch (Throwable e) {
            throw new Exception("Cannot create image to import", e);
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
        files = new ArrayList<File>();
        sheets = currentSchema();
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
        sheets.clear();
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
     * Exports the file with the specified extension either
     * <code>OME-XML</code> or <code>OME-TIFF</code>.
     *
     * @param extension The extension to use.
     * @return The exporter file.
     * @throws Exception Thrown if an error occurred.
     */
    private File export(String extension)
            throws Exception
    {
        // First create an image
        Image image = createImageToExport();
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + extension);
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
        InputStream stream = this.getClass().getResourceAsStream(CATALOG);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(stream);
            String current = doc.getDocumentElement().getAttribute(CURRENT);
            if (StringUtils.isBlank(current))
                throw new Exception("No schema specified.");
            return extractCurrentSchema(current, doc);
        } catch (Exception e) {
            throw new Exception("Unable to parse the catalog.", e);
        }
    }

    /**
     * Creates the transformations.
     * @return Object[][] data.
     */
    @DataProvider(name = "createTransform")
    public Object[][] createTransform() throws Exception {
        List<Target> targets = new ArrayList<Target>();
        Object[][] data = null;
        List<String> l;
        Iterator<String> j;
        Entry<String, List<String>> e;
        Iterator<Entry<String, List<String>>> i = sheets.entrySet().iterator();
        while (i.hasNext()) {
            e = i.next();
            l = e.getValue();
            List<InputStream> streams = new ArrayList<InputStream>();
            j = l.iterator();
            while (j.hasNext()) {
                streams.add(this.getClass().getResourceAsStream(
                        "/transforms/"+j.next()));
            }
            StreamSource[] schemas = new StreamSource[1];
            schemas[0] = new StreamSource(this.getClass().getResourceAsStream(
                    "/released-schema/"+e.getKey()+"/ome.xsd"));
            targets.add(new Target(schemas, streams, e.getKey()));
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
     * Test the export of an image as OME-XML.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testExportAsOMEXMLDowngrade(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = export(OME_XML);
            //transform
            transformed = applyTransforms(f, target.getTransforms());
            //validate the file
            validate(transformed, target.getSchemas());
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
     * Tests to export an image as OME-TIFF. The image has an annotation linked
     * to it.
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
        File transformedXML = null;
        File result = null;
        RandomAccessInputStream in = null;
        RandomAccessOutputStream out = null;
        try {
            f = export(OME_TIFF);
            //extract XML and copy to tmp file
            TiffParser parser = new TiffParser(f.getAbsolutePath());
            inputXML = File.createTempFile(RandomStringUtils.random(10),
                    "." + OME_XML);
            FileUtils.writeStringToFile(inputXML, parser.getComment());
            //transform XML
            transformed = applyTransforms(inputXML, target.getTransforms());
            //Generate OME-TIFF
            result = File.createTempFile(RandomStringUtils.random(10), "."
                    + OME_TIFF);
            FileUtils.copyFile(f, result);
            String path = result.getAbsolutePath();
            TiffSaver saver = new TiffSaver(path);
            saver.setBigTiff(parser.isBigTiff());
            in = new RandomAccessInputStream(path);
            saver.overwriteComment(in, FileUtils.readFileToString(transformed));
            
            //validate the OME-TIFF
            parser = new TiffParser(result.getAbsolutePath());
            transformedXML = File.createTempFile(RandomStringUtils.random(10),
                    "." + OME_XML);
            FileUtils.writeStringToFile(transformedXML, parser.getComment());
            validate(transformedXML, target.getSchemas());
            //import the file
            importFile(result, OME_XML);
        } catch (Throwable e) {
            throw new Exception("Cannot downgrade image: "+target.getSource(),
                    e);
        } finally {
            if (f != null) f.delete();
            if (transformed != null) transformed.delete();
            if (inputXML != null) inputXML.delete();
            if (result != null) result.delete();
            if (transformedXML != null) transformedXML.delete();
            if (in != null) in.close();
        }
    }

    class Target {

        /** The schema used to validate the change.*/
        private StreamSource[] schemas;

        /** The transforms to apply.*/
        private List<InputStream> transforms;

        /** The source schema.*/
        private String source;

        /**
         * Creates a new instance.
         *
         * @param schemas The schema used to validate the change.
         * @param transforms The transforms to apply.
         * @param source The source schema.
         */
        Target(StreamSource[] schemas, List<InputStream> transforms,
                String source)
        {
            this.schemas = schemas;
            this.transforms = transforms;
            this.source = source;
        }

        /**
         * Returns the schema used to validate.
         *
         * @return See above.
         */
        StreamSource[] getSchemas() { return schemas; }

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

        @Override
        public Source resolve(String href, String base)
                throws TransformerException {
            InputStream s = this.getClass().getResourceAsStream(
                    UNITS_CONVERSION);
            return new StreamSource(s);
        }
    }
}
