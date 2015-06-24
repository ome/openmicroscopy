/*
 *  Copyright 2006-2015 University of Dundee. All rights reserved.
 *  Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

import static omero.rtypes.rint;
import static omero.rtypes.rstring;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import ome.formats.model.UnitsFactory;
import ome.units.UNITS;
import omero.api.IPixelsPrx;
import omero.model.*;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsTime;
import pojos.DatasetData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.ScreenData;

/**
 * Helper class.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ModelMockFactory {

    /** The default width of an image. */
    static final int WIDTH = 100;

    /** The default height of an image. */
    static final int HEIGHT = 100;

    /** The basic formats tested. */
    static final String[] FORMATS = { "jpeg", "png" };

    /** Identifies the laser light source. */
    static String LASER = Laser.class.getName();

    /** Identifies the filament light source. */
    static String FILAMENT = Filament.class.getName();

    /** Identifies the arc light source. */
    static String ARC = Arc.class.getName();

    /** Identifies the arc light source. */
    static String LIGHT_EMITTING_DIODE = LightEmittingDiode.class.getName();

    /** The possible sources of light. */
    static String[] LIGHT_SOURCES = { LASER, FILAMENT, ARC,
            LIGHT_EMITTING_DIODE };

    /** The default number of channels. */
    static int DEFAULT_CHANNELS_NUMBER = 3;

    /** The default size along the X-axis. */
    static int SIZE_X = 10;

    /** The default size along the Y-axis. */
    static int SIZE_Y = 10;

    /** The number of z-sections. */
    static int SIZE_Z = 10;

    /** The number of time points. */
    static int SIZE_T = 10;

    /** The dimension order for the pixels type. */
    static String XYZCT = "XYZCT";

    /** The unsigned int 16 pixels Type. */
    static String UINT16 = "uint16";

    /** The bit pixels Type. */
    static String BIT = "bit";

    /** Helper reference to the <code>IPixels</code> service. */
    private IPixelsPrx pixelsService;

    private static Frequency hz(double d) {
        return new FrequencyI(d, UNITS.HZ);
    }

    private static ElectricPotential volt(double d) {
        return new ElectricPotentialI(d, UNITS.VOLT);
    }

    private static Power watt(double d) {
        return new PowerI(d, UNITS.WATT);
    }

    /**
     * Creates a new instance.
     *
     * @param pixelsService
     */
    ModelMockFactory(IPixelsPrx pixelsService) {
        this.pixelsService = pixelsService;
    }

    // POJO
    /**
     * Creates a default dataset and returns it.
     *
     * @return See above.
     */
    public DatasetData simpleDatasetData() {
        DatasetData dd = new DatasetData();
        dd.setName("t1");
        dd.setDescription("t1");
        return dd;
    }

    /**
     * Creates a default project and returns it.
     *
     * @return See above.
     */
    public ProjectData simpleProjectData() {
        ProjectData data = new ProjectData();
        data.setName("project1");
        data.setDescription("project1");
        return data;
    }

    /**
     * Creates a default screen and returns it.
     *
     * @return See above.
     */
    public ScreenData simpleScreenData() {
        ScreenData data = new ScreenData();
        data.setName("screen name");
        data.setDescription("screen description");
        data.setProtocolDescription("Protocol description");
        data.setProtocolIdentifier("Protocol identifier");
        data.setReagentSetDescripion("Reagent description");
        data.setReagentSetIdentifier("Reagent identifier");
        return data;
    }

    /**
     * Creates a default plate and returns it.
     *
     * @return See above.
     */
    public PlateData simplePlateData() {
        PlateData data = new PlateData();
        data.setName("plate name");
        data.setDescription("plate name");
        data.setStatus("done");
        data.setExternalIdentifier("External Identifier");
        return data;
    }

    /**
     * Creates a default plate acquisition and returns it.
     *
     * @return See above.
     */
    public PlateAcquisitionData simplePlateAcquisitionData() {
        PlateAcquisitionData data = new PlateAcquisitionData();
        data.setName("plate acquisition name");
        data.setDescription("plate acquisition name");
        return data;
    }

    /**
     * Creates a default image and returns it.
     *
     * @return See above.
     */
    public Image simpleImage() {
        // prepare data
        Image img = new ImageI();
        String uuidAsString = UUID.randomUUID().toString();
        String uniqueName = String.format("test-image:%s", uuidAsString);
        String uniqueDesc = String.format("test-desc:%s", uuidAsString);
        img.setName(rstring(uniqueName));
        img.setDescription(rstring(uniqueDesc));
        img.setSeries(rint(0));
        return img;
    }

    /**
     * @return a default fileset
     */
    public Fileset simpleFileset() {
        Fileset fs = new FilesetI();
        fs.setTemplatePrefix(omero.rtypes.rstring("fileset-" + System.nanoTime() + "/"));
        return fs;
    }

    /**
     * Creates and returns an original file object.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public OriginalFile createOriginalFile() throws Exception {
        OriginalFileI oFile = new OriginalFileI();
        oFile.setName(omero.rtypes.rstring("of1"));
        oFile.setPath(omero.rtypes.rstring("/omero"));
        oFile.setSize(omero.rtypes.rlong(0));
        oFile.setHash(omero.rtypes.rstring("pending"));
        oFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
        return oFile;
    }

    /**
     * Creates and returns a thumbnail.
     *
     * @return See above.
     */
    public Thumbnail createThumbnail() {
        ThumbnailI thumbnail = new ThumbnailI();
        thumbnail.setMimeType(omero.rtypes.rstring("application/octet-stream"));
        thumbnail.setSizeX(omero.rtypes.rint(10));
        thumbnail.setSizeY(omero.rtypes.rint(11));
        return thumbnail;
    }

    /**
     * Creates and returns a detector. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Detector createDetector() throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService
                .getAllEnumerations(DetectorType.class.getName());
        Detector detector = new DetectorI();
        detector.setAmplificationGain(omero.rtypes.rdouble(0));
        detector.setGain(omero.rtypes.rdouble(1));
        detector.setManufacturer(omero.rtypes.rstring("manufacturer"));
        detector.setModel(omero.rtypes.rstring("model"));
        detector.setSerialNumber(omero.rtypes.rstring("serial number"));
        detector.setLotNumber(omero.rtypes.rstring("lot number"));
        detector.setOffsetValue(omero.rtypes.rdouble(0));
        detector.setType((DetectorType) types.get(0));
        return detector;
    }

    /**
     * Creates an Optical Transfer Function object.
     *
     * @param filterSet
     *            The filter set linked to it.
     * @param objective
     *            The objective linked to it.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public OTF createOTF(FilterSet filterSet, Objective objective)
            throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService.getAllEnumerations(PixelsType.class
                .getName());
        OTF otf = new OTFI();
        otf.setFilterSet(filterSet);
        otf.setObjective(objective);
        otf.setPath(omero.rtypes.rstring("/OMERO"));
        otf.setOpticalAxisAveraged(omero.rtypes.rbool(true));
        otf.setPixelsType((PixelsType) types.get(0));
        otf.setSizeX(omero.rtypes.rint(10));
        otf.setSizeY(omero.rtypes.rint(10));
        return otf;
    }

    /**
     * Creates and returns a filter. This will have to be linked to an
     * instrument.
     *
     * @param cutIn
     *            The cut in value.
     * @param cutOut
     *            The cut out value.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Filter createFilter(int cutIn, int cutOut) throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService.getAllEnumerations(FilterType.class
                .getName());
        Filter filter = new FilterI();
        filter.setLotNumber(omero.rtypes.rstring("lot number"));
        filter.setSerialNumber(omero.rtypes.rstring("serial number"));
        filter.setManufacturer(omero.rtypes.rstring("manufacturer"));
        filter.setModel(omero.rtypes.rstring("model"));
        filter.setType((FilterType) types.get(0));

        TransmittanceRangeI transmittance = new TransmittanceRangeI();
        transmittance.setCutIn(new LengthI(cutIn, UnitsFactory.TransmittanceRange_CutIn));
        transmittance.setCutOut(new LengthI(cutOut, UnitsFactory.TransmittanceRange_CutOut));
        transmittance.setCutInTolerance(new LengthI(1, UnitsFactory.TransmittanceRange_CutInTolerance));
        transmittance.setCutOutTolerance(new LengthI(1, UnitsFactory.TransmittanceRange_CutOutTolerance));
        filter.setTransmittanceRange(transmittance);
        return filter;
    }

    /**
     * Creates a basic filter set.
     *
     * @return See above.
     */
    public FilterSet createFilterSet() {
        FilterSet set = new FilterSetI();
        set.setLotNumber(omero.rtypes.rstring("lot number"));
        set.setManufacturer(omero.rtypes.rstring("manufacturer"));
        set.setModel(omero.rtypes.rstring("model"));
        set.setSerialNumber(omero.rtypes.rstring("serial number"));
        return set;
    }

    /**
     * Creates and returns a dichroic. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Dichroic createDichroic() throws Exception {
        Dichroic dichroic = new DichroicI();
        dichroic.setManufacturer(omero.rtypes.rstring("manufacturer"));
        dichroic.setModel(omero.rtypes.rstring("model"));
        dichroic.setLotNumber(omero.rtypes.rstring("lot number"));
        dichroic.setSerialNumber(omero.rtypes.rstring("serial number"));
        return dichroic;
    }

    /**
     * Creates and returns an objective. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Objective createObjective() throws Exception {
        Objective objective = new ObjectiveI();
        objective.setManufacturer(omero.rtypes.rstring("manufacturer"));
        objective.setModel(omero.rtypes.rstring("model"));
        objective.setSerialNumber(omero.rtypes.rstring("serial number"));
        objective.setLotNumber(omero.rtypes.rstring("lot number"));
        objective.setCalibratedMagnification(omero.rtypes.rdouble(1));
        // correction
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService.getAllEnumerations(Correction.class
                .getName());
        objective.setCorrection((Correction) types.get(0));
        // immersion
        types = pixelsService.getAllEnumerations(Immersion.class.getName());
        objective.setImmersion((Immersion) types.get(0));

        objective.setIris(omero.rtypes.rbool(true));
        objective.setLensNA(omero.rtypes.rdouble(0.5));
        objective.setNominalMagnification(omero.rtypes.rdouble(1));
        objective.setWorkingDistance(new LengthI(1, UnitsFactory.Objective_WorkingDistance));
        return objective;
    }

    /**
     * Creates and returns the settings of the specified objective.
     *
     * @param objective
     *            The objective to link the settings to.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public ObjectiveSettings createObjectiveSettings(Objective objective)
            throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService.getAllEnumerations(Medium.class
                .getName());
        ObjectiveSettings settings = new ObjectiveSettingsI();
        settings.setCorrectionCollar(omero.rtypes.rdouble(1));
        settings.setRefractiveIndex(omero.rtypes.rdouble(1));
        settings.setMedium((Medium) types.get(0));
        settings.setObjective(objective);
        return settings;
    }

    /**
     * Creates and returns the stage label.
     *
     * @return See above.
     */
    public StageLabel createStageLabel() {
        StageLabel label = new StageLabelI();
        label.setName(omero.rtypes.rstring("label"));
        label.setPositionX(new LengthI(1, UnitsFactory.StageLabel_X));
        label.setPositionY(new LengthI(1, UnitsFactory.StageLabel_Y));
        label.setPositionZ(new LengthI(1, UnitsFactory.StageLabel_Z));
        return label;
    }

    /**
     * Creates and returns the environment.
     *
     * @return See above.
     */
    public ImagingEnvironment createImageEnvironment() {
        ImagingEnvironment env = new ImagingEnvironmentI();
        env.setAirPressure(new PressureI(1, UnitsFactory.ImagingEnvironment_AirPressure));
        env.setCo2percent(omero.rtypes.rdouble(0.5));
        env.setHumidity(omero.rtypes.rdouble(0.5));
        env.setTemperature(new TemperatureI(1, UnitsFactory.ImagingEnvironment_Temperature));
        return env;
    }

    /**
     * Creates and returns the settings of the specified detector.
     *
     * @param detector
     *            The detector to link the settings to.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    DetectorSettings createDetectorSettings(Detector detector) throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService.getAllEnumerations(Binning.class
                .getName());
        DetectorSettings settings = new DetectorSettingsI();
        settings.setBinning((Binning) types.get(0));
        settings.setDetector(detector);
        settings.setGain(omero.rtypes.rdouble(1));
        settings.setOffsetValue(omero.rtypes.rdouble(1));
        settings.setReadOutRate(hz(1));
        settings.setVoltage(volt(1));
        return settings;
    }

    /**
     * Creates and returns the settings of the specified source of light.
     *
     * @param light
     *            The light to link the settings to.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public LightSettings createLightSettings(LightSource light)
            throws Exception {
        // already tested see PixelsService enumeration.
        List<IObject> types = pixelsService
                .getAllEnumerations(MicrobeamManipulationType.class.getName());
        LightSettings settings = new LightSettingsI();
        settings.setLightSource(light);
        settings.setAttenuation(omero.rtypes.rdouble(1));
        MicrobeamManipulation mm = new MicrobeamManipulationI();
        mm.setType((MicrobeamManipulationType) types.get(0));
        Experiment exp = new ExperimentI();
        types = pixelsService
                .getAllEnumerations(ExperimentType.class.getName());
        exp.setType((ExperimentType) types.get(0));
        mm.setExperiment(exp);
        // settings.setMicrobeamManipulation(mm);
        settings.setWavelength(new LengthI(500.1, UnitsFactory.LightSourceSettings_Wavelength));
        return settings;
    }

    /**
     * Creates a light path.
     *
     * @param emissionFilter
     *            The emission filter or <code>null</code>.
     * @param dichroic
     *            The dichroic or <code>null</code>.
     * @param excitationFilter
     *            The excitation filter or <code>null</code>.
     * @return See above.
     */
    public LightPath createLightPath(Filter emissionFilter, Dichroic dichroic,
            Filter excitationFilter) {
        LightPath path = new LightPathI();
        if (dichroic != null)
            path.setDichroic(dichroic);
        return path;
    }

    /**
     * Creates and returns a filament. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Filament createFilament() throws Exception {
        List<IObject> types = pixelsService
                .getAllEnumerations(FilamentType.class.getName());
        Filament filament = new FilamentI();
        filament.setManufacturer(omero.rtypes.rstring("manufacturer"));
        filament.setModel(omero.rtypes.rstring("model"));
        filament.setPower(watt(1));
        filament.setSerialNumber(omero.rtypes.rstring("serial number"));
        filament.setLotNumber(omero.rtypes.rstring("lot number"));
        filament.setType((FilamentType) types.get(0));
        return filament;
    }

    /**
     * Creates and returns a filament. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Arc createArc() throws Exception {
        List<IObject> types = pixelsService.getAllEnumerations(ArcType.class
                .getName());
        Arc arc = new ArcI();
        arc.setManufacturer(omero.rtypes.rstring("manufacturer"));
        arc.setModel(omero.rtypes.rstring("model"));
        arc.setPower(watt(1));
        arc.setSerialNumber(omero.rtypes.rstring("serial number"));
        arc.setLotNumber(omero.rtypes.rstring("lot number"));
        arc.setType((ArcType) types.get(0));
        return arc;
    }

    /**
     * Creates and returns a filament. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public LightEmittingDiode createLightEmittingDiode() throws Exception {
        LightEmittingDiode light = new LightEmittingDiodeI();
        light.setManufacturer(omero.rtypes.rstring("manufacturer"));
        light.setModel(omero.rtypes.rstring("model"));
        light.setPower(watt(1));
        light.setSerialNumber(omero.rtypes.rstring("serial number"));
        light.setLotNumber(omero.rtypes.rstring("lot number"));
        return light;
    }

    /**
     * Creates and returns a laser. This will have to be linked to an
     * instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Laser createLaser() throws Exception {
        Laser laser = new LaserI();
        laser.setManufacturer(omero.rtypes.rstring("manufacturer"));
        laser.setModel(omero.rtypes.rstring("model"));
        laser.setLotNumber(omero.rtypes.rstring("lot number"));
        laser.setSerialNumber(omero.rtypes.rstring("serial number"));
        // type
        List<IObject> types = pixelsService.getAllEnumerations(LaserType.class
                .getName());
        laser.setType((LaserType) types.get(0));
        // laser medium
        types = pixelsService.getAllEnumerations(LaserMedium.class.getName());
        laser.setLaserMedium((LaserMedium) types.get(0));

        // pulse
        types = pixelsService.getAllEnumerations(Pulse.class.getName());
        laser.setPulse((Pulse) types.get(0));

        laser.setFrequencyMultiplication(omero.rtypes.rint(1));
        laser.setPockelCell(omero.rtypes.rbool(false));
        laser.setTuneable(omero.rtypes.rbool(true));
        laser.setWavelength(new LengthI(500.1, UnitsFactory.Laser_Wavelength));
        laser.setPower(watt(1));
        laser.setRepetitionRate(hz(1));
        return laser;
    }

    /**
     * Creates and returns an instrument.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Instrument createInstrument() throws Exception {
        List<IObject> types = pixelsService
                .getAllEnumerations(MicroscopeType.class.getName());
        Instrument instrument = new InstrumentI();
        MicroscopeI microscope = new MicroscopeI();
        microscope.setManufacturer(omero.rtypes.rstring("manufacturer"));
        microscope.setModel(omero.rtypes.rstring("model"));
        microscope.setSerialNumber(omero.rtypes.rstring("serial number"));
        microscope.setLotNumber(omero.rtypes.rstring("lot number"));
        microscope.setType((MicroscopeType) types.get(0));
        instrument.setMicroscope(microscope);
        return instrument;
    }

    /**
     * Creates and returns an instrument. The creation using the
     * <code>add*</code> methods has been tested i.e. addDectector, etc.
     *
     * @param light
     *            The type of light source.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Instrument createInstrument(String light) throws Exception {
        return createInstrument(light, null);
    }

    /**
     * Creates and returns an instrument. The creation using the
     * <code>add*</code> methods has been tested i.e. addDectector, etc.
     *
     * @param light
     *            The type of light source.
     * @param pump
     *            Pass the type of light source of the pump or <code>null</code>
     *            .
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Instrument createInstrument(String light, String pump)
            throws Exception {
        Instrument instrument = createInstrument();
        instrument.addDetector(createDetector());
        instrument.addFilter(createFilter(500, 560));
        Dichroic dichroic = createDichroic();
        instrument.addDichroic(dichroic);
        Objective objective = createObjective();
        FilterSet filterSet = createFilterSet();
        instrument.addObjective(objective);
        instrument.addFilterSet(filterSet);
        instrument.addOTF(createOTF(filterSet, objective));
        if (LASER.equals(light)) {
            Laser laser = createLaser();
            instrument.addLightSource(laser);
            if (pump != null) {
                LightSource ls = null;
                if (LASER.equals(pump))
                    ls = createLaser();
                else if (FILAMENT.equals(pump))
                    ls = createArc();
                else if (ARC.equals(pump))
                    ls = createFilament();
                if (ls != null) {
                    instrument.addLightSource(ls);
                    laser.setPump(ls);
                }
            }
        } else if (FILAMENT.equals(light))
            instrument.addLightSource(createFilament());
        else if (ARC.equals(light))
            instrument.addLightSource(createArc());
        else if (LIGHT_EMITTING_DIODE.equals(light))
            instrument.addLightSource(createLightEmittingDiode());
        return instrument;
    }

    /**
     * Creates a plane info object.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public PlaneInfo createPlaneInfo() throws Exception {
        return createPlaneInfo(0, 0, 0);
    }

    /**
     * Creates a plane info object.
     *
     * @param z
     *            The selected z-section.
     * @param t
     *            The selected time-point.
     * @param c
     *            The selected channel.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public PlaneInfo createPlaneInfo(int z, int t, int c) throws Exception {
        PlaneInfo planeInfo = new PlaneInfoI();
        planeInfo.setTheZ(omero.rtypes.rint(z));
        planeInfo.setTheC(omero.rtypes.rint(c));
        planeInfo.setTheT(omero.rtypes.rint(t));

        UnitsTime seconds = UnitsTime.SECOND;
        Time deltaT = new TimeI();
        deltaT.setValue(0.5);
        deltaT.setUnit(seconds);
        planeInfo.setDeltaT(deltaT);
        return planeInfo;
    }

    /**
     * Creates a pixels object.
     *
     * @param sizeX
     *            The size along the X-axis.
     * @param sizeY
     *            The size along the Y-axis.
     * @param sizeZ
     *            The number of Z-sections.
     * @param sizeT
     *            The number of time-points.
     * @param sizeC
     *            The number of channels.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Pixels createPixels(int sizeX, int sizeY, int sizeZ, int sizeT,
            int sizeC) throws Exception {
        List<IObject> types = pixelsService.getAllEnumerations(PixelsType.class
                .getName());
        Iterator<IObject> i = types.iterator();
        PixelsType object;
        PixelsType type = null;
        while (i.hasNext()) {
            object = (PixelsType) i.next();
            if (UINT16.equals(object.getValue().getValue())) {
                type = object;
                break;
            }
        }
        if (type == null)
            type = (PixelsType) types.get(0);
        types = pixelsService
                .getAllEnumerations(DimensionOrder.class.getName());
        i = types.iterator();
        DimensionOrder o;
        DimensionOrder order = null;
        while (i.hasNext()) {
            o = (DimensionOrder) i.next();
            if (XYZCT.equals(o.getValue().getValue())) {
                order = o;
                break;
            }
        }
        if (order == null)
            order = (DimensionOrder) types.get(0);

        UnitsLength mm = UnitsLength.MILLIMETER;
        Length mm1 = new LengthI();
        mm1.setValue(1.0);
        mm1.setUnit(mm);

        Pixels pixels = new PixelsI();
        pixels.setPhysicalSizeX(mm1);
        pixels.setPhysicalSizeY(mm1);
        pixels.setPhysicalSizeZ(mm1);
        pixels.setSizeX(omero.rtypes.rint(sizeX));
        pixels.setSizeY(omero.rtypes.rint(sizeY));
        pixels.setSizeZ(omero.rtypes.rint(sizeZ));
        pixels.setSizeT(omero.rtypes.rint(sizeT));
        pixels.setSizeC(omero.rtypes.rint(sizeC));
        pixels.setSha1(omero.rtypes.rstring("Pending..."));
        pixels.setPixelsType(type);
        pixels.setDimensionOrder(order);
        List<Channel> channels = new ArrayList<Channel>();
        for (int j = 0; j < sizeC; j++) {
            channels.add(createChannel(j));
        }
        pixels.addAllChannelSet(channels);

        for (int z = 0; z < sizeZ; z++) {
            for (int t = 0; t < sizeT; t++) {
                for (int c = 0; c < sizeC; c++) {
                    pixels.addPlaneInfo(createPlaneInfo(z, t, c));
                }
            }
        }
        return pixels;
    }

    /**
     * Creates a channel.
     *
     * @param w
     *            The wavelength in nanometers.
     * @return See Above.
     * @throws Exception
     */
    public Channel createChannel(int w) throws Exception {
        Channel channel = new ChannelI();
        LogicalChannel lc = new LogicalChannelI();
        lc.setEmissionWave(new LengthI(200.1, UnitsFactory.Channel_EmissionWavelength));
        List<IObject> types = pixelsService
                .getAllEnumerations(ContrastMethod.class.getName());
        ContrastMethod cm = (ContrastMethod) types.get(0);

        types = pixelsService.getAllEnumerations(Illumination.class.getName());
        Illumination illumination = (Illumination) types.get(0);
        types = pixelsService.getAllEnumerations(AcquisitionMode.class
                .getName());
        AcquisitionMode mode = (AcquisitionMode) types.get(0);
        lc.setContrastMethod(cm);
        lc.setIllumination(illumination);
        lc.setMode(mode);
        channel.setLogicalChannel(lc);
        StatsInfo info = new StatsInfoI();
        info.setGlobalMin(omero.rtypes.rdouble(0.0));
        info.setGlobalMax(omero.rtypes.rdouble(1.0));
        channel.setStatsInfo(info);
        return channel;
    }

    /**
     * Creates a default pixels set.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Pixels createPixels() throws Exception {
        return createPixels(SIZE_X, SIZE_Y, SIZE_Y, SIZE_Y,
                DEFAULT_CHANNELS_NUMBER);
    }

    /**
     * Creates an image. This method has been tested in
     * <code>PixelsServiceTest</code>.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Image createImage() throws Exception {
        return createImage(1, 1, 1, 1, 1);
    }

    /**
     * Creates an image.
     *
     * @param sizeX
     *            The size along the X-axis.
     * @param sizeY
     *            The size along the Y-axis.
     * @param sizeZ
     *            The number of Z-sections.
     * @param sizeT
     *            The number of time-points.
     * @param sizeC
     *            The number of channels.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public Image createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
            int sizeC) throws Exception {
        Image image = simpleImage();
        Pixels pixels = createPixels(sizeX, sizeY, sizeZ, sizeT, sizeC);
        image.addPixels(pixels);
        return image;
    }

    /**
     * Creates a plate.
     *
     * @param rows
     *            The number of rows.
     * @param columns
     *            The number of columns.
     * @param fields
     *            The number of fields.
     * @param numberOfPlateAcquisition
     *            The number of plate acquisitions.
     * @param fullImage
     *            Pass <code>true</code> to add image with pixels,
     *            <code>false</code> to create a simple image.
     * @return See above.
     */
    public Plate createPlate(int rows, int columns, int fields,
            int numberOfPlateAcquisition, boolean fullImage) throws Exception {
        if (numberOfPlateAcquisition < 0)
            numberOfPlateAcquisition = 0;
        Plate p = new PlateI();
        p.setRows(omero.rtypes.rint(rows));
        p.setColumns(omero.rtypes.rint(columns));
        p.setName(omero.rtypes.rstring("plate name"));
        p.setDescription(omero.rtypes.rstring("plate description"));
        p.setStatus(omero.rtypes.rstring("plate status"));
        p.setExternalIdentifier(omero.rtypes.rstring("external identifier"));
        // now make wells
        Well well;
        WellSample sample;
        List<PlateAcquisition> pas = new ArrayList<PlateAcquisition>();
        PlateAcquisition pa;
        for (int i = 0; i < numberOfPlateAcquisition; i++) {
            pa = new PlateAcquisitionI();
            pa.setName(omero.rtypes.rstring("plate acquisition"));
            pa.setPlate(p);
            pas.add(pa);
        }
        Iterator<PlateAcquisition> i;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                well = new WellI();
                well.setRow(omero.rtypes.rint(row));
                well.setColumn(omero.rtypes.rint(column));
                if (pas.size() == 0) {
                    for (int field = 0; field < fields; field++) {
                        sample = new WellSampleI();
                        if (fullImage)
                            sample.setImage(createImage());
                        else
                            sample.setImage(simpleImage());
                        well.addWellSample(sample);
                    }
                } else {
                    i = pas.iterator();
                    while (i.hasNext()) {
                        pa = i.next();
                        for (int field = 0; field < fields; field++) {
                            sample = new WellSampleI();
                            if (fullImage)
                                sample.setImage(createImage());
                            else
                                sample.setImage(simpleImage());
                            well.addWellSample(sample);
                            pa.addWellSample(sample);
                        }
                    }
                }

                p.addWell(well);
            }
        }
        return p;
    }

    /**
     * Creates a basic plate and links the wells to the passed reagent.
     *
     * @param rows
     *            The number of rows.
     * @param columns
     *            The number of columns.
     * @param fields
     *            The number of fields.
     * @param r
     *            The reagent.
     * @return See above.
     */
    public Plate createPlateWithReagent(int rows, int columns, int fields,
            Reagent r) {
        Plate p = new PlateI();
        p.setRows(omero.rtypes.rint(rows));
        p.setColumns(omero.rtypes.rint(columns));
        p.setName(omero.rtypes.rstring("plate"));
        // now make wells
        Well well;
        WellSample sample;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                well = new WellI();
                well.setRow(omero.rtypes.rint(row));
                well.setColumn(omero.rtypes.rint(column));
                well.linkReagent(r);
                for (int field = 0; field < fields; field++) {
                    sample = new WellSampleI();
                    sample.setImage(simpleImage());
                    well.addWellSample(sample);
                }
                p.addWell(well);
            }
        }
        return p;
    }

    /**
     * Returns the reagent.
     *
     * @return See above.
     */
    public Reagent createReagent() {
        Reagent reagent = new ReagentI();
        reagent.setDescription(omero.rtypes.rstring("Reagent Description"));
        reagent.setName(omero.rtypes.rstring("Reagent Name"));
        reagent.setReagentIdentifier(omero.rtypes.rstring("Reagent Identifier"));
        return reagent;
    }

    /**
     * Returns an Image with a Roi and one Rect attached.
     */
    public Image createImageWithRoi() throws Exception {
        Roi roi = new RoiI();
        roi.addShape(new RectI());
        Image image = createImage();
        image.addRoi(roi);
        return image;
    }

    // imaging
    /**
     * Creates an image file of the specified format.
     *
     * @param file
     *            The file where to write the image.
     * @param format
     *            One of the follow types: jpeg, png.
     * @throws Exception
     *             Thrown if an error occurred while encoding the image.
     */
    public void createImageFile(File file, String format) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(file);
        writer.setOutput(ios);
        writer.write(new BufferedImage(WIDTH, HEIGHT,
                BufferedImage.TYPE_INT_RGB));
        ios.close();
    }

    /**
     * For a given IAnnotated type
     *
     * @param o
     * @param a
     */
    public IObject createAnnotationLink(IObject o, Annotation a)
            throws Exception {
        String name;
        if (o instanceof Annotation) {
            name = "omero.model.AnnotationAnnotationLinkI";
        } else {
            name = o.getClass().getSimpleName();
            name = name.substring(0, name.length() - 1);
            name = "omero.model." + name + "AnnotationLinkI";
        }
        Class<?> linkClass = Class.forName(name);
        IObject link = (IObject) linkClass.newInstance();
        Method linkMethod = null;
        for (Method m : linkClass.getMethods()) {
            if ("link".equals(m.getName())) {
                linkMethod = m;
                break;
            }
        }
        linkMethod.invoke(link, o, a, null); // Last is Ice.Current;
        return link;
    }
}
