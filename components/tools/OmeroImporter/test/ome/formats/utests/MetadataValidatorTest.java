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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.DetectorSettings;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.LightSettings;
import omero.model.LogicalChannel;
import omero.model.ObjectiveSettings;
import omero.model.OTF;

/**
 * Generic set of unit tests, that are designed to be initiated on a 
 * Bio-Formats "ID" target, which sanity check metadata validity. For the most
 * part this "sanity checking" is isolated to reference validation.
 *  
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class MetadataValidatorTest
{   
	/** Usage name. */
    private static final String APP_NAME = "metadata-validator";
    
    /** Our target file. */
    private static String TARGET = null;

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
	
	@BeforeTest
	public void setUp() throws Exception
	{
		assertNotNull("Metadata validator must be initialized with a TARGET " +
				      "via main()", TARGET);
		ServiceFactoryPrx sf = new TestServiceFactory();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper = new OMEROWrapper();
        wrapper.setMetadataStore(store);
        wrapper.setId(TARGET);
        store.postProcess();
        containerCache = store.getContainerCache();
        referenceCache = store.getReferenceCache();
        System.err.println("Starting container cache...");
        for (LSID key : containerCache.keySet())
        {
        	String s = String.format("%s == %s,%s", 
        			key, containerCache.get(key).sourceObject,
        			containerCache.get(key).LSID);
        	System.err.println(s);
        }
        System.err.println("Starting reference cache...");
        for (LSID key : referenceCache.keySet())
        {
        	for (LSID value : referenceCache.get(key))
        	{
        		String s = String.format("%s == %s", key, value);
        		System.err.println(s);
        	}
        }
        System.err.println("containerCache contains " + containerCache.size()
        		+ " entries.");
        System.err.println("referenceCache contains " 
        		+ store.countCachedReferences(null, null)
        		+ " entries.");
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
					"DetectorSettings %s missing from reference cache",
					container.LSID);
			assertTrue(e, referenceCache.containsKey(lsid));
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
					"ObjectiveSettings %s missing from reference cache",
					container.LSID);
			assertTrue(e, referenceCache.containsKey(lsid));
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
					"LightSourceSettings %s missing from reference cache",
					container.LSID);
			assertTrue(e, referenceCache.containsKey(lsid));
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
					"Instrument %s not referenced by any image.", lsid));
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
					"OTF %s not referenced by any object.", lsid));
		}
	}
	
    /**
     * Prints usage so we don't have to.
     */
    public static void usage()
    {
        System.err.println(String.format(
                "Usage: %s <target>", APP_NAME));
        System.exit(1);
    }
    
    /**
     * Command line application entry point which parses CLI arguments and
     * passes them into the test case. Return codes are:
     * <ul>
     *   <li>0 on success</li>
     *   <li>1 on argument parsing failure</li>
     *   <li>2 on exception</li>
     * </ul>
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws Throwable
    {
    	if (args.length != 1)
    	{
    		usage();
    	}
    	TARGET = args[0];
    	log.info("Testing metadata of: " + TARGET);
    	TestNG.main(new String[] { 
    			"-testclass", "ome.formats.utests.MetadataValidatorTest" });
    }
}
