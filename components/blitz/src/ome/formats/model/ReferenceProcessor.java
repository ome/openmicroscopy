/*
 * ome.formats.model.ReferenceProcessor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ome.formats.Index;
import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.DetectorSettings;
import omero.model.LightPath;
import omero.model.LightSettings;
import omero.model.ObjectiveSettings;
import omero.model.WellSample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes the references of an IObjectContainerStore and ensures
 * that container references are consistent with the LSID stored in the
 * IObjectContainer itself. It also keeps track of all LSID references
 * in their string form so that the may be given to the IObjectContainerStore.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ReferenceProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Logger log = LoggerFactory.getLogger(ReferenceProcessor.class);

    /* (non-Javadoc)
     * @see ome.formats.model.ModelProcessor#process(ome.formats.model.IObjectContainerStore)
     */
    public void process(IObjectContainerStore store)
	throws ModelException
    {
        Map<String, String[]> referenceStringCache =
		new HashMap<String, String[]>();
	Map<LSID, List<LSID>> referenceCache = store.getReferenceCache();
	Map<LSID, IObjectContainer> containerCache = store.getContainerCache();
	try
        {
            for (LSID target : referenceCache.keySet())
            {
		IObjectContainer container = containerCache.get(target);
		Class targetClass = target.getJavaClass();
		List<String> references = new ArrayList<String>();
		for (LSID reference : referenceCache.get(target))
		{
			if (container == null)
			{
				// Handle the cases where a "Settings" object has been
				// used to link an element of the Instrument to the
				// Image but there were no acquisition specific
				// settings to record. Hence, the "Settings" object
				// needs to be created as no MetadataStore methods
				// pertaining to the "Settings" object have been
				// entered.
				LinkedHashMap<Index, Integer> indexes =
					new LinkedHashMap<Index, Integer>();
				int[] indexArray = target.getIndexes();
				if (targetClass == null)
				{
					log.warn("Unknown target class for LSID: " + target);
					references.add(reference.toString());
					continue;
				}
				else if (targetClass.equals(DetectorSettings.class))
				{
					indexes.put(Index.IMAGE_INDEX, indexArray[0]);
					indexes.put(Index.CHANNEL_INDEX, indexArray[1]);
				}
				else if (targetClass.equals(LightSettings.class))
				{
					if (indexArray.length == 2) {
						indexes.put(Index.IMAGE_INDEX, indexArray[0]);
						indexes.put(Index.CHANNEL_INDEX, indexArray[1]);
					}
					else if (indexArray.length == 3) {
						indexes.put(Index.EXPERIMENT_INDEX, indexArray[0]);
						indexes.put(Index.MICROBEAM_MANIPULATION_INDEX, indexArray[1]);
						indexes.put(Index.LIGHT_SOURCE_SETTINGS_INDEX, indexArray[2]);
					}
				}
				else if (targetClass.equals(ObjectiveSettings.class))
				{
					indexes.put(Index.IMAGE_INDEX, indexArray[0]);
				}
				else if (targetClass.equals(WellSample.class))
				{
					// A WellSample has been used to link an Image to
					// a Well and there was no acquisition specific
					// metadata to record about the WellSample. We now
					// need to create it.
					indexes.put(Index.PLATE_INDEX, indexArray[0]);
					indexes.put(Index.WELL_INDEX, indexArray[1]);
					indexes.put(Index.WELL_SAMPLE_INDEX, indexArray[2]);
				}
				else if (targetClass.equals(LightPath.class))
				{
				    // A LightPath has been used to link emission or
				    // excition filters and / or a dichroic to an
				    // image. We now need to create it.
				    indexes.put(Index.IMAGE_INDEX, indexArray[0]);
				    indexes.put(Index.CHANNEL_INDEX, indexArray[1]);
				}
				else
				{
					throw new RuntimeException(String.format(
							"Unable to synchronize reference %s --> %s",
							reference, target));
				}
				container =
					store.getIObjectContainer(targetClass, indexes);
			}

			// Add our LSIDs to the string based reference cache.
			references.add(reference.toString());
		}
		String lsid = targetClass == null? target.toString()
						: container.LSID;
		// We don't want to overwrite any existing references that may
		// have come from other LSID mappings (such as a generated
		// LSID) so add any existing LSIDs to the list of references.
		if (referenceStringCache.containsKey(lsid))
		{
			String[] existing = referenceStringCache.get(lsid);
			references.addAll(Arrays.asList(existing));
		}
			String[] referencesAsString =
				references.toArray(new String[references.size()]);
			referenceStringCache.put(lsid, referencesAsString);
            }
            store.setReferenceStringCache(referenceStringCache);
        }
	catch (Exception e)
	{
		throw new ModelException("Error processing references.", null, e);
	}
    }
}
