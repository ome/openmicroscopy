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

import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the Plates of an IObjectContainerStore and ensures
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class PlateProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(PlateProcessor.class);

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
    	List<Plate> plates = store.getSourceObjects(Plate.class);
    	for (Plate plate : plates)
    	{
    		if (plate.getColumnNamingConvention() == null)
    		{
    			plate.setColumnNamingConvention(rstring("1"));
    		}
    		if (plate.getRowNamingConvention() == null)
    		{
    			plate.setRowNamingConvention(rstring("A"));
    		}
    		if (plate.getWellOriginX() == null)
    		{
    			plate.setWellOriginX(rdouble(0.5));
    		}
    		if (plate.getWellOriginY() == null)
    		{
    			plate.setWellOriginY(rdouble(0.5));
    		}
    		if (plate.getDefaultSample() == null)
    		{
    			plate.setDefaultSample(rint(0));
    		}
    	}
    }
}
