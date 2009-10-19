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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Generic set of unit tests, that are designed to be initiated on a 
 * Bio-Formats "ID" target, which sanity check metadata validity.
 *  
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class MetadataValidatorTest
{   
	/** Logger for this class */
	private static final Log log = 
		LogFactory.getLog(MetadataValidatorTest.class);
	
	/** Our testing Metadata store client */
	private OMEROMetadataStoreClient store;
	
	/** The OMERO basic wrapper for Bio-Formats readers */
	private OMEROWrapper wrapper;
	
	/** Our current container cache. */
    Map<LSID, IObjectContainer> containerCache;
    
    /** Our current reference cache. */
    Map<LSID, List<LSID>> referenceCache;
	
    @Parameters({ "target" })
	@BeforeTest
	public void setUp(String target) throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper = new OMEROWrapper(new ImportConfig());
        wrapper.setMetadataStore(store);
        store.setReader(wrapper);
        wrapper.setId(target);
        store.postProcess();
        containerCache = store.getContainerCache();
        referenceCache = store.getReferenceCache();
        log.debug("Starting container cache...");
        for (LSID key : containerCache.keySet())
        {
        	String s = String.format("%s == %s,%s", 
        			key, containerCache.get(key).sourceObject,
        			containerCache.get(key).LSID);
        	log.debug(s);
        }
        log.debug("Starting reference cache...");
        for (LSID key : referenceCache.keySet())
        {
        	for (LSID value : referenceCache.get(key))
        	{
        		String s = String.format("%s == %s", key, value);
        		log.debug(s);
        	}
        }
        log.debug("containerCache contains " + containerCache.size()
        		+ " entries.");
        log.debug("referenceCache contains " 
        		+ store.countCachedReferences(null, null)
        		+ " entries.");
	}
    
    @AfterTest
    public void tearDown() throws Exception
    {
    	wrapper.close();
    	store = null;
    	wrapper = null;
    	containerCache = null;
    	referenceCache = null;
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
	
	@Test
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
	
	@Test
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
	
	@Test
	public void testChannelCount()
	{
		List<IObjectContainer> containers = 
			store.getIObjectContainers(Pixels.class);
		for (IObjectContainer container : containers)
		{
			Pixels pixels = (Pixels) container.sourceObject;
			assertNotNull(pixels.getSizeC());
			int sizeC = pixels.getSizeC().getValue();
			Integer imageIndex = container.indexes.get("imageIndex");
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
						"logicalChannelIndex=%d", imageIndex, c);
				assertEquals(e, 1, count);
			}
		}
	}
	
	@Test
	public void testLogicalChannelCount()
	{
		List<IObjectContainer> containers = 
			store.getIObjectContainers(Pixels.class);
		for (IObjectContainer container : containers)
		{
			Pixels pixels = (Pixels) container.sourceObject;
			assertNotNull(pixels.getSizeC());
			int sizeC = pixels.getSizeC().getValue();
			Integer imageIndex = container.indexes.get("imageIndex");
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
						"logicalChannelIndex=%d", imageIndex, c);
				assertEquals(e, 1, count);
			}
		}
	}
	
	@Test
	public void testPlatesExist()
	{
		List<IObjectContainer> containers = 
			store.getIObjectContainers(WellSample.class);
		for (IObjectContainer container : containers)
		{
			Map<String, Integer> indexes = container.indexes;
			Integer plateIndex = indexes.get("plateIndex");
			String e = String.format(
					"Plate %d not found in container cache", plateIndex);
			assertTrue(e, 
					store.countCachedContainers(Plate.class, plateIndex) > 0);
		}
	}
	
	@Test
	public void testWellsExist()
	{
		List<IObjectContainer> containers = 
			store.getIObjectContainers(WellSample.class);
		for (IObjectContainer container : containers)
		{
			Map<String, Integer> indexes = container.indexes;
			Integer plateIndex = indexes.get("plateIndex");
			Integer wellIndex = indexes.get("wellIndex");
			String e = String.format(
					"Well %d not found in container cache", wellIndex);
			int count = store.countCachedContainers(Well.class, plateIndex, 
                    wellIndex);
			assertTrue(e, count == 1);
		}
	}
	
	@Test
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
	
	@Test
	public void testDetectorSettingsIndexes()
	{
		Class<? extends IObject> klass = DetectorSettings.class;
		List<IObjectContainer> containers = 
			store.getIObjectContainers(klass);
		for (IObjectContainer container : containers)
		{
			Map<String, Integer> indexes = container.indexes;
			Integer imageIndex = indexes.get("imageIndex");
			Integer logicalChannelIndex = 
				indexes.get("logicalChannelIndex");
			int imageCount = store.countCachedContainers(Image.class, null);
			String e = String.format("imageIndex %d >= imageCount %d", 
					imageIndex, imageCount);
			assertFalse(e, imageIndex >= imageCount);
			int logicalChannelCount = store.countCachedContainers(
					LogicalChannel.class, imageIndex);
			e = String.format(
					"logicalChannelIndex %d >= logicalChannelCount %d",
					logicalChannelIndex, logicalChannelCount);
			assertFalse(e, logicalChannelIndex >= logicalChannelCount);
		}
	}
	
	@Test
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
	
	@Test
	public void testObjectiveSettingsIndexes()
	{
		Class<? extends IObject> klass = ObjectiveSettings.class;
		List<IObjectContainer> containers = 
			store.getIObjectContainers(klass);
		for (IObjectContainer container : containers)
		{
			Map<String, Integer> indexes = container.indexes;
			Integer imageIndex = indexes.get("imageIndex");
			int imageCount = store.countCachedContainers(Image.class, null);
			String e = String.format("imageIndex %d >= imageCount %d", 
					imageIndex, imageCount);
			assertFalse(e, imageIndex >= imageCount);
		}
	}
	
	@Test
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
	
	@Test
	public void testLightSourceSettingsIndexes()
	{
		Class<? extends IObject> klass = LightSettings.class;
		List<IObjectContainer> containers = 
			store.getIObjectContainers(klass);
		for (IObjectContainer container : containers)
		{
			Map<String, Integer> indexes = container.indexes;
			Integer imageIndex = indexes.get("imageIndex");
			Integer logicalChannelIndex = 
				indexes.get("logicalChannelIndex");
			int imageCount = store.countCachedContainers(Image.class, null);
			String e = String.format("imageIndex %d >= imageCount %d", 
					imageIndex, imageCount);
			assertFalse(e, imageIndex >= imageCount);
			int logicalChannelCount = store.countCachedContainers(
					LogicalChannel.class, imageIndex);
			e = String.format(
					"logicalChannelIndex %d >= logicalChannelCount %d",
					logicalChannelIndex, logicalChannelCount);
			assertFalse(e, logicalChannelIndex >= logicalChannelCount);
		}
	}
	
	@Test
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
	
	@Test
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
				if (asString.endsWith("OMERO_EMISSION_FILTER")
					|| asString.endsWith("OMERO_EXCITATION_FILTER"))
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
	
	@Test
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
	
	@Test
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
	
	@Test
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
