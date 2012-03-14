/*
 * ome.formats.model.PlaneInfoProcessor
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

import java.util.List;

import ome.formats.Index;
import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Plane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the plane info sets of an IObjectContainerStore and removes
 * entities of this rapidly exploding object that have no metadata populated.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class PlaneInfoProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(PlaneInfoProcessor.class);

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
        List<IObjectContainer> containers =
            store.getIObjectContainers(Plane.class);
        for (IObjectContainer container : containers)
        {
		Plane pi = (Plane) container.sourceObject;
		if (pi.getDeltaT() == null
			&& pi.getExposureTime() == null
			&& pi.getPositionX() == null
			&& pi.getPositionY() == null
			&& pi.getPositionZ() == null)
		{
			LSID lsid = new LSID(
			        Plane.class,
			        container.indexes.get(Index.IMAGE_INDEX.getValue()),
			        container.indexes.get(Index.PLANE_INDEX.getValue()));
			log.debug("Removing empty PlaneInfo: " + lsid);
			store.removeIObjectContainer(lsid);
		}
        }
    }
}
