/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.testing.ObjectFactory;
import omero.OptimisticLockException;
import omero.RInt;
import omero.RLong;
import omero.api.IAdminPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.*;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/**
 * Base test for integration tests.
 *
 * @since Beta4.2
 */
@Test(groups = { "client", "integration", "blitz" })
public class AbstractTest
	extends TestCase
{


	/** Identifies the laser light source. */
	protected String LASER = Laser.class.getName();
	
	/** Identifies the filament light source. */
	protected String FILAMENT = Filament.class.getName();
	
	/** Identifies the arc light source. */
	protected String ARC = Arc.class.getName();
	
	/** Identifies the arc light source. */
	protected String LIGHT_EMITTING_DIODE = LightEmittingDiode.class.getName();
	
	/** The possible sources of light. */
	protected String[] LIGHT_SOURCES = {LASER, FILAMENT, ARC, 
			LIGHT_EMITTING_DIODE};
	
	/** The default number of channels. */
	public int DEFAULT_CHANNELS_NUMBER = 3;
	
	/** The default size along the X-axis. */
	public int SIZE_X = 10;
	
	/** The default size along the Y-axis. */
	public int SIXE_Y = 10;
	
	/** The number of z-sections. */
	public int SIXE_Z = 10;
	
	/** The number of time points. */
	public int SIXE_T = 10;
	
	/** Identifies the <code>system</code> group. */
	public String SYSTEM_GROUP = "system";
	
	/** Identifies the <code>user</code> group. */
	public String USER_GROUP = "user";
	
	/** Identifies the <code>guest</code> group. */
	public String GUEST_GROUP = "guest";
	
	/** Holds the error, info, warning. */
    protected Log log = LogFactory.getLog(getClass());

	/** The client object, this is the entry point to the Server. */
    protected omero.client client;

    /** A root-client object. */
    protected omero.client root;

    /** Helper reference to the <code>Service factory</code>. */
    protected ServiceFactoryPrx factory;

    /** Helper reference to the <code>IQuery</code> service. */
    protected IQueryPrx iQuery;

    /** Helper reference to the <code>IUpdate</code> service. */
    protected IUpdatePrx iUpdate;

    /** Helper reference to the <code>IAdmin</code> service. */
    protected IAdminPrx iAdmin;
    
    
    // ~ Helpers
    // =========================================================================
    /**
     * Creates a default image and returns it.
     *
     * @param time The acquisition time.
     * @return See above.
     */
    protected Image simpleImage(long time)
    {
        // prepare data
        Image img = new ImageI();
        img.setName(rstring("image1"));
        img.setDescription(rstring("descriptionImage1"));
        img.setAcquisitionDate(rtime(time));
        return img;
    }
    
    /**
     * Creates a default dataset and returns it.
     * 
     * @return See above.
     */
    protected DatasetData simpleDatasetData()
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
    protected ProjectData simpleProjectData()
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
    protected ScreenData simpleScreenData()
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
    protected PlateData simplePlateData()
    {
    	PlateData data = new PlateData();
        data.setName("plate1");
        data.setDescription("plate1");
        return data;
    }

    /**
     * Creates and returns an original file object.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected OriginalFileI createOriginalFile()
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
    protected Detector createDetector()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
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
    protected OTF createOTF(FilterSet filterSet, Objective objective)
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
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
    protected Filter createFilter(int cutIn, int cutOut)
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
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
    protected FilterSet createFilterSet()
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
    protected Dichroic createDichroic()
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
    protected Objective createObjective()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	
    	Objective objective = new ObjectiveI();
    	objective.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	objective.setModel(omero.rtypes.rstring("model"));
    	objective.setCalibratedMagnification(omero.rtypes.rdouble(1));
    	//correction
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
    			Correction.class.getName());
    	objective.setCorrection((Correction) types.get(0));
    	//immersion
    	types = svc.getAllEnumerations(Immersion.class.getName());
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
    protected ObjectiveSettings createObjectiveSettings(Objective objective)
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
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
    protected StageLabel createStageLabel()
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
    protected ImagingEnvironment createImageEnvironment()
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
    protected DetectorSettings createDetectorSettings(Detector detector)
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
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
    protected LightSettings createLightSettings(LightSource light)
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	//already tested see PixelsService enumeration.
    	List<IObject> types = svc.getAllEnumerations(
    			MicrobeamManipulationType.class.getName());
    	LightSettings settings = new LightSettingsI();
    	settings.setLightSource(light);
    	settings.setAttenuation(omero.rtypes.rdouble(1));
    	MicrobeamManipulation mm = new MicrobeamManipulationI();
    	mm.setType((MicrobeamManipulationType) types.get(0));
    	Experiment exp = new ExperimentI();
    	types = svc.getAllEnumerations(
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
    protected LightPath createLightPath(Filter emissionFilter, 
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
    protected Filament createFilament()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = 
    		svc.getAllEnumerations(FilamentType.class.getName());
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
    protected Arc createArc()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = svc.getAllEnumerations(ArcType.class.getName());
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
    protected LightEmittingDiode createLightEmittingDiode()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = svc.getAllEnumerations(
    			LightEmittingDiode.class.getName());
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
    protected Laser createLaser()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	Laser laser = new LaserI();
    	laser.setManufacturer(omero.rtypes.rstring("manufacturer"));
    	laser.setModel(omero.rtypes.rstring("model"));
    	laser.setFrequencyMultiplication(omero.rtypes.rint(1));
    	// type
    	List<IObject> types = svc.getAllEnumerations(LaserType.class.getName());
    	laser.setType((LaserType) types.get(0));
    	//laser medium
    	types = svc.getAllEnumerations(LaserMedium.class.getName());
    	laser.setLaserMedium((LaserMedium) types.get(0));
    	
    	//pulse
    	types = svc.getAllEnumerations(Pulse.class.getName());
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
    protected Instrument createInstrument()
    	throws Exception
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> types = svc.getAllEnumerations(
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
    protected Instrument createInstrument(String light)
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
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
        // administrator client
        omero.client tmp = new omero.client();
        String rootpass = tmp.getProperty("omero.rootpass");
        root = new omero.client();
        root.createSession("root", rootpass);
        tmp.__del__();

        newUserAndGroup("rw----");
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterClass
    public void tearDown() 
    	throws Exception
    {
        if (client != null) {
            client.__del__();
        }

        if (root != null) {
            root.__del__();
        }
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     * 
     * @param perms The permissions level.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(String perms) 
    	throws Exception
    {
        return newUserAndGroup(new PermissionsI(perms));
    }

    /**
     * Creates a new group and experimenter and returns the event context.
     * 
     * @param perms The permissions level.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected EventContext newUserAndGroup(Permissions perms) 
    	throws Exception
    {
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integeration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(perms);
        rootAdmin.createGroup(g);
        rootAdmin.createUser(e, uuid);

        if (client != null) {
            client.__del__();
        }
        
        client = new omero.client();
        factory = client.createSession(uuid, uuid);
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
        iAdmin = factory.getAdminService();
        return iAdmin.getEventContext();
    }

    /**
     * Creates a pixels set.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Pixels createPixels() 
    	throws Exception
    {
        return createPixels(null);
    }

    /**
     * Creates a pixels set.
     * 
     * @param example The pixels set of reference.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Pixels createPixels(Pixels example) 
    	throws Exception {
        IceMapper mapper = new IceMapper();
        ome.model.core.Pixels p = 
        	(ome.model.core.Pixels) mapper.reverse(example);
        p = ObjectFactory.createPixelGraphWithChannels(p, 
        		DEFAULT_CHANNELS_NUMBER);
        return (Pixels) mapper.map(p);
    }

    /**
     * Creates a channel.
     * 
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected Channel createChannel() 
    	throws Exception
    {
        IceMapper mapper = new IceMapper();
        return (Channel) mapper.map(ObjectFactory.createChannel(null));
    }
    
    /**
     * Creates a plane info object.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    protected PlaneInfo createPlaneInfo()
   		throws Exception
    {
        PlaneInfo planeInfo = new PlaneInfoI();
        planeInfo.setTheZ(omero.rtypes.rint(0));
        planeInfo.setTheC(omero.rtypes.rint(0));
        planeInfo.setTheT(omero.rtypes.rint(0));
        planeInfo.setDeltaT(omero.rtypes.rdouble(0.5));
        return planeInfo;
    }
    
    /**
     * Compares the passed rendering definitions.
     * 
     * @param def1 The first rendering definition to handle.
     * @param def2 The second rendering definition to handle.
     * @throws Exception Thrown if an error occurred.
     */
    protected void compareRenderingDef(RenderingDef def1, RenderingDef def2)
		throws Exception 
	{
		assertNotNull(def1);
		assertNotNull(def2);
		assertTrue(def1.getDefaultZ().getValue() == 
				def2.getDefaultZ().getValue());
		assertTrue(def1.getDefaultT().getValue() == 
			def2.getDefaultT().getValue());
		assertTrue(def1.getModel().getValue().getValue().equals( 
			def2.getModel().getValue().getValue()));
		QuantumDef q1 = def1.getQuantization();
		QuantumDef q2 = def2.getQuantization();
		assertNotNull(q1);
		assertNotNull(q2);
		assertTrue(q1.getBitResolution().getValue() == 
			q2.getBitResolution().getValue());
		assertTrue(q1.getCdStart().getValue() == 
			q2.getCdStart().getValue());
		assertTrue(q1.getCdEnd().getValue() == 
			q2.getCdEnd().getValue());
		List<ChannelBinding> channels1 = def1.copyWaveRendering();
		List<ChannelBinding> channels2 = def2.copyWaveRendering();
		assertNotNull(channels1);
		assertNotNull(channels2);
		assertTrue(channels1.size() == channels2.size());
		Iterator<ChannelBinding> i = channels1.iterator();
		ChannelBinding c1, c2;
		int index = 0;
		while (i.hasNext()) {
			c1 = i.next();
			c2 = channels2.get(index);
			assertTrue(c1.getAlpha().getValue() == c2.getAlpha().getValue());
			assertTrue(c1.getRed().getValue() == c2.getRed().getValue());
			assertTrue(c1.getGreen().getValue() == c2.getGreen().getValue());
			assertTrue(c1.getBlue().getValue() == c2.getBlue().getValue());
			assertTrue(c1.getCoefficient().getValue() 
					== c2.getCoefficient().getValue());
			assertTrue(c1.getFamily().getValue().getValue().equals(
					c2.getFamily().getValue().getValue()) );
			assertTrue(c1.getInputStart().getValue() == 
				c2.getInputStart().getValue());
			assertTrue(c1.getInputEnd().getValue() == 
				c2.getInputEnd().getValue());
			Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
			Boolean b2 = Boolean.valueOf(c2.getActive().getValue());
			assertTrue(b1.equals(b2));
			b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
			b2 = Boolean.valueOf(c2.getNoiseReduction().getValue());
			assertTrue(b1.equals(b2));
		}
	}
 
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	protected Image createImage()
		throws Exception
	{
		return createImage(1, 1, 1, 1, 1);
	}
	
	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	protected Image createImage(int sizeC, int sizeX, int sizeY, int sizeZ, 
			int sizeT)
		throws Exception
	{
		IPixelsPrx svc = factory.getPixelsService();

    	List<IObject> types = 
    		svc.getAllEnumerations(PixelsType.class.getName());
    	List<Integer> channels = new ArrayList<Integer>();
    	for (int i = 0; i < sizeC; i++) {
			channels.add(i);
		}
    	
    	RLong id = svc.createImage(sizeX, sizeY, sizeZ, sizeT, channels, 
    			(PixelsType) types.get(1), "test", "");
    	//Retrieve the image.
    	ParametersI param = new ParametersI();
    	param.addId(id.getValue());
    	Image img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	return (Image) iUpdate.saveAndReturnObject(img);
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
	protected Plate createPlate(int rows, int columns, int fields, boolean
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
	
}
