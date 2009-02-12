/*
 * ome.formats.model.ChannelProcessor
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

import java.io.File;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

import loci.formats.IFormatReader;

import ome.formats.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Arc;
import omero.model.Detector;
import omero.model.Filament;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the pixels sets of an IObjectContainerStore and ensures
 * that LogicalChannel containers are present in the container cache, adding
 * them if they are missing.
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class InstrumentProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(InstrumentProcessor.class);

    /** Container store we're currently working with. */
    private IObjectContainerStore store;

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
        this.store = store;

        List<IObjectContainer> containers = 
            store.getIObjectContainers(Detector.class);
        containers.addAll(store.getIObjectContainers(Objective.class));
        containers.addAll(store.getIObjectContainers(OTF.class));
        containers.addAll(store.getIObjectContainers(Arc.class));
        containers.addAll(store.getIObjectContainers(Laser.class));
        containers.addAll(store.getIObjectContainers(Filament.class));
        
        for (IObjectContainer container : containers)
        {
            Integer instrumentIndex = container.indexes.get("instrumentIndex");
            Instrument instrument = (Instrument) store.getSourceObject(new LSID(Instrument.class, instrumentIndex));

            // If image is missing
            if (instrument == null)
            {
                LinkedHashMap<String, Integer> indexes = 
                    new LinkedHashMap<String, Integer>();
                indexes.put("instrumentIndex", instrumentIndex);
                container = store.getIObjectContainer(Instrument.class, indexes);
                instrument = (Instrument) container.sourceObject;
            }
        }
     }
}
