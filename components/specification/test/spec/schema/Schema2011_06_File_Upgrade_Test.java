/*
 * Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package spec.schema;

import java.io.InputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import loci.common.services.ServiceFactory;
import loci.common.xml.XMLTools;
import loci.formats.services.OMEXMLService;

import ome.xml.model.BinData;
import ome.xml.model.Channel;
import ome.xml.model.Experimenter;
import ome.xml.model.ExperimenterGroup;
import ome.xml.model.Image;
import ome.xml.model.Instrument;
import ome.xml.model.OME;
import ome.xml.model.Objective;
import ome.xml.model.Pixels;
import ome.xml.model.Plate;
import ome.xml.model.Screen;
import ome.xml.model.StructuredAnnotations;
import ome.xml.model.Well;
import ome.xml.model.PlateAcquisition;
import ome.xml.model.WellSample;
import ome.xml.model.XMLAnnotation;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * import the reference strings for the associated sample file
 */
import spec.schema.samples.Upgrade2011_06.ref;

/** 
 * Collections of tests.
 * Checks if the upgrade from 2011-06 schema to 2012-06 schema works for
 * the file 2011-06/6x4y1z1t1c8b-swatch-upgrade.ome
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:callan@lifesci.dundee.ac.uk">callan@lifesci.dundee.ac.uk</a>
 * @author Andrew Patterson &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:ajpatterson@lifesci.dundee.ac.uk">ajpatterson@lifesci.dundee.ac.uk</a>
 */

public class Schema2011_06_File_Upgrade_Test {
    private static final Templates UPDATE_201106 =
        XMLTools.getStylesheet("/Xslt/2011-06-to-2012-06.xsl",
                Schema2011_06_TO_2012_06_Test.class);

    private OME ome;
    
    private Image image0;
    private Plate plate0;
    private Plate plate1;
    private Well well0;
    private Instrument instrument0;
    private Pixels pixels0;
    private StructuredAnnotations annotations;
    private WellSample wellSample0;

    private PlateAcquisition plateAcquisition0;
    private Screen screen0;
    private Screen screen1;
    private Screen screen2;
    private Screen screen3;
    private Experimenter experimenter0;
    private Experimenter experimenter1;
    private Experimenter experimenter2;
    private Experimenter experimenter3;
    private Experimenter experimenter4;
    private Experimenter experimenter5;
    private Experimenter experimenter6;
    private ExperimenterGroup experimenterGroup0;
    private ExperimenterGroup experimenterGroup1;
    private ExperimenterGroup experimenterGroup2;
    private ExperimenterGroup experimenterGroup3;
    private ExperimenterGroup experimenterGroup4;
    private Objective objective0;
    private Channel channel0;
    private Channel channel1;
    private Channel channel2;
    private BinData bindata0;
    private BinData bindata1;
    private BinData bindata2;
    private XMLAnnotation xmlAnnotation0;
    private XMLAnnotation xmlAnnotation1;

    @BeforeClass
    public void setUp() throws Exception {
        InputStream source = this.getClass().getResourceAsStream(ref.FILE_LOCATION);
        System.err.println(source);
        ServiceFactory sf = new ServiceFactory();
        OMEXMLService service = sf.getInstance(OMEXMLService.class);
        String xml = XMLTools.transformXML(
                new StreamSource(source), UPDATE_201106);
        ome = (OME) service.createOMEXMLRoot(xml);
    }

