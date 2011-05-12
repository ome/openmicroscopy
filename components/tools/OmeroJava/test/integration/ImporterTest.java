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
import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.formats.OMEROMetadataStoreClient;
import ome.xml.model.OME;
import omero.ServerError;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.Annotation;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Dichroic;
import omero.model.Experiment;
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
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.MicrobeamManipulation;
import omero.model.Microscope;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Reagent;
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
	
	/** The collection of files that have to be deleted. */
	private List<File> files;
	
    /**
     * Attempts to create a Java timestamp from an XML date/time string.
     * @param value An <i>xsd:dateTime</i> string.
     * @return A value Java timestamp for <code>value</code> or
     * <code>null</code> if timestamp parsing failed. The error will be logged
     * at the <code>ERROR</code> log level.
     */
    private Timestamp timestampFromXmlString(String value)
    {
        try
        {
            SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            return new Timestamp(sdf.parse(value).getTime());
        }
        catch (ParseException e)
        {
            log.error(String.format(
                    "Parsing timestamp '%s' failed!", value), e);
        }
        return null;
    }

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param objective The objective to check.
	 * @param xml The XML version.
	 */
	private void validateObjective(Objective objective, 
			ome.xml.model.Objective xml)
	{
		assertEquals(objective.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(objective.getModel().getValue(), 
				xml.getModel());
		assertEquals(objective.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(objective.getCalibratedMagnification().getValue(), 
				xml.getCalibratedMagnification().doubleValue());
		assertTrue(objective.getCorrection().getValue().getValue().equals( 
				xml.getCorrection().getValue()));
		assertTrue(objective.getImmersion().getValue().getValue().equals(  
				xml.getImmersion().getValue()));
		assertEquals(objective.getIris().getValue(), 
				xml.getIris().booleanValue());
		assertEquals(objective.getLensNA().getValue(), 
				xml.getLensNA().doubleValue());
		assertEquals(objective.getNominalMagnification().getValue(), 
				xml.getNominalMagnification().getValue().intValue());
		assertEquals(objective.getWorkingDistance().getValue(), 
				xml.getWorkingDistance());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param detector The detector to check.
	 * @param xml The XML version.
	 */
	private void validateDetector(Detector detector, 
			ome.xml.model.Detector xml)
	{
		assertEquals(detector.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(detector.getModel().getValue(), 
				xml.getModel());
		assertEquals(detector.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(detector.getAmplificationGain().getValue(), 
				xml.getAmplificationGain().doubleValue());
		assertEquals(detector.getGain().getValue(), 
				xml.getGain());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param arc The arc to check.
	 * @param xml The XML version.
	 */
	private void validateArc(Arc arc, ome.xml.model.Arc xml)
	{
		assertEquals(arc.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(arc.getModel().getValue(), 
				xml.getModel());
		assertEquals(arc.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(arc.getPower().getValue(), xml.getPower());
		assertTrue(arc.getType().getValue().getValue().equals(
				XMLMockObjects.ARC_TYPE.getValue()));
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param laser The laser to check.
	 * @param xml The XML version.
	 */
	private void validateLaser(Laser laser, ome.xml.model.Laser xml)
	{
		assertEquals(laser.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(laser.getModel().getValue(), 
				xml.getModel());
		assertEquals(laser.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(laser.getPower().getValue(), xml.getPower());
		assertTrue(laser.getType().getValue().getValue().equals(
				XMLMockObjects.LASER_TYPE.getValue()));
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param filament The filament to check.
	 * @param xml The XML version.
	 */
	private void validateFilament(Filament filament, ome.xml.model.Filament xml)
	{
		assertEquals(filament.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(filament.getModel().getValue(), 
				xml.getModel());
		assertEquals(filament.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(filament.getPower().getValue(), xml.getPower());
		assertTrue(filament.getType().getValue().getValue().equals(
				XMLMockObjects.FILAMENT_TYPE.getValue()));
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param filter The filter to check.
	 * @param xml The XML version.
	 */
	private void validateFilter(Filter filter, 
			ome.xml.model.Filter xml)
	{
		assertEquals(filter.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(filter.getModel().getValue(), 
				xml.getModel());
		assertEquals(filter.getLotNumber().getValue(), 
				xml.getLotNumber());
		assertTrue(filter.getType().getValue().getValue().equals( 
				xml.getType().getValue()));
		TransmittanceRange tr = filter.getTransmittanceRange();
		ome.xml.model.TransmittanceRange xmlTr = xml.getTransmittanceRange();
		assertEquals(tr.getCutIn().getValue(), 
				xmlTr.getCutIn().getValue().intValue());
		assertEquals(tr.getCutOut().getValue(), 
				xmlTr.getCutOut().getValue().intValue());
		assertEquals(tr.getCutInTolerance().getValue(), 
				xmlTr.getCutInTolerance().getValue().intValue());
		assertEquals(tr.getCutOutTolerance().getValue(), 
				xmlTr.getCutOutTolerance().getValue().intValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param dichroic The dichroic to check.
	 * @param xml The XML version.
	 */
	private void validateDichroic(Dichroic dichroic, 
			ome.xml.model.Dichroic xml)
	{
		assertEquals(dichroic.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(dichroic.getModel().getValue(), 
				xml.getModel());
		assertEquals(dichroic.getLotNumber().getValue(), 
				xml.getLotNumber());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param diode The light emitting diode to check.
	 * @param xml The XML version.
	 */
	private void validateLightEmittingDiode(LightEmittingDiode diode, 
			ome.xml.model.LightEmittingDiode xml)
	{
		assertEquals(diode.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(diode.getModel().getValue(), 
				xml.getModel());
		assertEquals(diode.getPower().getValue(), xml.getPower());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param settings The settings to check.
	 * @param xml The XML version.
	 */
	private void validateDetectorSettings(DetectorSettings settings, 
			ome.xml.model.DetectorSettings xml)
	{
		assertEquals(settings.getBinning().getValue().getValue(), 
				xml.getBinning().getValue());
		assertEquals(settings.getGain().getValue(), xml.getGain());
		assertEquals(settings.getOffsetValue().getValue(), xml.getOffset());
		assertEquals(settings.getReadOutRate().getValue(), 
				xml.getReadOutRate());
		assertEquals(settings.getVoltage().getValue(), 
				xml.getVoltage());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param settings The settings to check.
	 * @param xml The XML version.
	 */
	private void validateObjectiveSettings(ObjectiveSettings settings, 
			ome.xml.model.ObjectiveSettings xml)
	{
		assertEquals(settings.getCorrectionCollar().getValue(), 
				xml.getCorrectionCollar().doubleValue());
		assertEquals(settings.getRefractiveIndex().getValue(), 
				xml.getRefractiveIndex().doubleValue());
		assertEquals(settings.getMedium().getValue().getValue(), 
				xml.getMedium().getValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param settings The settings to check.
	 * @param xml The XML version.
	 */
	private void validateLightSourceSettings(LightSettings settings, 
			ome.xml.model.LightSourceSettings xml)
	{
		assertEquals(settings.getAttenuation().getValue(), 
				xml.getAttenuation().getValue().doubleValue());
		assertEquals(settings.getWavelength().getValue(), 
				xml.getWavelength().getValue().intValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param env The environment to check.
	 * @param xml The XML version.
	 */
	private void validateImagingEnvironment(ImagingEnvironment env, 
			ome.xml.model.ImagingEnvironment xml)
	{
		assertEquals(env.getAirPressure().getValue(), 
				xml.getAirPressure().doubleValue());
		assertEquals(env.getCo2percent().getValue(), 
				xml.getCO2Percent().getValue().doubleValue());
		assertEquals(env.getHumidity().getValue(), 
				xml.getHumidity().getValue().doubleValue());
		assertEquals(env.getTemperature().getValue(), 
				xml.getTemperature().doubleValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param label The label to check.
	 * @param xml The XML version.
	 */
	private void validateStageLabel(StageLabel label, 
			ome.xml.model.StageLabel xml)
	{
		assertEquals(label.getName().getValue(), xml.getName());
		assertEquals(label.getPositionX().getValue(), xml.getX().doubleValue());
		assertEquals(label.getPositionY().getValue(), xml.getY().doubleValue());
		assertEquals(label.getPositionZ().getValue(), xml.getZ().doubleValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param microscope The microscope to check.
	 * @param xml The XML version.
	 */
	private void validateMicroscope(Microscope microscope, 
			ome.xml.model.Microscope xml)
	{
		assertEquals(microscope.getManufacturer().getValue(), 
				xml.getManufacturer());
		assertEquals(microscope.getModel().getValue(), 
				xml.getModel());
		assertEquals(microscope.getSerialNumber().getValue(), 
				xml.getSerialNumber());
		assertEquals(microscope.getType().getValue().getValue(), 
				xml.getType().getValue());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param lc The logical channel to check.
	 * @param xml The XML version.
	 */
	private void validateChannel(LogicalChannel lc, 
			ome.xml.model.Channel xml)
	{
		assertEquals(lc.getName().getValue(), xml.getName());
		assertEquals(lc.getIllumination().getValue().getValue(), 
				xml.getIlluminationType().getValue());
		assertEquals(lc.getMode().getValue().getValue(), 
				xml.getAcquisitionMode().getValue());
		assertEquals(lc.getContrastMethod().getValue().getValue(), 
				xml.getContrastMethod().getValue());
		assertEquals(lc.getEmissionWave().getValue(), 
				xml.getEmissionWavelength().getValue().intValue());
		assertEquals(lc.getExcitationWave().getValue(), 
				xml.getExcitationWavelength().getValue().intValue());
		assertEquals(lc.getFluor().getValue(), xml.getFluor());
		assertEquals(lc.getNdFilter().getValue(), xml.getNDFilter());
		assertEquals(lc.getPockelCellSetting().getValue(), 
				xml.getPockelCellSetting().intValue());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param plate The plate to check.
	 * @param xml The XML version.
	 */
	private void validatePlate(Plate plate, ome.xml.model.Plate xml)
	{
		assertEquals(plate.getName().getValue(), xml.getName());
		assertEquals(plate.getDescription().getValue(), xml.getDescription());
		assertEquals(plate.getRowNamingConvention().getValue(), 
				xml.getRowNamingConvention().getValue());
		assertEquals(plate.getColumnNamingConvention().getValue(), 
				xml.getColumnNamingConvention().getValue());
		assertEquals(plate.getRows().getValue(), 
				xml.getRows().getValue().intValue());
		assertEquals(plate.getColumns().getValue(), 
				xml.getColumns().getValue().intValue());
		assertEquals(plate.getExternalIdentifier().getValue(),
				xml.getExternalIdentifier());
		assertEquals(plate.getWellOriginX().getValue(), 
				xml.getWellOriginX().doubleValue());
		assertEquals(plate.getWellOriginY().getValue(), 
				xml.getWellOriginY().doubleValue());
		assertEquals(plate.getStatus().getValue(), xml.getStatus());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param screen The screen to check.
	 * @param xml The XML version.
	 */
	private void validateScreen(Screen screen, ome.xml.model.Screen xml)
	{
		assertEquals(screen.getName().getValue(), xml.getName());
		assertEquals(screen.getDescription().getValue(), xml.getDescription());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param reagent The reagent to check.
	 * @param xml The XML version.
	 */
	private void validateReagent(Reagent reagent, ome.xml.model.Reagent xml)
	{
		assertEquals(reagent.getName().getValue(), xml.getName());
		assertEquals(reagent.getDescription().getValue(), xml.getDescription());
		assertEquals(reagent.getReagentIdentifier().getValue(), 
				xml.getReagentIdentifier());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param well The plate to check.
	 * @param xml The XML version.
	 */
	private void validateWell(Well well, ome.xml.model.Well xml)
	{
		assertEquals(well.getColumn().getValue(), 
				xml.getColumn().getValue().intValue());
		assertEquals(well.getRow().getValue(), 
				xml.getRow().getValue().intValue());
		assertEquals(well.getExternalDescription().getValue(), 
				xml.getExternalDescription());
		assertEquals(well.getExternalIdentifier().getValue(), 
				xml.getExternalIdentifier());
		Color xmlColor = new Color(xml.getColor());
		assertEquals(well.getAlpha().getValue(), xmlColor.getAlpha());
		assertEquals(well.getRed().getValue(), xmlColor.getRed());
		assertEquals(well.getGreen().getValue(), xmlColor.getGreen());
		assertEquals(well.getBlue().getValue(), xmlColor.getBlue());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param ws The well sample to check.
	 * @param xml The XML version.
	 */
	private void validateWellSample(WellSample ws, ome.xml.model.WellSample xml)
	{
		assertEquals(ws.getPosX().getValue(), 
				xml.getPositionX().doubleValue());
		assertEquals(ws.getPosY().getValue(), 
				xml.getPositionY().doubleValue());
		Timestamp ts = timestampFromXmlString(xml.getTimepoint());
		assertEquals(ws.getTimepoint().getValue(), ts.getTime());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param pa The plate acquisition to check.
	 * @param xml The XML version.
	 */
	private void validatePlateAcquisition(PlateAcquisition pa, 
			ome.xml.model.PlateAcquisition xml)
	{
		assertEquals(pa.getName().getValue(), xml.getName());
		assertEquals(pa.getDescription().getValue(), 
				xml.getDescription());
		Timestamp ts = timestampFromXmlString(xml.getEndTime());
		assertNotNull(ts);
		assertNotNull(pa.getEndTime());
		assertEquals(pa.getEndTime().getValue(), ts.getTime());
		ts = timestampFromXmlString(xml.getStartTime());
		assertNotNull(ts);
		assertNotNull(pa.getStartTime());
		assertEquals(pa.getStartTime().getValue(), ts.getTime());
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param mm The microbeam manipulation to check.
	 * @param xml The XML version.
	 */
	private void validateMicrobeamManipulation(MicrobeamManipulation mm, 
			ome.xml.model.MicrobeamManipulation xml)
	{
		assertEquals(mm.getType().getValue().getValue(), 
				xml.getType().getValue());
		List<LightSettings> settings = mm.copyLightSourceSettings();
		assertEquals(1, mm.sizeOfLightSourceSettings());
		assertEquals(1, settings.size());
		validateLightSourceSettings(settings.get(0), 
				xml.getLightSourceSettings(0));
	}

	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param experiment The microbeam manipulation to check.
	 * @param xml The XML version.
	 */
	private void validateExperiment(Experiment experiment, 
			ome.xml.model.Experiment xml)
	{
		assertEquals(experiment.getType().getValue().getValue(), 
				xml.getType().getValue());
		assertEquals(experiment.getDescription().getValue(), 
				xml.getDescription());
	}
	
	/**
	 * Validates if the inserted object corresponds to the XML object.
	 * 
	 * @param otf The otf to check.
	 * @param xml The XML version.
	 */
	private void validateOTF(OTF otf, ome.xml.model.OTF xml)
	{
		assertEquals(otf.getOpticalAxisAveraged().getValue(), 
				xml.getOpticalAxisAveraged().booleanValue());
		assertEquals(otf.getSizeX().getValue(), 
				xml.getSizeX().getValue().intValue());
		assertEquals(otf.getSizeY().getValue(), 
				xml.getSizeY().getValue().intValue());
		assertEquals(otf.getPixelsType().getValue().getValue(), 
				xml.getType().getValue());
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
		for (int i = 0; i < ModelMockFactory.FORMATS.length; i++) {
			f = File.createTempFile("testImportGraphicsImages"
					+ModelMockFactory.FORMATS[i], 
					"."+ModelMockFactory.FORMATS[i]);
			mmFactory.createImageFile(f, ModelMockFactory.FORMATS[i]);
			files.add(f);
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
			fail("Cannot import the following formats:"+s);
		}
		assertTrue("File Imported", failures.size() == 0);
	}
	
	/**
     * Tests the import of an OME-XML file with one image.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportSimpleImage()
		throws Exception
	{
		File f = File.createTempFile("testImportSimpleImage", 
				"."+OME_FORMAT);
		files.add(f);
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
		int size = 
			XMLMockObjects.SIZE_Z*XMLMockObjects.SIZE_C*XMLMockObjects.SIZE_T;
		//test the pixels
		assertTrue(p.getSizeX().getValue() == XMLMockObjects.SIZE_X);
		assertTrue(p.getSizeY().getValue() == XMLMockObjects.SIZE_Y);
		assertTrue(p.getSizeZ().getValue() == XMLMockObjects.SIZE_Z);
		assertTrue(p.getSizeC().getValue() == XMLMockObjects.SIZE_C);
		assertTrue(p.getSizeT().getValue() == XMLMockObjects.SIZE_T);
		assertTrue(p.getPixelsType().getValue().getValue().equals(
				XMLMockObjects.PIXEL_TYPE.getValue()));
		assertTrue(p.getDimensionOrder().getValue().getValue().equals(
				XMLMockObjects.DIMENSION_ORDER.getValue()));
		//Check the plane info
		
		String sql = "select p from PlaneInfo as p where pixels.id = :pid";
		ParametersI param = new ParametersI();
		param.addLong("pid", p.getId().getValue());
		List<IObject> l = iQuery.findAllByQuery(sql, param);
		assertEquals(size, l.size());
		Iterator<IObject> i;
		PlaneInfo plane;
		int found = 0;
		for (int z = 0; z < XMLMockObjects.SIZE_Z; z++) {
			for (int t = 0; t < XMLMockObjects.SIZE_T; t++) {
				for (int c = 0; c < XMLMockObjects.SIZE_C; c++) {
					i = l.iterator();
					while (i.hasNext()) {
						plane = (PlaneInfo) i.next();
						if (plane.getTheC().getValue() == c && 
							plane.getTheZ().getValue() == z &&
							plane.getTheT().getValue() == t)
							found++;
					}
				}
			}
		}
		assertTrue(found == size);
	}
	
	/**
     * Tests the import of an OME-XML file with one image w/o binary data.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportSimpleImageMetadataOnly()
		throws Exception
	{
		File f = File.createTempFile("testImportSimpleImageMetadataOnly", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createImage(), true);
		try {
			importFile(f, OME_FORMAT, true);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
	}
	
	/**
     * Tests the import of an OME-XML file with one image w/o binary data.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled = true)
	public void testImportSimpleImageMetadataOnlyNoBinaryInFile()
		throws Exception
	{
		File f = File.createTempFile(
				"testImportSimpleImageMetadataOnlyNoBinaryInFile", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(f, xml.createImage(), false);
		try {
			importFile(f, OME_FORMAT, true);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
	}

	/**
     * Tests the import of an OME-XML file with an annotated image.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportAnnotatedImage()
		throws Exception
	{
		File f = File.createTempFile("testImportAnnotatedImage", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new  XMLMockObjects();
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
		//always companion file.
		if (l.size() < XMLMockObjects.ANNOTATIONS.length) {
			fail(String.format("%d < ANNOTATION count %d",
					l.size(), XMLMockObjects.ANNOTATIONS.length));
		}
		int count = 0;
		Annotation a;
		for (IObject object : l) {
			a = ((ImageAnnotationLink) object).getChild();
			if (a instanceof CommentAnnotation) count++;
			else if (a instanceof TagAnnotation) count++;
			else if (a instanceof TermAnnotation) count++;
			else if (a instanceof BooleanAnnotation) count++;
			else if (a instanceof LongAnnotation) count++;
		}
		assertEquals(XMLMockObjects.ANNOTATIONS.length, count);
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
		//Method already tested in PojosServiceTest
		ParametersI po = new ParametersI();
		po.acquisitionData();
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(id);
		List images = factory.getContainerService().getImages(
				Image.class.getName(), ids, po);
		assertEquals(1, images.size());
		Image image = (Image) images.get(0);
		//load the image and make we have everything
		assertNotNull(image.getImagingEnvironment());
		validateImagingEnvironment(image.getImagingEnvironment(), 
				xml.createImageEnvironment());
		assertNotNull(image.getStageLabel());
		validateStageLabel(image.getStageLabel(), 
				xml.createStageLabel());
		
		ObjectiveSettings settings = image.getObjectiveSettings();
		assertNotNull(settings);
		validateObjectiveSettings(image.getObjectiveSettings(), 
				xml.createObjectiveSettings(0));
		
		Instrument instrument = image.getInstrument();
		assertNotNull(instrument);
		//check the instrument
		instrument = factory.getMetadataService().loadInstrument(
				instrument.getId().getValue());
		assertNotNull(instrument);
    	ome.xml.model.Laser xmlLaser = (ome.xml.model.Laser) 
    		xml.createLightSource(ome.xml.model.Laser.class.getName(), 0);
    	ome.xml.model.Arc xmlArc = (ome.xml.model.Arc) 
			xml.createLightSource(ome.xml.model.Arc.class.getName(), 0);
    	ome.xml.model.Filament xmlFilament = (ome.xml.model.Filament) 
			xml.createLightSource(ome.xml.model.Filament.class.getName(), 0);
    	ome.xml.model.LightEmittingDiode xmlDiode = 
    		(ome.xml.model.LightEmittingDiode) 
		xml.createLightSource(ome.xml.model.LightEmittingDiode.class.getName(), 
				0);
    	
    	ome.xml.model.Objective xmlObjective = xml.createObjective(0);
    	ome.xml.model.Detector xmlDetector = xml.createDetector(0);
    	ome.xml.model.Filter xmlFilter = xml.createFilter(0, 
    			XMLMockObjects.CUT_IN, XMLMockObjects.CUT_OUT);
    	ome.xml.model.Dichroic xmlDichroic = xml.createDichroic(0);
    	assertEquals(XMLMockObjects.NUMBER_OF_OBJECTIVES, 
    			instrument.sizeOfObjective());
    	assertEquals(XMLMockObjects.NUMBER_OF_DECTECTORS, 
    			instrument.sizeOfDetector());
    	assertEquals(XMLMockObjects.NUMBER_OF_DICHROICS, 
    			instrument.sizeOfDichroic());
    	assertEquals(XMLMockObjects.NUMBER_OF_FILTERS, 
    			instrument.sizeOfFilter());
    	assertEquals(1, instrument.sizeOfFilterSet());
    	assertEquals(1, instrument.sizeOfOtf());
    	
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
    	assertTrue(xmlChannel.getColor().intValue() == 
    		XMLMockObjects.DEFAULT_COLOR.getRGB());
    	Color c;
    	while (i.hasNext()) {
			channel = i.next();
			assertEquals(channel.getAlpha().getValue(), 
					XMLMockObjects.DEFAULT_COLOR.getAlpha());
			assertEquals(channel.getRed().getValue(), 
					XMLMockObjects.DEFAULT_COLOR.getRed());
			assertEquals(channel.getGreen().getValue(), 
					XMLMockObjects.DEFAULT_COLOR.getGreen());
			assertEquals(channel.getBlue().getValue(), 
					XMLMockObjects.DEFAULT_COLOR.getBlue());
			ids.add(channel.getLogicalChannel().getId().getValue());
		}
    	List<LogicalChannel> l = 
    		factory.getMetadataService().loadChannelAcquisitionData(ids);
    	assertEquals(channels.size(), l.size());
    	
    	LogicalChannel lc;
    	DetectorSettings ds;
    	LightSettings ls;
    	ome.xml.model.DetectorSettings xmlDs = xml.createDetectorSettings(0);
    	ome.xml.model.LightSourceSettings xmlLs = 
    		xml.createLightSourceSettings(0);
    	
    	ome.xml.model.MicrobeamManipulation xmlMM = 
    		xml.createMicrobeamManipulation(0);
    	ome.xml.model.Experiment xmlExp = ome.getExperiment(0);
    	ome.xml.model.OTF xmlOTF = ome.getInstrument(0).getOTF(0);
    	
    	// Validate experiment (initial checks)
    	assertNotNull(image.getExperiment());
    	Experiment exp = (Experiment) factory.getQueryService().findByQuery(
    			"select e from Experiment as e " +
    			"join fetch e.type " +
    			"left outer join fetch e.microbeamManipulation as mm " +
    			"join fetch mm.type " +
    			"left outer join fetch mm.lightSourceSettings as lss " +
    			"left outer join fetch lss.lightSource " +
    			"where e.id = :id", new ParametersI().addId(
    					image.getExperiment().getId().getValue()));
    	assertNotNull(exp);
    	assertEquals(1, exp.sizeOfMicrobeamManipulation());
    	MicrobeamManipulation mm = exp.copyMicrobeamManipulation().get(0);
    	validateExperiment(exp, xmlExp);
    	validateMicrobeamManipulation(mm, xmlMM);

    	LightPath path;
    	Iterator<LogicalChannel> k = l.iterator();
    	while (k.hasNext()) {
			lc = k.next();
			validateChannel(lc, xmlChannel);
			assertNotNull(lc.getOtf());
			validateOTF(lc.getOtf(), xmlOTF);
			ds = lc.getDetectorSettings();
			assertNotNull(ds);
			assertNotNull(ds.getDetector());
			assertTrue(detectorIds.contains(
					ds.getDetector().getId().getValue()));
			validateDetectorSettings(ds, xmlDs);
			ls = lc.getLightSourceSettings();
			assertNotNull(ls);
			assertNotNull(ls.getLightSource());
			assertTrue(lightIds.contains(
					ls.getLightSource().getId().getValue()));
			validateLightSourceSettings(ls, xmlLs);
			path = lc.getLightPath();
			assertNotNull(lc);
			assertNotNull(path.getDichroic());
		}
	}

	/**
     * Tests the import of an OME-XML file with an image with ROI.
     * @throws Exception Thrown if an error occurred.
     */
	public void testImportImageWithROI()
		throws Exception
	{
		File f = File.createTempFile("testImportImageWithROI", 
				"."+OME_FORMAT);
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
		assertTrue(rois.size() == XMLMockObjects.SIZE_C);
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
	@Test
	public void testImportPlate()
		throws Exception
	{
		File f = File.createTempFile("testImportPlate", "."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome = xml.createPopulatedPlate(0);
		writer.writeFile(f, ome, true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import the plate", e);
		}
        Pixels p = pixels.get(0);
        WellSample ws = getWellSample(p);
		validateWellSample(ws, ome.getPlate(0).getWell(0).getWellSample(0));
		Well well = ws.getWell();
		assertNotNull(well);
		validateWell(well, ome.getPlate(0).getWell(0));
		Plate plate = ws.getWell().getPlate();
		assertNotNull(plate);
		validatePlate(plate, ome.getPlate(0));
	}
	
	/**
     * Tests the import of an OME-XML file with a screen and 
     * a fully populated plate.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportScreenWithOnePlate()
		throws Exception
	{
		File f = File.createTempFile("testImportScreenWithOnePlate", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome = xml.createPopulatedScreen(1, 2, 2, 2, 2);
		writer.writeFile(f, ome, true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import the plate", e);
		}
        Pixels p = pixels.get(0);
        WellSample ws = getWellSample(p);
		validateWellSample(ws, ome.getPlate(0).getWell(0).getWellSample(0));
		Well well = ws.getWell();
		assertNotNull(well);
		validateWell(well, ome.getPlate(0).getWell(0));
		Plate plate = ws.getWell().getPlate();
		assertNotNull(plate);
		validatePlate(plate, ome.getPlate(0));
		validateScreen(plate.copyScreenLinks().get(0).getParent(), 
				ome.getScreen(0));
	}
	
	/**
     * Tests the import of an OME-XML file with a screen and 
     * two fully populated plates.
     * @throws Exception Thrown if an error occurred.
     */
	@Test(enabled=false)
	public void testImportScreenWithTwoPlates()
		throws Exception
	{
		File f = File.createTempFile("testImportScreenWithTwoPlates", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome = xml.createPopulatedScreen(2, 2, 2, 2, 2);
		writer.writeFile(f, ome, true);
		List<Pixels> pixels = null;
		try {
			pixels = importFile(f, OME_FORMAT);
		} catch (Throwable e) {
			throw new Exception("cannot import the plate", e);
		}
		WellSample ws;
		Well well;
		Plate plate;
		ome.getScreen(0);
		for (Pixels p : pixels) {
			ws = getWellSample(p);
			assertNotNull(ws);
			well = ws.getWell();
			assertNotNull(well);
			plate = ws.getWell().getPlate();
			assertNotNull(plate);
			validateScreen(plate.copyScreenLinks().get(0).getParent(), 
					ome.getScreen(0));
		}
	}
	
	/**
     * Tests the import of an OME-XML file with a fully populated plate
     * with a plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportPlateOnePlateAcquisition()
		throws Exception
	{
		File f = File.createTempFile("testImportPlateOnePlateAcquisition", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome =  xml.createPopulatedPlate(1);
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
		assertTrue(results.size() == 1);
		WellSample ws = (WellSample) results.get(0);
		assertNotNull(ws.getWell());
		assertNotNull(ws.getWell().getPlate());
		PlateAcquisition pa = ws.getPlateAcquisition();
		assertNotNull(pa);
		validatePlateAcquisition(pa, ome.getPlate(0).getPlateAcquisition(0));
	}

	/**
     * Tests the import of an OME-XML file with a fully populated plate
     * with a plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportPlateMultiplePlateAcquisitions()
		throws Exception
	{
		File f = File.createTempFile(
				"testImportPlateMultiplePlateAcquisitions",
				"."+OME_FORMAT);
		files.add(f);
		int n = 3;
		int fields = 3;
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome =  xml.createPopulatedPlate(n, fields);
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
		assertNotNull(ws.getWell());
		Plate plate = ws.getWell().getPlate();
		sql = "select ws from WellSample as ws ";
		sql += "join fetch ws.plateAcquisition as pa ";
		sql += "join fetch ws.well as w ";
		sql += "join fetch w.plate as p ";
		sql += "where p.id = :id";
		param = new ParametersI();
		param.addId(plate.getId().getValue());
		assertEquals(fields*n, iQuery.findAllByQuery(sql, param).size());

		sql = "select pa from PlateAcquisition as pa ";
		sql += "where pa.plate.id = :id";
		List<IObject> pas = iQuery.findAllByQuery(sql, param);
		assertEquals(n, pas.size());
		
		
		Iterator<IObject> j = pas.iterator();
		sql = "select ws from WellSample as ws ";
		sql += "join fetch ws.plateAcquisition as pa ";
		sql += "where pa.id = :id";
		IObject obj;
		while (j.hasNext()) {
			obj = j.next();
			param = new ParametersI();
			param.addId(obj.getId().getValue());
			assertEquals(fields, iQuery.findAllByQuery(sql, param).size());
		}
	}
	
	/**
     * Tests the import of an OME-XML file with a plate
     * with wells linked to a reagent.
     * @throws Exception Thrown if an error occurred.
     */
	@Test
	public void testImportPlateWithReagent()
		throws Exception
	{
		File f = File.createTempFile("testImportPlateWithReagent", 
				"."+OME_FORMAT);
		files.add(f);
		XMLMockObjects xml = new XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		OME ome =  xml.createBasicPlateWithReagent();
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
		assertEquals(1, results.size());
		WellSample ws = (WellSample) results.get(0);
		//assertNotNull(ws.getPlateAcquisition());
		assertNotNull(ws.getWell());
		id = ws.getWell().getId().getValue();
		sql = "select l from WellReagentLink as l ";
		sql += "join fetch l.child as c ";
		sql += "join fetch l.parent as p ";
		sql += "where p.id = :id";
		param = new ParametersI();
		param.addId(id);
		WellReagentLink wr = (WellReagentLink) iQuery.findByQuery(sql, param);
		assertNotNull(wr);
		assertNotNull(wr.getParent());
		assertNotNull(wr.getChild());
		validateReagent(wr.getChild(), ome.getScreen(0).getReagent(0));
		id = wr.getChild().getId().getValue();
		sql = "select s from Screen as s ";
		sql += "join fetch s.reagent as r ";
		sql += "where r.id = :id";
		param = new ParametersI();
		param.addId(id);
		omero.model.Screen screen = 
			(omero.model.Screen) iQuery.findByQuery(sql, param);
		assertNotNull(screen);
		assertEquals(1, screen.sizeOfReagents());
		assertEquals(wr.getChild().getId().getValue(),
				screen.copyReagents().get(0).getId().getValue());
	}
	
}
