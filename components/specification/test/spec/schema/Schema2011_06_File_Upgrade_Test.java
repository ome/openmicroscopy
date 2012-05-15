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

import ome.xml.model.Image;
import ome.xml.model.OME;
import ome.xml.model.Plate;
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
        Assert.assertEquals(ref.Plate1Well1Column, well1.getColumn().toString());
        Assert.assertEquals(ref.Plate1Well1Row, well1.getRow().toString());
        Assert.assertEquals(ref.Plate1Well1Color, well1.getColor().getValue().toString());
        Assert.assertEquals(ref.Plate1Well1Status, well1.getType());
    }
    @Test (groups = {"spw"}, dependsOnMethods = {"testPlate1Well1"})
    public void testPlate1WellSample1() {
        Assert.assertNotNull(well1);
        Assert.assertEquals(1, well1.sizeOfWellSampleList());
        WellSample wellSample1 = well1.getWellSample(0);
        Assert.assertNotNull(wellSample1);
        Assert.assertEquals(ref.Plate1Well1WellSample1Index, wellSample1.getIndex().toString());
    }

    @Test (groups = {"spw"}, dependsOnMethods = {"testOmeNode"})
    public void testPlate2Description() {
        Assert.assertNotNull(ome);
        Plate plate2 = ome.getPlate(1);
        Assert.assertNotNull(plate2);
        Assert.assertNull(plate2.getDescription());
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
