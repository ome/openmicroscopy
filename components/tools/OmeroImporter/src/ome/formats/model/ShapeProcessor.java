/*
 * ome.formats.model.ShapeProcessor
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
import java.util.LinkedHashMap;
import java.util.List;

import omero.metadatastore.IObjectContainer;
import omero.model.Ellipse;
import omero.model.IObject;
import omero.model.Line;
import omero.model.Mask;
import omero.model.Path;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the shapes of a IObjectContainerStore and ensures
 * that the ROI containers are present.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ShapeProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(ShapeProcessor.class);
    
    /** Exhaustive list of ROI types. */
    private static List<Class<? extends IObject>> roiClasses = 
    	new ArrayList<Class<? extends IObject>>(); 
    
    static 
    {
    	roiClasses.add(Text.class);
    	roiClasses.add(Rect.class);
    	roiClasses.add(Ellipse.class);
    	roiClasses.add(Mask.class);
    	roiClasses.add(Point.class);
    	roiClasses.add(Path.class);
    	roiClasses.add(Polygon.class);
    	roiClasses.add(Polyline.class);
    	roiClasses.add(Line.class);
    }

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
    	for (Class<? extends IObject> klass : roiClasses)
    	{
	        List<IObjectContainer> containers =
	        	store.getIObjectContainers(klass);
	        for (IObjectContainer container : containers)
	        {
	            Integer imageIndex = container.indexes.get("imageIndex");
	            Integer roiIndex = container.indexes.get("roiIndex");
				LinkedHashMap<String, Integer> indexes = 
					new LinkedHashMap<String, Integer>();
				indexes.put("imageIndex", imageIndex);
				indexes.put("roiIndex", roiIndex);
				// Creates an ROI if one doesn't exist
				store.getIObjectContainer(Roi.class, indexes);
	        }
    	}
    }
}
