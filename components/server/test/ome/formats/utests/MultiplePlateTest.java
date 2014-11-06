/*
 *   Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.util.LSID;
import ome.formats.OMEROMetadataStore;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filter;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightPath;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.screen.Plate;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import junit.framework.TestCase;

public class MultiplePlateTest extends TestCase
{
    private OMEROMetadataStore store;

    @Override
    protected void setUp() throws Exception
    {
        store = new OMEROMetadataStore();
        Map<String, Integer> indexes;
        
        Instrument instrument = new Instrument();
        indexes =  new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", 0);
        store.updateObject("Instrument:0", instrument, indexes);

        
        for (int d = 0; d < 2; d++) {
            Dichroic dichroic = new Dichroic();
            dichroic.setModel("Dichroic:" + d);
            indexes =  new LinkedHashMap<String, Integer>();
            indexes.put("instrumentIndex", 0);
            indexes.put("dichroicIndex", 0);
            store.updateObject(
                    String.format("Dichroic:0:%d", d), dichroic, indexes);
        }

        for (int f = 0; f < 8; f++) {
            Filter filter = new Filter();
            if (f % 2 == 0) {
                filter.setModel("emFilter:" + f);
            } else {
                filter.setModel("exFilter" + f);
            }
            indexes =  new LinkedHashMap<String, Integer>();
            indexes.put("instrumentIndex", 0);
            indexes.put("filterIndex", f);
            store.updateObject(
                    String.format("Filter:%d:%d", 0, f),
                    filter, indexes
            );
        }

        // We are using separate loops for the below metadata population to
        // better mimic exactly the order that OMERO.importer sends us data.
        for (int i = 0; i < 9; i++)
        {
            Image image = new Image();
            indexes = new LinkedHashMap<String, Integer>();
            indexes.put("imageIndex", i);
            store.updateObject("Image:" + i, image, indexes);
        }
        // Populate Pixels
        for (int p = 0; p < 9; p++) {
            Pixels pixels = new Pixels();
            indexes = new LinkedHashMap<String, Integer>();
            indexes.put("imageIndex", p);
            store.updateObject(
                    String.format("Pixels:%d", p),
                    pixels, indexes
            );
        }
        // Populate Channels
        for (int i = 0; i < 9; i++) {
            for (int c = 0; c < 2; c++) {
                Channel channel = new Channel();
                channel.setVersion(c);
                LogicalChannel logicalChannel = new LogicalChannel();

                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("imageIndex", i);
                indexes.put("channelIndex", c);
                store.updateObject(
                        String.format("Channel:%d:%d", i, c),
                        channel, indexes
                );

                store.updateObject(
                        String.format("LogicalChannel:%d:%d", i, c),
                        logicalChannel,
                        indexes
                );
            }
        }
        // Populate Light Path
        for (int i = 0; i < 9; i++) {
            for (int c = 0; c < 2; c++) {
                LightPath lightPath = new LightPath();
                indexes =  new LinkedHashMap<String, Integer>();
                indexes.put("imageIndex", i);
                indexes.put("channelIndex", c);
                store.updateObject(
                        String.format("LightPath:%d:%d", i, c),
                        lightPath, indexes
                );
            }
        }

        for (int i = 0; i < 3; i++)
        {
            Plate plate = new Plate();
            plate.setName("Plate:" + i);
            indexes = new LinkedHashMap<String, Integer>();
            indexes.put("plateIndex", i);
            store.updateObject("Plate:" + i, plate, indexes);
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                Well well = new Well();
                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("plateIndex", i);
                indexes.put("wellIndex", j);
                store.updateObject(String.format(
                        "Well:%d:%d", i, j), well, indexes);
            }
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                WellSample wellSample = new WellSample();
                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("plateIndex", i);
                indexes.put("wellIndex", j);
                indexes.put("wellSampleIndex", 0);
                store.updateObject(String.format(
                        "WellSample:%d:%d:%d", i, j, 0), wellSample, indexes);
            }
        }

        Map<String, String[]> referenceCache = new HashMap<String, String[]>();
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                referenceCache.put(
                        String.format("WellSample:%d:%d:%d", i, j, 0),
                        new String[] { "Image:" + ((i * 3) + j) });
            }
        }

        for (int i = 0; i < 9; i++) {
            referenceCache.put(
                    String.format("Image:%d", i),
                    new String [] {"Instrument:0"});

            referenceCache.put(
                    String.format("LightPath:%d:%d", i, 0),
                    new String [] {
                        "Filter:0:0:OMERO_EMISSION_FILTER",
                        "Filter:0:1:OMERO_EXCITATION_FILTER",
                        "Filter:0:2:OMERO_EMISSION_FILTER",
                        "Filter:0:3:OMERO_EXCITATION_FILTER",
                        "Dichroic:0:0",
                    });
            referenceCache.put(
                    String.format("LightPath:%d:%d", i, 1),
                    new String [] {
                        "Filter:0:5:OMERO_EXCITATION_FILTER",
                        "Filter:0:4:OMERO_EMISSION_FILTER",
                        "Filter:0:7:OMERO_EXCITATION_FILTER",
                        "Filter:0:6:OMERO_EMISSION_FILTER",
                        "Dichroic:0:1"
                    });
        }

        store.updateReferences(referenceCache);
    }

    /**
     * Tests Objects and object References created in setUp();
     * Checks that object exists and are correctly linked.
     */
    public void testMetadata()
    {
        Instrument instrument = (Instrument) store.getObjectByLSID(
                new LSID("Instrument:0"));
        assertNotNull(instrument);
        assertEquals(8, instrument.sizeOfFilter());
        assertEquals(2, instrument.sizeOfDichroic());

        for (int i = 0; i < 3; i++)
        {
            Plate plate = (Plate) store.getObjectByLSID(new LSID("Plate:" + i));
            assertNotNull(plate);
            assertEquals("Plate:" + i, plate.getName());
            assertEquals(3, plate.sizeOfWells());
            Iterator<Well> wellIterator = plate.iterateWells();
            while (wellIterator.hasNext())
            {
                Well well = wellIterator.next();
                assertNotNull(well);
                assertEquals(1, well.sizeOfWellSamples());
                WellSample wellSample = well.iterateWellSamples().next();
                assertNotNull(wellSample);

                Image image = wellSample.getImage();
                assertNotNull(image);
                Instrument imageInstrument = image.getInstrument();
                assertNotNull(imageInstrument);
                assertEquals(instrument, imageInstrument);
                Pixels pixels = image.getPrimaryPixels();
                assertNotNull(pixels);
                assertEquals(2, pixels.sizeOfChannels());
                for (Channel channel : pixels.<Channel>collectChannels(null)) {
                    assertChannel(channel);
                }
            }
        }
    }
    
    /**
     * Checks that channel is linked to LogicalChannel and LightPath.
     * Checks that LightPath is linked to correct Filters and Dichroic.
     * @param channel Channel object.
     */
    public void assertChannel(Channel channel)
    {
        LogicalChannel logicalChannel = channel.getLogicalChannel();
        assertNotNull(logicalChannel);
        LightPath lightPath = logicalChannel.getLightPath();
        assertNotNull(lightPath);
        assertEquals(2, lightPath.sizeOfEmissionFilterLink());
        assertEquals(2, lightPath.sizeOfExcitationFilterLink());

        for (Filter filter : lightPath.linkedEmissionFilterList()) {
            String model = filter.getModel();
            assertNotNull(model);
            assertTrue(model.startsWith("emFilter"));
        }

        for (Filter filter : lightPath.linkedExcitationFilterList()) {
            String model = filter.getModel();
            assertNotNull(model);
            assertTrue(model.startsWith("exFilter"));
        }

        Dichroic dichroic = lightPath.getDichroic();
        assertNotNull(dichroic);
        assertEquals(
                "Dichroic:" + channel.getVersion(),
                dichroic.getModel()
            );
    }
}