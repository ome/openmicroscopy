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

import ome.formats.Index;
import omero.metadatastore.IObjectContainer;
import omero.model.Ellipse;
import omero.model.IObject;
import omero.model.Line;
import omero.model.Mask;
import omero.model.Path;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rectangle;
import omero.model.Roi;
import omero.model.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger log = LoggerFactory.getLogger(ShapeProcessor.class);

    /** Exhaustive list of ROI types. */
    private static final List<Class<? extends IObject>> SHAPE_TYPES =
	new ArrayList<Class<? extends IObject>>();

    static
    {
        SHAPE_TYPES.add(Line.class);
        SHAPE_TYPES.add(Rectangle.class);
        SHAPE_TYPES.add(Mask.class);
        SHAPE_TYPES.add(Ellipse.class);
        SHAPE_TYPES.add(Point.class);
        SHAPE_TYPES.add(Polyline.class);
        SHAPE_TYPES.add(Path.class);
        SHAPE_TYPES.add(Label.class);
        // XXX: Unused OME-XML type
        SHAPE_TYPES.add(Polygon.class);
    }

    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
	for (Class<? extends IObject> klass : SHAPE_TYPES)
	{
	        List<IObjectContainer> containers =
			store.getIObjectContainers(klass);
	        for (IObjectContainer container : containers)
	        {
	            Integer roiIndex = container.indexes.get(Index.ROI_INDEX.getValue());
				LinkedHashMap<Index, Integer> indexes =
					new LinkedHashMap<Index, Integer>();
				indexes.put(Index.ROI_INDEX, roiIndex);
				// Creates an ROI if one doesn't exist
				store.getIObjectContainer(Roi.class, indexes);
	        }
	}
    }
}
