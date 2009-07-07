/*
 * ome.formats.model.WellProcessor
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

import static omero.rtypes.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import omero.metadatastore.IObjectContainer;
import omero.model.Plate;
import omero.model.Well;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the Wells of an IObjectContainerStore and ensures that the Plate 
 * has been populated. It also ensures that the Plate has a name.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class WellProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(WellProcessor.class);

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
        List<IObjectContainer> containers = 
            store.getIObjectContainers(Well.class);
        Set<Integer> plateIndexes = new HashSet<Integer>();
        for (IObjectContainer container : containers)
        {
        	Integer plateIndex = container.indexes.get("plateIndex");
        	plateIndexes.add(plateIndex);
        }
        for (Integer plateIndex : plateIndexes)
        {
        	LinkedHashMap<String, Integer> indexes = 
        		new LinkedHashMap<String, Integer>();
        	indexes.put("plateIndex", plateIndex);
        	IObjectContainer container = 
        		store.getIObjectContainer(Plate.class, indexes);
        	Plate plate = (Plate) container.sourceObject;
        	if (plate.getName() == null)
        	{
        		log.warn("Missing plate name for: " + container.LSID);
        		plate.setName(rstring("Plate"));
        	}
        }
    }
}
