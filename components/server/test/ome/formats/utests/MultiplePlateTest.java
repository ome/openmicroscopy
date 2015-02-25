/*
 *   Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ome.util.LSID;
import ome.formats.OMEROMetadataStore;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filter;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.screen.Plate;
import ome.model.screen.Well;
import ome.model.screen.WellSample;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MultiplePlateTest
{
    private OMEROMetadataStore store;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        store = new OMEROMetadataStore();
        Map<String, Integer> indexes;
        
        Instrument instrument = new Instrument();
        indexes =  new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", 0);
        store.updateObject("Instrument:0", instrument, indexes);

        Objective objective = new Objective();
        indexes =  new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", 0);
        indexes.put("objectiveIndex", 0);
        store.updateObject("Objective:0:0", objective, indexes);

        LightSource lightSource = new Laser();
        indexes =  new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", 0);
        indexes.put("lightSourceIndex", 0);
        store.updateObject("LightSource:0:0", lightSource, indexes);

        Detector detector = new Detector();
        indexes =  new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", 0);
        indexes.put("detectorIndex", 0);
        store.updateObject("Detector:0:0", detector, indexes);

        for (int d = 0; d < 2; d++) {
            Dichroic dichroic = new Dichroic();
            dichroic.setModel("Dichroic:" + d);
            indexes =  new LinkedHashMap<String, Integer>();
            indexes.put("instrumentIndex", 0);
            indexes.put("dichroicIndex", d);
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
            Pixels pixels = new Pixels();
            store.updateObject(
                    String.format("Pixels:%d", i),
                    pixels, indexes
            );
            ObjectiveSettings objectiveSettings = new ObjectiveSettings();
            store.updateObject(
                    String.format("ObjectiveSettings:%d", i),
                    objectiveSettings, indexes
            );
        }
        // Populate Channels
        for (int i = 0; i < 9; i++) {
            for (int c = 0; c < 2; c++) {
                Channel channel = new Channel();
                channel.setVersion(c);
                LogicalChannel logicalChannel = new LogicalChannel();
                LightSettings lightSettings = new LightSettings();
                DetectorSettings detectorSettings = new DetectorSettings();

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

                store.updateObject(
                        String.format("LightSettings:%d:%d", i, c),
                        lightSettings,
                        indexes
                );

                store.updateObject(
                        String.format("DetectorSettings:%d:%d", i, c),
                        detectorSettings,
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
                    String.format("ObjectiveSettings:%d", i),
                    new String [] {"Objective:0:0"});

            referenceCache.put(
                    String.format("LightSettings:%d:%d", i, 0),
                    new String [] {"LightSource:0:0"});

            referenceCache.put(
                    String.format("LightSettings:%d:%d", i, 1),
                    new String [] {"LightSource:0:0"});

            referenceCache.put(
                    String.format("DetectorSettings:%d:%d", i, 0),
                    new String [] {"Detector:0:0"});

            referenceCache.put(
                    String.format("DetectorSettings:%d:%d", i, 1),
                    new String [] {"Detector:0:0"});

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
    @Test
    public void testMetadata()
    {
        Instrument instrument = (Instrument) store.getObjectByLSID(
                new LSID("Instrument:0"));
        Assert.assertNotNull(instrument);
        Assert.assertEquals(instrument.sizeOfFilter(), 8);
        Assert.assertEquals(instrument.sizeOfDichroic(), 2);
        Assert.assertEquals(instrument.sizeOfObjective(), 1);
        Assert.assertEquals(instrument.sizeOfLightSource(), 1);
        Assert.assertEquals(instrument.sizeOfDetector(), 1);

        Objective objective = instrument.iterateObjective().next();
        Assert.assertNotNull(objective);
        LightSource lightSource = instrument.iterateLightSource().next();
        Assert.assertNotNull(lightSource);
        Detector detector = instrument.iterateDetector().next();
        Assert.assertNotNull(detector);

        for (int i = 0; i < 3; i++)
        {
            Plate plate = (Plate) store.getObjectByLSID(new LSID("Plate:" + i));
            Assert.assertNotNull(plate);
            Assert.assertEquals("Plate:" + i, plate.getName());
            Assert.assertEquals(plate.sizeOfWells(), 3);
            Iterator<Well> wellIterator = plate.iterateWells();
            while (wellIterator.hasNext())
            {
                Well well = wellIterator.next();
                Assert.assertNotNull(well);
                Assert.assertEquals(well.sizeOfWellSamples(), 1);
                WellSample wellSample = well.iterateWellSamples().next();
                Assert.assertNotNull(wellSample);

                Image image = wellSample.getImage();
                Assert.assertNotNull(image);
                Instrument imageInstrument = image.getInstrument();
                Assert.assertNotNull(imageInstrument);
                Assert.assertEquals(instrument, imageInstrument);
                ObjectiveSettings objectiveSettings =
                        image.getObjectiveSettings();
                Assert.assertNotNull(objectiveSettings);
                Objective imageObjective = objectiveSettings.getObjective();
                Assert.assertEquals(imageObjective, objective);
                Pixels pixels = image.getPrimaryPixels();
                Assert.assertNotNull(pixels);
                Assert.assertEquals(pixels.sizeOfChannels(), 2);
                for (Channel channel : pixels.<Channel>collectChannels(null)) {
                    assertChannel(channel, lightSource, detector);
                }
            }
        }
    }

    /**
     * Checks that channel is linked to LogicalChannel and LightPath.
     * Checks that LightPath is linked to correct Filters and Dichroic.
     * @param channel Channel object.
     * @param expectedLightSource LightSource from Instrument.
     * @param expectedDetector Detector from Instrument
     */
    public void assertChannel(
            Channel channel, LightSource expectedLightSource,
            Detector expectedDetector)
    {
        LogicalChannel logicalChannel = channel.getLogicalChannel();
        Assert.assertNotNull(logicalChannel);
        LightPath lightPath = logicalChannel.getLightPath();
        Assert.assertNotNull(lightPath);
        Assert.assertEquals(lightPath.sizeOfEmissionFilterLink(), 2);
        Assert.assertEquals(lightPath.sizeOfExcitationFilterLink(), 2);

        LightSettings lightSettings = logicalChannel.getLightSourceSettings();
        Assert.assertNotNull(lightSettings);
        LightSource lightSource = lightSettings.getLightSource();
        Assert.assertNotNull(lightSource);
        Assert.assertEquals(lightSource, expectedLightSource);

        DetectorSettings detectorSettings =
                logicalChannel.getDetectorSettings();
        Assert.assertNotNull(detectorSettings);
        Detector detector = detectorSettings.getDetector();
        Assert.assertNotNull(detector);
        Assert.assertEquals(detector, expectedDetector);

        for (Filter filter : lightPath.linkedEmissionFilterList()) {
            String model = filter.getModel();
            Assert.assertNotNull(model);
            Assert.assertTrue(model.startsWith("emFilter"));
        }

        for (Filter filter : lightPath.linkedExcitationFilterList()) {
            String model = filter.getModel();
            Assert.assertNotNull(model);
            Assert.assertTrue(model.startsWith("exFilter"));
        }

        Dichroic dichroic = lightPath.getDichroic();
        Assert.assertNotNull(dichroic);
        Assert.assertEquals(
                "Dichroic:" + channel.getVersion(),
                dichroic.getModel()
            );
    }

    @Test
    public void testCheckAndCollapseGraph()
    {
        store.checkAndCollapseGraph();

        Set<LogicalChannel> uniqueLogicalChannels =
                new HashSet<LogicalChannel>();
        Set<LightSettings> uniqueLightSettings =
                new HashSet<LightSettings>();
        Set<DetectorSettings> uniqueDetectorSettings =
                new HashSet<DetectorSettings>();
        Set<LightPath> uniqueLightPaths = new HashSet<LightPath>();
        Set<ObjectiveSettings> uniqueObjectiveSettings =
                new HashSet<ObjectiveSettings>();
        for (int i = 0; i < 3; i++)
        {
            Plate plate = (Plate) store.getObjectByLSID(new LSID("Plate:" + i));
            Assert.assertNotNull(plate);
            Assert.assertEquals("Plate:" + i, plate.getName());
            Iterator<Well> wellIterator = plate.iterateWells();
            while (wellIterator.hasNext())
            {
                Well well = wellIterator.next();
                WellSample wellSample = well.iterateWellSamples().next();
                Image image = wellSample.getImage();
                uniqueObjectiveSettings.add(image.getObjectiveSettings());
                Pixels pixels = image.getPrimaryPixels();
                for (Channel channel : pixels.<Channel>collectChannels(null)) {
                    LogicalChannel logicalChannel = channel.getLogicalChannel();
                    uniqueLogicalChannels.add(logicalChannel);
                    uniqueLightPaths.add(logicalChannel.getLightPath());
                    uniqueLightSettings.add(
                            logicalChannel.getLightSourceSettings());
                    uniqueDetectorSettings.add(
                            logicalChannel.getDetectorSettings());
                }
            }
        }
        Assert.assertEquals(uniqueObjectiveSettings.size(), 1);
        Assert.assertEquals(uniqueLightSettings.size(), 1);
        Assert.assertEquals(uniqueDetectorSettings.size(), 1);
        Assert.assertEquals(uniqueLightPaths.size(), 2);
        Assert.assertEquals(uniqueLogicalChannels.size(), 2);
    }
}
