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

import ome.xml.model.Experimenter;
import ome.xml.model.ExperimenterGroup;
import ome.xml.model.Image;
import ome.xml.model.Instrument;
import ome.xml.model.OME;
import ome.xml.model.Objective;
import ome.xml.model.Pixels;
import ome.xml.model.Plate;
import ome.xml.model.Screen;
import ome.xml.model.Well;
import ome.xml.model.PlateAcquisition;
import ome.xml.model.WellSample;

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
    private Plate plate1;
    private Well well1;
    private Instrument instrument1;
    private Pixels pixels0;
    
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
    public void testPlate1Description() {
        Assert.assertNotNull(ome);
        plate1 = ome.getPlate(0);
        Assert.assertNotNull(plate1);
        Assert.assertEquals(ref.Plate1Description, plate1.getDescription());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate1Description"})
    public void testPlateAcquisition1Description() {
        Assert.assertNotNull(plate1);
        Assert.assertEquals(1, plate1.sizeOfPlateAcquisitionList());
        PlateAcquisition plateAcquisition1 = plate1.getPlateAcquisition(0);
        Assert.assertNotNull(plateAcquisition1);
        Assert.assertEquals(ref.Plate1PlateAcquisition1Description, plateAcquisition1.getDescription());
    }
    
    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate1Description"})
    public void testPlate1Well1() {
        Assert.assertNotNull(plate1);
        Assert.assertEquals(1, plate1.sizeOfWellList());
        well1 = plate1.getWell(0);
        Assert.assertNotNull(well1);
        Assert.assertEquals(ref.Plate1Well1Column, well1.getColumn());
        Assert.assertEquals(ref.Plate1Well1Row, well1.getRow());
        Assert.assertEquals(ref.Plate1Well1Color, well1.getColor());
        Assert.assertEquals(ref.Plate1Well1Status, well1.getType());
    }
    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate1Well1"})
    public void testPlate1WellSample1() {
        Assert.assertNotNull(well1);
        Assert.assertEquals(1, well1.sizeOfWellSampleList());
        WellSample wellSample1 = well1.getWellSample(0);
        Assert.assertNotNull(wellSample1);
        Assert.assertEquals(ref.Plate1Well1WellSample1Index, wellSample1.getIndex());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testPlate2Description() {
        Assert.assertNotNull(ome);
        Plate plate2 = ome.getPlate(1);
        Assert.assertNotNull(plate2);
        Assert.assertNull(plate2.getDescription());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen1() {
        Assert.assertNotNull(ome);
        Screen screen1 = ome.getScreen(0);
        Assert.assertNotNull(screen1);
        Assert.assertEquals(ref.Screen1Name, screen1.getName());
        Assert.assertEquals(ref.Screen1Description, screen1.getDescription());
        Assert.assertEquals(ref.Screen1ProtocolDescription, screen1.getProtocolDescription());
        Assert.assertEquals(ref.Screen1ProtocolIdentifier, screen1.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen1ReagentSetDescription, screen1.getReagentSetDescription());
        Assert.assertEquals(ref.Screen1ReagentSetIdentifier, screen1.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen1Type, screen1.getType());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen2() {
        Assert.assertNotNull(ome);
        Screen screen = ome.getScreen(1);
        Assert.assertNotNull(screen);
        Assert.assertEquals(ref.Screen2Name, screen.getName());
        Assert.assertEquals(ref.Screen2Description, screen.getDescription());
        Assert.assertEquals(ref.Screen2ProtocolDescription, screen.getProtocolDescription());
        Assert.assertEquals(ref.Screen2ProtocolIdentifier, screen.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen2ReagentSetDescription, screen.getReagentSetDescription());
        Assert.assertEquals(ref.Screen2ReagentSetIdentifier, screen.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen2Type, screen.getType());
    }
    
    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen3() {
        Assert.assertNotNull(ome);
        Screen screen = ome.getScreen(2);
        Assert.assertNotNull(screen);
        Assert.assertEquals(ref.Screen3Name, screen.getName());
        Assert.assertEquals(ref.Screen3Description, screen.getDescription());
        Assert.assertEquals(ref.Screen3ProtocolDescription, screen.getProtocolDescription());
        Assert.assertEquals(ref.Screen3ProtocolIdentifier, screen.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen3ReagentSetDescription, screen.getReagentSetDescription());
        Assert.assertEquals(ref.Screen3ReagentSetIdentifier, screen.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen3Type, screen.getType());
    }
    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testScreen4() {
        Assert.assertNotNull(ome);
        Screen screen = ome.getScreen(3);
        Assert.assertNotNull(screen);
        Assert.assertEquals(ref.Screen4Name, screen.getName());
        Assert.assertEquals(ref.Screen4Description, screen.getDescription());
        Assert.assertEquals(ref.Screen4ProtocolDescription, screen.getProtocolDescription());
        Assert.assertEquals(ref.Screen4ProtocolIdentifier, screen.getProtocolIdentifier());
        Assert.assertEquals(ref.Screen4ReagentSetDescription, screen.getReagentSetDescription());
        Assert.assertEquals(ref.Screen4ReagentSetIdentifier, screen.getReagentSetIdentifier());
        Assert.assertEquals(ref.Screen4Type, screen.getType());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter1() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(0);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter2() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(1);
        Assert.assertNotNull(experimenter);
        Assert.assertEquals(ref.Experimenter2FirstName, experimenter.getFirstName());
        Assert.assertEquals(ref.Experimenter2MiddleName, experimenter.getMiddleName());
        Assert.assertEquals(ref.Experimenter2LastName, experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertEquals(ref.Experimenter2Email, experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter3() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(2);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter4() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(3);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter5() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(4);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter6() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(5);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenter7() {
        Assert.assertNotNull(ome);
        Experimenter experimenter = ome.getExperimenter(6);
        Assert.assertNotNull(experimenter);
        Assert.assertNull(experimenter.getFirstName());
        Assert.assertNull(experimenter.getMiddleName());
        Assert.assertNull(experimenter.getLastName());
        Assert.assertNull(experimenter.getUserName());
        Assert.assertNull(experimenter.getInstitution());
        Assert.assertNull(experimenter.getEmail());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup1() {
        Assert.assertNotNull(ome);
        ExperimenterGroup experimenterGroup = ome.getExperimenterGroup(0);
        Assert.assertNotNull(experimenterGroup);
        Assert.assertEquals(ref.Group1Name, experimenterGroup.getName());
        Assert.assertEquals(ref.Group1Description, experimenterGroup.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup2() {
        Assert.assertNotNull(ome);
        ExperimenterGroup experimenterGroup = ome.getExperimenterGroup(1);
        Assert.assertNotNull(experimenterGroup);
        Assert.assertEquals(ref.Group2Name, experimenterGroup.getName());
        Assert.assertEquals(ref.Group2Description, experimenterGroup.getDescription());
    }
    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup3() {
        Assert.assertNotNull(ome);
        ExperimenterGroup experimenterGroup = ome.getExperimenterGroup(2);
        Assert.assertNotNull(experimenterGroup);
        Assert.assertEquals(ref.Group3Name, experimenterGroup.getName());
        Assert.assertEquals(ref.Group3Description, experimenterGroup.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup4() {
        Assert.assertNotNull(ome);
        ExperimenterGroup experimenterGroup = ome.getExperimenterGroup(3);
        Assert.assertNotNull(experimenterGroup);
        Assert.assertEquals(ref.Group4Name, experimenterGroup.getName());
        Assert.assertEquals(ref.Group4Description, experimenterGroup.getDescription());
    }

    @Test (groups = {"exper"}, dependsOnMethods = {"testOmeNode"})
    public void testExperimenterGroup5() {
        Assert.assertNotNull(ome);
        ExperimenterGroup experimenterGroup = ome.getExperimenterGroup(4);
        Assert.assertNotNull(experimenterGroup);
        Assert.assertEquals(ref.Group5Name, experimenterGroup.getName());
        Assert.assertEquals(ref.Group5Description, experimenterGroup.getDescription());
    }

    @Test (groups = {"instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testInstrument1() {
        Assert.assertNotNull(ome);
        instrument1 = ome.getInstrument(0);
        Assert.assertNotNull(instrument1);
        Assert.assertEquals(0, instrument1.sizeOfDetectorList());
        Assert.assertEquals(0, instrument1.sizeOfDichroicList());
        Assert.assertEquals(0, instrument1.sizeOfFilterList());
        Assert.assertEquals(0, instrument1.sizeOfFilterSetList());
        Assert.assertEquals(0, instrument1.sizeOfLightSourceList());
        Assert.assertEquals(1, instrument1.sizeOfObjectiveList());
        /* Perhaps: Assert.assertEquals(1, instrument1()); */
    }

    @Test (groups = {"instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testObjective1() {
        Assert.assertNotNull(ome);
        Objective objective = instrument1.getObjective(0);
        Assert.assertNotNull(objective);
        Assert.assertEquals(ref.Instrument1Objective1CalibratedMagnification, objective.getCalibratedMagnification());
        Assert.assertEquals(ref.Instrument1Objective1LotNumber, objective.getLotNumber());
        Assert.assertEquals(ref.Instrument1Objective1Manufacturer, objective.getManufacturer());
        Assert.assertEquals(ref.Instrument1Objective1NominalMagnification, objective.getNominalMagnification());
        Assert.assertNull(objective.getCorrection());
        Assert.assertNull(objective.getImmersion());
        Assert.assertNull(objective.getIris());
        Assert.assertNull(objective.getLensNA());
        Assert.assertNull(objective.getModel());
        Assert.assertNull(objective.getSerialNumber());
        Assert.assertNull(objective.getWorkingDistance());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testOmeNode"})
    public void testImageName() {
        Assert.assertNotNull(ome);
        Assert.assertEquals(1, ome.sizeOfImageList());
        image0 = ome.getImage(0);
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0Name, image0.getName());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testImageName"})
    public void testImageDate() {
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0AcquiredDate, image0.getAcquisitionDate());
    }

    @Test (groups = {"image"}, dependsOnMethods = {"testImageName"})
    public void testPixels() {
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
