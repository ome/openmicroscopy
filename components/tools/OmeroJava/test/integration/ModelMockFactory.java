/*
 * $Id$
 *
 *  Copyright 2006-2010 University of Dundee. All rights reserved.
 *  Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

//Java imports
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.api.IPixelsPrx;
import omero.model.Arc;
import omero.model.ArcI;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.Channel;
import omero.model.ChannelI;
import omero.model.Correction;
import omero.model.Detector;
import omero.model.DetectorI;
import omero.model.DetectorSettings;
import omero.model.DetectorSettingsI;
import omero.model.DetectorType;
import omero.model.Dichroic;
import omero.model.DichroicI;
import omero.model.DimensionOrder;
import omero.model.Experiment;
import omero.model.ExperimentI;
import omero.model.ExperimentType;
import omero.model.Filament;
import omero.model.FilamentI;
import omero.model.FilamentType;
import omero.model.Filter;
import omero.model.FilterI;
import omero.model.FilterSet;
import omero.model.FilterSetI;
import omero.model.FilterType;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Immersion;
import omero.model.Instrument;
import omero.model.InstrumentI;
import omero.model.Laser;
import omero.model.LaserI;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LightEmittingDiode;
import omero.model.LightEmittingDiodeI;
import omero.model.LightPath;
import omero.model.LightPathI;
import omero.model.LightSettings;
import omero.model.LightSettingsI;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.LogicalChannelI;
import omero.model.Medium;
import omero.model.MicrobeamManipulation;
import omero.model.MicrobeamManipulationI;
import omero.model.MicrobeamManipulationType;
import omero.model.MicroscopeI;
import omero.model.MicroscopeType;
import omero.model.OTF;
import omero.model.OTFI;
import omero.model.Objective;
import omero.model.ObjectiveI;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.PlaneInfo;
import omero.model.PlaneInfoI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;
import omero.model.PlateI;
import omero.model.Pulse;
import omero.model.Reagent;
import omero.model.ReagentI;
import omero.model.StageLabel;
import omero.model.StageLabelI;
import omero.model.StatsInfo;
import omero.model.StatsInfoI;
import omero.model.TransmittanceRangeI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import pojos.DatasetData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Helper class.
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
class ModelMockFactory 
{

	/** Identifies the laser light source. */
	static String LASER = Laser.class.getName();
	
	/** Identifies the filament light source. */
	static String FILAMENT = Filament.class.getName();
	
	/** Identifies the arc light source. */
	static String ARC = Arc.class.getName();
	
	/** Identifies the arc light source. */
	static String LIGHT_EMITTING_DIODE = LightEmittingDiode.class.getName();
	
	/** The possible sources of light. */
	static String[] LIGHT_SOURCES = {LASER, FILAMENT, ARC, LIGHT_EMITTING_DIODE};
	
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

	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsService
	 */
	ModelMockFactory(IPixelsPrx pixelsService)
	{
		this.pixelsService = pixelsService;
	}
	
	//POJO
    /**
     * Creates a default dataset and returns it.
     * 
     * @return See above.
     */
    DatasetData simpleDatasetData()
    {
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
    ProjectData simpleProjectData()
    {
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
    ScreenData simpleScreenData()
    {
    	ScreenData data = new ScreenData();
        data.setName("screen1");
        data.setDescription("screen1");
        return data;
    }
    
    /**
     * Creates a default project and returns it.
     * 
     * @return See above.
     */
    PlateData simplePlateData()
    {
    	PlateData data = new PlateData();
        data.setName("plate1");
        data.setDescription("plate1");
        return data;
    }

    /**
     * Creates a default image and returns it.
     *
     * @param time The acquisition time.
     * @return See above.
     */
    Image simpleImage(long time)
    {
        // prepare data
        Image img = new ImageI();
        img.setName(rstring("image1"));
        img.setDescription(rstring("descriptionImage1"));
        img.setAcquisitionDate(rtime(time));
        return img;
    }
    
    /**
     * Creates and returns an original file object.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    OriginalFileI createOriginalFile()
    	throws Exception
    {
    	OriginalFileI oFile = new OriginalFileI();
		oFile.setName(omero.rtypes.rstring("of1"));
		oFile.setPath(omero.rtypes.rstring("/omero"));
		oFile.setSize(omero.rtypes.rlong(0));
		oFile.setSha1(omero.rtypes.rstring("pending"));
		oFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
		return oFile;
    }
    

    /**
     * Creates and returns a detector. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Detector createDetector()
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			DetectorType.class.getName());
    	Detector detector = new DetectorI();
    	detector.setAmplificationGain(omero.rtypes.rdouble(0));
    	detector.setGain(omero.rtypes.rdouble(1));
    	detector.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	detector.setModel(omero.rtypes.rstring("model"));
    	detector.setSerialNumber(omero.rtypes.rstring("number"));
    	detector.setOffsetValue(omero.rtypes.rdouble(0));
    	detector.setType((DetectorType) types.get(0));
    	return detector;
    }
    
    /**
     * Creates an Optical Transfer Function object.
     * 
     * @param filterSet The filter set linked to it.
     * @param objective The objective linked to it.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    OTF createOTF(FilterSet filterSet, Objective objective)
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			PixelsType.class.getName());
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
     * Creates and returns a filter. 
     * This will have to be linked to an instrument.
     * 
     * @param cutIn The cut in value.
     * @param cutOut The cut out value.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Filter createFilter(int cutIn, int cutOut)
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			FilterType.class.getName());
    	Filter filter = new FilterI();
    	filter.setLotNumber(omero.rtypes.rstring("lot number"));
    	filter.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	filter.setModel(omero.rtypes.rstring("model"));
    	filter.setType((FilterType) types.get(0));
    	
    	TransmittanceRangeI transmittance = new TransmittanceRangeI();
    	transmittance.setCutIn(omero.rtypes.rint(cutIn));
    	transmittance.setCutOut(omero.rtypes.rint(cutOut));
    	filter.setTransmittanceRange(transmittance);
    	return filter;
    }
    
    /**
     * Creates a basic filter set.
     * 
     * @return See above.
     */
    FilterSet createFilterSet()
    {
    	FilterSet set = new FilterSetI();
    	set.setLotNumber(omero.rtypes.rstring("lot number"));
    	set.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	set.setModel(omero.rtypes.rstring("model"));
    	return set;
    }
    
    /**
     * Creates and returns a dichroic. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Dichroic createDichroic()
    	throws Exception
    {
    	Dichroic dichroic = new DichroicI();
    	dichroic.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	dichroic.setModel(omero.rtypes.rstring("model"));
    	dichroic.setLotNumber(omero.rtypes.rstring("lot number"));
    	return dichroic;
    }
    
    /**
     * Creates and returns an objective. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Objective createObjective()
    	throws Exception
    {
    	Objective objective = new ObjectiveI();
    	objective.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	objective.setModel(omero.rtypes.rstring("model"));
    	objective.setSerialNumber(omero.rtypes.rstring("0123456"));
    	objective.setCalibratedMagnification(omero.rtypes.rdouble(1));
    	//correction
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			Correction.class.getName());
    	objective.setCorrection((Correction) types.get(0));
    	//immersion
    	types = pixelsService.getAllEnumerations(Immersion.class.getName());
    	objective.setImmersion((Immersion) types.get(0));
    	
    	objective.setIris(omero.rtypes.rbool(true));
    	objective.setLensNA(omero.rtypes.rdouble(0.5));
    	objective.setNominalMagnification(omero.rtypes.rint(1));
    	objective.setWorkingDistance(omero.rtypes.rdouble(1));
    	return objective;
    }
    
    /**
     * Creates and returns the settings of the specified objective. 
     * 
     * @param objective The objective to link the settings to.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    ObjectiveSettings createObjectiveSettings(Objective objective)
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			Medium.class.getName());
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
    StageLabel createStageLabel()
    {
    	StageLabel label = new StageLabelI();
    	label.setName(omero.rtypes.rstring("label"));
    	label.setPositionX(omero.rtypes.rdouble(1));
    	label.setPositionY(omero.rtypes.rdouble(1));
    	label.setPositionZ(omero.rtypes.rdouble(1));
    	return label;
    }

    /**
     * Creates and returns the environment.
     * 
     * @return See above.
     */
    ImagingEnvironment createImageEnvironment()
    {
    	ImagingEnvironment env = new ImagingEnvironmentI();
    	env.setAirPressure(omero.rtypes.rdouble(1));
    	env.setCo2percent(omero.rtypes.rdouble(10));
    	env.setHumidity(omero.rtypes.rdouble(1));
    	env.setTemperature(omero.rtypes.rdouble(1));
    	return env;
    }
    
    /**
     * Creates and returns the settings of the specified detector. 
     * 
     * @param detector The detector to link the settings to.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    DetectorSettings createDetectorSettings(Detector detector)
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			Binning.class.getName());
    	DetectorSettings settings = new DetectorSettingsI();
    	settings.setBinning((Binning) types.get(0));
    	settings.setDetector(detector);
    	settings.setGain(omero.rtypes.rdouble(1));
    	settings.setOffsetValue(omero.rtypes.rdouble(1));
    	settings.setReadOutRate(omero.rtypes.rdouble(1));
    	settings.setVoltage(omero.rtypes.rdouble(1));
    	return settings;
    }
    
    /**
     * Creates and returns the settings of the specified source of light. 
     * 
     * @param light The light to link the settings to.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    LightSettings createLightSettings(LightSource light)
    	throws Exception
    {
    	//already tested see PixelsService enumeration.
    	List<IObject> types = pixelsService.getAllEnumerations(
    			MicrobeamManipulationType.class.getName());
    	LightSettings settings = new LightSettingsI();
    	settings.setLightSource(light);
    	settings.setAttenuation(omero.rtypes.rdouble(1));
    	MicrobeamManipulation mm = new MicrobeamManipulationI();
    	mm.setType((MicrobeamManipulationType) types.get(0));
    	Experiment exp = new ExperimentI();
    	types = pixelsService.getAllEnumerations(
    			ExperimentType.class.getName());
    	exp.setType((ExperimentType) types.get(0));
    	mm.setExperiment(exp);
    	settings.setMicrobeamManipulation(mm);
    	settings.setWavelength(omero.rtypes.rint(500));
    	return settings;
    }
    
    /**
     * Creates a light path.
     * 
     * @param emissionFilter The emission filter or <code>null</code>.
     * @param dichroic       The dichroic or <code>null</code>.
     * @param excitationFilter The excitation filter or <code>null</code>.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    LightPath createLightPath(Filter emissionFilter, 
    		Dichroic dichroic, Filter excitationFilter)
    {
    	LightPath path = new LightPathI();
    	if (dichroic != null) path.setDichroic(dichroic);
    	return path;
    }
    
    /**
     * Creates and returns a filament. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Filament createFilament()
    	throws Exception
    {
    	List<IObject> types = 
    		pixelsService.getAllEnumerations(FilamentType.class.getName());
    	Filament filament = new FilamentI();
    	filament.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	filament.setModel(omero.rtypes.rstring("model"));
    	filament.setPower(omero.rtypes.rdouble(1));
    	filament.setSerialNumber(omero.rtypes.rstring("serial number"));
    	filament.setType((FilamentType) types.get(0));
    	return filament;
    }
    
    /**
     * Creates and returns a filament. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Arc createArc()
    	throws Exception
    {
    	List<IObject> types = pixelsService.getAllEnumerations(
    			ArcType.class.getName());
    	Arc arc = new ArcI();
    	arc.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	arc.setModel(omero.rtypes.rstring("model"));
    	arc.setPower(omero.rtypes.rdouble(1));
    	arc.setSerialNumber(omero.rtypes.rstring("serial number"));
    	arc.setType((ArcType) types.get(0));
    	return arc;
    }
    
    /**
     * Creates and returns a filament. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    LightEmittingDiode createLightEmittingDiode()
    	throws Exception
    {
    	LightEmittingDiode light = new LightEmittingDiodeI();
    	light.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	light.setModel(omero.rtypes.rstring("model"));
    	light.setPower(omero.rtypes.rdouble(1));
    	light.setSerialNumber(omero.rtypes.rstring("serial number"));
    	return light;
    }
    
    /**
     * Creates and returns a laser. 
     * This will have to be linked to an instrument.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Laser createLaser()
    	throws Exception
    {
    	Laser laser = new LaserI();
    	laser.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	laser.setModel(omero.rtypes.rstring("model"));
    	laser.setFrequencyMultiplication(omero.rtypes.rint(1));
    	// type
    	List<IObject> types = pixelsService.getAllEnumerations(
    			LaserType.class.getName());
    	laser.setType((LaserType) types.get(0));
    	//laser medium
    	types = pixelsService.getAllEnumerations(
    			LaserMedium.class.getName());
    	laser.setLaserMedium((LaserMedium) types.get(0));
    	
    	//pulse
    	types = pixelsService.getAllEnumerations(Pulse.class.getName());
    	laser.setPulse((Pulse) types.get(0));
    	
    	laser.setFrequencyMultiplication(omero.rtypes.rint(0));
    	laser.setPockelCell(omero.rtypes.rbool(false));
    	laser.setPower(omero.rtypes.rdouble(0));
    	laser.setRepetitionRate(omero.rtypes.rdouble(1));
    	return laser;
    }
    
    /**
     * Creates and returns an instrument. 
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Instrument createInstrument()
    	throws Exception
    {
    	List<IObject> types = pixelsService.getAllEnumerations(
    			MicroscopeType.class.getName());
    	Instrument instrument = new InstrumentI();
    	MicroscopeI microscope = new MicroscopeI();
    	microscope.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	microscope.setModel(omero.rtypes.rstring("model"));
    	microscope.setSerialNumber(omero.rtypes.rstring("number"));
    	microscope.setType((MicroscopeType) types.get(0));
    	instrument.setMicroscope(microscope);
    	return instrument;
    }
    
    /**
     * Creates and returns an instrument. The creation using the 
     * <code>add*</code> methods has been tested i.e. addDectector, etc.
     * 
     * @param light The type of light source.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    Instrument createInstrument(String light)
    	throws Exception
    {
    	Instrument instrument = createInstrument();
    	instrument.addDetector(createDetector());
    	instrument.addFilter(createFilter(500, 560));
    	instrument.addDichroic(createDichroic());
    	Objective objective = createObjective();
    	FilterSet filterSet = createFilterSet();
    	instrument.addObjective(objective);
    	instrument.addFilterSet(filterSet);
    	instrument.addOTF(createOTF(filterSet, objective));
    	if (LASER.equals(light))
    		instrument.addLightSource(createLaser());
    	else if (FILAMENT.equals(light))
    		instrument.addLightSource(createFilament());
    	else if (ARC.equals(light))
    		instrument.addLightSource(createArc());
    	else if (LIGHT_EMITTING_DIODE.equals(light))
    		instrument.addLightSource(createLightEmittingDiode());
    	return instrument;
    }
    
    /**
     * Creates a plane info object.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    PlaneInfo createPlaneInfo()
   		throws Exception
    {
        return createPlaneInfo(0, 0, 0);
    }
    
    /**
     * Creates a plane info object.
     * 
     * @param z The selected z-section.
     * @param t The selected time-point.
     * @param c The selected channel.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    PlaneInfo createPlaneInfo(int z, int t, int c)
   		throws Exception
    {
        PlaneInfo planeInfo = new PlaneInfoI();
        planeInfo.setTheZ(omero.rtypes.rint(z));
        planeInfo.setTheC(omero.rtypes.rint(c));
        planeInfo.setTheT(omero.rtypes.rint(t));
        planeInfo.setDeltaT(omero.rtypes.rdouble(0.5));
        return planeInfo;
    }
	
	/**
	 * Creates a pixels object.
	 * 
	 * @param sizeX The size along the X-axis.
	 * @param sizeY The size along the Y-axis.
	 * @param sizeZ The number of Z-sections.
	 * @param sizeT The number of time-points.
	 * @param sizeC The number of channels.
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	Pixels createPixels(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC)
		throws Exception
	{
		List<IObject> types = pixelsService.getAllEnumerations(
    			PixelsType.class.getName());
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
		if (type == null) type = (PixelsType) types.get(0);
		types = pixelsService.getAllEnumerations(
				DimensionOrder.class.getName());
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
		if (order == null) order = (DimensionOrder) types.get(0);
		Pixels pixels = new PixelsI();
		pixels.setPhysicalSizeX(omero.rtypes.rdouble(1.0));
		pixels.setPhysicalSizeY(omero.rtypes.rdouble(1.0));
		pixels.setPhysicalSizeZ(omero.rtypes.rdouble(1.0));
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
		return pixels;
	}
	
	/**
	 * Creates a channel.
	 * 
	 * @param w The wavelength in nanometers.
	 * @return See Above.
	 * @throws Exception
	 */
	Channel createChannel(int w)
		throws Exception
	{
		Channel channel = new ChannelI();
		LogicalChannel lc = new LogicalChannelI();
		lc.setEmissionWave(omero.rtypes.rint(w));
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
	 * @throws Exception Thrown if an error occurred.
	 */
	Pixels createPixels()
		throws Exception
	{
		return createPixels(SIZE_X, SIZE_Y, SIZE_Y, SIZE_Y, 
				DEFAULT_CHANNELS_NUMBER);
	}
	
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	Image createImage()
		throws Exception
	{
		return createImage(1, 1, 1, 1, 1);
	}
	
	/**
	 * Creates an image. 
	 * 
	 * @param sizeX The size along the X-axis.
	 * @param sizeY The size along the Y-axis.
	 * @param sizeZ The number of Z-sections.
	 * @param sizeT The number of time-points.
	 * @param sizeC The number of channels.
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	Image createImage(int sizeX, int sizeY, int sizeZ, 
			int sizeT, int sizeC)
		throws Exception
	{
		
		Image image = simpleImage(new Date().getTime());
		Pixels pixels = createPixels(sizeX, sizeY, sizeZ, sizeT, sizeC);
		image.addPixels(pixels);
		return image;
	}

	/**
	 * Creates a plate.
	 * 
	 * @param rows The number of rows.
	 * @param columns The number of columns.
	 * @param fields The number of fields.
	 * @param plateAcquisition Pass <code>true</code> to add a plate acquisition,
	 * 					       <code>false</code> otherwise.
	 * @param fullImage Pass <code>true</code> to add image with pixels, 
	 * 					<code>false</code> to create a simple image.
	 * @return See above.
	 */
    Plate createPlate(int rows, int columns, int fields, boolean
            plateAcquisition, boolean fullImage)
        throws Exception
    {
        Plate p = new PlateI();
        p.setRows(omero.rtypes.rint(rows));
        p.setCols(omero.rtypes.rint(columns));
        p.setName(omero.rtypes.rstring("plate"));
        //now make wells
        Well well;
        WellSample sample;
        PlateAcquisition pa = null;
        if (plateAcquisition) {
            pa = new PlateAcquisitionI();
            pa.setName(omero.rtypes.rstring("plate acquisition"));
            pa.setPlate(p);
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                well = new WellI();
                well.setRow(omero.rtypes.rint(row));
                well.setColumn(omero.rtypes.rint(column));
                for (int field = 0; field < fields; field++) {
                    sample = new WellSampleI();
                    if (fullImage) sample.setImage(createImage());
                    else sample.setImage(simpleImage(0));
                    well.addWellSample(sample);
                    if (plateAcquisition) pa.addWellSample(sample);
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
    Reagent createReagent()
    {
    	Reagent reagent = new ReagentI();
    	reagent.setDescription(omero.rtypes.rstring("Reagent Description"));
    	reagent.setName(omero.rtypes.rstring("Reagent Name"));
    	return reagent;
    }
    
}
