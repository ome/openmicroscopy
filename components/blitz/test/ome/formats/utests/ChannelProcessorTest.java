/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.utests;

import loci.formats.FormatTools;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelData;
import ome.formats.model.ChannelProcessor;
import ome.formats.model.UnitsFactory;
import ome.xml.model.enums.FilamentType;
import ome.xml.model.enums.LaserType;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Channel;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the creation of channel objects.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 */
public class ChannelProcessorTest
{

	/** Reference to the wrapper. */
	private OMEROWrapper wrapper;

	/** Reference to the store. */
	private OMEROMetadataStoreClient store;

	/** Identifies the index of the filter set. */
	private static final int FILTER_SET_INDEX = 0;

	/** Identifies the index of the filter. */
	private static final int FILTER_INDEX = 0;

	/** Identifies the index of the light source. */
	private static final int LIGHTSOURCE_INDEX = 0;

	/** Identifies the index of the instrument. */
	private static final int INSTRUMENT_INDEX = 0;

	/** Identifies the index of the image. */
	private static final int IMAGE_INDEX = 0;

	/** Identifies the index of the channel. */
	private static final int CHANNEL_INDEX = 0;

        /** The LOCI graphics domain. */
        private static final String[] GRAPHICS_DOMAIN =
            new String[] { FormatTools.GRAPHICS_DOMAIN };

    private static ome.units.quantity.Length makeWave(double d)
    {
        return LengthI.convert(new LengthI(d, UnitsLength.NANOMETER));
    }

    private static ome.units.quantity.Length makeCut(double d)
    {
        return LengthI.convert(new LengthI(d, UnitsFactory.TransmittanceRange_CutIn));
    }