    @Test
    public void testOmeNode() {
        Assert.assertNotNull(ome);
        Assert.assertEquals(1, ome.sizeOfDatasetList());
        Assert.assertEquals(5, ome.sizeOfExperimenterGroupList());
        Assert.assertEquals(7, ome.sizeOfExperimenterList());
        Assert.assertEquals(1, ome.sizeOfImageList());
        Assert.assertEquals(1, ome.sizeOfInstrumentList());
        Assert.assertEquals(2, ome.sizeOfPlateList());
        Assert.assertEquals(1, ome.sizeOfProjectList());
        Assert.assertEquals(5, ome.sizeOfROIList());
        Assert.assertEquals(4, ome.sizeOfScreenList());
        Assert.assertNull(ome.getCreator());
        Assert.assertNull(ome.getUUID());
        Assert.assertNotNull(ome.getStructuredAnnotations());
    }

    
    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testPlate0Description() {
        Assert.assertNotNull(ome);
        plate0 = ome.getPlate(0);
        Assert.assertNotNull(plate0);
        Assert.assertEquals(ref.Plate0Description, plate0.getDescription());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate0Description"})
    public void testPlateAcquisition0Description() {
        Assert.assertNotNull(plate0);
        Assert.assertEquals(1, plate0.sizeOfPlateAcquisitionList());
        plateAcquisition0 = plate0.getPlateAcquisition(0);
        Assert.assertNotNull(plateAcquisition0);
        Assert.assertEquals(ref.Plate1PlateAcquisition1Description, plateAcquisition0.getDescription());
    }
    
    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate0Description"})
    public void testPlate0Well0() {
        Assert.assertNotNull(plate0);
        Assert.assertEquals(1, plate0.sizeOfWellList());
        well0 = plate0.getWell(0);
        Assert.assertNotNull(well0);
        Assert.assertEquals(ref.Plate1Well1Column, well0.getColumn());
        Assert.assertEquals(ref.Plate1Well1Row, well0.getRow());
        Assert.assertEquals(ref.Plate1Well1Color, well0.getColor());
        Assert.assertEquals(ref.Plate1Well1Status, well0.getType());
    }
    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate0Well0"})
    public void testPlate0WellSample0() {
        Assert.assertNotNull(well0);
        Assert.assertEquals(1, well0.sizeOfWellSampleList());
        wellSample0 = well0.getWellSample(0);
        Assert.assertNotNull(wellSample0);
        Assert.assertEquals(ref.Plate1Well1WellSample1Index, wellSample0.getIndex());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testPlate1Description() {
        Assert.assertNotNull(ome);
        plate1 = ome.getPlate(1);
        Assert.assertNotNull(plate1);
        Assert.assertNull(plate1.getDescription());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen0() {
        Assert.assertNotNull(ome);
        screen0 = ome.getScreen(0);
        Assert.assertNotNull(screen0);
        Assert.assertEquals(ref.Screen0Name, screen0.getName());
        Assert.assertEquals(ref.Screen1Description, screen0.getDescription());
        Assert.assertEquals(ref.Screen0ProtocolDescription, screen0.getProtocolDescription());
        Assert.assertEquals(ref.Screen1ProtocolIdentifier, screen0.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen1ReagentSetDescription, screen0.getReagentSetDescription());
        Assert.assertEquals(ref.Screen1ReagentSetIdentifier, screen0.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen1Type, screen0.getType());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen1() {
        Assert.assertNotNull(ome);
        screen1 = ome.getScreen(1);
        Assert.assertNotNull(screen1);
        Assert.assertEquals(ref.Screen2Name, screen1.getName());
        Assert.assertEquals(ref.Screen2Description, screen1.getDescription());
        Assert.assertEquals(ref.Screen2ProtocolDescription, screen1.getProtocolDescription());
        Assert.assertEquals(ref.Screen2ProtocolIdentifier, screen1.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen2ReagentSetDescription, screen1.getReagentSetDescription());
        Assert.assertEquals(ref.Screen2ReagentSetIdentifier, screen1.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen2Type, screen1.getType());
    }
    
    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen2() {
        Assert.assertNotNull(ome);
        screen2 = ome.getScreen(2);
        Assert.assertNotNull(screen2);
        Assert.assertEquals(ref.Screen3Name, screen2.getName());
        Assert.assertEquals(ref.Screen3Description, screen2.getDescription());
        Assert.assertEquals(ref.Screen3ProtocolDescription, screen2.getProtocolDescription());
        Assert.assertEquals(ref.Screen3ProtocolIdentifier, screen2.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen3ReagentSetDescription, screen2.getReagentSetDescription());
        Assert.assertEquals(ref.Screen3ReagentSetIdentifier, screen2.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen3Type, screen2.getType());
    }
    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen3() {
        Assert.assertNotNull(ome);
        screen3 = ome.getScreen(3);
        Assert.assertNotNull(screen3);
        Assert.assertEquals(ref.Screen4Name, screen3.getName());
        Assert.assertEquals(ref.Screen4Description, screen3.getDescription());
        Assert.assertEquals(ref.Screen4ProtocolDescription, screen3.getProtocolDescription());
        Assert.assertEquals(ref.Screen4ProtocolIdentifier, screen3.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen4ReagentSetDescription, screen3.getReagentSetDescription());
        Assert.assertEquals(ref.Screen4ReagentSetIdentifier, screen3.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen4Type, screen3.getType());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter0() {
        Assert.assertNotNull(ome);
        experimenter0 = ome.getExperimenter(0);
        Assert.assertNotNull(experimenter0);
        Assert.assertNull(experimenter0.getFirstName());
        Assert.assertNull(experimenter0.getMiddleName());
        Assert.assertNull(experimenter0.getLastName());
        Assert.assertNull(experimenter0.getUserName());
        Assert.assertNull(experimenter0.getInstitution());
        Assert.assertNull(experimenter0.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter1() {
        Assert.assertNotNull(ome);
        experimenter1 = ome.getExperimenter(1);
        Assert.assertNotNull(experimenter1);
        Assert.assertEquals(ref.Experimenter1FirstName, experimenter1.getFirstName());
        Assert.assertEquals(ref.Experimenter1MiddleName, experimenter1.getMiddleName());
        Assert.assertEquals(ref.Experimenter1LastName, experimenter1.getLastName());
        Assert.assertNull(experimenter1.getUserName());
        Assert.assertNull(experimenter1.getInstitution());
        Assert.assertEquals(ref.Experimenter1Email, experimenter1.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter2() {
        Assert.assertNotNull(ome);
        experimenter2 = ome.getExperimenter(2);
        Assert.assertNotNull(experimenter2);
        Assert.assertNull(experimenter2.getFirstName());
        Assert.assertNull(experimenter2.getMiddleName());
        Assert.assertNull(experimenter2.getLastName());
        Assert.assertNull(experimenter2.getUserName());
        Assert.assertNull(experimenter2.getInstitution());
        Assert.assertNull(experimenter2.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter3() {
        Assert.assertNotNull(ome);
        experimenter3 = ome.getExperimenter(3);
        Assert.assertNotNull(experimenter3);
        Assert.assertNull(experimenter3.getFirstName());
        Assert.assertNull(experimenter3.getMiddleName());
        Assert.assertNull(experimenter3.getLastName());
        Assert.assertNull(experimenter3.getUserName());
        Assert.assertNull(experimenter3.getInstitution());
        Assert.assertNull(experimenter3.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter4() {
        Assert.assertNotNull(ome);
        experimenter4 = ome.getExperimenter(4);
        Assert.assertNotNull(experimenter4);
        Assert.assertNull(experimenter4.getFirstName());
        Assert.assertNull(experimenter4.getMiddleName());
        Assert.assertNull(experimenter4.getLastName());
        Assert.assertNull(experimenter4.getUserName());
        Assert.assertNull(experimenter4.getInstitution());
        Assert.assertNull(experimenter4.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter5() {
        Assert.assertNotNull(ome);
        experimenter5 = ome.getExperimenter(5);
        Assert.assertNotNull(experimenter5);
        Assert.assertNull(experimenter5.getFirstName());
        Assert.assertNull(experimenter5.getMiddleName());
        Assert.assertNull(experimenter5.getLastName());
        Assert.assertNull(experimenter5.getUserName());
        Assert.assertNull(experimenter5.getInstitution());
        Assert.assertNull(experimenter5.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter6() {
        Assert.assertNotNull(ome);
        experimenter6 = ome.getExperimenter(6);
        Assert.assertNotNull(experimenter6);
        Assert.assertNull(experimenter6.getFirstName());
        Assert.assertNull(experimenter6.getMiddleName());
        Assert.assertNull(experimenter6.getLastName());
        Assert.assertNull(experimenter6.getUserName());
        Assert.assertNull(experimenter6.getInstitution());
        Assert.assertNull(experimenter6.getEmail());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup0() {
        Assert.assertNotNull(ome);
        experimenterGroup0 = ome.getExperimenterGroup(0);
        Assert.assertNotNull(experimenterGroup0);
        Assert.assertEquals(ref.Group0Name, experimenterGroup0.getName());
        Assert.assertEquals(ref.Group0Description, experimenterGroup0.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup1() {
        Assert.assertNotNull(ome);
        experimenterGroup1 = ome.getExperimenterGroup(1);
        Assert.assertNotNull(experimenterGroup1);
        Assert.assertEquals(ref.Group1Name, experimenterGroup1.getName());
        Assert.assertEquals(ref.Group1Description, experimenterGroup1.getDescription());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup2() {
        Assert.assertNotNull(ome);
        experimenterGroup2 = ome.getExperimenterGroup(2);
        Assert.assertNotNull(experimenterGroup2);
        Assert.assertEquals(ref.Group2Name, experimenterGroup2.getName());
        Assert.assertEquals(ref.Group2Description, experimenterGroup2.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup3() {
        Assert.assertNotNull(ome);
        experimenterGroup3 = ome.getExperimenterGroup(3);
        Assert.assertNotNull(experimenterGroup3);
        Assert.assertEquals(ref.Group3Name, experimenterGroup3.getName());
        Assert.assertEquals(ref.Group3Description, experimenterGroup3.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup4() {
        Assert.assertNotNull(ome);
        experimenterGroup4 = ome.getExperimenterGroup(4);
        Assert.assertNotNull(experimenterGroup4);
        Assert.assertEquals(ref.Group4Name, experimenterGroup4.getName());
        Assert.assertEquals(ref.Group4Description, experimenterGroup4.getDescription());
    }

    @Test (groups = {"instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testInstrument0() {
        Assert.assertNotNull(ome);
        instrument0 = ome.getInstrument(0);
        Assert.assertNotNull(instrument0);
        Assert.assertEquals(0, instrument0.sizeOfDetectorList());
        Assert.assertEquals(0, instrument0.sizeOfDichroicList());
        Assert.assertEquals(0, instrument0.sizeOfFilterList());
        Assert.assertEquals(0, instrument0.sizeOfFilterSetList());
        Assert.assertEquals(0, instrument0.sizeOfLightSourceList());
        Assert.assertEquals(1, instrument0.sizeOfObjectiveList());
        /* Perhaps: Assert.assertEquals(1, instrument1()); */
    }

    @Test (groups = {"instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testObjective0() {
        Assert.assertNotNull(ome);
        objective0 = instrument0.getObjective(0);
        Assert.assertNotNull(objective0);
        Assert.assertEquals(ref.Instrument0Objective0CalibratedMagnification, objective0.getCalibratedMagnification());
        Assert.assertEquals(ref.Instrument0Objective0LotNumber, objective0.getLotNumber());
        Assert.assertEquals(ref.Instrument0Objective0Manufacturer, objective0.getManufacturer());
        Assert.assertEquals(ref.Instrument0Objective0NominalMagnification, objective0.getNominalMagnification());
        Assert.assertNull(objective0.getCorrection());
        Assert.assertNull(objective0.getImmersion());
        Assert.assertNull(objective0.getIris());
        Assert.assertNull(objective0.getLensNA());
        Assert.assertNull(objective0.getModel());
        Assert.assertNull(objective0.getSerialNumber());
        Assert.assertNull(objective0.getWorkingDistance());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testOmeNode"})
    public void testImage0Name() {
        Assert.assertNotNull(ome);
        Assert.assertEquals(1, ome.sizeOfImageList());
        image0 = ome.getImage(0);
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0Name, image0.getName());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testImage0Name"})
    public void testImage0Date() {
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0AcquiredDate, image0.getAcquisitionDate());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testImage0Name"})
    public void testPixels0() {
        Assert.assertNotNull(image0);
        pixels0 = image0.getPixels();
        Assert.assertEquals(3, pixels0.sizeOfBinDataList());
        Assert.assertEquals(3, pixels0.sizeOfChannelList());
        Assert.assertEquals(0, pixels0.sizeOfPlaneList());
        Assert.assertEquals(0, pixels0.sizeOfTiffDataList());
        Assert.assertEquals(ref.Image0Pixels0_0DimensionOrder, pixels0.getDimensionOrder());
        Assert.assertEquals(ref.Image0Pixels0_0PhysicalSizeX, pixels0.getPhysicalSizeX());
        Assert.assertEquals(ref.Image0Pixels0_0PhysicalSizeY, pixels0.getPhysicalSizeY());
        Assert.assertEquals(ref.Image0Pixels0_0Type, pixels0.getType());
        Assert.assertEquals(ref.Image0Pixels0_0SizeC, pixels0.getSizeC());
        Assert.assertEquals(ref.Image0Pixels0_0SizeT, pixels0.getSizeT());
        Assert.assertEquals(ref.Image0Pixels0_0SizeX, pixels0.getSizeX());
        Assert.assertEquals(ref.Image0Pixels0_0SizeY, pixels0.getSizeY());
        Assert.assertEquals(ref.Image0Pixels0_0SizeZ, pixels0.getSizeZ());
        Assert.assertNull(pixels0.getMetadataOnly());
        Assert.assertNull(pixels0.getPhysicalSizeZ());
        Assert.assertNull(pixels0.getTimeIncrement());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testChannel0() {
        Assert.assertNotNull(pixels0);
        channel0 = pixels0.getChannel(0);
        Assert.assertEquals(ref.Image0Pixels0_0Channel0AcquisitionMode, channel0.getAcquisitionMode());
        Assert.assertEquals(ref.Image0Pixels0_0Channel0Color, channel0.getColor());
        Assert.assertNull(channel0.getContrastMethod());
        Assert.assertNull(channel0.getDetectorSettings());
        Assert.assertNull(channel0.getEmissionWavelength());
        Assert.assertNull(channel0.getExcitationWavelength());
        Assert.assertNull(channel0.getFluor());
        Assert.assertNull(channel0.getIlluminationType());
        Assert.assertNull(channel0.getLightPath());
        Assert.assertNull(channel0.getLightSourceSettings());
        Assert.assertNull(channel0.getName());
        Assert.assertNull(channel0.getNDFilter());
        Assert.assertNull(channel0.getPinholeSize());
        Assert.assertNull(channel0.getPockelCellSetting());
        Assert.assertNull(channel0.getSamplesPerPixel());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testChannel1() {
        Assert.assertNotNull(pixels0);
        channel1 = pixels0.getChannel(1);
        Assert.assertEquals(ref.Image0Pixels0_0Channel1AcquisitionMode, channel1.getAcquisitionMode());
        Assert.assertEquals(ref.Image0Pixels0_0Channel1Color, channel1.getColor());
        Assert.assertNull(channel1.getContrastMethod());
        Assert.assertNull(channel1.getDetectorSettings());
        Assert.assertNull(channel1.getEmissionWavelength());
        Assert.assertNull(channel1.getExcitationWavelength());
        Assert.assertNull(channel1.getFluor());
        Assert.assertNull(channel1.getIlluminationType());
        Assert.assertNull(channel1.getLightPath());
        Assert.assertNull(channel1.getLightSourceSettings());
        Assert.assertNull(channel1.getName());
        Assert.assertNull(channel1.getNDFilter());
        Assert.assertNull(channel1.getPinholeSize());
        Assert.assertNull(channel1.getPockelCellSetting());
        Assert.assertNull(channel1.getSamplesPerPixel());
    }
    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testChannel2() {
        Assert.assertNotNull(pixels0);
        channel2 = pixels0.getChannel(2);
        Assert.assertEquals(ref.Image0Pixels0_0Channel2AcquisitionMode, channel2.getAcquisitionMode());
        Assert.assertEquals(ref.Image0Pixels0_0Channel2Color, channel2.getColor());
        Assert.assertNull(channel2.getContrastMethod());
        Assert.assertNull(channel2.getDetectorSettings());
        Assert.assertNull(channel2.getEmissionWavelength());
        Assert.assertNull(channel2.getExcitationWavelength());
        Assert.assertNull(channel2.getFluor());
        Assert.assertNull(channel2.getIlluminationType());
        Assert.assertNull(channel2.getLightPath());
        Assert.assertNull(channel2.getLightSourceSettings());
        Assert.assertNull(channel2.getName());
        Assert.assertNull(channel2.getNDFilter());
        Assert.assertNull(channel2.getPinholeSize());
        Assert.assertNull(channel2.getPockelCellSetting());
        Assert.assertNull(channel2.getSamplesPerPixel());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testBinData0() {
        Assert.assertNotNull(pixels0);
        bindata0 = pixels0.getBinData(0);
        Assert.assertEquals(ref.Image0Pixels0_0Bindata0Length, bindata0.getLength());
        Assert.assertEquals(ref.Image0Pixels0_0Bindata0BigEndian, bindata0.getBigEndian());
        Assert.assertNull(bindata0.getCompression());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testBinData1() {
        Assert.assertNotNull(pixels0);
        bindata1 = pixels0.getBinData(1);
        Assert.assertEquals(ref.Image0Pixels0_0Bindata1Length, bindata1.getLength());
        Assert.assertEquals(ref.Image0Pixels0_0Bindata1BigEndian, bindata1.getBigEndian());
        Assert.assertNull(bindata1.getCompression());
    }
    
    @Test (groups = {"image"}, dependsOnMethods = {"testPixels0"})
    public void testBinData2() {
        Assert.assertNotNull(pixels0);
        bindata2 = pixels0.getBinData(2);
        Assert.assertEquals(ref.Image0Pixels0_0Bindata2Length, bindata2.getLength());
        Assert.assertEquals(ref.Image0Pixels0_0Bindata2BigEndian, bindata2.getBigEndian());
        Assert.assertNull(bindata2.getCompression());
    }

    @Test (groups = {"annotation"}, dependsOnMethods = {"testOmeNode"})
    public void testAnnotations() {
        Assert.assertNotNull(ome);
        annotations = ome.getStructuredAnnotations();
        Assert.assertEquals(2, annotations.sizeOfXMLAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfBooleanAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfCommentAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfDoubleAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfFileAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfListAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfLongAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfTagAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfTermAnnotationList());
        Assert.assertEquals(0, annotations.sizeOfTimestampAnnotationList());
    }

    @Test (groups = {"annotation"}, dependsOnMethods = {"testAnnotations"})
    public void testXMLAnnotation0() {
        Assert.assertNotNull(annotations);
        xmlAnnotation0 = annotations.getXMLAnnotation(0);
        Assert.assertEquals(ref.Annotation1Value, xmlAnnotation0.getValue());
        Assert.assertNull(xmlAnnotation0.getNamespace());
        Assert.assertNull(xmlAnnotation0.getDescription());
    }
    
    @Test (groups = {"annotation"}, dependsOnMethods = {"testAnnotations"})
    public void testXMLAnnotation1() {
        Assert.assertNotNull(annotations);
        xmlAnnotation1 = annotations.getXMLAnnotation(1);
        Assert.assertEquals(ref.Annotation2Value, xmlAnnotation1.getValue());
        Assert.assertNull(xmlAnnotation1.getNamespace());
        Assert.assertNull(xmlAnnotation1.getDescription());
    }

/*
    @Test (groups = {"roi"})
    public void testROI() {
        Assert.assertTrue(false,"To Do");
    }

    @Test (groups = {"links"}, dependsOnGroups = {"roi", "image"})
    public void testROILinkage() {
        Assert.assertTrue(false,"To Do");
    }
*/

}
