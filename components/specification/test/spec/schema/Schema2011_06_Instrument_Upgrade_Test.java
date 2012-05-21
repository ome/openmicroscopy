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
import ome.xml.model.Image;
import ome.xml.model.Instrument;
import ome.xml.model.OME;
import ome.xml.model.Objective;
import ome.xml.model.Pixels;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * import the reference strings for the associated sample file
 */
import spec.schema.samples.Instrument2011_06.ref;

/** 
 * Collections of tests.
 * Checks if the upgrade from 2011-06 schema to 2012-06 schema works for
 * the file 2011-06/6x4y1z1t1c8b-swatch-instrument.ome
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:callan@lifesci.dundee.ac.uk">callan@lifesci.dundee.ac.uk</a>
 * @author Andrew Patterson &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:ajpatterson@lifesci.dundee.ac.uk">ajpatterson@lifesci.dundee.ac.uk</a>
 */

public class Schema2011_06_Instrument_Upgrade_Test {
    private static final Templates UPDATE_201106 =
        XMLTools.getStylesheet("/Xslt/2011-06-to-2012-06.xsl",
                Schema2011_06_TO_2012_06_Test.class);

    private OME ome;
    
    private Image image0;
    private Instrument instrument0;
    private Pixels pixels0;
    private Objective objective0;
    private Channel channel0;
    private BinData bindata0;
    
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
        Assert.assertEquals(0, ome.sizeOfDatasetList());
        Assert.assertEquals(0, ome.sizeOfExperimenterGroupList());
        Assert.assertEquals(0, ome.sizeOfExperimenterList());
        Assert.assertEquals(1, ome.sizeOfImageList());
        Assert.assertEquals(1, ome.sizeOfInstrumentList());
        Assert.assertEquals(0, ome.sizeOfPlateList());
        Assert.assertEquals(0, ome.sizeOfProjectList());
        Assert.assertEquals(0, ome.sizeOfROIList());
        Assert.assertEquals(0, ome.sizeOfScreenList());
        Assert.assertNull(ome.getCreator());
        Assert.assertNull(ome.getUUID());
        Assert.assertNull(ome.getStructuredAnnotations());
    }

    @Test (groups = {"11-06-i-instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testInstrument0() {
        Assert.assertNotNull(ome);
        instrument0 = ome.getInstrument(0);
        Assert.assertNotNull(instrument0);
        Assert.assertEquals(1, instrument0.sizeOfDetectorList());
        Assert.assertEquals(1, instrument0.sizeOfDichroicList());
        Assert.assertEquals(4, instrument0.sizeOfFilterList());
        Assert.assertEquals(1, instrument0.sizeOfFilterSetList());
        Assert.assertEquals(2, instrument0.sizeOfLightSourceList());
        Assert.assertEquals(1, instrument0.sizeOfObjectiveList());
    }

    @Test (groups = {"11-06-i-instrument"}, dependsOnMethods = {"testOmeNode"})
    public void testObjective0() {
        Assert.assertNotNull(ome);
        objective0 = instrument0.getObjective(0);
        Assert.assertNotNull(objective0);
        Assert.assertEquals(ref.Instrument0Objective0CalibratedMagnification, objective0.getCalibratedMagnification());
        Assert.assertEquals(ref.Instrument0Objective0Manufacturer, objective0.getManufacturer());
        Assert.assertEquals(ref.Instrument0Objective0NominalMagnification, objective0.getNominalMagnification());
        Assert.assertEquals(ref.Instrument0Objective0Correction, objective0.getCorrection());
        Assert.assertEquals(ref.Instrument0Objective0Immersion, objective0.getImmersion());
        Assert.assertNull(objective0.getIris());
        Assert.assertEquals(ref.Instrument0Objective0LensNA, objective0.getLensNA());
        Assert.assertEquals(ref.Instrument0Objective0Model, objective0.getModel());
        Assert.assertNull(objective0.getSerialNumber());
        Assert.assertEquals(ref.Instrument0Objective0WorkingDistance, objective0.getWorkingDistance());
    }

    @Test (groups = {"11-06-i-image"}, dependsOnMethods = {"testOmeNode"})
    public void testImage0Name() {
        Assert.assertNotNull(ome);
        Assert.assertEquals(1, ome.sizeOfImageList());
        image0 = ome.getImage(0);
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0Name, image0.getName());
    }

    @Test (groups = {"11-06-i-image"}, dependsOnMethods = {"testImage0Name"})
    public void testImage0Date() {
        Assert.assertNotNull(image0);
        Assert.assertEquals(ref.Image0AcquiredDate, image0.getAcquisitionDate());
    }

    @Test (groups = {"11-06-i-image"}, dependsOnMethods = {"testImage0Name"})
    public void testPixels0() {
        Assert.assertNotNull(image0);
        pixels0 = image0.getPixels();
        Assert.assertEquals(1, pixels0.sizeOfBinDataList());
        Assert.assertEquals(1, pixels0.sizeOfChannelList());
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

    @Test (groups = {"11-06-i-image"}, dependsOnMethods = {"testPixels0"})
    public void testChannel0() {
        Assert.assertNotNull(pixels0);
        channel0 = pixels0.getChannel(0);
        Assert.assertEquals(ref.Image0Pixels0_0Channel0Color, channel0.getColor());
        Assert.assertNull(channel0.getContrastMethod());
        Assert.assertNull(channel0.getEmissionWavelength());
        Assert.assertNull(channel0.getExcitationWavelength());
        Assert.assertNull(channel0.getFluor());
        Assert.assertNull(channel0.getIlluminationType());
        Assert.assertNull(channel0.getName());
        Assert.assertNull(channel0.getNDFilter());
        Assert.assertNull(channel0.getPinholeSize());
        Assert.assertNull(channel0.getPockelCellSetting());
        Assert.assertNull(channel0.getSamplesPerPixel());
        Assert.assertNull(channel0.getAcquisitionMode());
        Assert.assertNotNull(channel0.getLightPath());
        Assert.assertNotNull(channel0.getLightSourceSettings());
        Assert.assertNotNull(channel0.getDetectorSettings());
    }

    @Test (groups = {"11-06-i-image"}, dependsOnMethods = {"testPixels0"})
    public void testBinData0() {
        Assert.assertNotNull(pixels0);
        bindata0 = pixels0.getBinData(0);
        Assert.assertEquals(ref.Image0Pixels0_0Bindata0Length, bindata0.getLength());
        Assert.assertEquals(ref.Image0Pixels0_0Bindata0BigEndian, bindata0.getBigEndian());
        Assert.assertNull(bindata0.getCompression());
    }
    @Test (groups = {"11-06-i-links"}, dependsOnGroups = {"11-06-i-image", "11-06-i-instrument"})
    public void testImage0Linkage() {
        Assert.assertNotNull(image0);
        Assert.assertEquals(0, image0.sizeOfLinkedAnnotationList());
        Assert.assertEquals(0, image0.sizeOfLinkedROIList());
     }
}
