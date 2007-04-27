/*
 * roi.model.ROIShape 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui.roi.model;

//Java imports
import java.awt.geom.Rectangle2D;

//Third-party libraries
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIShape 
{
	private ROI			parent;
	private	Coord3D		coord;
	private Rectangle2D boundingBox;
	
	private Figure		figure;
	
	public ROIShape(ROI parent, Coord3D coord, ROIShape shape)
	{
		this.parent = parent;
		this.coord = coord;
		this.boundingBox = (Rectangle2D) shape.getBoundingBox().clone();
		this.figure = (Figure) shape.getFigure().clone();
	}
	
	public ROIShape(ROI parent, Coord3D coord, Figure figure, Rectangle2D boundingBox)
	{
		this.parent = parent;
		this.coord = coord;
		this.figure = figure;
		this.boundingBox = boundingBox;
	}
	
	public long getID()
	{
		return parent.getID();
	}
	
	public Coord3D getCoord3D()
	{
		return coord;
	}
	
	public Rectangle2D getBoundingBox()
	{
		return boundingBox;
	}
	
	public Figure getFigure()
	{
		return figure;
	}
	
	public ROI getROI()
	{
		return parent;
	}
	
}


