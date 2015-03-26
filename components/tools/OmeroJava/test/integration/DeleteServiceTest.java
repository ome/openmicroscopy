/*
 * Copyright 2006-2015 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.formats.OMEROMetadataStoreClient;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.RawFileStorePrx;
import omero.cmd.Delete;
import omero.cmd.Delete2;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.cmd.graphs.ChildOption;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Annotation;
import omero.model.Arc;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Dichroic;
import omero.model.Experiment;
import omero.model.Filament;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.ImagingEnvironment;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightEmittingDiode;
import omero.model.LightPath;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.Microscope;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsOriginalFileMapI;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StageLabel;
import omero.model.StatsInfo;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Thumbnail;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.ParametersI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import pojos.FileAnnotationData;

/**
 * Collections of tests for the <code>Delete</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class DeleteServiceTest extends AbstractServerTest {

    /** The namespace. */
    public static final String NAMESPACE = "omero.test.namespace";

    /** The namespace. */
    public static final String NAMESPACE_2 = "omero.test.namespace2";

    /** Identifies the fileset as root. */
    public static final String REF_FILESET = "/Fileset";

    /** Identifies the image as root. */
    public static final String REF_IMAGE = "/Image";

    /** Identifies the dataset as root. */
    public static final String REF_DATASET = "/Dataset";

    /** Identifies the project as root. */
    public static final String REF_PROJECT = "/Project";

    /** Identifies the screen as root. */
    public static final String REF_SCREEN = "/Screen";

    /** Identifies the plate as root. */
    public static final String REF_PLATE = "/Plate";

    /** Identifies the detector as root. */
    public static final String REF_DETECTOR = "/Detector";

    /** Identifies the detector as root. */
    public static final String REF_FILAMENT = "Filament";

    /** Identifies the detector as root. */
    public static final String REF_ARC = "Arc";

    /** Identifies the detector as root. */
    public static final String REF_LED = "LightEmittingDiode";

    /** Identifies the detector as root. */
    public static final String REF_LASER = "Laser";

    /** Identifies the instrument as root. */
    public static final String REF_INSTRUMENT = "/Instrument";

    /** Identifies the light source as root. */
    public static final String REF_LIGHTSOURCE = "/LightSource";

    /** Identifies the ROI as root. */
    public static final String REF_ROI = "/Roi";

    /** Identifies the Plate Acquisition as root. */
    public static final String REF_PLATE_ACQUISITION = "/PlateAcquisition";

    /** Identifies the Original file as root. */
    public static final String REF_ORIGINAL_FILE = "/OriginalFile";

    /**
     * Identifies annotation paths. This should be used in the
     * {@link Delete#type} string, while the other paths can be used in for
     * {@link Delete#options} keys.
     */
    public static final String REF_ANN = "/Annotation";

    /** Identifies the Tag. */
    public static final String REF_TAG = "/TagAnnotation";

    /** Identifies the Term. */
    public static final String REF_TERM = "/TermAnnotation";

    /** Identifies the File. */
    public static final String REF_FILE = "/FileAnnotation";
    
    /** Identifies the MapAnnotation. */
    public static final String REF_MAP = "/MapAnnotation";
    
    /** Indicates to force the deletion. */
    public static final String FORCE = "FORCE";

    /** Indicates to keep a certain type of annotations. */
    public static final String KEEP = "KEEP";

    /** Indicates to exclude name space. */
    public static final String EXCLUDE = "excludes=";

    /** Indicates to include name space. */
    public static final String INCLUDE = "includes=";

    /** Separator between option and include/exclude condition. */
    public static final String SEPARATOR = ";";

    /** Separator between option NS. */
    public static final String NS_SEPARATOR = ",";

    /**
     * Indicates to delete the annotation even if it is linked to another
     * object.
     */
    public static final String HARD = "HARD";

    /** The options to keep the sharable annotations. */
    public static final Map<String, String> SHARABLE_TO_KEEP;

    static {
        SHARABLE_TO_KEEP = new HashMap<String, String>();
        SHARABLE_TO_KEEP.put(REF_TAG, KEEP);
        SHARABLE_TO_KEEP.put(REF_TERM, KEEP);
        SHARABLE_TO_KEEP.put(REF_FILE, KEEP);
        SHARABLE_TO_KEEP.put(REF_MAP, KEEP);
    }

    /** The options to keep the sharable annotations. */
    public static final List<String> SHARABLE_TO_KEEP_LIST;

    static {
        SHARABLE_TO_KEEP_LIST = new ArrayList<String>();
        SHARABLE_TO_KEEP_LIST.add("TagAnnotation");
        SHARABLE_TO_KEEP_LIST.add("TermAnnotation");
        SHARABLE_TO_KEEP_LIST.add("FileAnnotation");
        SHARABLE_TO_KEEP_LIST.add("MapAnnotation");
    }
    /**
     * Since so many tests rely on counting the number of objects present
     * globally, we're going to start each method with a new user in a new
     * group.
     */
    @BeforeMethod
    public void createNewUser() throws Exception {
        newUserAndGroup("rw----");
    }

    /**
     * Since we are creating a new client on each invocation, we should also
     * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but not
     * the very last invocation.
     */
    @AfterMethod
    public void close() throws Exception {
        clean();
    }

    /**
     * Returns a map from IObject instance saved to the DB to delete command
     * need to remove it. E.g.
     *
     * <pre>
     * {
     *   new ProjectI() : "/Project"
     * }
     * </pre>
     */
    private Map<String, IObject> createIObjects() throws Exception {
        Map<String, IObject> objects = new HashMap<String, IObject>();
        objects.put(Image.class.getSimpleName(),
                iUpdate.saveAndReturnObject(mmFactory.createImage()));
        objects.put(Dataset.class.getSimpleName(),
                iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject()));
        objects.put(Project.class.getSimpleName(),
                iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject()));
        objects.put(Plate.class.getSimpleName(),
                iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject()));
        objects.put(Screen.class.getSimpleName(),
                iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject()));
 //       objects.put(REF_INSTRUMENT, iUpdate.saveAndReturnObject(mmFactory
 //               .createInstrument()));
        // Create another instrument to hold the detector to be deleted
        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument());
        Detector detector = mmFactory.createDetector();
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        // add detector to the list
        objects.put(Detector.class.getSimpleName(), detector);

        Filament lightFilament = mmFactory.createFilament();
        lightFilament.setInstrument((Instrument) instrument.proxy());
        lightFilament = (Filament) iUpdate.saveAndReturnObject(lightFilament);
        // add light to the list
        objects.put(Filament.class.getSimpleName(), lightFilament);

        Arc lightArc = mmFactory.createArc();
        lightArc.setInstrument((Instrument) instrument.proxy());
        lightArc = (Arc) iUpdate.saveAndReturnObject(lightArc);
        // add light to the list
        objects.put(Arc.class.getSimpleName(), lightArc);

        LightEmittingDiode lightLed = mmFactory.createLightEmittingDiode();
        lightLed.setInstrument((Instrument) instrument.proxy());
        lightLed = (LightEmittingDiode) iUpdate.saveAndReturnObject(lightLed);
        // add light to the list
        objects.put(LightEmittingDiode.class.getSimpleName(), lightLed);

        Laser lightLaser = mmFactory.createLaser();
        lightLaser.setInstrument((Instrument) instrument.proxy());
        lightLaser = (Laser) iUpdate.saveAndReturnObject(lightLaser);
        // add light to the list
        objects.put(Laser.class.getSimpleName(), lightLaser);

        return objects;
    }

    /**
     * Converts the key.
     *
     * @param value The value to convert.
     * @return
     */
    private String convert(String value)
    {
        if (REF_FILAMENT.equals(value) || REF_ARC.equals(value) ||
                REF_LED.equals(value) || REF_LASER.equals(value))
            return LightSource.class.getSimpleName();
        return value;
    }

    /**
     * Creates a basic query to check if the object has been deleted.
     *
     * @param i
     *            The string identifying the class.
     * @return See above.
     */
    private String createBasicContainerQuery(Class<? extends IObject> k) {
        if (Image.class.isAssignableFrom(k)) {
            return "select i from Image as i where i.id = :id";
        } else if (Dataset.class.isAssignableFrom(k)) {
            return "select d from Dataset as d where d.id = :id";
        } else if (Project.class.isAssignableFrom(k)) {
            return "select p from Project as p where p.id = :id";
        } else if (Plate.class.isAssignableFrom(k)) {
            return "select p from Plate as p where p.id = :id";
        } else if (Screen.class.isAssignableFrom(k)) {
            return "select s from Screen as s where s.id = :id";
        } else if (Detector.class.isAssignableFrom(k)) {
            return "select d from Detector as d where d.id = :id";
        } else if (Instrument.class.isAssignableFrom(k)) {
            return "select i from Instrument as i where i.id = :id";
        } else if (Filament.class.isAssignableFrom(k)) {
            return "select l from LightSource as l where l.id = :id";
        } else if (Arc.class.isAssignableFrom(k)) {
            return "select l from LightSource as l where l.id = :id";
        } else if (LightEmittingDiode.class.isAssignableFrom(k)) {
            return "select l from LightSource as l where l.id = :id";
        } else if (Laser.class.isAssignableFrom(k)) {
            return "select l from LightSource as l where l.id = :id";
        }
        throw new UnsupportedOperationException("Unknown type: " + k);
    }

    /**
     * Test to delete an image w/o pixels. 
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteBasicImage() throws Exception {
        Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        assertNotNull(img);
        long id = img.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        img = (Image) iQuery.findByQuery(sb.toString(), param);
        assertNull(img);
    }

    /**
     * Test to delete an empty instrument.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteBasicInstrument() throws Exception {
        Instrument ins = (Instrument) iUpdate.saveAndReturnObject(mmFactory
                .createInstrument());
        assertNotNull(ins);
        long id = ins.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Instrument.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Instrument i ");
        sb.append("where i.id = :id");
        ins = (Instrument) iQuery.findByQuery(sb.toString(), param);
        assertNull(ins);
    }

    /**
     * Test to delete an empty instrument.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteBasicDetector() throws Exception {
        // Create an instrument to hold the detector to be deleted
        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument());
        Detector detector = mmFactory.createDetector();
        assertNotNull(detector);
        detector.setInstrument((Instrument) instrument.proxy());
        detector = (Detector) iUpdate.saveAndReturnObject(detector);
        assertNotNull(detector);

        // find detector
        long id = detector.getId().getValue();
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select d from Detector d ");
        sb.append("where d.id = :id");

        detector = (Detector) iQuery.findByQuery(sb.toString(), param);
        assertNotNull(detector);

        // delete detector
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Detector.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);

        // fail to find detector
        detector = (Detector) iQuery.findByQuery(sb.toString(), param);
        assertNull(detector);
}
    
    /**
     * Test to delete a simple plate i.e. w/o wells or acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteEmptyPlate() throws Exception {
        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());
        assertNotNull(p);
        long id = p.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Plate i ");
        sb.append("where i.id = :id");
        p = (Plate) iQuery.findByQuery(sb.toString(), param);
        assertNull(p);
    }

    /**
     * Test to delete a populated plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlate() throws Exception {
        int[] values = { 0, 1 };
        Plate p;
        List results;
        PlateAcquisition pa = null;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        int n;
        for (int i = 0; i < values.length; i++) {
            n = values[i];
            p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1,
                    1, n, false));
            results = loadWells(p.getId().getValue(), false);

            param = new ParametersI();
            param.addLong("plateID", p.getId().getValue());
            sb = new StringBuilder();
            sb.append("select pa from PlateAcquisition as pa "
                    + "where pa.plate.id = :plateID");
            pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);

            j = results.iterator();
            wellSampleIds = new ArrayList<Long>();
            imageIds = new ArrayList<Long>();
            while (j.hasNext()) {
                well = (Well) j.next();
                for (int k = 0; k < well.sizeOfWellSamples(); k++) {
                    field = well.getWellSample(k);
                    wellSampleIds.add(field.getId().getValue());
                    assertNotNull(field.getImage());
                    imageIds.add(field.getImage().getId().getValue());
                }
            }
            // Now delete the plate
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Instrument.class.getSimpleName(),
                    Collections.singletonList(p.getId().getValue()));
            callback(true, client, dc);

            param = new ParametersI();
            param.addId(p.getId().getValue());
            sb = new StringBuilder();
            // check the plate
            sb.append("select p from Plate as p where p.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));

            // check the well
            param = new ParametersI();
            param.addLong("plateID", p.getId().getValue());
            sb = new StringBuilder();
            sb.append("select well from Well as well ");
            sb.append("left outer join fetch well.plate as pt ");
            sb.append("where pt.id = :plateID");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);

            // check the well samples.
            sb = new StringBuilder();
            param = new ParametersI();
            param.addIds(wellSampleIds);
            sb.append("select p from WellSample as p where p.id in (:ids)");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);

            // check the image.
            sb = new StringBuilder();
            param = new ParametersI();
            param.addIds(imageIds);
            sb.append("select p from Image as p where p.id in (:ids)");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);
            if (pa != null && n > 0) {
                param = new ParametersI();
                param.addId(pa.getId().getValue());
                sb = new StringBuilder();
                // check the plate
                sb.append("select p from PlateAcquisition as p "
                        + "where p.id = :id");
                assertNull(iQuery.findByQuery(sb.toString(), param));
            }
        }
    }

    /**
     * Test to delete a populated plate. The boolean flag indicates to create or
     * no plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateUsingQueue() throws Exception {
        int[] values = { 0, 1 };
        int b;
        Plate p;
        List results;
        PlateAcquisition pa = null;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        for (int i = 0; i < values.length; i++) {
            b = values[i];
            p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1,
                    1, b, false));
            param = new ParametersI();
            param.addLong("plateID", p.getId().getValue());
            sb = new StringBuilder();
            sb.append("select well from Well as well ");
            sb.append("left outer join fetch well.plate as pt ");
            sb.append("left outer join fetch well.wellSamples as ws ");
            sb.append("left outer join fetch ws.image as img ");
            sb.append("where pt.id = :plateID");
            results = iQuery.findAllByQuery(sb.toString(), param);

            sb = new StringBuilder();
            sb.append("select pa from PlateAcquisition as pa "
                    + "where pa.plate.id = :plateID");
            pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);

            j = results.iterator();
            wellSampleIds = new ArrayList<Long>();
            imageIds = new ArrayList<Long>();
            while (j.hasNext()) {
                well = (Well) j.next();
                for (int k = 0; k < well.sizeOfWellSamples(); k++) {
                    field = well.getWellSample(k);
                    wellSampleIds.add(field.getId().getValue());
                    assertNotNull(field.getImage());
                    imageIds.add(field.getImage().getId().getValue());
                }
            }
            // Now delete the plate
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Plate.class.getSimpleName(),
                    Collections.singletonList(p.getId().getValue()));
            callback(true, client, dc);

            // check the plate
            assertDoesNotExist(p);

            // check the well
            param = new ParametersI();
            param.addLong("plateID", p.getId().getValue());
            sb = new StringBuilder();
            sb.append("select well from Well as well ");
            sb.append("left outer join fetch well.plate as pt ");
            sb.append("where pt.id = :plateID");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);

            // check the well samples.
            sb = new StringBuilder();
            param = new ParametersI();
            param.addIds(wellSampleIds);
            sb.append("select p from WellSample as p where p.id in (:ids)");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);

            // check the image.
            sb = new StringBuilder();
            param = new ParametersI();
            param.addIds(imageIds);
            sb.append("select p from Image as p where p.id in (:ids)");
            results = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(results.size(), 0);
            if (pa != null && b > 0) {
                param = new ParametersI();
                param.addId(pa.getId().getValue());
                sb = new StringBuilder();
                // check the plate
                sb.append("select p from PlateAcquisition as p "
                        + "where p.id = :id");
                assertNull(iQuery.findByQuery(sb.toString(), param));
            }
        }
    }

    /**
     * Tests to delete a dataset with images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteDataset() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image image1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image image2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image1);
        link.setParent(d);
        links.add(link);

        link = new DatasetImageLinkI();
        link.setChild(image2);
        link.setParent(d);
        links.add(link);

        iUpdate.saveAndReturnArray(links);

        List<Long> ids = new ArrayList<Long>();
        ids.add(image1.getId().getValue());
        ids.add(image2.getId().getValue());

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d.getId().getValue()));
        callback(true, client, dc);

        // Check if objects have been deleted
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Image as i where i.id in (:ids)";
        List results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        param = new ParametersI();
        param.addId(d.getId().getValue());
        sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete a project containing a dataset with images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteProject() throws Exception {
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image image1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image image2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image1);
        link.setParent(d);
        links.add(link);

        link = new DatasetImageLinkI();
        link.setChild(image2);
        link.setParent(d);
        links.add(link);

        ProjectDatasetLink l = new ProjectDatasetLinkI();
        l.setChild(d);
        l.setParent(p);
        links.add(l);
        iUpdate.saveAndReturnArray(links);

        List<Long> ids = new ArrayList<Long>();
        ids.add(image1.getId().getValue());
        ids.add(image2.getId().getValue());

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Project.class.getSimpleName(),
                Collections.singletonList(d.getId().getValue()));
        callback(true, client, dc);

        // Check if objects have been deleted
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Image as i where i.id in (:ids)";
        List results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        param = new ParametersI();
        param.addId(d.getId().getValue());
        sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p.getId().getValue());
        sql = "select i from Project as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete a screen containing 2 plates, one w/o plate acquisition
     * and one with plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteScreen() throws Exception {
        Screen screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        // Plate w/o plate acquisition
        Plate p1 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        // Plate with plate acquisition
        Plate p2 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 1, false));
        List<IObject> links = new ArrayList<IObject>();
        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild(p1);
        link.setParent(screen);
        links.add(link);
        link = new ScreenPlateLinkI();
        link.setChild(p2);
        link.setParent(screen);
        links.add(link);
        iUpdate.saveAndReturnArray(links);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Screen.class.getSimpleName(),
                Collections.singletonList(screen.getId().getValue()));
        callback(true, client, dc);

        List<Long> ids = new ArrayList<Long>();
        ids.add(p1.getId().getValue());
        ids.add(p2.getId().getValue());

        // Check if the plates exist.
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Plate as i where i.id in (:ids)";
        List results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        param = new ParametersI();
        param.addId(screen.getId().getValue());
        sql = "select i from Screen as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete an image with pixels, channels, logical channels and
     * statistics.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImage() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // channels.
        long id = img.getId().getValue();

        List<Long> channels = new ArrayList<Long>();
        List<Long> logicalChannels = new ArrayList<Long>();
        List<Long> infos = new ArrayList<Long>();
        Channel channel;
        LogicalChannel lc;
        StatsInfo info;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            assertNotNull(channel);
            channels.add(channel.getId().getValue());
            lc = channel.getLogicalChannel();
            assertNotNull(lc);
            logicalChannels.add(lc.getId().getValue());
            info = channel.getStatsInfo();
            assertNotNull(info);
            infos.add(info.getId().getValue());
        }

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        param = new ParametersI();
        param.addId(pixId);
        sb.append("select i from Pixels i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        Iterator<Long> i = channels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from Channel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = infos.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from StatsInfo i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = logicalChannels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from LogicalChannel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
    }

    /**
     * Test to delete an image with pixels, channels, logical channels and
     * statistics.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageUsingQueue() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // channels.
        long id = img.getId().getValue();

        List<Long> channels = new ArrayList<Long>();
        List<Long> logicalChannels = new ArrayList<Long>();
        List<Long> infos = new ArrayList<Long>();
        Channel channel;
        LogicalChannel lc;
        StatsInfo info;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            assertNotNull(channel);
            channels.add(channel.getId().getValue());
            lc = channel.getLogicalChannel();
            assertNotNull(lc);
            logicalChannels.add(lc.getId().getValue());
            info = channel.getStatsInfo();
            assertNotNull(info);
            infos.add(info.getId().getValue());
        }

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        param = new ParametersI();
        param.addId(pixId);
        sb.append("select i from Pixels i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        Iterator<Long> i = channels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from Channel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = infos.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from StatsInfo i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = logicalChannels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from LogicalChannel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
    }

    /**
     * Test to delete an image with rendering settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithRenderingSettings() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        // method already tested in RenderingSettingsServiceTest
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        // check if we have settings now.
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> settings = iQuery.findAllByQuery(sql, param);
        // now delete the image
        assertTrue(settings.size() > 0);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        callback(true, client, dc);

        // check if the settings have been deleted.
        Iterator<IObject> i = settings.iterator();
        IObject o;
        while (i.hasNext()) {
            o = i.next();
            param = new ParametersI();
            param.addId(o.getId().getValue());
            sql = "select rdef from RenderingDef as rdef "
                    + "where rdef.id = :id";
            assertNull(iQuery.findByQuery(sql, param));
        }
    }

    /**
     * Test to delete an image with rendering settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithRenderingSettingsUsingQueue()
            throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        // method already tested in RenderingSettingsServiceTest
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        // check if we have settings now.
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> settings = iQuery.findAllByQuery(sql, param);
        // now delete the image
        assertTrue(settings.size() > 0);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        callback(true, client, dc);
        // check if the settings have been deleted.
        Iterator<IObject> i = settings.iterator();
        IObject o;
        while (i.hasNext()) {
            o = i.next();
            param = new ParametersI();
            param.addId(o.getId().getValue());
            sql = "select rdef from RenderingDef as rdef "
                    + "where rdef.id = :id";
            assertNull(iQuery.findByQuery(sql, param));
        }
    }

    /**
     * Test to delete an image with acquisition data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithAcquisitionData() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // create an instrument.
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        assertNotNull(instrument);

        // retrieve the elements we need for the settings.
        // retrieve the detector.
        ParametersI param = new ParametersI();
        param.addLong("iid", instrument.getId().getValue());
        String sql = "select d from Detector as d where d.instrument.id = :iid";
        Detector detector = (Detector) iQuery.findByQuery(sql, param);
        sql = "select d from FilterSet as d where d.instrument.id = :iid";
        FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
        sql = "select d from Laser as d where d.instrument.id = :iid";
        Laser laser = (Laser) iQuery.findByQuery(sql, param);
        sql = "select d from Dichroic as d where d.instrument.id = :iid";
        Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
        sql = "select d from OTF as d where d.instrument.id = :iid";
        OTF otf = (OTF) iQuery.findByQuery(sql, param);
        sql = "select d from Objective as d where d.instrument.id = :iid";
        Objective objective = (Objective) iQuery.findByQuery(sql, param);

        img.setInstrument(instrument);
        img.setImagingEnvironment(mmFactory.createImageEnvironment());
        img.setObjectiveSettings(mmFactory.createObjectiveSettings(objective));
        img.setStageLabel(mmFactory.createStageLabel());
        iUpdate.saveAndReturnObject(img);
        param = new ParametersI();
        param.acquisitionData();
        List<Long> ids = new ArrayList<Long>();
        ids.add(img.getId().getValue());
        // method already tested in PojosService test
        List results = factory.getContainerService().getImages(
                Image.class.getName(), ids, param);
        img = (Image) results.get(0);
        ObjectiveSettings settings = img.getObjectiveSettings();
        StageLabel label = img.getStageLabel();
        ImagingEnvironment env = img.getImagingEnvironment();

        LogicalChannel lc;
        Channel channel;
        ids = new ArrayList<Long>();
        long detectorSettingsID = 0;
        long lightSourceSettingsID = 0;
        long lightPathID = 0;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setOtf(otf);
            lc.setDetectorSettings(mmFactory.createDetectorSettings(detector));
            lc.setFilterSet(filterSet);
            lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
            lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
            lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
            assertNotNull(lc);
            ids.add(lc.getId().getValue());
            detectorSettingsID = lc.getDetectorSettings().getId().getValue();
            lightSourceSettingsID = lc.getLightSourceSettings().getId()
                    .getValue();
            lightPathID = lc.getLightPath().getId().getValue();
        }

        // Now we try to delete the image.

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        callback(true, client, dc);
        // Follow the section with acquisition data.
        // Now check if the settings are still there.

        param = new ParametersI();
        param.addId(detectorSettingsID);
        sql = "select d from DetectorSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightSourceSettingsID);
        sql = "select d from LightSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightPathID);
        sql = "select d from LightPath as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // instrument
        param.addId(instrument.getId().getValue());
        sql = "select d from Instrument as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(detector.getId().getValue());
        sql = "select d from Detector as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(otf.getId().getValue());
        sql = "select d from OTF as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(objective.getId().getValue());
        sql = "select d from Objective as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(dichroic.getId().getValue());
        sql = "select d from Dichroic as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(filterSet.getId().getValue());
        sql = "select d from FilterSet as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(laser.getId().getValue());
        sql = "select d from Laser as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(settings.getId().getValue());
        sql = "select d from ObjectiveSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(env.getId().getValue());
        sql = "select d from ImagingEnvironment as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(env.getId().getValue());
        sql = "select d from StageLabel as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete an image with ROis.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithROIs() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            serverROI.addShape(rect);
        }
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        List<Long> shapeIds = new ArrayList<Long>();
        Shape shape;
        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }
        // Delete the image.

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image.getId().getValue()));
        callback(true, client, dc);
        // check if the objects have been delete.

        ParametersI param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        String sql = "select d from Roi as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        List results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);
    }

    /**
     * Test to deletes rois as root.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteROIs() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            serverROI.addShape(rect);
        }
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        List<Long> shapeIds = new ArrayList<Long>();
        Shape shape;
        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Roi.class.getSimpleName(),
                Collections.singletonList(serverROI.getId().getValue()));
        callback(true, client, dc);

        // make sure we still have the image
        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());
        String sql = "select d from Image as d where d.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        // check if the objects have been delete.
        param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        sql = "select d from Roi as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);
    }

    /**
     * Test to delete object with annotations that cannot be shared e.g.
     * Comments.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteObjectWithNonSharableAnnotations() throws Exception {
        Map<String, IObject> objects = createIObjects();
        IObject obj = null;
        Long id = null;
        String type = null;
        List<Long> annotationIds;
        ParametersI param;
        String sql;
        List<IObject> l;
        for (Map.Entry<String, IObject> entry : objects.entrySet()) {
            type = convert(entry.getKey());
            obj = entry.getValue();
            id = obj.getId().getValue();
            annotationIds = createNonSharableAnnotation(obj, null);


            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    type, Collections.singletonList(id));
            callback(true, client, dc);

            assertDoesNotExist(obj);

            param = new ParametersI();
            param.addIds(annotationIds);
            assertTrue(annotationIds.size() > 0);
            sql = "select i from Annotation as i where i.id in (:ids)";
            l = iQuery.findAllByQuery(sql, param);
            assertEquals(obj + "-->" + l.toString(), 0, l.size());
        }
    }

    /**
     * Test to delete object with annotations that cannot be shared e.g. tags,
     * terms. One run will delete the annotations, a second will keep them. This
     * will test the usage of the option parameters.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteObjectWithSharableAnnotations() throws Exception {
        Boolean[] values = { Boolean.valueOf(false), Boolean.valueOf(true) };
        IObject obj = null;
        String type = null;
        Long id = null;
        List<Long> annotationIds;
        ParametersI param;
        String sql;
        List l;
        for (int j = 0; j < values.length; j++) {
            Map<String, IObject> objects = createIObjects();
            for (Map.Entry<String, IObject> entry : objects.entrySet()) {
                type = convert(entry.getKey());
                obj = entry.getValue();
                id = obj.getId().getValue();
                annotationIds = createSharableAnnotation(obj, null);
                if (values[j]) {
                    final Delete2 dc = new Delete2();
                    final ChildOption co = new ChildOption();
                    co.excludeType = SHARABLE_TO_KEEP_LIST;
                    dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                            type, Collections.singletonList(id));
                    dc.childOptions = Collections.singletonList(co);
                    callback(true, client, dc);
                } else {
                    final Delete2 dc = new Delete2();
                    dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                            type, Collections.singletonList(id));
                    callback(true, client, dc);
                }

                param = new ParametersI();
                param.addId(id);
                sql = createBasicContainerQuery(obj.getClass());
                assertNull(iQuery.findByQuery(sql, param));
                // annotations should be deleted to
                param = new ParametersI();
                param.addIds(annotationIds);
                assertTrue(annotationIds.size() > 0);
                sql = "select i from Annotation as i where i.id in (:ids)";
                l = iQuery.findAllByQuery(sql, param);
                if (values[j]) {
                    assertEquals(l.size(), annotationIds.size(), l.size());
                } else {
                    assertEquals(l.toString(), 0, l.size());
                }
            }
        }
    }

    /**
     * Test to delete a plate with non sharable annotations linked to the well
     * and well samples and plate with Plate acquisition and annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithNonSharableAnnotations() throws Exception {
        Plate p;
        List results;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        List<Long> annotationIds = new ArrayList<Long>();
        List<Long> r;
        List l;
        p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1,
                0, false));
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("left outer join fetch well.wellSamples as ws ");
        sb.append("left outer join fetch ws.image as img ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);

        j = results.iterator();
        wellSampleIds = new ArrayList<Long>();
        imageIds = new ArrayList<Long>();
        while (j.hasNext()) {
            well = (Well) j.next();
            r = createNonSharableAnnotation(well, null);
            if (r.size() > 0)
                annotationIds.addAll(r);
            for (int f = 0; f < well.sizeOfWellSamples(); f++) {
                field = well.getWellSample(f);
                assertNotNull(field.getImage());
                r = createSharableAnnotation(field.getImage(), null);
                if (r.size() > 0)
                    annotationIds.addAll(r);
                wellSampleIds.add(field.getId().getValue());
                imageIds.add(field.getImage().getId().getValue());
            }
        }
        // Now delete the plate
        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(p.getId().getValue()));
        callback(true, client, dc);

        param = new ParametersI();
        param.addId(p.getId().getValue());
        sb = new StringBuilder();
        // check the plate
        sb.append("select p from Plate as p where p.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));

        // check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // Check if annotations have been deleted.
        param = new ParametersI();
        param.addIds(annotationIds);
        assertTrue(annotationIds.size() > 0);
        sb = new StringBuilder();
        sb.append("select i from Annotation as i where i.id in (:ids)");
        l = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(l.toString(), 0, l.size());
    }

    /**
     * Test to delete a plate with ROI on images. The ROI will have
     * measurements.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithROIMeasurements() throws Exception {
        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = (Well) results.get(0);
        // create the roi.
        Image image = well.getWellSample(0).getImage();
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }
        // First create a table
        String uuid = "Measurement_" + UUID.randomUUID().toString();
        TablePrx table = factory.sharedResources().newTable(1, uuid);
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        table.initialize(columns);
        assertNotNull(table);
        OriginalFile of = table.getOriginalFile();
        assertTrue(of.getId().getValue() > 0);
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.MEASUREMENT_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        long id = fa.getId().getValue();
        // link fa to ROI
        List<IObject> links = new ArrayList<IObject>();
        RoiAnnotationLink rl = new RoiAnnotationLinkI();
        rl.setChild(new FileAnnotationI(id, false));
        rl.setParent(new RoiI(roi.getId().getValue(), false));
        links.add(rl);
        PlateAnnotationLink il = new PlateAnnotationLinkI();
        il.setChild(new FileAnnotationI(id, false));
        il.setParent(new PlateI(p.getId().getValue(), false));
        links.add(il);
        iUpdate.saveAndReturnArray(links);

        final Delete2 dc = new Delete2();
        final ChildOption co = new ChildOption();
        co.excludeType = Collections.singletonList(FileAnnotation.class.getSimpleName());
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(p.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);
        // Shouldn't have measurements
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select a from Annotation as a ");
        sb.append("where a.id = :id");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to delete a plate with sharable annotations linked to the well and
     * well samples and plate with Plate acquisition and annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithSharableAnnotations() throws Exception {
        int[] values = { 0, 1 };
        Boolean[] annotations = { Boolean.valueOf(false), Boolean.valueOf(true) };
        int b;
        Plate p;
        List results;
        PlateAcquisition pa = null;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        Set<Long> annotationIds = new HashSet<Long>();
        List<Long> r;
        List l;

        for (int i = 0; i < values.length; i++) {
            b = values[i];
            for (int k = 0; k < annotations.length; k++) {
                annotationIds.clear();
                p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(
                        1, 1, 1, b, false));
                results = loadWells(p.getId().getValue(), true);
                sb = new StringBuilder();
                param = new ParametersI();
                param.addLong("plateID", p.getId().getValue());
                sb.append("select pa from PlateAcquisition as pa "
                        + "where pa.plate.id = :plateID");
                pa = (PlateAcquisition) iQuery
                        .findByQuery(sb.toString(), param);

                j = results.iterator();
                wellSampleIds = new ArrayList<Long>();
                imageIds = new ArrayList<Long>();
                while (j.hasNext()) {
                    well = (Well) j.next();
                    r = createSharableAnnotation(well, null);
                    if (r.size() > 0)
                        annotationIds.addAll(r);
                    for (int f = 0; f < well.sizeOfWellSamples(); f++) {
                        field = well.getWellSample(f);
                        assertNotNull(field.getImage());
                        r = createSharableAnnotation(field.getImage(), null);
                        if (r.size() > 0)
                            annotationIds.addAll(r);
                        wellSampleIds.add(field.getId().getValue());
                        imageIds.add(field.getImage().getId().getValue());
                    }
                }
                if (pa != null && b > 0) {
                    r = createSharableAnnotation(pa, null);
                    if (r.size() > 0)
                        annotationIds.addAll(r);
                }
                if (annotations[k]) {
                    final Delete2 dc = new Delete2();
                    final ChildOption co = new ChildOption();
                    co.excludeType = Collections.singletonList(FileAnnotation.class.getSimpleName());
                    dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                            Plate.class.getSimpleName(),
                            Collections.singletonList(p.getId().getValue()));
                    dc.childOptions = Collections.singletonList(co);
                    callback(true, client, dc);
                } else {
                    final Delete2 dc = new Delete2();
                    dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                            Plate.class.getSimpleName(),
                            Collections.singletonList(p.getId().getValue()));
                    callback(true, client, dc);
                }

                param = new ParametersI();
                param.addId(p.getId().getValue());
                sb = new StringBuilder();
                // check the plate
                sb.append("select p from Plate as p where p.id = :id");
                assertNull(iQuery.findByQuery(sb.toString(), param));

                // check the well
                param = new ParametersI();
                param.addLong("plateID", p.getId().getValue());
                sb = new StringBuilder();
                sb.append("select well from Well as well ");
                sb.append("left outer join fetch well.plate as pt ");
                sb.append("where pt.id = :plateID");
                results = iQuery.findAllByQuery(sb.toString(), param);
                assertEquals(results.size(), 0);

                // check the well samples.
                sb = new StringBuilder();
                param = new ParametersI();
                param.addIds(wellSampleIds);
                sb.append("select p from WellSample as p where p.id in (:ids)");
                results = iQuery.findAllByQuery(sb.toString(), param);
                assertEquals(results.size(), 0);

                // check the image.
                sb = new StringBuilder();
                param = new ParametersI();
                param.addIds(imageIds);
                sb.append("select p from Image as p where p.id in (:ids)");
                results = iQuery.findAllByQuery(sb.toString(), param);
                assertEquals(results.size(), 0);
                if (pa != null && b > 0) {
                    param = new ParametersI();
                    param.addId(pa.getId().getValue());
                    sb = new StringBuilder();
                    // check the plate
                    sb.append("select p from PlateAcquisition as p "
                            + "where p.id = :id");
                    assertNull(iQuery.findByQuery(sb.toString(), param));
                }

                // Check if annotations have been deleted.
                param = new ParametersI();
                param.addIds(annotationIds);
                assertTrue(annotationIds.size() > 0);
                sb = new StringBuilder();
                sb.append("select i from Annotation as i where i.id in (:ids)");
                l = iQuery.findAllByQuery(sb.toString(), param);
                if (annotations[k]) {
                    assertEquals(l.toString(), annotationIds.size(), l.size());
                } else {
                    assertEquals(l.toString(), 0, l.size());
                }
            }
        }
    }

    /**
     * Tests to delete a dataset with images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCascadingDeleteDatasetAsRoot() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img1);
        iUpdate.saveAndReturnObject(l);
        l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img2);
        iUpdate.saveAndReturnObject(l);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d.getId().getValue()));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(d.getId().getValue());
        String sql = "select d from Dataset d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Image i where i.id in (:ids)";
        assertEquals(0, iQuery.findAllByQuery(sql, param).size());
    }

    /**
     * Tests to delete a project with dataset and images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCascadingDeleteProjectAsRoot() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img1);
        iUpdate.saveAndReturnObject(l);
        l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img2);
        iUpdate.saveAndReturnObject(l);

        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        ProjectDatasetLink pl = new ProjectDatasetLinkI();
        pl.link(new ProjectI(p.getId().getValue(), false), new DatasetI(d
                .getId().getValue(), false));

        iUpdate.saveAndReturnObject(pl);
        pl = new ProjectDatasetLinkI();
        pl.link(new ProjectI(p.getId().getValue(), false), d2);
        iUpdate.saveAndReturnObject(pl);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Project.class.getSimpleName(),
                Collections.singletonList(p.getId().getValue()));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(p.getId().getValue());
        String sql = "select p from Project p where p.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        List<Long> ids = new ArrayList<Long>();
        ids.add(d.getId().getValue());
        ids.add(d2.getId().getValue());
        param = new ParametersI();
        param.addIds(ids);
        sql = "select d from Dataset d where d.id in (:ids)";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);
        ids.clear();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Image i where i.id in (:ids)";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);
    }

    /**
     * Tests to delete a screen.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCascadingDeleteScreenAsRoot() throws Exception {
        int[] values = { 0, 1 };
        Plate plate;
        String sql;
        PlateAcquisition pa = null;
        ParametersI param;
        Screen screen;
        ScreenPlateLink link;
        for (int i = 0; i < values.length; i++) {
            param = new ParametersI();
            plate = mmFactory.createPlate(1, 1, 1, values[i], false);
            plate = (Plate) iUpdate.saveAndReturnObject(plate);
            sql = "select pa from PlateAcquisition as pa "
                    + "where pa.plate.id = :plateID";
            param.addLong("plateID", plate.getId().getValue());
            pa = (PlateAcquisition) iQuery.findByQuery(sql, param);
            screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                    .simpleScreenData().asIObject());
            link = new ScreenPlateLinkI();
            link.link(screen, plate);
            iUpdate.saveAndReturnObject(link);

            final Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Screen.class.getSimpleName(),
                    Collections.singletonList(screen.getId().getValue()));
            callback(true, client, dc);

            param = new ParametersI();
            sql = "select s from Screen as s where s.id = :id";
            param.addId(screen.getId().getValue());
            assertNull(iQuery.findByQuery(sql, param));
            param = new ParametersI();
            sql = "select p from Plate as p where p.id = :id";
            param.addId(plate.getId().getValue());
            assertNull(iQuery.findByQuery(sql, param));
            if (values[i] > 0 && pa != null) {
                param = new ParametersI();
                sql = "select pa from PlateAcquisition as pa "
                        + "where pa.plate.id = :plateID";
                param.addLong("plateID", plate.getId().getValue());
                assertNull(iQuery.findByQuery(sql, param));
            }
        }
    }

    /**
     * Tests to delete an image with a companion file. This is usually the case
     * when the image is imported.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithCompanionFile() throws Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);

        long id = fa.getId().getValue();

        final Delete2 dc = new Delete2();
        final ChildOption co = new ChildOption();
        co.excludeType = Collections.singletonList(FileAnnotation.class.getSimpleName());
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);

        // File annotation should be deleted even if
        // required to keep file annotation
        ParametersI param = new ParametersI();
        param.addId(id);
        String sql = "select a from Annotation as a where a.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete an image with a shared Tag. The tag should not be deleted
     * b/c of the <code>Soft</code> option i.e. default option.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteObjectWithSharedAnnotationSoftOption()
            throws Exception {
        int n = SHARABLE_TO_KEEP.size();
        // images
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        List<Long> ids = createSharableAnnotation(img1, img2);
        assertEquals(n, ids.size());
        // now delete the image 1.
        Delete2 dc = new Delete2();
        ChildOption co = new ChildOption();
        co.excludeType = SHARABLE_TO_KEEP_LIST;
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select a from Annotation as a where a.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(n, results.size());

        // datasets
        Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        ids = createSharableAnnotation(d1, d2);
        // now delete the dataset 1.
        dc = new Delete2();
        co = new ChildOption();
        co.excludeType = SHARABLE_TO_KEEP_LIST;
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d1.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);
        param = new ParametersI();
        param.addIds(ids);
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(n, results.size());

        // projects
        Project p1 = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Project p2 = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        ids = createSharableAnnotation(p1, p2);
        // now delete the project 1.
        dc = new Delete2();
        co = new ChildOption();
        co.excludeType = SHARABLE_TO_KEEP_LIST;
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Project.class.getSimpleName(),
                Collections.singletonList(p1.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);

        param = new ParametersI();
        param.addIds(ids);
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(n, results.size());

        // screens
        Screen s1 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Screen s2 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        ids = createSharableAnnotation(s1, s2);
        // now delete the screen 1.
        dc = new Delete2();
        co = new ChildOption();
        co.excludeType = SHARABLE_TO_KEEP_LIST;
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Screen.class.getSimpleName(),
                Collections.singletonList(s1.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);

        param = new ParametersI();
        param.addIds(ids);
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(n, results.size());

        // Plates
        Plate plate1 = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());
        Plate plate2 = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());
        ids = createSharableAnnotation(plate1, plate2);
        // now delete the plate 1.
        dc = new Delete2();
        co = new ChildOption();
        co.excludeType = SHARABLE_TO_KEEP_LIST;
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(plate1.getId().getValue()));
        dc.childOptions = Collections.singletonList(co);
        callback(true, client, dc);
        param = new ParametersI();
        param.addIds(ids);
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(n, results.size());
    }

    /**
     * Tests to delete a shared tag as root. The tag should be deleted but no
     * the objects linked to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteSharableTagAsRoot() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Project p1 = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Screen screen1 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate plate1 = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());

        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag shared"));
        tag = (TagAnnotation) iUpdate.saveAndReturnObject(tag);
        long tagId = tag.getId().getValue();
        List<IObject> links = new ArrayList<IObject>();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(tag);
        link.setParent(img1);
        links.add(link);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.setChild(new TagAnnotationI(tagId, false));
        dl.setParent(d1);
        links.add(dl);
        ProjectAnnotationLink pl = new ProjectAnnotationLinkI();
        pl.setChild(new TagAnnotationI(tagId, false));
        pl.setParent(p1);
        links.add(pl);
        ScreenAnnotationLink sl = new ScreenAnnotationLinkI();
        sl.setChild(new TagAnnotationI(tagId, false));
        sl.setParent(screen1);
        links.add(sl);
        PlateAnnotationLink platel = new PlateAnnotationLinkI();
        platel.setChild(new TagAnnotationI(tagId, false));
        platel.setParent(plate1);
        links.add(platel);
        iUpdate.saveAndReturnArray(links);
        // delete the tag
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(tagId));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(tagId);
        String sql = "select a from Annotation as a where a.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // We should still have the objects.
        param = new ParametersI();
        param.addId(img1.getId().getValue());
        sql = "select a from Image as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(d1.getId().getValue());
        sql = "select a from Dataset as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p1.getId().getValue());
        sql = "select a from Project as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(screen1.getId().getValue());
        sql = "select a from Screen as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(plate1.getId().getValue());
        sql = "select a from Plate as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete a shared term as root. The tag should be deleted but no
     * the objects linked to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteSharableTermAsRoot() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Project p1 = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Screen screen1 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate plate1 = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());

        TermAnnotation tag = new TermAnnotationI();
        tag.setTermValue(omero.rtypes.rstring("term shared"));
        tag = (TermAnnotation) iUpdate.saveAndReturnObject(tag);
        long tagId = tag.getId().getValue();
        List<IObject> links = new ArrayList<IObject>();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(tag);
        link.setParent(img1);
        links.add(link);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.setChild(new TagAnnotationI(tagId, false));
        dl.setParent(d1);
        links.add(dl);
        ProjectAnnotationLink pl = new ProjectAnnotationLinkI();
        pl.setChild(new TermAnnotationI(tagId, false));
        pl.setParent(p1);
        links.add(pl);
        ScreenAnnotationLink sl = new ScreenAnnotationLinkI();
        sl.setChild(new TermAnnotationI(tagId, false));
        sl.setParent(screen1);
        links.add(sl);
        PlateAnnotationLink platel = new PlateAnnotationLinkI();
        platel.setChild(new TermAnnotationI(tagId, false));
        platel.setParent(plate1);
        links.add(platel);
        iUpdate.saveAndReturnArray(links);
        // delete the tag
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(tagId));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(tagId);
        String sql = "select a from Annotation as a where a.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // We should still have the objects.
        param = new ParametersI();
        param.addId(img1.getId().getValue());
        sql = "select a from Image as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(d1.getId().getValue());
        sql = "select a from Dataset as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p1.getId().getValue());
        sql = "select a from Project as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(screen1.getId().getValue());
        sql = "select a from Screen as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(plate1.getId().getValue());
        sql = "select a from Plate as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete a shared file annotation as root. The tag should be
     * deleted but no the objects linked to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteSharableFileAsRoot() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Project p1 = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Screen screen1 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate plate1 = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());

        FileAnnotation tag = new FileAnnotationI();
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        tag.setFile(of);
        tag = (FileAnnotation) iUpdate.saveAndReturnObject(tag);
        long tagId = tag.getId().getValue();
        List<IObject> links = new ArrayList<IObject>();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(tag);
        link.setParent(img1);
        links.add(link);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.setChild(new TagAnnotationI(tagId, false));
        dl.setParent(d1);
        links.add(dl);
        ProjectAnnotationLink pl = new ProjectAnnotationLinkI();
        pl.setChild(new TermAnnotationI(tagId, false));
        pl.setParent(p1);
        links.add(pl);
        ScreenAnnotationLink sl = new ScreenAnnotationLinkI();
        sl.setChild(new TermAnnotationI(tagId, false));
        sl.setParent(screen1);
        links.add(sl);
        PlateAnnotationLink platel = new PlateAnnotationLinkI();
        platel.setChild(new TermAnnotationI(tagId, false));
        platel.setParent(plate1);
        links.add(platel);
        iUpdate.saveAndReturnArray(links);
        // delete the tag
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(tagId));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(tagId);
        String sql = "select a from Annotation as a where a.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // We should still have the objects.
        param = new ParametersI();
        param.addId(img1.getId().getValue());
        sql = "select a from Image as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(d1.getId().getValue());
        sql = "select a from Dataset as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p1.getId().getValue());
        sql = "select a from Project as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(screen1.getId().getValue());
        sql = "select a from Screen as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(plate1.getId().getValue());
        sql = "select a from Plate as a where a.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete screen with a plate and a reagent.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteScreenWithReagent() throws Exception {
        Screen s = mmFactory.simpleScreenData().asScreen();
        Reagent r = mmFactory.createReagent();
        s.addReagent(r);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
        s.linkPlate(p);
        s = (Screen) iUpdate.saveAndReturnObject(s);
        long screenId = s.getId().getValue();
        // reagent first
        String sql = "select r from Reagent as r ";
        sql += "join fetch r.screen as s ";
        sql += "where s.id = :id";
        ParametersI param = new ParametersI();
        param.addId(screenId);
        r = (Reagent) iQuery.findByQuery(sql, param);
        long reagentID = r.getId().getValue();
        //
        sql = "select s from ScreenPlateLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        ScreenPlateLink link = (ScreenPlateLink) iQuery.findByQuery(sql, param);
        p = link.getChild();
        long plateID = p.getId().getValue();

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Screen.class.getSimpleName(),
                Collections.singletonList(screenId));
        callback(true, client, dc);

        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to delete plate with a reagent.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithReagent() throws Exception {
        Screen s = mmFactory.simpleScreenData().asScreen();
        Reagent r = mmFactory.createReagent();
        s.addReagent(r);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
        s.linkPlate(p);
        s = (Screen) iUpdate.saveAndReturnObject(s);
        long screenId = s.getId().getValue();
        // reagent first
        String sql = "select r from Reagent as r ";
        sql += "join fetch r.screen as s ";
        sql += "where s.id = :id";
        ParametersI param = new ParametersI();
        param.addId(screenId);
        r = (Reagent) iQuery.findByQuery(sql, param);
        long reagentID = r.getId().getValue();
        //
        sql = "select s from ScreenPlateLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        ScreenPlateLink link = (ScreenPlateLink) iQuery.findByQuery(sql, param);
        p = link.getChild();
        long plateID = p.getId().getValue();

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(plateID));
        callback(true, client, dc);


        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete a plate with sharable annotations linked to the well and
     * well samples and plate with Plate acquisition and annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateAcquisitionWithNonSharableAnnotations()
            throws Exception {
        Plate p;
        PlateAcquisition pa = null;
        StringBuilder sb;
        ParametersI param;
        List<Long> annotationIds = new ArrayList<Long>();
        p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1,
                1, false));
        sb = new StringBuilder();
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb.append("select pa from PlateAcquisition as pa "
                + "where pa.plate.id = :plateID");
        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
        annotationIds.addAll(createSharableAnnotation(pa, null));

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(p.getId().getValue()));
        callback(true, client, dc);
        // Check if annotations have been deleted.
        param = new ParametersI();
        param.addIds(annotationIds);
        sb = new StringBuilder();
        sb.append("select i from Annotation as i where i.id in (:ids)");
        List l = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(l.toString(), 0, l.size());
    }

    /**
     * Test to delete an object with annotations with namespace. All annotations
     * which do not have the given namespace should be deleted; others should be
     * kept.
     *
     * Example usage: keep all annotations except for comments.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteObjectWithAnnotationWithoutNS() throws Exception {
        Map<String, IObject> objects = createIObjects();
        IObject obj = null;
        Long id = null;
        String type = null;
        List<Long> annotationIds;
        List<Long> annotationIdsNS;
        ParametersI param;
        String sql;
        List<IObject> l;
        Map<String, String> options;
        for (Map.Entry<String, IObject> entry : objects.entrySet()) {
            type = convert(entry.getKey());
            obj = entry.getValue();
            id = obj.getId().getValue();
            annotationIds = createNonSharableAnnotation(obj, null);
            annotationIdsNS = createNonSharableAnnotation(obj, NAMESPACE);

            options = new HashMap<String, String>();
            options.put(REF_ANN, KEEP + SEPARATOR + EXCLUDE + NAMESPACE);
            //delete(new Delete(type, id, options));
            final Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    type, Collections.singletonList(id));
            final ChildOption co = new ChildOption();
            co.excludeNs = Collections.singletonList(NAMESPACE);
            dc.childOptions = Collections.singletonList(co);
            callback(true, client, dc);

            assertDoesNotExist(obj);

            param = new ParametersI();
            param.addIds(annotationIds);
            assertTrue(annotationIds.size() > 0);
            sql = "select i from Annotation as i where i.id in (:ids)";
            l = iQuery.findAllByQuery(sql, param);
            assertEquals(obj + "-->" + l.toString(), annotationIds.size(),
                    l.size());
            param = new ParametersI();
            param.addIds(annotationIdsNS);
            assertTrue(annotationIdsNS.size() > 0);
            sql = "select i from Annotation as i where i.id in (:ids)";
            l = iQuery.findAllByQuery(sql, param);
            assertEquals(obj + "-->" + l.toString(), 0, l.size());
        }
    }

    /**
     * Test to delete an object with annotations with namespace. All annotations
     * which do not have the given namespace should be deleted; others should be
     * kept.
     *
     * Example usage: keep all annotations except for comments.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteObjectWithAnnotationWithoutNSMultipleNS()
            throws Exception {
        Map<String, IObject> objects = createIObjects();
        IObject obj = null;
        Long id = null;
        String type = null;
        List<Long> annotationIds;
        List<Long> annotationIdsNS = new ArrayList<Long>();
        ParametersI param;
        String sql;
        List<IObject> l;
        Map<String, String> options;
        for (Map.Entry<String, IObject> entry : objects.entrySet()) {
            type = convert(entry.getKey());
            obj = entry.getValue();
            id = obj.getId().getValue();
            annotationIds = createNonSharableAnnotation(obj, null);
            annotationIdsNS.addAll(createNonSharableAnnotation(obj, NAMESPACE));
            annotationIdsNS
                    .addAll(createNonSharableAnnotation(obj, NAMESPACE_2));
            options = new HashMap<String, String>();
            options.put(REF_ANN, KEEP + SEPARATOR + EXCLUDE + NAMESPACE
                    + NS_SEPARATOR + NAMESPACE_2);
            //delete(new Delete(type, id, options));

            List<String> ns = new ArrayList<String>();
            ns.add(NAMESPACE);
            ns.add(NAMESPACE_2);
            final Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    type, Collections.singletonList(id));
            final ChildOption co = new ChildOption();
            co.excludeNs = ns;
            dc.childOptions = Collections.singletonList(co);
            callback(true, client, dc);

            assertDoesNotExist(obj);

            param = new ParametersI();
            param.addIds(annotationIds);
            assertTrue(annotationIds.size() > 0);
            sql = "select i from Annotation as i where i.id in (:ids)";
            l = iQuery.findAllByQuery(sql, param);
            assertEquals(obj + "-->" + l.toString(), annotationIds.size(),
                    l.size());
            param = new ParametersI();
            param.addIds(annotationIdsNS);
            assertTrue(annotationIdsNS.size() > 0);
            sql = "select i from Annotation as i where i.id in (:ids)";
            l = iQuery.findAllByQuery(sql, param);
            assertEquals(obj + "-->" + l.toString(), 0, l.size());
        }
    }

    /**
     * Test to delete an image with acquisition data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithAcquisitionDataUsingQueue() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // create an instrument.
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        assertNotNull(instrument);

        // retrieve the elements we need for the settings.
        // retrieve the detector.
        ParametersI param = new ParametersI();
        param.addLong("iid", instrument.getId().getValue());
        String sql = "select d from Detector as d where d.instrument.id = :iid";
        Detector detector = (Detector) iQuery.findByQuery(sql, param);
        sql = "select d from FilterSet as d where d.instrument.id = :iid";
        FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
        sql = "select d from Laser as d where d.instrument.id = :iid";
        Laser laser = (Laser) iQuery.findByQuery(sql, param);
        sql = "select d from Dichroic as d where d.instrument.id = :iid";
        Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
        sql = "select d from OTF as d where d.instrument.id = :iid";
        OTF otf = (OTF) iQuery.findByQuery(sql, param);
        sql = "select d from Objective as d where d.instrument.id = :iid";
        Objective objective = (Objective) iQuery.findByQuery(sql, param);

        img.setInstrument(instrument);
        img.setImagingEnvironment(mmFactory.createImageEnvironment());
        img.setObjectiveSettings(mmFactory.createObjectiveSettings(objective));
        img.setStageLabel(mmFactory.createStageLabel());
        iUpdate.saveAndReturnObject(img);
        param = new ParametersI();
        param.acquisitionData();
        List<Long> ids = new ArrayList<Long>();
        ids.add(img.getId().getValue());
        // method already tested in PojosService test
        List results = factory.getContainerService().getImages(
                Image.class.getName(), ids, param);
        img = (Image) results.get(0);
        ObjectiveSettings settings = img.getObjectiveSettings();
        StageLabel label = img.getStageLabel();
        ImagingEnvironment env = img.getImagingEnvironment();

        LogicalChannel lc;
        Channel channel;
        ids = new ArrayList<Long>();
        long detectorSettingsID = 0;
        long lightSourceSettingsID = 0;
        long lightPathID = 0;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setOtf(otf);
            lc.setDetectorSettings(mmFactory.createDetectorSettings(detector));
            lc.setFilterSet(filterSet);
            lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
            lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
            lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
            assertNotNull(lc);
            ids.add(lc.getId().getValue());
            detectorSettingsID = lc.getDetectorSettings().getId().getValue();
            lightSourceSettingsID = lc.getLightSourceSettings().getId()
                    .getValue();
            lightPathID = lc.getLightPath().getId().getValue();
        }

        // Now we try to delete the image.
        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        callback(true, client, dc);

        // Follow the section with acquisition data.
        // Now check if the settings are still there.

        param = new ParametersI();
        param.addId(detectorSettingsID);
        sql = "select d from DetectorSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightSourceSettingsID);
        sql = "select d from LightSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightPathID);
        sql = "select d from LightPath as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // instrument
        param.addId(instrument.getId().getValue());
        sql = "select d from Instrument as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(detector.getId().getValue());
        sql = "select d from Detector as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(otf.getId().getValue());
        sql = "select d from OTF as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(objective.getId().getValue());
        sql = "select d from Objective as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(dichroic.getId().getValue());
        sql = "select d from Dichroic as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(filterSet.getId().getValue());
        sql = "select d from FilterSet as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(laser.getId().getValue());
        sql = "select d from Laser as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(settings.getId().getValue());
        sql = "select d from ObjectiveSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(env.getId().getValue());
        sql = "select d from ImagingEnvironment as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(label.getId().getValue());
        sql = "select d from StageLabel as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete an image with an instrument with 2 objectives and 2 OTF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithInstrument() throws Exception {
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // create an instrument.
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        Objective objective = mmFactory.createObjective();
        OTF otf = mmFactory.createOTF(instrument.copyFilterSet().get(0),
                objective);
        instrument.addOTF(otf);
        instrument.addObjective(objective);

        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);

        assertNotNull(instrument);

        // retrieve the elements we need for the settings.
        // retrieve the detector.
        ParametersI param = new ParametersI();
        param.addLong("iid", instrument.getId().getValue());
        String sql = "select d from Detector as d where d.instrument.id = :iid";
        Detector detector = (Detector) iQuery.findByQuery(sql, param);
        sql = "select d from FilterSet as d where d.instrument.id = :iid";
        FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
        sql = "select d from Laser as d where d.instrument.id = :iid";
        Laser laser = (Laser) iQuery.findByQuery(sql, param);
        sql = "select d from Dichroic as d where d.instrument.id = :iid";
        Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
        sql = "select d from OTF as d where d.instrument.id = :iid";
        List<IObject> l = iQuery.findAllByQuery(sql, param);
        otf = (OTF) l.get(0);
        sql = "select d from Objective as d where d.instrument.id = :iid";
        l = iQuery.findAllByQuery(sql, param);
        Iterator<IObject> j = l.iterator();
        long objectiveID = otf.getObjective().getId().getValue();
        IObject iObject;
        while (j.hasNext()) {
            iObject = j.next();
            if (objectiveID != iObject.getId().getValue()) {
                objective = (Objective) iObject;
                break;
            }
        }
        img.setInstrument(instrument);
        img.setImagingEnvironment(mmFactory.createImageEnvironment());
        img.setObjectiveSettings(mmFactory.createObjectiveSettings(objective));
        img.setStageLabel(mmFactory.createStageLabel());
        iUpdate.saveAndReturnObject(img);

        param = new ParametersI();
        param.acquisitionData();
        List<Long> ids = new ArrayList<Long>();
        ids.add(img.getId().getValue());
        // method already tested in PojosService test
        List results = factory.getContainerService().getImages(
                Image.class.getName(), ids, param);
        img = (Image) results.get(0);
        ObjectiveSettings settings = img.getObjectiveSettings();
        StageLabel label = img.getStageLabel();
        ImagingEnvironment env = img.getImagingEnvironment();

        LogicalChannel lc;
        Channel channel;
        ids = new ArrayList<Long>();
        long detectorSettingsID = 0;
        long lightSourceSettingsID = 0;
        long lightPathID = 0;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setOtf(otf);
            lc.setDetectorSettings(mmFactory.createDetectorSettings(detector));
            lc.setFilterSet(filterSet);
            lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
            lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
            lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
            assertNotNull(lc);
            ids.add(lc.getId().getValue());
            detectorSettingsID = lc.getDetectorSettings().getId().getValue();
            lightSourceSettingsID = lc.getLightSourceSettings().getId()
                    .getValue();
            lightPathID = lc.getLightPath().getId().getValue();
        }

        // Now we try to delete the image.
        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        callback(true, client, dc);

        // Follow the section with acquisition data.
        // Now check if the settings are still there.

        param = new ParametersI();
        param.addId(detectorSettingsID);
        sql = "select d from DetectorSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightSourceSettingsID);
        sql = "select d from LightSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(lightPathID);
        sql = "select d from LightPath as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // instrument
        param.addId(instrument.getId().getValue());
        sql = "select d from Instrument as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
        param.addId(detector.getId().getValue());
        sql = "select d from Detector as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(instrument.getId().getValue());

        sql = "select d from OTF as d where d.instrument.id = :id";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);

        sql = "select d from Objective as d where d.instrument.id = :id";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);

        param.addId(dichroic.getId().getValue());
        sql = "select d from Dichroic as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(filterSet.getId().getValue());
        sql = "select d from FilterSet as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(laser.getId().getValue());
        sql = "select d from Laser as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(settings.getId().getValue());
        sql = "select d from ObjectiveSettings as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(env.getId().getValue());
        sql = "select d from ImagingEnvironment as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param.addId(env.getId().getValue());
        sql = "select d from StageLabel as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete the plate acquisition of a plate with 2 plate
     * acquisitions.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateAcquisition() throws Exception {
        Plate p;
        ParametersI param;
        int n = 2;
        int fields = 3;
        p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1,
                fields, n, false));
        String sql = "select pa from PlateAcquisition as pa ";
        sql += "where pa.plate.id = :id";
        param = new ParametersI();
        param.addId(p.getId().getValue());
        List<IObject> pas = iQuery.findAllByQuery(sql, param);
        assertEquals(pas.size(), n);
        // Delete the first one.
        long id = pas.get(0).getId().getValue();

        param = new ParametersI();
        param.addId(id);
        sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "where pa.id = :id";
        List<IObject> wellSamples = iQuery.findAllByQuery(sql, param);
        assertEquals(wellSamples.size(), fields);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                PlateAcquisition.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);

        sql = "select pa from PlateAcquisition as pa ";
        sql += "where pa.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "where pa.id = :id";
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);

        sql = "select pa from PlateAcquisition as pa ";
        sql += "where pa.plate.id = :id";
        param = new ParametersI();
        param.addId(p.getId().getValue());
        pas = iQuery.findAllByQuery(sql, param);
        assertEquals(pas.size(), (n - 1));
        PlateAcquisition pa = (PlateAcquisition) pas.get(0);

        List<Long> annotationIds = new ArrayList<Long>();
        annotationIds.addAll(createNonSharableAnnotation(pa, null));
        id = pa.getId().getValue();

        param = new ParametersI();
        param.addId(id);
        sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "where pa.id = :id";
        List<IObject> samples = iQuery.findAllByQuery(sql, param);
        assertEquals(samples.size(), fields);
        Iterator<IObject> i = samples.iterator();
        List<Long> imageIds = new ArrayList<Long>();
        WellSample ws;
        while (i.hasNext()) {
            ws = (WellSample) i.next();
            imageIds.add(ws.getImage().getId().getValue());
            annotationIds.addAll(createNonSharableAnnotation(ws.getImage(), null));
        }
        assertEquals(imageIds.size(), fields);
        assertTrue(annotationIds.size() > 0);
        // now delete the plate acquisition
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                PlateAcquisition.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);

        // Annotation should be gone
        sql = "select a from Annotation as a ";
        sql += "where a.id in (:ids)";
        param = new ParametersI();
        param.addIds(annotationIds);
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);
        sql = "select a from Image as a ";
        sql += "where a.id in (:ids)";
        param = new ParametersI();
        param.addIds(imageIds);
        assertEquals(iQuery.findAllByQuery(sql, param).size(), 0);
    }

    /**
     * Test to delete a project but not the datasets. The method tests the
     * <code>KEEP</code> option.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteProjectNotContent() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        ProjectDatasetLink pl = new ProjectDatasetLinkI();
        pl.link(new ProjectI(p.getId().getValue(), false), new DatasetI(d
                .getId().getValue(), false));
        iUpdate.saveAndReturnObject(pl);

        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        DatasetImageLink dl = new DatasetImageLinkI();
        dl.link(new DatasetI(d.getId().getValue(), false), new ImageI(image
                .getId().getValue(), false));
        iUpdate.saveAndReturnObject(dl);
        // Now delete the project
        List<String> options = new ArrayList<String>();
        options.add(Dataset.class.getSimpleName());
        options.add(Image.class.getSimpleName());
        long id = p.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Project.class.getSimpleName(),
                Collections.singletonList(id));
        ChildOption co = new ChildOption();
        co.excludeType = options;
        callback(true, client, dc);

        assertDoesNotExist(p);
        assertExists(d);
        assertExists(image);

        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());
        String sql = "select p from DatasetImageLink as p ";
        sql += "where p.child.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to delete a screen but not the plates. The method tests the
     * <code>KEEP</code> option.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteScreenNotContent() throws Exception {
        Screen s = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());
        ScreenPlateLink l = new ScreenPlateLinkI();
        l.link(new ScreenI(s.getId().getValue(), false), p);
        iUpdate.saveAndReturnObject(l);

        // Now delete the screen
        long id = s.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Screen.class.getSimpleName(),
                Collections.singletonList(id));
        ChildOption co = new ChildOption();
        co.excludeType = Collections.singletonList(Plate.class.getSimpleName());
        callback(true, client, dc);

        assertDoesNotExist(s);
        assertExists(p);

    }

    /**
     * Test to delete a dataset but not the images. Test the <code>KEEP</code>
     * option.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteDatasetNotContent() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        Pixels pixels = img.getPrimaryPixels();
        assertNotNull(pixels);
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        iUpdate.saveAndReturnObject(l);

        // Now delete the dataset
        long id = d.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(id));
        ChildOption co = new ChildOption();
        co.excludeType = Collections.singletonList(Image.class.getSimpleName());
        callback(true, client, dc);

        assertDoesNotExist(d);
        assertExists(img);
        assertExists(pixels);
    }

    /**
     * Test to delete images sharing instrument, detector, objective, light etc.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImagesSharingAcquisitionData() throws Exception {
        Image img1 = mmFactory.createImage();
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        Pixels pixels = img1.getPrimaryPixels();
        long pixId1 = pixels.getId().getValue();

        Image img2 = mmFactory.createImage();
        img2 = (Image) iUpdate.saveAndReturnObject(img2);

        pixels = img2.getPrimaryPixels();
        long pixId2 = pixels.getId().getValue();

        // create an instrument.
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        assertNotNull(instrument);
        long instrumentID = instrument.getId().getValue();
        List<Detector> detectors = instrument.copyDetector();
        List<Objective> objectives = instrument.copyObjective();
        List<LightSource> lights = instrument.copyLightSource();
        List<FilterSet> filterSets = instrument.copyFilterSet();
        List<Dichroic> dichroics = instrument.copyDichroic();

        assertTrue(detectors.size() > 0);
        assertTrue(objectives.size() > 0);
        assertTrue(lights.size() > 0);
        assertTrue(filterSets.size() > 0);
        // Objective objective = instrument.c
        img1.setInstrument(instrument);
        img1.setObjectiveSettings(mmFactory.createObjectiveSettings(objectives
                .get(0)));
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        img2.setInstrument(instrument);
        img2.setObjectiveSettings(mmFactory.createObjectiveSettings(objectives
                .get(0)));
        img2 = (Image) iUpdate.saveAndReturnObject(img2);
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        IPixelsPrx prx = factory.getPixelsService();
        Pixels pixels1 = prx.retrievePixDescription(pixId1);
        Pixels pixels2 = prx.retrievePixDescription(pixId2);

        LogicalChannel lc;
        Channel channel;
        List<IObject> lcs = new ArrayList<IObject>();
        for (int i = 0; i < pixels1.getSizeC().getValue(); i++) {
            channel = pixels1.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setDetectorSettings(mmFactory.createDetectorSettings(detectors
                    .get(0)));
            lc.setFilterSet(filterSets.get(0));
            lc.setLightSourceSettings(mmFactory.createLightSettings(lights
                    .get(0)));
            lc.setLightPath(mmFactory.createLightPath(null, dichroics.get(0),
                    null));
            lcs.add(lc);
        }

        for (int i = 0; i < pixels2.getSizeC().getValue(); i++) {
            channel = pixels2.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setDetectorSettings(mmFactory.createDetectorSettings(detectors
                    .get(0)));
            lc.setFilterSet(filterSets.get(0));
            lc.setLightSourceSettings(mmFactory.createLightSettings(lights
                    .get(0)));
            lc.setLightPath(mmFactory.createLightPath(null, dichroics.get(0),
                    null));
            lcs.add(lc);
        }
        iUpdate.saveAndReturnArray(lcs);
        List<Request> commands = new ArrayList<Request>();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        commands.add(dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        commands.add(dc);
        DoAll all = new DoAll();
        all.requests = commands;
        doChange(client, factory, all, true, null);
        // Now delete the image.
        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        ParametersI param = new ParametersI();
        param.addIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        // check detectors
        ids.clear();
        Iterator<Detector> d = detectors.iterator();
        while (d.hasNext()) {
            ids.add(d.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Detector i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        ids.clear();
        Iterator<Objective> o = objectives.iterator();
        while (o.hasNext()) {
            ids.add(o.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Objective i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<FilterSet> fs = filterSets.iterator();
        while (fs.hasNext()) {
            ids.add(fs.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from FilterSet i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<Dichroic> di = dichroics.iterator();
        while (di.hasNext()) {
            ids.add(di.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Dichroic i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<LightSource> l = lights.iterator();
        while (l.hasNext()) {
            ids.add(l.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from LightSource i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
        param = new ParametersI();
        param.addId(instrumentID);
        sb = new StringBuilder();
        sb.append("select i from Instrument i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to delete images sharing detector settings, objective settings, etc.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImagesSharingAcquisitionSettings() throws Exception {
        Image img1 = mmFactory.createImage();
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        Pixels pixels = img1.getPrimaryPixels();
        long pixId1 = pixels.getId().getValue();

        Image img2 = mmFactory.createImage();
        img2 = (Image) iUpdate.saveAndReturnObject(img2);

        pixels = img2.getPrimaryPixels();
        long pixId2 = pixels.getId().getValue();

        // create an instrument.
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        assertNotNull(instrument);
        long instrumentID = instrument.getId().getValue();
        List<Detector> detectors = instrument.copyDetector();
        List<Objective> objectives = instrument.copyObjective();
        List<LightSource> lights = instrument.copyLightSource();
        List<FilterSet> filterSets = instrument.copyFilterSet();
        List<Dichroic> dichroics = instrument.copyDichroic();

        assertTrue(detectors.size() > 0);
        assertTrue(objectives.size() > 0);
        assertTrue(lights.size() > 0);
        assertTrue(filterSets.size() > 0);
        // Objective objective = instrument.
        ObjectiveSettings os = mmFactory.createObjectiveSettings(objectives
                .get(0));
        img1.setInstrument(instrument);
        img1.setObjectiveSettings(os);
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        img2.setInstrument(instrument);
        img2.setObjectiveSettings(os);
        img2 = (Image) iUpdate.saveAndReturnObject(img2);
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        IPixelsPrx prx = factory.getPixelsService();
        Pixels pixels1 = prx.retrievePixDescription(pixId1);
        Pixels pixels2 = prx.retrievePixDescription(pixId2);

        LogicalChannel lc;
        Channel channel;
        DetectorSettings ds = mmFactory
                .createDetectorSettings(detectors.get(0));
        LightSettings ls = mmFactory.createLightSettings(lights.get(0));
        LightPath lp = mmFactory.createLightPath(null, dichroics.get(0), null);
        List<IObject> lcs = new ArrayList<IObject>();
        for (int i = 0; i < pixels1.getSizeC().getValue(); i++) {
            channel = pixels1.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setDetectorSettings(ds);
            lc.setFilterSet(filterSets.get(0));
            lc.setLightSourceSettings(ls);
            lc.setLightPath(lp);
            lcs.add(lc);
        }

        for (int i = 0; i < pixels2.getSizeC().getValue(); i++) {
            channel = pixels2.getChannel(i);
            lc = channel.getLogicalChannel();
            lc.setDetectorSettings(ds);
            lc.setFilterSet(filterSets.get(0));
            lc.setLightSourceSettings(ls);
            lc.setLightPath(lp);
            lcs.add(lc);
        }
        iUpdate.saveAndReturnArray(lcs);
        List<Request> commands = new ArrayList<Request>();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        commands.add(dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        commands.add(dc);
        DoAll all = new DoAll();
        all.requests = commands;
        doChange(client, factory, all, true, null);
        // Now delete the image.
        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        ParametersI param = new ParametersI();
        param.addIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        // check detectors
        ids.clear();
        Iterator<Detector> d = detectors.iterator();
        while (d.hasNext()) {
            ids.add(d.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Detector i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        ids.clear();
        Iterator<Objective> o = objectives.iterator();
        while (o.hasNext()) {
            ids.add(o.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Objective i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<FilterSet> fs = filterSets.iterator();
        while (fs.hasNext()) {
            ids.add(fs.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from FilterSet i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<Dichroic> di = dichroics.iterator();
        while (di.hasNext()) {
            ids.add(di.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from Dichroic i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        Iterator<LightSource> l = lights.iterator();
        while (l.hasNext()) {
            ids.add(l.next().getId().getValue());
        }
        param = new ParametersI();
        param.addIds(ids);
        sb = new StringBuilder();
        sb.append("select i from LightSource i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
        param = new ParametersI();
        param.addId(instrumentID);
        sb = new StringBuilder();
        sb.append("select i from Instrument i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to delete images sharing logical channels. This case may happen when
     * handling Plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImagesSharingLogicalChannels() throws Exception {
        Image img1 = mmFactory.createImage();
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        Pixels pixels = img1.getPrimaryPixels();
        long pixId1 = pixels.getId().getValue();

        Image img2 = mmFactory.createImage();
        img2 = (Image) iUpdate.saveAndReturnObject(img2);

        pixels = img2.getPrimaryPixels();
        long pixId2 = pixels.getId().getValue();

        IPixelsPrx prx = factory.getPixelsService();
        Pixels pixels1 = prx.retrievePixDescription(pixId1);
        Pixels pixels2 = prx.retrievePixDescription(pixId2);

        Channel channel;
        LogicalChannel lc;
        List<LogicalChannel> list = new ArrayList<LogicalChannel>();

        for (int i = 0; i < pixels1.getSizeC().getValue(); i++) {
            channel = pixels1.getChannel(i);
            lc = channel.getLogicalChannel();
            list.add(lc);
        }
        List<IObject> l = new ArrayList<IObject>();
        for (int i = 0; i < pixels2.getSizeC().getValue(); i++) {
            channel = pixels1.getChannel(i);
            channel.setLogicalChannel(list.get(i));
            l.add(channel);
        }
        iUpdate.saveAndReturnArray(l);
        List<Request> commands = new ArrayList<Request>();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        commands.add(dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        commands.add(dc);
        DoAll all = new DoAll();
        all.requests = commands;
        doChange(client, factory, all, true, null);
        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        ParametersI param = new ParametersI();
        param.addIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        ids.clear();
        Iterator<LogicalChannel> j = list.iterator();
        while (j.hasNext()) {
            ids.add(j.next().getId().getValue());
        }
        sb = new StringBuilder();
        sb.append("select i from LogicalChannel i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * After a discussion Sept. 2010, this is expected to fail. An Image in a
     * Well can only be deleted via the Well or the Plate.
     */
    @Test(groups = {"ticket:2768"})
    public void testDeleteImageThatsInAWell() throws Exception {

        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        List<Well> wells = loadWells(p.getId().getValue(), false);
        List<Image> images = new ArrayList<Image>();
        for (Well well : wells) {
            for (WellSample ws : well.copyWellSamples()) {
                images.add(ws.getImage());
            }
        }
        assertTrue(images.size() > 0);
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Image.class.getSimpleName(),
                    Collections.singletonList(images.get(0).getId().getValue()));
            callback(true, client, dc);
            fail("Should not be allowed.");
        } catch (AssertionError afe) {
            // Ok.
        }

        assertExists(wells.get(0));
        assertExists(images.get(0));
    }

    /**
     * Test to delete multiple images at the same time.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2877")
    public void testDeleteMultipleObjectsOfSameType() throws Exception {
        Image img1 = mmFactory.createImage();
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        Image img2 = mmFactory.createImage();
        img2 = (Image) iUpdate.saveAndReturnObject(img2);
        Delete2 dc = new Delete2();
        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),ids);
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to delete multiple images at the same time.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteMultipleObjectsOfDifferentTypes() throws Exception {
        Image img1 = mmFactory.createImage();
        img1 = (Image) iUpdate.saveAndReturnObject(img1);
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        List<Request> commands = new ArrayList<Request>();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        commands.add(dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d.getId().getValue()));
        commands.add(dc);
        DoAll all = new DoAll();
        all.requests = commands;
        doChange(client, factory, all, true, null);
        ParametersI param = new ParametersI();
        param.addId(img1.getId().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        param = new ParametersI();
        param.addId(d.getId().getValue());
        sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to delete a tagged image. The tag is also linked to another image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2945" })
    public void testDeleteTaggedImages() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        List<Long> ids = createSharableAnnotation(img1, img2);
        assertTrue(ids.size() > 0);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id in (:ids)");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
                ids.size());
    }

    /**
     * Tests to delete a file annotation and make sure that the original file is
     * deleted to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2884" })
    public void testDeleteFileAnnotation() throws Exception {
        // creation and linkage have already been tested
        // File
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(of);
        Annotation data = (Annotation) iUpdate.saveAndReturnObject(fa);
        long id = data.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);
        assertDoesNotExist(data);
        assertDoesNotExist(of);

    }

    /**
     * Test to make sure that the file annotation linked to several images is
     * kept when one of the images is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2884" })
    public void testDeleteFileAnnotationMultiplyLinked() throws Exception {
        //
        // Create two images
        //
        Image image0 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image image1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        of.unload();

        //
        // Create an annotation with a dummy file.
        //
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(of);
        long ofId = of.getId().getValue();
        Annotation data = (Annotation) iUpdate.saveAndReturnObject(fa);
        long id = data.getId().getValue();
        RawFileStorePrx prx = factory.createRawFileStore();
        try {
            prx.setFileId(ofId);
            prx.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
        } finally {
            prx.close();
        }

        //
        // Link annotation to both images
        //
        ImageAnnotationLink link0 = new ImageAnnotationLinkI();
        link0.link(image0, fa);
        link0 = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link0);

        ImageAnnotationLink link1 = new ImageAnnotationLinkI();
        link1.link(image1, fa);
        link1 = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link1);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image0.getId().getValue()));
        callback(true, client, dc);
        //
        // Check results
        //
        assertExists(of);
        assertExists(data);
        assertExists(image1);
        assertExists(link1);
        prx = factory.createRawFileStore();
        byte[] buf = null;
        try {
            prx.setFileId(ofId);
            buf = prx.read(0, 4);
        } finally {
            prx.close();
        }
        assertEquals(new byte[] { 1, 2, 3, 4 }, buf);
    }

    /**
     * Test to delete an image and make sure the original files are removed.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2884" })
    public void testDeleteImageAndOriginalFile() throws Exception {
        // First create an image
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();

        OriginalFile f = mmFactory.createOriginalFile();
        f = (OriginalFile) iUpdate.saveAndReturnObject(f);

        RawFileStorePrx svc = factory.createRawFileStore();
        svc.setFileId(f.getId().getValue());
        byte[] data = new byte[] { 1, 2 };
        svc.write(data, 0, data.length);
        svc.close();

        long fileID = f.getId().getValue();
        String sql = "select i from OriginalFile i where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(fileID);
        f = (OriginalFile) iQuery.findByQuery(sql, param);
        // upload file, method tested in RawFileStore
        assertNotNull(f);
        PixelsOriginalFileMapI m = new PixelsOriginalFileMapI();
        m.setChild(new PixelsI(pixels.getId().getValue(), false));
        m.setParent(f);
        m = (PixelsOriginalFileMapI) iUpdate.saveAndReturnObject(m);

        long imageID = image.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(imageID));
        callback(true, client, dc);
        sql = "select i from Pixels i where i.id = :id";
        param = new ParametersI();
        param.addId(pixels.getId().getValue());
        assertNull(iQuery.findByQuery(sql, param));

        assertDoesNotExist(f);
    }

    /**
     * Test to delete an image and make sure the thumbnail is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithThumbnail() throws Exception {
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        Thumbnail thumbnail = mmFactory.createThumbnail();
        thumbnail.setPixels(pixels);
        thumbnail = (Thumbnail) iUpdate.saveAndReturnObject(thumbnail);
        assertNotNull(thumbnail);
        long imageID = image.getId().getValue();
        long thumbnailID = thumbnail.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image.getId().getValue()));
        callback(true, client, dc);
        String sql = "select i from Thumbnail i where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(thumbnailID);
        assertNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Use of the savepoint/release/rollback methods in
     * {@link ome.services.graphs.BaseGraphSpec} seem to prevent transactions
     * from being properly rolled back.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2917"})
    public void testTxIntegrity() throws Exception {
        List<IObject> images = new ArrayList<IObject>();
        images.add(mmFactory.createImage());
        images.add(mmFactory.createImage());
        Fileset fileset = mmFactory.simpleFileset();
        ((Image) images.get(0)).setFileset(fileset);
        ((Image) images.get(1)).setFileset(fileset);
        List<IObject> objs = iUpdate.saveAndReturnArray(images);

        Image image0 = (Image) objs.get(0);
        Image image1 = (Image) objs.get(1);

        fileset = image0.getFileset();
        assertEquals(fileset, image1.getFileset());

        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Image.class.getSimpleName(),
                    Collections.singletonList(image0.getId().getValue()));
            callback(true, client, dc);
            fail("Should throw on constraint violation");
        } catch (AssertionError e) {
            // ok. constraint violation was thrown, there for the delete()
            // method failed, so now we can test that the tx was actually
            // rolled back.
        }

        // Now check that everything still exists.
        assertExists(image0);
        assertExists(image0.getPrimaryPixels());
        assertExists(image1);
        assertExists(image1.getPrimaryPixels());
        assertExists(fileset);
    }

    /**
     * Tests to delete the original file not linked to anything.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSlowDeleteOfOriginalFile() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                OriginalFile.class.getSimpleName(),
                Collections.singletonList(of.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(of);
    }

    /**
     * Tests to delete the original file linked to file annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSlowDeleteOfOriginalFileWithAnnotation() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        createSharableAnnotation(of, null);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                OriginalFile.class.getSimpleName(),
                Collections.singletonList(of.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(of);
    }

    /**
     * Tests to delete an object already deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteTwice() throws Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        callback(true, client, dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        callback(false, client, dc);
    }

    /**
     * Tests to delete an image with annotation using the
     * <code>deleteImage</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteFullImage() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());

        List<Long> ids = createNonSharableAnnotation(image, null);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image.getId().getValue()));
        callback(true, client, dc);
        String sql = "select a from Annotation as a where a.id in (:ids)";
        ParametersI p = new ParametersI();
        p.addIds(ids);
        assertEquals(iQuery.findAllByQuery(sql, p).size(), 0);
    }

    /**
     * Tests to delete an image with plane info linked to the pixels set using
     * the <code>deleteImage</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithPlaneInfo() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels = image.getPrimaryPixels();
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = mmFactory.createPlaneInfo();
        planeInfo.setPixels(pixels);
        planeInfo = (PlaneInfo) iUpdate.saveAndReturnObject(planeInfo);
        // now Delete the image.
        assertExists(planeInfo);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(image);
        assertDoesNotExist(pixels);
        assertDoesNotExist(planeInfo);
    }

    /**
     * Tests to delete an imported image. The image should have all the model
     * objects, a companion file and a thumbnail.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:3030")
    public void testDeleteImportedImage() throws Exception {
        File f = File.createTempFile("testDeleteImportedImage", "."
                + ImporterTest.OME_FORMAT);
        f.deleteOnExit();
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImageWithAcquisitionData(), true);
        OMEROMetadataStoreClient importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);
        List<Pixels> list;
        try {
            list = importFile(importer, f, ImporterTest.OME_FORMAT, false);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        IMetadataPrx iMetadata = factory.getMetadataService();

        Pixels pixels = list.get(0);
        long id = pixels.getId().getValue();

        pixels = factory.getPixelsService().retrievePixDescription(id);

        List<Channel> channels = pixels.copyChannels();

        assertTrue(channels.size() > 0);
        Channel channel;
        Iterator<Channel> j = channels.iterator();
        long lcID;
        List<Long> ids;
        LogicalChannel lc;
        List l;
        List<LogicalChannel> logicalChannels = new ArrayList<LogicalChannel>();
        List<LightPath> lightPaths = new ArrayList<LightPath>();
        List<DetectorSettings> detectorSettings = new ArrayList<DetectorSettings>();
        List<LightSettings> lightSourceSettings = new ArrayList<LightSettings>();
        while (j.hasNext()) {
            channel = j.next();
            ids = new ArrayList<Long>(1);
            lcID = channel.getLogicalChannel().getId().getValue();
            ids.add(lcID);
            l = iMetadata.loadChannelAcquisitionData(ids);
            lc = (LogicalChannel) l.get(0);
            logicalChannels.add(lc);
            lightPaths.add(lc.getLightPath());
            detectorSettings.add(lc.getDetectorSettings());
            lightSourceSettings.add(lc.getLightSourceSettings());
        }

        String sql = "select info from PlaneInfo as info where pixels.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);

        List<IObject> planes = iQuery.findAllByQuery(sql, param);
        assertTrue(planes.size() > 0);

        long imageID = pixels.getImage().getId().getValue();
        ParametersI po = new ParametersI();
        po.acquisitionData();
        ids = new ArrayList<Long>(1);
        ids.add(imageID);
        List images = factory.getContainerService().getImages(
                Image.class.getName(), ids, po);
        Image image = (Image) images.get(0);
        long instrumentID = image.getInstrument().getId().getValue();

        Instrument instrument = factory.getMetadataService().loadInstrument(
                instrumentID);
        ImagingEnvironment env = image.getImagingEnvironment();
        Microscope miscrocope = instrument.getMicroscope();
        StageLabel stage = image.getStageLabel();
        ObjectiveSettings settings = image.getObjectiveSettings();
        Experiment experiment = image.getExperiment();
        // from instrument
        List<Detector> detectors = instrument.copyDetector();
        List<Dichroic> dichroics = instrument.copyDichroic();
        List<Filter> filters = instrument.copyFilter();
        List<Objective> objectives = instrument.copyObjective();
        List<LightSource> lightSources = instrument.copyLightSource();
        List<OTF> otfs = instrument.copyOtf();

        // Delete the image.
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(image.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(image);
        assertDoesNotExist(pixels);

        assertDoesNotExist(instrument);
        assertDoesNotExist(miscrocope);
        assertDoesNotExist(env);
        assertDoesNotExist(stage);
        assertDoesNotExist(settings);
        assertDoesNotExist(experiment);

        Iterator i = planes.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = detectors.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = dichroics.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = filters.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = objectives.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = lightSources.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = otfs.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = planes.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = channels.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = logicalChannels.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = lightPaths.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = detectorSettings.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
        i = lightSourceSettings.iterator();
        while (i.hasNext()) {
            assertDoesNotExist((IObject) i.next());
        }
    }

    /**
     * Simulates an SVS import in which many Pixels are attached to a single,
     * archived OriginalFile.
     */
    public void testDeletePixelsAndFiles() throws Exception {
        Image img1 = mmFactory.createImage();
        Image img2 = mmFactory.createImage();
        OriginalFile file = mmFactory.createOriginalFile();
        img1.getPrimaryPixels().linkOriginalFile(file);
        img2.getPrimaryPixels().linkOriginalFile(file);

        file = (OriginalFile) iUpdate.saveAndReturnObject(file);
        img1 = file.linkedPixelsList().get(0).getImage();
        img2 = file.linkedPixelsList().get(1).getImage();

        assertExists(img1);
        assertExists(img2);
        assertExists(file);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img1.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(img1);
        assertExists(img2);
        assertExists(file);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(img1);
        assertDoesNotExist(img2);
        assertDoesNotExist(file);

    }
}
