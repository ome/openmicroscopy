/*
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import ome.xml.model.OME;
import ome.xml.model.primitives.Color;
import omero.api.IAdminPrx;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.cmd.Delete2;
import omero.gateway.util.Requests;
import omero.model.Annotation;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Dichroic;
import omero.model.Ellipse;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterI;
import omero.model.Filament;
import omero.model.Filter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImagingEnvironment;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightEmittingDiode;
import omero.model.LightPath;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Line;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.Mask;
import omero.model.MicrobeamManipulation;
import omero.model.Microscope;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Point;
import omero.model.Polyline;
import omero.model.Reagent;
import omero.model.Rectangle;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.Shape;
import omero.model.StageLabel;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.TransmittanceRange;
import omero.model.Well;
import omero.model.WellReagentLink;
import omero.model.WellSample;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Collection of tests to import images. The imported images are not currently
 * deleted after the test.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ImporterTest extends AbstractServerTest {

    /** {@link EventContext} that is set on {@link #loginMethod()} */
    private EventContext ownerEc;

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param objective
     *            The objective to check.
     * @param xml
     *            The XML version.
     */
    private void validateObjective(Objective objective,
            ome.xml.model.Objective xml) {
        Assert.assertEquals(objective.getManufacturer().getValue(),
                xml.getManufacturer());
        Assert.assertEquals(objective.getModel().getValue(), xml.getModel());
        Assert.assertEquals(objective.getSerialNumber().getValue(),
                xml.getSerialNumber());
        Assert.assertEquals(objective.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(objective.getCalibratedMagnification().getValue(), xml
                .getCalibratedMagnification().doubleValue());
        Assert.assertEquals(objective.getCorrection().getValue().getValue(), xml
                .getCorrection().getValue());
        Assert.assertEquals(objective.getImmersion().getValue().getValue(), xml
                .getImmersion().getValue());
        Assert.assertEquals(objective.getIris().getValue(), xml.getIris()
                .booleanValue());
        Assert.assertEquals(objective.getLensNA().getValue(), xml.getLensNA()
                .doubleValue());
        Assert.assertEquals(objective.getNominalMagnification().getValue(),
                xml.getNominalMagnification());
        Assert.assertEquals(objective.getWorkingDistance().getValue(),
                xml.getWorkingDistance().value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param detector
     *            The detector to check.
     * @param xml
     *            The XML version.
     */
    private void validateDetector(Detector detector, ome.xml.model.Detector xml) {
        Assert.assertEquals(detector.getManufacturer().getValue(),
                xml.getManufacturer());
        Assert.assertEquals(detector.getModel().getValue(), xml.getModel());
        Assert.assertEquals(detector.getSerialNumber().getValue(),
                xml.getSerialNumber());
        Assert.assertEquals(detector.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(detector.getAmplificationGain().getValue(), xml
                .getAmplificationGain().doubleValue());
        Assert.assertEquals(detector.getGain().getValue(), xml.getGain());
        Assert.assertEquals(detector.getZoom().getValue(), xml.getZoom());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param arc
     *            The arc to check.
     * @param xml
     *            The XML version.
     */
    private void validateArc(Arc arc, ome.xml.model.Arc xml) {
        Assert.assertEquals(arc.getManufacturer().getValue(), xml.getManufacturer());
        Assert.assertEquals(arc.getModel().getValue(), xml.getModel());
        Assert.assertEquals(arc.getSerialNumber().getValue(), xml.getSerialNumber());
        Assert.assertEquals(arc.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(arc.getPower().getValue(), xml.getPower().value());
        Assert.assertEquals(arc.getType().getValue().getValue(),
                XMLMockObjects.ARC_TYPE.getValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param laser
     *            The laser to check.
     * @param xml
     *            The XML version.
     */
    private void validateLaser(Laser laser, ome.xml.model.Laser xml) {
        Assert.assertEquals(laser.getManufacturer().getValue(), xml.getManufacturer());
        Assert.assertEquals(laser.getModel().getValue(), xml.getModel());
        Assert.assertEquals(laser.getSerialNumber().getValue(), xml.getSerialNumber());
        Assert.assertEquals(laser.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(laser.getPower().getValue(), xml.getPower().value());
        Assert.assertEquals(laser.getType().getValue().getValue(),
                XMLMockObjects.LASER_TYPE.getValue());
        Assert.assertEquals(laser.getFrequencyMultiplication().getValue(),
                xml.getFrequencyMultiplication().getValue().intValue());
        Assert.assertEquals(laser.getLaserMedium().getValue().getValue(),
               xml.getLaserMedium().getValue());
        Assert.assertEquals(laser.getPockelCell().getValue(),
                xml.getPockelCell().booleanValue());
        Assert.assertEquals(laser.getTuneable().getValue(),
                xml.getTuneable().booleanValue());
        Assert.assertEquals(laser.getRepetitionRate().getValue(),
                xml.getRepetitionRate().value());
        Assert.assertEquals(laser.getWavelength().getValue(),
                xml.getWavelength().value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param filament
     *            The filament to check.
     * @param xml
     *            The XML version.
     */
    private void validateFilament(Filament filament, ome.xml.model.Filament xml) {
        Assert.assertEquals(filament.getManufacturer().getValue(),
                xml.getManufacturer());
        Assert.assertEquals(filament.getModel().getValue(), xml.getModel());
        Assert.assertEquals(filament.getSerialNumber().getValue(),
                xml.getSerialNumber());
        Assert.assertEquals(filament.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(filament.getPower().getValue(), xml.getPower().value());
        Assert.assertEquals(filament.getType().getValue().getValue(),
                XMLMockObjects.FILAMENT_TYPE.getValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param filter
     *            The filter to check.
     * @param xml
     *            The XML version.
     */
    private void validateFilter(Filter filter, ome.xml.model.Filter xml) {
        Assert.assertEquals(filter.getManufacturer().getValue(), xml.getManufacturer());
        Assert.assertEquals(filter.getModel().getValue(), xml.getModel());
        Assert.assertEquals(filter.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(filter.getSerialNumber().getValue(), xml.getSerialNumber());
        Assert.assertEquals(filter.getType().getValue().getValue(), xml.getType()
                .getValue());
        TransmittanceRange tr = filter.getTransmittanceRange();
        ome.xml.model.TransmittanceRange xmlTr = xml.getTransmittanceRange();
        Assert.assertEquals(tr.getCutIn().getValue(), xmlTr.getCutIn().value());
        Assert.assertEquals(tr.getCutOut().getValue(), xmlTr.getCutOut().value());
        Assert.assertEquals(tr.getCutInTolerance().getValue(), xmlTr.getCutInTolerance().value());
        Assert.assertEquals(tr.getCutOutTolerance().getValue(), xmlTr.getCutOutTolerance().value());
        Assert.assertEquals(tr.getTransmittance().getValue(), xmlTr.getTransmittance().getValue().doubleValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param dichroic
     *            The dichroic to check.
     * @param xml
     *            The XML version.
     */
    private void validateDichroic(Dichroic dichroic, ome.xml.model.Dichroic xml) {
        Assert.assertEquals(dichroic.getManufacturer().getValue(),
                xml.getManufacturer());
        Assert.assertEquals(dichroic.getModel().getValue(), xml.getModel());
        Assert.assertEquals(dichroic.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(dichroic.getSerialNumber().getValue(),
                xml.getSerialNumber());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param diode
     *            The light emitting diode to check.
     * @param xml
     *            The XML version.
     */
    private void validateLightEmittingDiode(LightEmittingDiode diode,
            ome.xml.model.LightEmittingDiode xml) {
        Assert.assertEquals(diode.getManufacturer().getValue(), xml.getManufacturer());
        Assert.assertEquals(diode.getModel().getValue(), xml.getModel());
        Assert.assertEquals(diode.getSerialNumber().getValue(), xml.getSerialNumber());
        Assert.assertEquals(diode.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(diode.getPower().getValue(), xml.getPower().value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param settings
     *            The settings to check.
     * @param xml
     *            The XML version.
     */
    private void validateDetectorSettings(DetectorSettings settings,
            ome.xml.model.DetectorSettings xml) {
        Assert.assertEquals(settings.getBinning().getValue().getValue(), xml
                .getBinning().getValue());
        Assert.assertEquals(settings.getGain().getValue(), xml.getGain());
        Assert.assertEquals(settings.getOffsetValue().getValue(), xml.getOffset());
        Assert.assertEquals(settings.getReadOutRate().getValue(),
                xml.getReadOutRate().value());
        Assert.assertEquals(settings.getVoltage().getValue(), xml.getVoltage().value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param settings
     *            The settings to check.
     * @param xml
     *            The XML version.
     */
    private void validateObjectiveSettings(ObjectiveSettings settings,
            ome.xml.model.ObjectiveSettings xml) {
        Assert.assertEquals(settings.getCorrectionCollar().getValue(), xml
                .getCorrectionCollar().doubleValue());
        Assert.assertEquals(settings.getRefractiveIndex().getValue(), xml
                .getRefractiveIndex().doubleValue());
        Assert.assertEquals(settings.getMedium().getValue().getValue(), xml
                .getMedium().getValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param settings
     *            The settings to check.
     * @param xml
     *            The XML version.
     */
    private void validateLightSourceSettings(LightSettings settings,
            ome.xml.model.LightSourceSettings xml) {
        Assert.assertEquals(settings.getAttenuation().getValue(), xml.getAttenuation()
                .getValue().doubleValue());
        Assert.assertEquals(settings.getWavelength().getValue(), xml.getWavelength()
                .value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param env
     *            The environment to check.
     * @param xml
     *            The XML version.
     */
    private void validateImagingEnvironment(ImagingEnvironment env,
            ome.xml.model.ImagingEnvironment xml) {
        Assert.assertEquals(env.getAirPressure().getValue(), xml.getAirPressure()
                .value());
        Assert.assertEquals(env.getCo2percent().getValue(), xml.getCO2Percent()
                .getValue().doubleValue());
        Assert.assertEquals(env.getHumidity().getValue(), xml.getHumidity().getValue()
                .doubleValue());
        Assert.assertEquals(env.getTemperature().getValue(), xml.getTemperature()
                .value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param label
     *            The label to check.
     * @param xml
     *            The XML version.
     */
    private void validateStageLabel(StageLabel label,
            ome.xml.model.StageLabel xml) {
        Assert.assertEquals(label.getName().getValue(), xml.getName());
        Assert.assertEquals(label.getPositionX().getValue(), xml.getX().value());
        Assert.assertEquals(label.getPositionY().getValue(), xml.getY().value());
        Assert.assertEquals(label.getPositionZ().getValue(), xml.getZ().value());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param microscope
     *            The microscope to check.
     * @param xml
     *            The XML version.
     */
    private void validateMicroscope(Microscope microscope,
            ome.xml.model.Microscope xml) {
        Assert.assertEquals(microscope.getManufacturer().getValue(),
                xml.getManufacturer());
        Assert.assertEquals(microscope.getModel().getValue(), xml.getModel());
        Assert.assertEquals(microscope.getSerialNumber().getValue(),
                xml.getSerialNumber());
        Assert.assertEquals(microscope.getLotNumber().getValue(), xml.getLotNumber());
        Assert.assertEquals(microscope.getType().getValue().getValue(), xml.getType()
                .getValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param lc
     *            The logical channel to check.
     * @param xml
     *            The XML version.
     */
    private void validateChannel(LogicalChannel lc, ome.xml.model.Channel xml) {
        Assert.assertEquals(lc.getName().getValue(), xml.getName());
        Assert.assertEquals(lc.getIllumination().getValue().getValue(), xml
                .getIlluminationType().getValue());
        Assert.assertEquals(lc.getMode().getValue().getValue(), xml
                .getAcquisitionMode().getValue());
        Assert.assertEquals(lc.getContrastMethod().getValue().getValue(), xml
                .getContrastMethod().getValue());
        Assert.assertEquals(lc.getEmissionWave().getValue(), xml
                .getEmissionWavelength().value());
        Assert.assertEquals(lc.getExcitationWave().getValue(), xml
                .getExcitationWavelength().value());
        Assert.assertEquals(lc.getFluor().getValue(), xml.getFluor());
        Assert.assertEquals(lc.getNdFilter().getValue(), xml.getNDFilter());
        Assert.assertEquals(lc.getPockelCellSetting().getValue(), xml
                .getPockelCellSetting().intValue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param plate
     *            The plate to check.
     * @param xml
     *            The XML version.
     */
    private void validatePlate(Plate plate, ome.xml.model.Plate xml) {
        Assert.assertEquals(plate.getName().getValue(), xml.getName());
        Assert.assertEquals(plate.getDescription().getValue(), xml.getDescription());
        Assert.assertEquals(plate.getRowNamingConvention().getValue(), xml
                .getRowNamingConvention().getValue());
        Assert.assertEquals(plate.getColumnNamingConvention().getValue(), xml
                .getColumnNamingConvention().getValue());
        Assert.assertEquals(plate.getRows().getValue(), xml.getRows().getValue()
                .intValue());
        Assert.assertEquals(plate.getColumns().getValue(), xml.getColumns().getValue()
                .intValue());
        Assert.assertEquals(plate.getExternalIdentifier().getValue(),
                xml.getExternalIdentifier());
        Assert.assertEquals(plate.getWellOriginX().getValue(), xml.getWellOriginX()
                .value().doubleValue());
        Assert.assertEquals(plate.getWellOriginY().getValue(), xml.getWellOriginY()
                .value().doubleValue());
        Assert.assertEquals(plate.getStatus().getValue(), xml.getStatus());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param screen
     *            The screen to check.
     * @param xml
     *            The XML version.
     */
    private void validateScreen(Screen screen, ome.xml.model.Screen xml) {
        Assert.assertEquals(screen.getName().getValue(), xml.getName());
        Assert.assertEquals(screen.getDescription().getValue(), xml.getDescription());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param reagent
     *            The reagent to check.
     * @param xml
     *            The XML version.
     */
    private void validateReagent(Reagent reagent, ome.xml.model.Reagent xml) {
        Assert.assertEquals(reagent.getName().getValue(), xml.getName());
        Assert.assertEquals(reagent.getDescription().getValue(), xml.getDescription());
        Assert.assertEquals(reagent.getReagentIdentifier().getValue(),
                xml.getReagentIdentifier());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param well
     *            The plate to check.
     * @param xml
     *            The XML version.
     */
    private void validateWell(Well well, ome.xml.model.Well xml) {
        Assert.assertEquals(well.getColumn().getValue(), xml.getColumn().getValue()
                .intValue());
        Assert.assertEquals(well.getRow().getValue(), xml.getRow().getValue()
                .intValue());
        Assert.assertEquals(well.getExternalDescription().getValue(),
                xml.getExternalDescription());
        Assert.assertEquals(well.getExternalIdentifier().getValue(),
                xml.getExternalIdentifier());
        Color source = xml.getColor();
        java.awt.Color xmlColor = new java.awt.Color(source.getRed(),
                source.getGreen(), source.getBlue(), source.getAlpha());
        Assert.assertEquals(well.getAlpha().getValue(), xmlColor.getAlpha());
        Assert.assertEquals(well.getRed().getValue(), xmlColor.getRed());
        Assert.assertEquals(well.getGreen().getValue(), xmlColor.getGreen());
        Assert.assertEquals(well.getBlue().getValue(), xmlColor.getBlue());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param ws
     *            The well sample to check.
     * @param xml
     *            The XML version.
     */
    private void validateWellSample(WellSample ws, ome.xml.model.WellSample xml) {
        Assert.assertEquals(ws.getPosX().getValue(), xml.getPositionX().value());
        Assert.assertEquals(ws.getPosY().getValue(), xml.getPositionY().value());
        Timestamp ts = new Timestamp(xml.getTimepoint().asInstant().getMillis());
        Assert.assertEquals(ws.getTimepoint().getValue(), ts.getTime());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param pa
     *            The plate acquisition to check.
     * @param xml
     *            The XML version.
     */
    private void validatePlateAcquisition(PlateAcquisition pa,
            ome.xml.model.PlateAcquisition xml) {
        Assert.assertEquals(pa.getName().getValue(), xml.getName());
        Assert.assertEquals(pa.getDescription().getValue(), xml.getDescription());
        Timestamp ts = new Timestamp(xml.getEndTime().asInstant().getMillis());
        Assert.assertNotNull(ts);
        Assert.assertNotNull(pa.getEndTime());
        Assert.assertEquals(pa.getEndTime().getValue(), ts.getTime());
        ts = new Timestamp(xml.getStartTime().asInstant().getMillis());
        Assert.assertNotNull(ts);
        Assert.assertNotNull(pa.getStartTime());
        Assert.assertEquals(pa.getStartTime().getValue(), ts.getTime());
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param mm
     *            The microbeam manipulation to check.
     * @param xml
     *            The XML version.
     */
    private void validateMicrobeamManipulation(MicrobeamManipulation mm,
            ome.xml.model.MicrobeamManipulation xml) {
        Assert.assertEquals(mm.getType().getValue().getValue(), xml.getType()
                .getValue());
        List<LightSettings> settings = mm.copyLightSourceSettings();
        Assert.assertEquals(1, mm.sizeOfLightSourceSettings());
        Assert.assertEquals(1, settings.size());
        validateLightSourceSettings(settings.get(0),
                xml.getLightSourceSettings(0));
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param experiment
     *            The microbeam manipulation to check.
     * @param xml
     *            The XML version.
     */
    private void validateExperiment(Experiment experiment,
            ome.xml.model.Experiment xml) {
        Assert.assertEquals(experiment.getType().getValue().getValue(), xml.getType()
                .getValue());
        Assert.assertEquals(experiment.getDescription().getValue(),
                xml.getDescription());
    }

    /**
     * Before each method call {@link #newUserAndGroup(String)}. If
     * {@link #disconnect()} is used anywhere, then this is necessary for all
     * methods, otherwise non-deterministic method ordering can cause those
     * tests which do not begin with this method call to Assert.fail.
     */
    @BeforeMethod
    protected void loginMethod() throws Exception {
        ownerEc = newUserAndGroup("rw----");
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
    }

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
    }

    /**
     * Tests the import of a <code>JPEG</code>, <code>PNG</code>
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportGraphicsImages() throws Exception {
        File f;
        List<String> failures = new ArrayList<String>();
        for (int i = 0; i < ModelMockFactory.FORMATS.length; i++) {
            f = File.createTempFile("testImportGraphicsImages"
                    + ModelMockFactory.FORMATS[i], "."
                    + ModelMockFactory.FORMATS[i]);
            mmFactory.createImageFile(f, ModelMockFactory.FORMATS[i]);
            f.deleteOnExit();
            try {
                importFile(f, ModelMockFactory.FORMATS[i]);
            } catch (Throwable e) {
                failures.add(ModelMockFactory.FORMATS[i]);
            }
        }
        if (failures.size() > 0) {
            Iterator<String> j = failures.iterator();
            String s = "";
            while (j.hasNext()) {
                s += j.next();
                s += " ";
            }
            Assert.fail("Cannot import the following formats:" + s);
        }
        Assert.assertEquals(0, failures.size());
    }

    @Test(timeOut = 20000)
    public void testImportFinishTooLargePixelsImage() throws Exception {
        // Simulates QA 17685 (an image with unreasonably huge pixel sizes stuck the import process
        // in a basically endless loop when trying to throw an exception); purpose is to
        // check that the import process finishes within a certain amount of time.
        File f = new File(
                System.getProperty("java.io.tmpdir"),
                "testImportInsaneImage&pixelType=uint8&sizeX=892411973&sizeY=1684497696&sizeZ=25971.fake");
        f.deleteOnExit();
        f.createNewFile();
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        Assert.assertEquals(p.getSizeX().getValue(), 892411973);
        Assert.assertEquals(p.getSizeY().getValue(), 1684497696);
        Assert.assertEquals(p.getSizeZ().getValue(), 25971);
        
        Delete2 dc = Requests.delete().target(p.getImage()).build();
        callback(true, root, dc);
    }

    /**
     * Tests the import of an OME-XML file with one image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportSimpleImage() throws Exception {
        File f = File.createTempFile("testImportSimpleImage", "." + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        int size = XMLMockObjects.SIZE_Z * XMLMockObjects.SIZE_C
                * XMLMockObjects.SIZE_T;
        // test the pixels
        Assert.assertEquals(p.getSizeX().getValue(), XMLMockObjects.SIZE_X.intValue());
        Assert.assertEquals(p.getSizeY().getValue(), XMLMockObjects.SIZE_Y.intValue());
        Assert.assertEquals(p.getSizeZ().getValue(), XMLMockObjects.SIZE_Z.intValue());
        Assert.assertEquals(p.getSizeC().getValue(), XMLMockObjects.SIZE_C.intValue());
        Assert.assertEquals(p.getSizeT().getValue(), XMLMockObjects.SIZE_T.intValue());
        Assert.assertEquals(p.getPixelsType().getValue().getValue(),
                XMLMockObjects.PIXEL_TYPE.getValue());
        Assert.assertEquals(p.getDimensionOrder().getValue().getValue(),
                XMLMockObjects.DIMENSION_ORDER.getValue());
        // Check the plane info

        String sql = "select p from PlaneInfo as p where pixels.id = :pid";
        ParametersI param = new ParametersI();
        param.addLong("pid", p.getId().getValue());
        List<IObject> l = iQuery.findAllByQuery(sql, param);
        Assert.assertEquals(size, l.size());
        Iterator<IObject> i;
        PlaneInfo plane;
        int found = 0;
        for (int z = 0; z < XMLMockObjects.SIZE_Z; z++) {
            for (int t = 0; t < XMLMockObjects.SIZE_T; t++) {
                for (int c = 0; c < XMLMockObjects.SIZE_C; c++) {
                    i = l.iterator();
                    while (i.hasNext()) {
                        plane = (PlaneInfo) i.next();
                        if (plane.getTheC().getValue() == c
                                && plane.getTheZ().getValue() == z
                                && plane.getTheT().getValue() == t)
                            found++;
                    }
                }
            }
        }
        Assert.assertEquals(found, size);
    }

    /**
     * Checks if import can still occur when the literal username cannot be written as a directory name in the managed repository.
     * @throws Exception unexpected
     */
    @Test
    public void testImportSimpleImageOddlyNamedUser() throws Exception {
        /* conceive a new user with an awkward name */
        final String username = "a / strange \\ name " + UUID.randomUUID();
        final String password = UUID.randomUUID().toString();
        Experimenter user = new ExperimenterI();
        user.setOmeName(omero.rtypes.rstring(username));
        user.setFirstName(omero.rtypes.rstring("integration"));
        user.setLastName(omero.rtypes.rstring("tester"));
        user.setLdap(omero.rtypes.rbool(false));

        /* actually create the user */
        final IAdminPrx rootAdmin = root.getSession().getAdminService();
        final EventContext ec = client.getSession().getAdminService().getEventContext();
        final ExperimenterGroup group = rootAdmin.getGroup(ec.groupId);
        final long userId = newUserInGroupWithPassword(user, group, password);

        /* add user to current group */
        user = rootAdmin.getExperimenter(userId);
        rootAdmin.addGroups(user, Arrays.asList(group));

        /* switch to being the new user */
        final omero.client client = newOmeroClient();
        client.createSession(username, password);
        init(client);

        try {
            /* see if import works */
            testImportSimpleImage();
        } finally {
            /* switch back to being the previous user */
            loginUser(ec);
        }
    }

    /**
     * Tests the import of an OME-XML file with one image w/o binary data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportSimpleImageMetadataOnly() throws Exception {
        File f = File.createTempFile("testImportSimpleImageMetadataOnly", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pix = null;
        try {
            pix = importFile(f, OME_FORMAT, true);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
    }

    /**
     * Tests the import of an OME-XML file with one image w/o binary data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportSimpleImageMetadataOnlyNoBinaryInFile()
            throws Exception {
        File f = File.createTempFile(
                "testImportSimpleImageMetadataOnlyNoBinaryInFile", "."
                        + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), false);
        List<Pixels> pix = null;
        try {
            pix = importFile(f, OME_FORMAT, true);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
    }

    /**
     * Tests the import of an OME-XML file with an annotated image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportAnnotatedImage() throws Exception {
        File f = File.createTempFile("testImportAnnotatedImage", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createAnnotatedImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();
        String sql = "select l from ImageAnnotationLink as l ";
        sql += "left outer join fetch l.parent as p ";
        sql += "join fetch l.child ";
        sql += "where p.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> l = iQuery.findAllByQuery(sql, param);
        // always companion file.
        if (l.size() < XMLMockObjects.ANNOTATIONS.length) {
            Assert.fail(String.format("%d < ANNOTATION count %d", l.size(),
                    XMLMockObjects.ANNOTATIONS.length));
        }
        int count = 0;
        Annotation a;
        for (IObject object : l) {
            a = ((ImageAnnotationLink) object).getChild();
            if (a instanceof CommentAnnotation)
                count++;
            else if (a instanceof TagAnnotation)
                count++;
            else if (a instanceof TermAnnotation)
                count++;
            else if (a instanceof BooleanAnnotation)
                count++;
            else if (a instanceof LongAnnotation)
                count++;
        }
        Assert.assertEquals(XMLMockObjects.ANNOTATIONS.length, count);
    }

    /**
     * Tests the import of an OME-XML file with an image with acquisition data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageWithAcquisitionData() throws Exception {
        File f = File.createTempFile("testImportImageWithAcquisitionData", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createImageWithAcquisitionData();
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();
        // Method already tested in PojosServiceTest
        ParametersI po = new ParametersI();
        po.acquisitionData();
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(id);
        List images = factory.getContainerService().getImages(
                Image.class.getName(), ids, po);
        Assert.assertEquals(1, images.size());
        Image image = (Image) images.get(0);
        // load the image and make we have everything
        Assert.assertNotNull(image.getImagingEnvironment());
        validateImagingEnvironment(image.getImagingEnvironment(),
                xml.createImageEnvironment());
        Assert.assertNotNull(image.getStageLabel());
        validateStageLabel(image.getStageLabel(), xml.createStageLabel());

        ObjectiveSettings settings = image.getObjectiveSettings();
        Assert.assertNotNull(settings);
        validateObjectiveSettings(image.getObjectiveSettings(),
                xml.createObjectiveSettings(0));

        Instrument instrument = image.getInstrument();
        Assert.assertNotNull(instrument);
        // check the instrument
        instrument = factory.getMetadataService().loadInstrument(
                instrument.getId().getValue());
        Assert.assertNotNull(instrument);
        ome.xml.model.Laser xmlLaser = (ome.xml.model.Laser) xml
                .createLightSource(ome.xml.model.Laser.class.getName(), 0);
        ome.xml.model.Arc xmlArc = (ome.xml.model.Arc) xml.createLightSource(
                ome.xml.model.Arc.class.getName(), 0);
        ome.xml.model.Filament xmlFilament = (ome.xml.model.Filament) xml
                .createLightSource(ome.xml.model.Filament.class.getName(), 0);
        ome.xml.model.LightEmittingDiode xmlDiode = (ome.xml.model.LightEmittingDiode) xml
                .createLightSource(
                        ome.xml.model.LightEmittingDiode.class.getName(), 0);

        ome.xml.model.Objective xmlObjective = xml.createObjective(0);
        ome.xml.model.Detector xmlDetector = xml.createDetector(0);
        ome.xml.model.Filter xmlFilter = xml.createFilter(0,
                XMLMockObjects.CUT_IN, XMLMockObjects.CUT_OUT);
        ome.xml.model.Dichroic xmlDichroic = xml.createDichroic(0);
        Assert.assertEquals(XMLMockObjects.NUMBER_OF_OBJECTIVES,
                instrument.sizeOfObjective());
        Assert.assertEquals(XMLMockObjects.NUMBER_OF_DECTECTORS,
                instrument.sizeOfDetector());
        Assert.assertEquals(XMLMockObjects.NUMBER_OF_DICHROICS,
                instrument.sizeOfDichroic());
        Assert.assertEquals(XMLMockObjects.NUMBER_OF_FILTERS,
                instrument.sizeOfFilter());
        Assert.assertEquals(1, instrument.sizeOfFilterSet());
        // Assert.assertEquals(1, instrument.sizeOfOtf()); DISABLED

        List<Detector> detectors = instrument.copyDetector();
        List<Long> detectorIds = new ArrayList<Long>();
        Detector de;
        Iterator j = detectors.iterator();
        while (j.hasNext()) {
            de = (Detector) j.next();
            detectorIds.add(de.getId().getValue());
            validateDetector(de, xmlDetector);
        }
        List<Objective> objectives = instrument.copyObjective();
        j = objectives.iterator();
        while (j.hasNext()) {
            validateObjective((Objective) j.next(), xmlObjective);
        }
        List<Filter> filters = instrument.copyFilter();
        j = filters.iterator();
        while (j.hasNext()) {
            validateFilter((Filter) j.next(), xmlFilter);
        }
        List<Dichroic> dichroics = instrument.copyDichroic();
        j = dichroics.iterator();
        while (j.hasNext()) {
            validateDichroic((Dichroic) j.next(), xmlDichroic);
        }

        List<LightSource> lights = instrument.copyLightSource();
        j = lights.iterator();
        List<Long> lightIds = new ArrayList<Long>();
        LightSource src;
        while (j.hasNext()) {
            src = (LightSource) j.next();
            if (src instanceof Laser)
                validateLaser((Laser) src, xmlLaser);
            else if (src instanceof Arc)
                validateArc((Arc) src, xmlArc);
            else if (src instanceof Filament)
                validateFilament((Filament) src, xmlFilament);

            lightIds.add(src.getId().getValue());
        }

        p = factory.getPixelsService().retrievePixDescription(
                p.getId().getValue());

        ids.clear();

        ome.xml.model.Channel xmlChannel = xml.createChannel(0);
        Channel channel;
        List<Channel> channels = p.copyChannels();
        Iterator<Channel> i = channels.iterator();
        // Assert.assertEquals(xmlChannel.getColor().intValue() ==
        // XMLMockObjects.DEFAULT_COLOR.getRGB());
        Color c;
        while (i.hasNext()) {
            channel = i.next();
            Assert.assertEquals(channel.getAlpha().getValue(),
                    XMLMockObjects.DEFAULT_COLOR.getAlpha());
            Assert.assertEquals(channel.getRed().getValue(),
                    XMLMockObjects.DEFAULT_COLOR.getRed());
            Assert.assertEquals(channel.getGreen().getValue(),
                    XMLMockObjects.DEFAULT_COLOR.getGreen());
            Assert.assertEquals(channel.getBlue().getValue(),
                    XMLMockObjects.DEFAULT_COLOR.getBlue());
            ids.add(channel.getLogicalChannel().getId().getValue());
        }
        List<LogicalChannel> l = factory.getMetadataService()
                .loadChannelAcquisitionData(ids);
        Assert.assertEquals(channels.size(), l.size());

        LogicalChannel lc;
        DetectorSettings ds;
        LightSettings ls;
        ome.xml.model.DetectorSettings xmlDs = xml.createDetectorSettings(0);
        ome.xml.model.LightSourceSettings xmlLs = xml
                .createLightSourceSettings(0);

        ome.xml.model.MicrobeamManipulation xmlMM = xml
                .createMicrobeamManipulation(0);
        ome.xml.model.Experiment xmlExp = ome.getExperiment(0);

        // Validate experiment (initial checks)
        Assert.assertNotNull(image.getExperiment());
        Experiment exp = (Experiment) factory
                .getQueryService()
                .findByQuery(
                        "select e from Experiment as e "
                                + "join fetch e.type "
                                + "left outer join fetch e.microbeamManipulation as mm "
                                + "join fetch mm.type "
                                + "left outer join fetch mm.lightSourceSettings as lss "
                                + "left outer join fetch lss.lightSource "
                                + "where e.id = :id",
                        new ParametersI().addId(image.getExperiment().getId()
                                .getValue()));
        Assert.assertNotNull(exp);
        Assert.assertEquals(1, exp.sizeOfMicrobeamManipulation());
        MicrobeamManipulation mm = exp.copyMicrobeamManipulation().get(0);
        validateExperiment(exp, xmlExp);
        validateMicrobeamManipulation(mm, xmlMM);

        LightPath path;
        Iterator<LogicalChannel> k = l.iterator();
        while (k.hasNext()) {
            lc = k.next();
            validateChannel(lc, xmlChannel);
            ds = lc.getDetectorSettings();
            Assert.assertNotNull(ds);
            Assert.assertNotNull(ds.getDetector());
            Assert.assertTrue(detectorIds
                    .contains(ds.getDetector().getId().getValue()));
            validateDetectorSettings(ds, xmlDs);
            ls = lc.getLightSourceSettings();
            Assert.assertNotNull(ls);
            Assert.assertNotNull(ls.getLightSource());
            Assert.assertTrue(lightIds
                    .contains(ls.getLightSource().getId().getValue()));
            validateLightSourceSettings(ls, xmlLs);
            path = lc.getLightPath();
            Assert.assertNotNull(lc);
            Assert.assertNotNull(path.getDichroic());
        }
    }

    /**
     * Tests the import of an OME-XML file with an image with ROI.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testImportImageWithROI() throws Exception {
        File f = File
                .createTempFile("testImportImageWithROI", "." + OME_FORMAT);
        f.deleteOnExit();
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
        // load the image and make the ROI
        // Method tested in ROIServiceTest
        IRoiPrx svc = factory.getRoiService();
        RoiResult r = svc.findByImage(id, new RoiOptions());
        Assert.assertNotNull(r);
        List<Roi> rois = r.rois;
        Assert.assertNotNull(rois);
        Assert.assertEquals(rois.size(), XMLMockObjects.SIZE_C.intValue());
        Iterator<Roi> i = rois.iterator();
        Roi roi;
        List<Shape> shapes;
        Iterator<Shape> j;
        Shape shape;
        int count;
        while (i.hasNext()) {
            count = 0;
            roi = i.next();
            shapes = roi.copyShapes();
            Assert.assertNotNull(shapes);
            Assert.assertEquals(shapes.size(), XMLMockObjects.SHAPES.length);
            // Check if the shape are of the supported types.

            j = shapes.iterator();
            while (j.hasNext()) {
                shape = j.next();
                if (shape instanceof Rectangle || shape instanceof Line
                        || shape instanceof Ellipse
                        || shape instanceof Polyline || shape instanceof Mask
                        || shape instanceof Point)
                    count++;
            }
            Assert.assertEquals(count, XMLMockObjects.SHAPES.length);
        }
    }

    /**
     * Tests the import of an OME-XML file with a fully populated plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportPlate() throws Exception {
        File f = File.createTempFile("testImportPlate", "." + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createPopulatedPlate(0);
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, ome.getPlate(0).getName());
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        Pixels p = pixels.get(0);
        WellSample ws = getWellSample(p);
        Assert.assertNotNull(ws);
        validateWellSample(ws, ome.getPlate(0).getWell(0).getWellSample(0));
        Well well = ws.getWell();
        Assert.assertNotNull(well);
        validateWell(well, ome.getPlate(0).getWell(0));
        Plate plate = ws.getWell().getPlate();
        Assert.assertNotNull(plate);
        validatePlate(plate, ome.getPlate(0));
    }

    /**
     * Tests the import of an OME-XML file with a screen and a fully populated
     * plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket12601"})
    public void testImportScreenWithOnePlate() throws Exception {
        File f = File.createTempFile("testImportScreenWithOnePlate", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        int rows = 2;
        int columns = 2;
        int fields = 2;
        int acquisition = 3;
        int plates = 1;
        OME ome = xml.createPopulatedScreen(plates, rows, columns, fields,
                acquisition);
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, ome.getPlate(0).getName());
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        Pixels pp = pixels.get(0);
        WellSample ws = getWellSample(pp);
        validateWellSample(ws, ome.getPlate(0).getWell(0).getWellSample(0));
        Well well = ws.getWell();
        Assert.assertNotNull(well);
        validateWell(well, ome.getPlate(0).getWell(0));
        Plate plate = ws.getWell().getPlate();
        Assert.assertNotNull(plate);
        validatePlate(plate, ome.getPlate(0));
        validateScreen(plate.copyScreenLinks().get(0).getParent(),
                ome.getScreen(0));
        PlateAcquisition pa;
        Map<Long, Set<Long>> ppaMap = new HashMap<Long, Set<Long>>();
        Map<Long, Set<Long>> pawsMap = new HashMap<Long, Set<Long>>();
        Set<Long> wsIds;
        Set<Long> paIds;
        for (Pixels p : pixels) {
            ws = getWellSample(p);
            Assert.assertNotNull(ws);
            well = ws.getWell();
            Assert.assertNotNull(well);
            plate = ws.getWell().getPlate();
            pa = ws.getPlateAcquisition();
            wsIds = pawsMap.get(pa.getId().getValue());
            if (wsIds == null) {
                wsIds = new HashSet<Long>();
                pawsMap.put(pa.getId().getValue(), wsIds);
            }
            wsIds.add(ws.getId().getValue());
            paIds = ppaMap.get(plate.getId().getValue());
            if (paIds == null) {
                paIds = new HashSet<Long>();
                ppaMap.put(plate.getId().getValue(), paIds);
            }
            paIds.add(pa.getId().getValue());
            Assert.assertNotNull(plate);
            validateScreen(plate.copyScreenLinks().get(0).getParent(),
                    ome.getScreen(0));
        }
        Assert.assertEquals(plates, ppaMap.size());
        Assert.assertEquals(plates * acquisition, pawsMap.size());
        Entry entry;
        Iterator i = ppaMap.entrySet().iterator();
        Long id, idw;
        Set<Long> l;
        Set<Long> wsList;
        Iterator<Long> j, k;
        List<Long> plateIds = new ArrayList<Long>();
        List<Long> wsListIds = new ArrayList<Long>();
        while (i.hasNext()) {
            entry = (Entry) i.next();
            l = (Set<Long>) entry.getValue();
            Assert.assertEquals(acquisition, l.size());
            j = l.iterator();
            while (j.hasNext()) {
                id = j.next();
                Assert.assertFalse(plateIds.contains(id));
                plateIds.add(id);
                wsList = pawsMap.get(id);
                Assert.assertEquals(rows * columns * fields, wsList.size());
                k = wsList.iterator();
                while (k.hasNext()) {
                    idw = k.next();
                    Assert.assertFalse(wsListIds.contains(idw));
                    wsListIds.add(idw);
                }
            }
        }
        Assert.assertEquals(rows * columns * fields * plates * acquisition,
                wsListIds.size());
    }

    /**
     * Tests the import of an OME-XML file with a screen and two fully populated
     * plates.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket12601"})
    public void testImportScreenWithTwoPlates() throws Exception {
        File f = File.createTempFile("testImportScreenWithTwoPlates", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        int rows = 2;
        int columns = 2;
        int fields = 2;
        int acquisition = 2;
        int plates = 2;
        OME ome = xml.createPopulatedScreen(plates, rows, columns, fields,
                acquisition);
        // We should have 2 plates
        // each plate will have 2 plate acquisitions
        // 2x2x2 fields
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        WellSample ws;
        Well well;
        Plate plate = null;
        PlateAcquisition pa;
        Map<Long, Set<Long>> ppaMap = new HashMap<Long, Set<Long>>();
        Map<Long, Set<Long>> pawsMap = new HashMap<Long, Set<Long>>();
        Set<Long> wsIds;
        Set<Long> paIds;
        for (Pixels p : pixels) {
            ws = getWellSample(p);
            Assert.assertNotNull(ws);
            well = ws.getWell();
            Assert.assertNotNull(well);
            plate = ws.getWell().getPlate();
            pa = ws.getPlateAcquisition();
            wsIds = pawsMap.get(pa.getId().getValue());
            if (wsIds == null) {
                wsIds = new HashSet<Long>();
                pawsMap.put(pa.getId().getValue(), wsIds);
            }
            wsIds.add(ws.getId().getValue());
            paIds = ppaMap.get(plate.getId().getValue());
            if (paIds == null) {
                paIds = new HashSet<Long>();
                ppaMap.put(plate.getId().getValue(), paIds);
            }
            paIds.add(pa.getId().getValue());
            Assert.assertNotNull(plate);
            validateScreen(plate.copyScreenLinks().get(0).getParent(),
                    ome.getScreen(0));
        }
        Assert.assertEquals(plates, ppaMap.size());
        Assert.assertEquals(plates * acquisition, pawsMap.size());
        Entry entry;
        Iterator i = ppaMap.entrySet().iterator();
        Long id, idw;
        Set<Long> l;
        Set<Long> wsList;
        Iterator<Long> j, k;
        List<Long> plateIds = new ArrayList<Long>();
        List<Long> wsListIds = new ArrayList<Long>();
        while (i.hasNext()) {
            entry = (Entry) i.next();
            l = (Set<Long>) entry.getValue();
            Assert.assertEquals(acquisition, l.size());
            j = l.iterator();
            while (j.hasNext()) {
                id = j.next();
                Assert.assertFalse(plateIds.contains(id));
                plateIds.add(id);
                wsList = pawsMap.get(id);
                Assert.assertEquals(rows * columns * fields, wsList.size());
                k = wsList.iterator();
                while (k.hasNext()) {
                    idw = k.next();
                    Assert.assertFalse(wsListIds.contains(idw));
                    wsListIds.add(idw);
                }
            }
        }
        Assert.assertEquals(rows * columns * fields * plates * acquisition,
                wsListIds.size());
    }

    /**
     * Tests the import of an OME-XML file with a fully populated plate with a
     * plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportPlateOnePlateAcquisition() throws Exception {
        File f = File.createTempFile("testImportPlateOnePlateAcquisition", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createPopulatedPlate(1);
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();
        String sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "join fetch ws.well as w ";
        sql += "join fetch w.plate as p ";
        sql += "where ws.image.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        Assert.assertEquals(results.size(), 1);
        WellSample ws = (WellSample) results.get(0);
        Assert.assertNotNull(ws.getWell());
        Assert.assertNotNull(ws.getWell().getPlate());
        PlateAcquisition pa = ws.getPlateAcquisition();
        Assert.assertNotNull(pa);
        validatePlateAcquisition(pa, ome.getPlate(0).getPlateAcquisition(0));
    }

    /**
     * Tests the import of an OME-XML file with a fully populated plate with a
     * plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket12601"})
    public void testImportPlateMultiplePlateAcquisitions() throws Exception {
        File f = File.createTempFile(
                "testImportPlateMultiplePlateAcquisitions", "." + OME_FORMAT);
        f.deleteOnExit();
        int n = 3;
        int fields = 3;
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createPopulatedPlate(n, fields);
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();
        String sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "join fetch ws.well as w ";
        sql += "join fetch w.plate as p ";
        sql += "where ws.image.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> results = iQuery.findAllByQuery(sql, param);

        WellSample ws = (WellSample) results.get(0);
        Assert.assertNotNull(ws.getWell());
        Plate plate = ws.getWell().getPlate();
        sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "join fetch ws.well as w ";
        sql += "join fetch w.plate as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(plate.getId().getValue());
        Assert.assertEquals(fields * n, iQuery.findAllByQuery(sql, param).size());

        sql = "select pa from PlateAcquisition as pa ";
        sql += "where pa.plate.id = :id";
        List<IObject> pas = iQuery.findAllByQuery(sql, param);
        Assert.assertEquals(n, pas.size());

        Iterator<IObject> j = pas.iterator();
        sql = "select ws from WellSample as ws ";
        sql += "join fetch ws.plateAcquisition as pa ";
        sql += "where pa.id = :id";
        IObject obj;
        while (j.hasNext()) {
            obj = j.next();
            param = new ParametersI();
            param.addId(obj.getId().getValue());
            Assert.assertEquals(fields, iQuery.findAllByQuery(sql, param).size());
        }
    }

    /**
     * Tests the import of an OME-XML file with a plate with wells linked to a
     * reagent.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportPlateWithReagent() throws Exception {
        File f = File.createTempFile("testImportPlateWithReagent", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createBasicPlateWithReagent();
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();
        String sql = "select ws from WellSample as ws ";
        sql += "left outer join fetch ws.plateAcquisition as pa ";
        sql += "join fetch ws.well as w ";
        sql += "join fetch w.plate as p ";
        sql += "where ws.image.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        Assert.assertEquals(1, results.size());
        WellSample ws = (WellSample) results.get(0);
        // Assert.assertNotNull(ws.getPlateAcquisition());
        Assert.assertNotNull(ws.getWell());
        id = ws.getWell().getId().getValue();
        sql = "select l from WellReagentLink as l ";
        sql += "join fetch l.child as c ";
        sql += "join fetch l.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(id);
        WellReagentLink wr = (WellReagentLink) iQuery.findByQuery(sql, param);
        Assert.assertNotNull(wr);
        Assert.assertNotNull(wr.getParent());
        Assert.assertNotNull(wr.getChild());
        validateReagent(wr.getChild(), ome.getScreen(0).getReagent(0));
        id = wr.getChild().getId().getValue();
        sql = "select s from Screen as s ";
        sql += "join fetch s.reagents as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(id);
        omero.model.Screen screen = (omero.model.Screen) iQuery.findByQuery(
                sql, param);
        Assert.assertNotNull(screen);
        Assert.assertEquals(1, screen.sizeOfReagents());
        Assert.assertEquals(wr.getChild().getId().getValue(), screen.copyReagents()
                .get(0).getId().getValue());
    }

    /**
     * Tests the import of an image into a specified dataset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageIntoDataset() throws Exception {
        // First create a dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        File f = File.createTempFile("testImportImageIntoDataset", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT, d);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();

        // Now check that we have an image link.
        ParametersI param = new ParametersI();
        param.addId(d.getId().getValue());
        String sql = "select i from DatasetImageLink as i where i.parent.id = :id";
        DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(sql,
                param);
        Assert.assertNotNull(link);
        Assert.assertEquals(link.getChild().getId().getValue(), id);
    }

    /**
     * Tests the import of an image into a specified dataset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageIntoDatasetFromOtherGroup() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        // group owner deletes it
        disconnect();
        // First create a dataset
        ExperimenterGroup group = newGroupAddUser("rw----", ownerEc.userId);
        Assert.assertNotEquals(group.getId().getValue(), ownerEc.groupId);
        loginUser(ownerEc);
        // newUserInGroup(ownerEc);

        File f = File.createTempFile("testImportImageIntoDataset", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT, d);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getImage().getId().getValue();

        // Now check that we have an image link.
        ParametersI param = new ParametersI();
        param.addId(d.getId().getValue());
        String sql = "select i from DatasetImageLink as i where i.parent.id = :id";
        DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(sql,
                param);
        Assert.assertNotNull(link);
        Assert.assertEquals(link.getChild().getId().getValue(), id);
    }

    /**
     * Tests the import of an image into a specified dataset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageIntoWrongDataset() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        d.setId(omero.rtypes.rlong(d.getId().getValue() * 100));
        // group owner deletes it
        disconnect();
        // First create a dataset
        ExperimenterGroup group = newGroupAddUser("rw----", ownerEc.userId);
        Assert.assertNotEquals(group.getId().getValue(), ownerEc.groupId);
        // newUserInGroup(ownerEc);

        File f = File.createTempFile("testImportImageIntoDataset", "."
                + OME_FORMAT);
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        try {
            importFile(f, OME_FORMAT, d);
            Assert.fail("An exception should have been thrown");
        } catch (Throwable e) {
        }
    }

}
