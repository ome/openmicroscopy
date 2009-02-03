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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.formats.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.DetectorSettings;
import omero.model.LightSettings;
import omero.model.ObjectiveSettings;

/**
 * Processes the references of an IObjectContainerStore and ensures
 * that containers are consistent. It also keeps track of all LSID references
 * in their string form so that the may be sent to the server.
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ReferenceProcessor implements ModelProcessor
{
	/** LSID reference map in string format. */
	private Map<String, String> referenceStringCache = 
		new HashMap<String, String>();
    	
    /* (non-Javadoc)
     * @see ome.formats.model.ModelProcessor#process(ome.formats.model.IObjectContainerStore)
     */
    public void process(IObjectContainerStore store)
    	throws ModelException
    {
    	Map<LSID, LSID> referenceCache = store.getReferenceCache();
    	Map<LSID, IObjectContainer> containerCache = store.getContainerCache();
    	try
        {
            for (LSID target : referenceCache.keySet())
            {
                LSID reference = referenceCache.get(target);
                IObjectContainer container = containerCache.get(target);
                if (container == null)
                {
                    Class targetClass = target.getJavaClass();
                    LinkedHashMap<String, Integer> indexes = 
                        new LinkedHashMap<String, Integer>();
                    int[] indexArray = target.getIndexes();
                    if (targetClass.equals(DetectorSettings.class))
                    {
                        indexes.put("imageIndex", indexArray[0]);
                        indexes.put("logicalChannelIndex", indexArray[1]);
                    }
                    else if (targetClass.equals(LightSettings.class))
                    {
                        indexes.put("imageIndex", indexArray[0]);
                        indexes.put("logicalChannelIndex", indexArray[1]);
                    }
                    else if (targetClass.equals(ObjectiveSettings.class))
                    {
                        indexes.put("imageIndex", indexArray[0]);
                    }
                    else
                    {
                        throw new RuntimeException(String.format(
                                "Unable to synchronize reference %s --> %s",
                                reference, target));
                    }
                    container = store.getIObjectContainer(targetClass, indexes);
                }
                referenceStringCache.put(container.LSID, reference.toString());
            }
            store.setReferenceStringCache(referenceStringCache);
        }
    	catch (Exception e)
    	{
    		throw new ModelException("Error processing references.", null, e);
    	}
    }
}
