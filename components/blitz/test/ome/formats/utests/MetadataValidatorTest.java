/*
 * ome.formats.utests.MetadataValidatorTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2009 University of Dundee. All rights reserved.
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;

import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.Channel;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Dichroic;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.Well;
import omero.model.WellSample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Generic set of unit tests, that are designed to be initiated on a
 * Bio-Formats "ID" target, which sanity check metadata validity.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
@Test(groups={"manual"})
public class MetadataValidatorTest
{
    /** Logger for this class */
    private static final Logger log =
        LoggerFactory.getLogger(MetadataValidatorTest.class);

    /** Our testing Metadata store client */
    private OMEROMetadataStoreClient store;

    /** Our testing Metadata store client (minimum metadata). */
    private OMEROMetadataStoreClient minimalStore;

    /** The OMERO basic wrapper for Bio-Formats readers */
    private OMEROWrapper wrapper;

    /** The OMERO basic wrapper (initialized with minimum metadata). */
    private OMEROWrapper minimalWrapper;

    /** Our current container cache. */
    private Map<LSID, IObjectContainer> containerCache;

    /** Our current reference cache. */
    private Map<LSID, List<LSID>> referenceCache;

    /** Our testing service factory. */
    private ServiceFactoryPrx sf;

    /** Our import configuration. */
    private ImportConfig config;

    @Parameters({ "target" })
    @BeforeClass
    public void setUp(String target) throws Exception
    {
        log.info("METADATA VALIDATOR TARGET: " + target);
        sf = new TestServiceFactory().proxy();
        config = new ImportConfig();
        // Let the user know at what level we're logging
        ch.qos.logback.classic.Logger lociLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("loci");
        ch.qos.logback.classic.Logger omeLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ome.formats");
        log.info(String.format(
                "Log levels -- Bio-Formats: %s OMERO.importer: %s",
                lociLogger.getLevel(), omeLogger.getLevel()));
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
                new BlitzInstanceProvider(store.getEnumerationProvider()));
        minimalStore = new OMEROMetadataStoreClient();
        minimalStore.initialize(sf);
        minimalStore.setEnumerationProvider(new TestEnumerationProvider());
        minimalStore.setInstanceProvider(
                new BlitzInstanceProvider(minimalStore.getEnumerationProvider()));
        wrapper = new OMEROWrapper(config);
        wrapper.setMetadataOptions(
                new DefaultMetadataOptions(MetadataLevel.ALL));
        minimalWrapper = new OMEROWrapper(config);
        minimalWrapper.setMetadataOptions(
                new DefaultMetadataOptions(MetadataLevel.MINIMUM));
        wrapper.setMetadataStore(store);
        store.setReader(wrapper.getImageReader());
        minimalStore.setReader(minimalWrapper.getImageReader());
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        wrapper.close();
        minimalWrapper.close();
        store.logout();
        minimalStore.logout();
    }

    @Parameters({ "target" })
    @Test
    public void testMetadataLevelAllSetId(String target) throws Exception
    {
        wrapper.setId(target);
        store.postProcess();
        traceMetadataStoreData(store);
    }

    @Parameters({ "target" })
    @Test
    public void testMetadataLevelMinimumSetId(String target) throws Exception
    {
        minimalWrapper.setId(target);
        minimalStore.postProcess();
        traceMetadataStoreData(minimalStore);
    }

    @Test(dependsOnMethods=
            {"testMetadataLevelAllSetId", "testMetadataLevelMinimumSetId"})
    public void testMetadataLevel()
        throws FormatException, IOException
    {
        assertEquals(MetadataLevel.MINIMUM,
                minimalWrapper.getMetadataOptions().getMetadataLevel());
        assertEquals(MetadataLevel.ALL,
                wrapper.getMetadataOptions().getMetadataLevel());
        assertFalse(0 == (wrapper.getSeriesMetadata().size()
                          + wrapper.getGlobalMetadata().size()));
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testMetadataLevelEquivalentDimensions()
    {
        assertEquals(wrapper.getSeriesCount(), minimalWrapper.getSeriesCount());
        for (int i = 0; i < minimalWrapper.getSeriesCount(); i++)
        {
            wrapper.setSeries(i);
            minimalWrapper.setSeries(i);
            assertEquals(wrapper.getSizeX(), minimalWrapper.getSizeX());
            assertEquals(wrapper.getSizeY(), minimalWrapper.getSizeY());
            assertEquals(wrapper.getSizeZ(), minimalWrapper.getSizeZ());
            assertEquals(wrapper.getSizeC(), minimalWrapper.getSizeC());
            assertEquals(wrapper.getSizeT(), minimalWrapper.getSizeT());
            assertEquals(wrapper.getPixelType(),
                         minimalWrapper.getPixelType());
            assertEquals(wrapper.isLittleEndian(),
                         minimalWrapper.isLittleEndian());
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testMetadataLevelEquivalentUsedFiles()
        throws FormatException, IOException
    {
        for (int i = 0; i < minimalWrapper.getSeriesCount(); i++)
        {
            minimalWrapper.setSeries(i);
            wrapper.setSeries(i);

            String[] pixelsOnlyFiles = minimalWrapper.getSeriesUsedFiles();
            String[] allFiles = wrapper.getSeriesUsedFiles();

            assertEquals(allFiles.length, pixelsOnlyFiles.length);

            Arrays.sort(allFiles);
            Arrays.sort(pixelsOnlyFiles);

            for (int j = 0; j < pixelsOnlyFiles.length; j++)
            {
                assertEquals(allFiles[j], pixelsOnlyFiles[j]);
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testMetadataLevelEquivalentPlaneData()
        throws FormatException, IOException
    {
        for (int i = 0; i < minimalWrapper.getSeriesCount(); i++)
        {
            minimalWrapper.setSeries(i);
            wrapper.setSeries(i);
            assertEquals(wrapper.getImageCount(),
                         minimalWrapper.getImageCount());
            for (int j = 0; j < minimalWrapper.getImageCount(); j++)
            {
                byte[] pixelsOnlyPlane = minimalWrapper.openBytes(j);
                String pixelsOnlySHA1 = sha1(pixelsOnlyPlane);
                byte[] allPlane = wrapper.openBytes(j);
                String allSHA1 = sha1(allPlane);

                if (!pixelsOnlySHA1.equals(allSHA1))
                {
                    fail(String.format(
                            "MISMATCH: Series:%d Image:%d PixelsOnly%s All:%s",
                            i, j, pixelsOnlySHA1, allSHA1));
                }
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testEquivalentBlockRetrievalPlaneData()
        throws FormatException, IOException
    {
        String fileName = wrapper.getCurrentFile();
        int sizeX = wrapper.getSizeX();
        int sizeY = wrapper.getSizeY();
        int sizeZ = wrapper.getSizeZ();
        int sizeC = wrapper.getSizeC();
        int sizeT = wrapper.getSizeT();
        int bytesPerPixel = wrapper.getBitsPerPixel() / 8;
        int planeSize = sizeX * sizeY * bytesPerPixel;
        byte[] planar = new byte[planeSize];
        byte[] block = new byte[planeSize];
        int planeNumber = 0;
        String planarDigest;
        String blockDigest;
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    planeNumber = wrapper.getIndex(z, c, t);
                    wrapper.openPlane2D(fileName, planeNumber,
                                        planar);
                    planarDigest = sha1(planar);
                    wrapper.openPlane2D(fileName, planeNumber, block, 0, 0,
                                        sizeX, sizeY);
                    blockDigest = sha1(block);
                    assertEquals(planarDigest, blockDigest);
                }
            }
        }
    }

    /**
     * Dumps <code>TRACE</code> data for a given metadata store.
     * @param store The store to dump <code>TRACE</code> data for.
     */
    private void traceMetadataStoreData(OMEROMetadataStoreClient store)
    {
        containerCache = store.getContainerCache();
        referenceCache = store.getReferenceCache();
        log.trace("Starting container cache...");
        for (LSID key : containerCache.keySet())
        {
            String s = String.format("%s == %s,%s",
                    key, containerCache.get(key).sourceObject,
                    containerCache.get(key).LSID);
            log.trace(s);
        }
        log.trace("Starting reference cache...");
        for (LSID key : referenceCache.keySet())
        {
            for (LSID value : referenceCache.get(key))
            {
                String s = String.format("%s == %s", key, value);
                log.trace(s);
            }
        }
        log.trace("containerCache contains " + containerCache.size()
                + " entries.");
        log.trace("referenceCache contains "
                + store.countCachedReferences(null, null)
                + " entries.");
        List<IObjectContainer> imageContainers =
            store.getIObjectContainers(Image.class);
        for (IObjectContainer imageContainer : imageContainers)
        {
            Image image = (Image) imageContainer.sourceObject;
            log.trace(String.format(
                    "Image indexes:%s name:%s", imageContainer.indexes,
                    image.getName().getValue()));
        }
    }

    /**
     * Calculates a SHA-1 digest on a byte array.
     * @param buf Byte array to calculate a SHA-1 digest for.
     * @return Hex string of the SHA-1 digest for <code>buf</code>.
     */
    private String sha1(byte[] buf)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return DataTools.bytesToHex(md.digest(buf));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Examines the container cache and returns whether or not an LSID is
     * present.
     * @param klass Instance class of the source object container.
     * @param lsid LSID to compare against.
     * @return <code>true</code> if the object exists in the container cache,
     * and <code>false</code> otherwise.
     */
    private boolean authoritativeLSIDExists(Class<? extends IObject> klass,
                                            LSID lsid)
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        for (IObjectContainer container : containers)
        {
            LSID containerLSID = new LSID(container.LSID);
            if (containerLSID.equals(lsid))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Examines the container cache and returns whether or not an LSID is
     * present.
     * @param klasses Instance classes of potential source object containers.
     * @param lsid LSID to compare against.
     * @return <code>true</code> if an object exists in the container cache,
     * for one of <code>klasses</code> and <code>false</code> otherwise.
     */
    private boolean authoritativeLSIDExists(List<Class<? extends IObject>> klasses,
                                            LSID lsid)
    {
        for (Class<? extends IObject> klass : klasses)
        {
            List<IObjectContainer> containers =
                store.getIObjectContainers(klass);

            for (IObjectContainer container : containers)
            {
                LSID containerLSID = new LSID(container.LSID);
                if (containerLSID.equals(lsid))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testCreationDateIsReasonable()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(Image.class);
        for (IObjectContainer container : containers)
        {
            Image image = (Image) container.sourceObject;
            assertNotNull(image.getAcquisitionDate());
            Date acquisitionDate =
                new Date(image.getAcquisitionDate().getValue());
            Date now = new Date(System.currentTimeMillis());
            Date january1st1995 = new GregorianCalendar(1995, 1, 1).getTime();
            if (acquisitionDate.after(now))
            {
                fail(String.format("%s after %s", acquisitionDate, now));
            }
            if (acquisitionDate.before(january1st1995))
            {
                fail(String.format("%s before %s", acquisitionDate,
                                   january1st1995));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testPlaneInfoZCT()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(PlaneInfo.class);
        for (IObjectContainer container : containers)
        {
            PlaneInfo planeInfo = (PlaneInfo) container.sourceObject;
            assertNotNull("theZ is null", planeInfo.getTheZ());
            assertNotNull("theC is null", planeInfo.getTheC());
            assertNotNull("theT is null", planeInfo.getTheT());
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testChannelCount()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(Pixels.class);
        for (IObjectContainer container : containers)
        {
            Pixels pixels = (Pixels) container.sourceObject;
            assertNotNull(pixels.getSizeC());
            int sizeC = pixels.getSizeC().getValue();
            Integer imageIndex =
                    container.indexes.get(Index.IMAGE_INDEX.getValue());
            int count = store.countCachedContainers(Channel.class, imageIndex);
            String e = String.format(
                    "Pixels sizeC %d != channel object count %d",
                    sizeC, count);
            for (int c = 0; c < sizeC; c++)
            {
                count = store.countCachedContainers(
                        Channel.class, imageIndex, c);
                e = String.format(
                        "Missing channel object; imageIndex=%d " +
                        "channelIndex=%d", imageIndex, c);
                assertEquals(e, 1, count);
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testLogicalChannelCount()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(Pixels.class);
        for (IObjectContainer container : containers)
        {
            Pixels pixels = (Pixels) container.sourceObject;
            assertNotNull(pixels.getSizeC());
            int sizeC = pixels.getSizeC().getValue();
            Integer imageIndex =
                    container.indexes.get(Index.IMAGE_INDEX.getValue());
            int count = store.countCachedContainers(LogicalChannel.class,
                                                    imageIndex);
            String e = String.format(
                    "Pixels sizeC %d != logical channel object count %d",
                    sizeC, count);
            assertEquals(e, sizeC, count);
            for (int c = 0; c < sizeC; c++)
            {
                count = store.countCachedContainers(
                    LogicalChannel.class, imageIndex, c);
                e = String.format(
                        "Missing logical channel object; imageIndex=%d " +
                        "channelIndex=%d", imageIndex, c);
                assertEquals(e, 1, count);
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testPlatesExist()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(WellSample.class);
        for (IObjectContainer container : containers)
        {
            Map<String, Integer> indexes = container.indexes;
            Integer plateIndex = indexes.get(Index.PLATE_INDEX.getValue());
            String e = String.format(
                    "Plate %d not found in container cache", plateIndex);
            assertTrue(e,
                    store.countCachedContainers(Plate.class, plateIndex) > 0);
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testWellsExist()
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(WellSample.class);
        for (IObjectContainer container : containers)
        {
            Map<String, Integer> indexes = container.indexes;
            Integer plateIndex = indexes.get(Index.PLATE_INDEX.getValue());
            Integer wellIndex = indexes.get(Index.WELL_INDEX.getValue());
            String e = String.format(
                    "Well %d not found in container cache", wellIndex);
            int count = store.countCachedContainers(Well.class, plateIndex,
                    wellIndex);
            assertTrue(e, count == 1);
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testAuthoritativeLSIDsAreUnique()
    {
        Set<String> authoritativeLSIDs = new HashSet<String>();
        Set<IObjectContainer> containers =
            new HashSet<IObjectContainer>(containerCache.values());
        for (IObjectContainer container : containers)
        {
            if (!authoritativeLSIDs.add(container.LSID))
            {
                String e = String.format(
                        "LSID from %s,%s not unique.",
                        container.sourceObject, container.LSID);
                fail(e);
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testDetectorSettingsIndexes()
    {
        Class<? extends IObject> klass = DetectorSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        for (IObjectContainer container : containers)
        {
            Map<String, Integer> indexes = container.indexes;
            Integer imageIndex = indexes.get(Index.IMAGE_INDEX.getValue());
            Integer channelIndex = indexes.get(Index.CHANNEL_INDEX.getValue());
            int imageCount = store.countCachedContainers(Image.class, null);
            String e = String.format("imageIndex %d >= imageCount %d",
                    imageIndex, imageCount);
            assertFalse(e, imageIndex >= imageCount);
            int logicalChannelCount = store.countCachedContainers(
                    LogicalChannel.class, imageIndex);
            e = String.format(
                    "channelIndex %d >= logicalChannelCount %d",
                    channelIndex, logicalChannelCount);
            assertFalse(e, channelIndex >= logicalChannelCount);
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testDetectorSettingsDetectorRef()
    {
        Class<? extends IObject> klass = DetectorSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            String e = String.format(
                    "%s %s not found in reference cache",
                    klass, lsid);
            assertTrue(e, referenceCache.containsKey(lsid));
            List<LSID> references = referenceCache.get(lsid);
            assertTrue(references.size() > 0);
            for (LSID referenceLSID : references)
            {
                assertNotNull(referenceLSID);
                klass = Detector.class;
                e = String.format(
                        "%s with LSID %s not found in container cache",
                        klass, referenceLSID);
                assertTrue(e, authoritativeLSIDExists(klass, referenceLSID));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testObjectiveSettingsIndexes()
    {
        Class<? extends IObject> klass = ObjectiveSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        for (IObjectContainer container : containers)
        {
            Map<String, Integer> indexes = container.indexes;
            Integer imageIndex = indexes.get(Index.IMAGE_INDEX.getValue());
            int imageCount = store.countCachedContainers(Image.class, null);
            String e = String.format("imageIndex %d >= imageCount %d",
                    imageIndex, imageCount);
            assertFalse(e, imageIndex >= imageCount);
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testObjectiveSettingsObjectiveRef()
    {
        Class<? extends IObject> klass = ObjectiveSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            String e = String.format(
                    "%s %s not found in reference cache", klass, lsid);
            assertTrue(e, referenceCache.containsKey(lsid));
            List<LSID> references = referenceCache.get(lsid);
            assertTrue(references.size() > 0);
            for (LSID referenceLSID : references)
            {
                assertNotNull(referenceLSID);
                klass = Objective.class;
                e = String.format(
                        "%s with LSID %s not found in container cache",
                        klass, referenceLSID);
                assertTrue(e, authoritativeLSIDExists(klass, referenceLSID));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testLightSourceSettingsIndexes()
    {
        Class<? extends IObject> klass = LightSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        for (IObjectContainer container : containers)
        {
            Map<String, Integer> indexes = container.indexes;
            Integer imageIndex = indexes.get(Index.IMAGE_INDEX.getValue());
            Integer channelIndex = indexes.get(Index.CHANNEL_INDEX.getValue());
            int imageCount = store.countCachedContainers(Image.class, null);
            String e = String.format("imageIndex %d >= imageCount %d",
                    imageIndex, imageCount);
            assertFalse(e, imageIndex >= imageCount);
            int logicalChannelCount = store.countCachedContainers(
                    LogicalChannel.class, imageIndex);
            e = String.format(
                    "channelIndex %d >= logicalChannelCount %d",
                    channelIndex, logicalChannelCount);
            assertFalse(e, channelIndex >= logicalChannelCount);
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testLightSourceSettingsLightSourceRef()
    {
        Class<? extends IObject> klass = LightSettings.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            String e = String.format(
                    "%s %s not found in reference cache", klass, container.LSID);
            assertTrue(e, referenceCache.containsKey(lsid));
            List<LSID> references = referenceCache.get(lsid);
            assertTrue(references.size() > 0);
            for (LSID referenceLSID : references)
            {
                assertNotNull(referenceLSID);
                klass = LightSource.class;
                e = String.format(
                        "%s with LSID %s not found in container cache",
                        klass, referenceLSID);
                assertTrue(e, authoritativeLSIDExists(klass, referenceLSID));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testLogicalChannelRefs()
    {
        Class<? extends IObject> klass = LogicalChannel.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            if (!referenceCache.containsKey(lsid))
            {
                continue;
            }
            List<LSID> references = referenceCache.get(lsid);
            assertTrue(references.size() > 0);
            for (LSID referenceLSID : references)
            {
                String asString = referenceLSID.toString();
                if (asString.endsWith(OMEROMetadataStoreClient.OMERO_EMISSION_FILTER_SUFFIX)
                    || asString.endsWith(OMEROMetadataStoreClient.OMERO_EXCITATION_FILTER_SUFFIX))
                {
                    int index = asString.lastIndexOf(':');
                    referenceLSID = new LSID(asString.substring(0, index));
                }
                assertNotNull(referenceLSID);
                List<Class<? extends IObject>> klasses =
                    new ArrayList<Class<? extends IObject>>();
                klasses.add(Filter.class);
                klasses.add(FilterSet.class);
                String e = String.format(
                        "LSID %s not found in container cache", referenceLSID);
                assertTrue(e, authoritativeLSIDExists(klasses, referenceLSID));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testInstrumentImageRef()
    {
        Class<? extends IObject> klass = Instrument.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            for (LSID target : referenceCache.keySet())
            {
                for (LSID reference : referenceCache.get(target))
                {
                    if (reference.equals(lsid))
                    {
                        return;
                    }
                }
            }
            fail(String.format(
                    "%s %s not referenced by any image.", klass, lsid));
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testFilterSetRefs()
    {
        Class<? extends IObject> klass = FilterSet.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            if (!referenceCache.containsKey(lsid))
            {
                continue;
            }
            List<LSID> references = referenceCache.get(lsid);
            assertTrue(references.size() > 0);
            for (LSID referenceLSID : references)
            {
                assertNotNull(referenceLSID);
                List<Class<? extends IObject>> klasses =
                    new ArrayList<Class<? extends IObject>>();
                klasses.add(Filter.class);
                klasses.add(Dichroic.class);
                String e = String.format(
                        "LSID %s not found in container cache", referenceLSID);
                assertTrue(e, authoritativeLSIDExists(klasses, referenceLSID));
            }
        }
    }

    @Test(dependsOnMethods={"testMetadataLevel"})
    public void testOTFIsReferenced()
    {
        Class<? extends IObject> klass = OTF.class;
        List<IObjectContainer> containers =
            store.getIObjectContainers(klass);
        referenceCache = store.getReferenceCache();
        for (IObjectContainer container : containers)
        {
            LSID lsid = new LSID(container.LSID);
            for (LSID target : referenceCache.keySet())
            {
                for (LSID reference : referenceCache.get(target))
                {
                    if (reference.equals(lsid))
                    {
                        return;
                    }
                }
            }
            fail(String.format(
                    "%s %s not referenced by any object.", klass, lsid));
        }
    }
}
