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
import ome.specification.SchemaResolver;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import omero.RType;
import omero.api.ExporterPrx;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.IObject;
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
    private static final String CATALOG = "/transforms/ome-transforms.xml";

    /** The conversion file to find. */
    private static final String UNITS_CONVERSION = "/transforms/units-conversion.xsl";

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
        String inputAsString = FileUtils.readFileToString(inputXML);
        try {
            StringWriter writer;
            StringReader reader;
            while (i.hasNext()) {
                stream = i.next();
                factory = TransformerFactory.newInstance();
                factory.setURIResolver(new Resolver());
                Source src = new StreamSource(stream);
                Templates template = factory.newTemplates(src);
                transformer = template.newTransformer();
                reader = new StringReader(inputAsString);
                StreamSource srcIn = new StreamSource(reader);
                writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(srcIn, result);
                inputAsString = writer.getBuffer().toString();
                stream.close();
                reader.close();
                writer.close();
            }
        } catch (Exception e) {
            throw new Exception("Cannot apply transform", e);
        }
        File f = File.createTempFile(RandomStringUtils.random(10), "."+ OME_XML);
        FileUtils.copyFile(inputXML, f);
        FileUtils.writeStringToFile(f, inputAsString);
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
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_XML);
        f.deleteOnExit();
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
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_XML);
        f.deleteOnExit();
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
        File f = File.createTempFile(RandomStringUtils.random(10), "."
                + OME_XML);
        f.deleteOnExit();
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
     * Creates the query to load the file set corresponding to a given image.
     *
     * @return See above.
     */
    private String createFileSetQuery()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("select fs from Fileset as fs ");
        buffer.append("join fetch fs.images as image ");
        buffer.append("left outer join fetch fs.usedFiles as usedFile ");
        buffer.append("join fetch usedFile.originalFile as f ");
        buffer.append("join fetch f.hasher ");
        buffer.append("where image.id in (:imageIds)");
        return buffer.toString();
    }

    /**
     * Exports the file with the specified extension either
     * <code>OME-XML</code> or <code>OME-TIFF</code>.
     *
     * @param extension The extension to use.
     * @param index The type of image to import. One of the constants defined
     *              by this clase.
     * @return The exporter file.
     * @throws Exception Thrown if an error occurred.
     */
    private File download(String extension, int index)
            throws Exception
    {
        // First create an image
        Image image = null;
        if (index == IMAGE_ROI) {
            image = createImageWithROIToExport();
        } else if (index == IMAGE_ANNOTATED_DATA) {
            image = createImageWithAnnotatedDataToExport();
        } else {
            image = createImageToExport();
        }
        File f = File.createTempFile(RandomStringUtils.random(10), "."
               + extension);
        FileOutputStream stream = new FileOutputStream(f);
        RawFileStorePrx store = null;
        try {
            IQueryPrx svc = factory.getQueryService();
            ParametersI param = new ParametersI();
            long id = image.getId().getValue();
            List<RType> l = new ArrayList<RType>();
            l.add(omero.rtypes.rlong(id));
            param.add("imageIds", omero.rtypes.rlist(l));

            
            param.map.put("id", omero.rtypes.rlong(
                    image.getPrimaryPixels().getId().getValue()));
            List<IObject> files = svc.findAllByQuery(createFileSetQuery(),
                    param);
            List<OriginalFile> values = new ArrayList<OriginalFile>();
            Iterator<IObject> i = files.iterator();
            Fileset set;
            List<FilesetEntry> entries;
            Iterator<FilesetEntry> j;
            while (i.hasNext()) {
                set = (Fileset) i.next();
                entries = set.copyUsedFiles();
                j = entries.iterator();
                while (j.hasNext()) {
                    FilesetEntry fs = j.next();
                    id = fs.getOriginalFile().getId().getValue();
                    break;
                }
            }
            store = factory.createRawFileStore();
            store.setFileId(id);
            long size = -1;
            long offset = 0;
            try {
                try {
                    size = store.size();
                    for (offset = 0; (offset+INC) < size;) {
                        stream.write(store.read(offset, INC));
                        offset += INC;
                    }
                } finally {
                    stream.write(store.read(offset, (int) (size-offset)));
                    stream.close();
                }
            } catch (Exception e) {
                if (stream != null) stream.close();
                throw new Exception("Unable to download image", e);
            }
        } catch (IOException e) {
            throw new Exception("Unable to download image", e);
        } finally {
            if (store != null) store.close();
        }
        return f;
    }

    /**
     * Exports the file with the specified extension either
     * <code>OME-XML</code> or <code>OME-TIFF</code>.
     *
     * @param extension The extension to use.
     * @param index The type of image to import. One of the constants defined
     *              by this clase.
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
                        NodeList upgrades = n.getElementsByTagName("upgrades");
                        umap = new HashMap<String, List<String>>();
                        for (int k = 0; k < upgrades.getLength(); k++) {
                            Element node = (Element) upgrades.item(k);
                            NodeList tt = node.getElementsByTagName(TARGET);
                            for (int l = 0; l < tt.getLength(); l++) {
                                parseTarget((Element) tt.item(l), umap);
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
        InputStream stream = this.getClass().getResourceAsStream(CATALOG);
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
        Iterator<Entry<String, List<String>>> i = downgrades.entrySet().iterator();
        while (i.hasNext()) {
            e = i.next();
            l = e.getValue();
            List<InputStream> streams = new ArrayList<InputStream>();
            j = l.iterator();
            while (j.hasNext()) {
                streams.add(this.getClass().getResourceAsStream(
                        "/transforms/"+j.next()));
            }
            targets.add(new Target(streams, e.getKey(), null));
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
    public void testDowngradeImageWithAcquisition(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = download(OME_XML, IMAGE);
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
     * Test the export of an image as OME-XML.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testDowngradeImageWithROI(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = download(OME_XML, IMAGE_ROI);
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
     * Test the export of an image as OME-XML with annotated acquisition data 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createTransform")
    public void testDowngradeImageWithAnnotatedAcquisitionData(Target target) throws Exception {
        File f = null;
        File transformed = null;
        try {
            f = download(OME_XML, IMAGE_ANNOTATED_DATA);
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
     * Test if an image validates.
     * @throws Exception Thrown if an error occurred.
     */
    public void testValidateImageWithAcquisition() throws Exception {
        File f = null;
        try {
            f = download(OME_XML, IMAGE);
            validate(f);
        } catch (Throwable e) {
            throw new Exception("Cannot validate the image: ", e);
        } finally {
            if (f != null) f.delete();
        }
    }

    /**
     * Test if an image validates.
     * @throws Exception Thrown if an error occurred.
     */
    public void testValidateImageWithROI() throws Exception {
        File f = null;
        try {
            f = download(OME_XML, IMAGE_ROI);
            validate(f);
        } catch (Throwable e) {
            throw new Exception("Cannot validate the image: ", e);
        } finally {
            if (f != null) f.delete();
        }
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
     * Test the export of an image as OME-XML.
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
     * Test the export of an image with ROI as OME-XML.
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
            f = export(OME_TIFF, IMAGE);
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
            validate(transformedXML);
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

        /** The transforms to apply.*/
        private List<InputStream> transforms;

        /** The source schema.*/
        private String source;

        /** The target schema.*/
        private String target;

        /**
         * Creates a new instance.
         *
         * @param transforms The transforms to apply.
         * @param source The source schema.
         */
        Target(List<InputStream> transforms, String target, String source)
        {
            this.transforms = transforms;
            this.source = source;
            this.target = target;
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

        /**
         * Returns the target schema.
         *
         * @return See above.
         */
        String getTarget() { return target; }

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