    /**
     * Initializes the components and populates the store.
     */
	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        wrapper = new OMEROWrapper(new ImportConfig());
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setReader(new TestReader());
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);

        // Need to populate at least one pixels and image field.
        store.setImageName("Image", IMAGE_INDEX);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        store.setPixelsSizeC(new PositiveInteger(6), IMAGE_INDEX);

        // First Laser, First LightSourceSettings
		store.setLaserID(
				"Laser:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLaserManufacturer("0", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX);
		store.setLaserType(LaserType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Laser:0", IMAGE_INDEX, CHANNEL_INDEX);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX);

		// First Filament , Second LightSourceSettings
		store.setFilamentID(
				"Filament:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentManufacturer("1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentType(
        FilamentType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Filament:1", IMAGE_INDEX, CHANNEL_INDEX + 1);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX + 1);

		// FilterSet
		store.setFilterSetID("FilterSet:0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetLotNumber("0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetID("FilterSet:1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);
		store.setFilterSetLotNumber("1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);

		// FilterSet linkages
		store.setChannelFilterSetRef("FilterSet:0", IMAGE_INDEX,
				CHANNEL_INDEX);
		store.setChannelFilterSetRef("FilterSet:1", IMAGE_INDEX,
				CHANNEL_INDEX + 1);

		// Filters
		store.setFilterID("Filter:0", INSTRUMENT_INDEX, FILTER_INDEX);
		store.setFilterLotNumber("0", INSTRUMENT_INDEX, FILTER_INDEX);
		store.setFilterID("Filter:1", INSTRUMENT_INDEX, FILTER_INDEX + 1);
		store.setFilterLotNumber("1", INSTRUMENT_INDEX, FILTER_INDEX + 1);
		store.setFilterID("Filter:2", INSTRUMENT_INDEX, FILTER_INDEX + 2);
		store.setFilterLotNumber("2", INSTRUMENT_INDEX, FILTER_INDEX + 2);
		store.setFilterID("Filter:3", INSTRUMENT_INDEX, FILTER_INDEX + 3);
		store.setFilterLotNumber("3", INSTRUMENT_INDEX, FILTER_INDEX + 3);
		store.setFilterID("Filter:4", INSTRUMENT_INDEX, FILTER_INDEX + 4);
		store.setFilterLotNumber("4", INSTRUMENT_INDEX, FILTER_INDEX + 4);
		store.setFilterID("Filter:5", INSTRUMENT_INDEX, FILTER_INDEX + 5);
		store.setFilterLotNumber("5", INSTRUMENT_INDEX, FILTER_INDEX + 5);
		store.setFilterID("Filter:6", INSTRUMENT_INDEX, FILTER_INDEX + 6);
		store.setFilterLotNumber("6", INSTRUMENT_INDEX, FILTER_INDEX + 6);
		store.setFilterID("Filter:7", INSTRUMENT_INDEX, FILTER_INDEX + 7);
		store.setFilterLotNumber("7", INSTRUMENT_INDEX, FILTER_INDEX + 7);

		// Filter linkages
		store.setFilterSetEmissionFilterRef("Filter:0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX, FILTER_INDEX);
		store.setFilterSetExcitationFilterRef("Filter:1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX, FILTER_INDEX);
		store.setFilterSetEmissionFilterRef("Filter:6", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1, FILTER_INDEX);
		store.setFilterSetExcitationFilterRef("Filter:7", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1, FILTER_INDEX);
		store.setLightPathEmissionFilterRef(
				"Filter:2", IMAGE_INDEX, CHANNEL_INDEX, CHANNEL_INDEX);
		store.setLightPathExcitationFilterRef(
				"Filter:3", IMAGE_INDEX, CHANNEL_INDEX, CHANNEL_INDEX);
		store.setLightPathEmissionFilterRef("Filter:4", IMAGE_INDEX,
        CHANNEL_INDEX + 1, CHANNEL_INDEX + 1);
		store.setLightPathExcitationFilterRef("Filter:5", IMAGE_INDEX,
        CHANNEL_INDEX + 1, CHANNEL_INDEX + 1);
	}

    private void checkChannelColor(Channel channel, int r, int g, int b)
    {
        Assert.assertNotNull(channel.getRed());
        Assert.assertEquals(channel.getRed().getValue(), r);
        Assert.assertNotNull(channel.getGreen());
        Assert.assertEquals(channel.getGreen().getValue(), g);
        Assert.assertNotNull(channel.getBlue());
        Assert.assertEquals(channel.getBlue().getValue(), b);
        Assert.assertNotNull(channel.getAlpha());
        Assert.assertEquals(channel.getAlpha().getValue(), 255);
    }

    /** Tests the color of the base channel.*/
    @Test
    public void testBaseDataChannelOne()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 255, 0, 0);
        Assert.assertNotNull(data.getLogicalChannel());
        Assert.assertNull(data.getLogicalChannel().getName());
    }


    /** Tests the color of the base channel two.*/
    @Test
    public void testBaseDataChannelTwo()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX + 1);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 0, 255, 0);
        Assert.assertNotNull(data.getLogicalChannel());
        Assert.assertNull(data.getLogicalChannel().getName());
    }

    /** Tests the color of the base channel three.*/
    @Test
    public void testBaseDataChannelThree()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX + 2);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 0, 0, 255);
    }

    /** Tests the color of the base channel four.*/
    @Test
    public void testBaseDataChannelFour()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX + 3);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 255, 0, 0);
    }

    /** Tests the color of the base channel five.*/
    @Test
    public void testBaseDataChannelFive()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX + 4);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 0, 255, 0);
    }

    /** Tests the color of the base channel six.*/
    @Test
    public void testBaseDataChannelSix()
    {
        ChannelProcessor processor = new ChannelProcessor();
        processor.process(store);
        ChannelData data = ChannelData.fromObjectContainerStore(
                store, IMAGE_INDEX, CHANNEL_INDEX + 5);
        Assert.assertNotNull(data.getChannel());
        checkChannelColor(data.getChannel(), 0, 0, 255);
    }

	/** Tests a graphic image. */
	@Test
	public void testGraphicsDomain()
	{
		ChannelProcessor processor = new ChannelProcessor();
                TestReader reader = new TestReader();
                reader.setDomains(GRAPHICS_DOMAIN);
                store.setReader(reader);
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(ChannelProcessor.RED_TEXT,
				data.getLogicalChannel().getName().getValue());
	}

	/** Tests an image with a logical channel with emission wavelength. */
	@Test
	public void testLogicalChannelGreenEmissionWavelength()
	{
		store.setChannelEmissionWavelength(makeWave(525.5),
			IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "525.5");
	}

	/** Tests an image with a logical channel with emission wavelength. */
	@Test
	public void testLogicalChannelBlueEmissionWavelength()
	{
		store.setChannelEmissionWavelength(
        makeWave(450.1), IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "450.1");
	}

	/** Tests an image with a logical channel with emission wavelength. */
	@Test
	public void testLogicalChannelRedEmissionWavelength()
	{
		store.setChannelEmissionWavelength(
        makeWave(625.5), IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "625.5");
	}

	/**
	 * Tests an image with a logical channel with filter set
	 * with emission filter.
	 */
	@Test
	public void testFilterSetEmFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(425), INSTRUMENT_INDEX, 0);
		store.setTransmittanceRangeCutOut(makeCut(430), INSTRUMENT_INDEX, 0);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "425");
	}

	/**
	 * Tests an image with a logical channel with a laser.
	 */
	@Test
	public void testLaserBlueWavelength()
	{
		store.setLaserWavelength(
        makeWave(435.5), INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "435.5");
	}

	/**
	 * Tests an image with a logical channel with excitation wavelength.
	 */
	@Test
	public void testLogicalChannelGreenExcitationWavelength()
	{
		store.setChannelExcitationWavelength(
        makeWave(525.5), IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "525.5");
	}

	/**
	 * Tests an image with a logical channel with excitation wavelength.
	 */
	@Test
	public void testLogicalChannelBlueExcitationWavelength()
	{
		store.setChannelExcitationWavelength(
        makeWave(450.1), IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "450.1");
	}

	/**
	 * Tests an image with a logical channel with excitation wavelength.
	 */
	@Test
	public void testLogicalChannelRedExcitationWavelength()
	{
		store.setChannelExcitationWavelength(
        makeWave(625.5), IMAGE_INDEX, CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "625.5");
	}

	/**
	 * Tests an image with a logical channel with a filter set with an
	 * excitation filter.
	 */
	@Test
	public void testFilterSetExFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(425), INSTRUMENT_INDEX, 1);
		store.setTransmittanceRangeCutOut(makeCut(430), INSTRUMENT_INDEX, 1);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "425");
	}

	/**
	 * Tests a logical channel with a light path with an emission filter.
	 */
	@Test
	public void testLogicalChannelLightPathEmFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 2);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "430");
	}

	/**
	 * Tests a logical channel with a light path with an excitation filter.
	 */
	@Test
	public void testLogicalChannelLightPathExFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 3);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "430");
	}

	/**
	 * Tests a logical channel with a light path with an emission filter.
	 * and an emission filter from a filter set.
	 * The value of the emission filter from the light path
	 * will determine the name and color.
	 */
	@Test
	public void testLogicalChannelLightPathEmFilterBlueAndFilterSetEmFilterRedWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutIn(makeCut(625), INSTRUMENT_INDEX, 0);
		store.setTransmittanceRangeCutOut(makeCut(640), INSTRUMENT_INDEX, 0);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "430");
	}

	/**
	 * Tests a logical channel with a light path with an excitation filter.
	 * and an excitation filter from a filter set.
	 * The value of the excitation filter from the light path
	 * will determine the name and color.
	 */
	@Test
	public void testLogicalChannelLightPathExFilterBlueAndFilterSetExFilterRedWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutIn(makeCut(625), INSTRUMENT_INDEX, 1);
		store.setTransmittanceRangeCutOut(makeCut(640), INSTRUMENT_INDEX, 1);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "430");
	}

	/**
	 * Tests a logical channel with a light path with an excitation filter.
	 * and an emission filter set from a filter set.
	 * The value of the emission filter from the filter set
	 * will determine the name and color.
	 */
	@Test
	public void testLogicalChannelLightPathExFilterBlueAndFilterSetEmFilterRedWavelength()
	{
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutIn(makeCut(625), INSTRUMENT_INDEX, 0);
		store.setTransmittanceRangeCutOut(makeCut(640), INSTRUMENT_INDEX, 0);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		Assert.assertNotNull(data.getLogicalChannel());
		Assert.assertNotNull(data.getLogicalChannel().getName());
		Assert.assertEquals(data.getLogicalChannel().getName().getValue(), "625");
	}

	/**
	 * Tests an image with 2 channels, one blue (light path emission filter)
	 * and another one with a transmitted light.
	 */
	@Test
	public void testChannelsEmFilterLightPathBlueAndTransmittedLight()
	{
		ChannelProcessor processor = new ChannelProcessor();
		store.setReader(new TestReader());
		store.setTransmittanceRangeCutIn(makeCut(430), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(makeCut(435), INSTRUMENT_INDEX, 2);
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		//
		data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX+1);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
	}

	/**
	 * Tests an image with 2 channels, one red (light path emission filter)
	 * and another one with a transmitted light.
	 */
	@Test
	public void testChannelsEmFilterLightPathRedAndTransmittedLight()
	{
		ChannelProcessor processor = new ChannelProcessor();
		store.setReader(new TestReader());
		store.setTransmittanceRangeCutIn(makeCut(600), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(makeCut(620), INSTRUMENT_INDEX, 2);
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		//
		data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX+1);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
	}

	/**
	 * Tests an image with 2 channels, one green (light path emission filter)
	 * and another one with a transmitted light.
	 */
	@Test
	public void testChannelsEmFilterLightPathGreenAndTransmittedLight()
	{
		ChannelProcessor processor = new ChannelProcessor();
		store.setReader(new TestReader());
		store.setTransmittanceRangeCutIn(makeCut(510), INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(makeCut(520), INSTRUMENT_INDEX, 2);
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		//
		data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX+1);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
	}

	/**
	 * Tests an image with 2 channels, one green (filter set emission filter)
	 * and another one with a transmitted light.
	 */
	@Test
	public void testChannelsEmFilterFilterSetGreenAndTransmittedLight()
	{
		ChannelProcessor processor = new ChannelProcessor();
		store.setReader(new TestReader());
		store.setTransmittanceRangeCutIn(makeCut(510), INSTRUMENT_INDEX, 0);
		store.setTransmittanceRangeCutOut(makeCut(520), INSTRUMENT_INDEX, 0);
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 0);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
		//
		data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX+1);
		Assert.assertNotNull(data.getChannel());
		Assert.assertNotNull(data.getChannel().getRed());
		Assert.assertEquals(data.getChannel().getRed().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getGreen());
		Assert.assertEquals(data.getChannel().getGreen().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getBlue());
		Assert.assertEquals(data.getChannel().getBlue().getValue(), 255);
		Assert.assertNotNull(data.getChannel().getAlpha());
		Assert.assertEquals(data.getChannel().getAlpha().getValue(), 255);
	}

}
