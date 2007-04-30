/*
 * roi.model.ROI 
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
import java.util.TreeMap;

//Third-party libraries
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
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
public class ROI
{
	final 	static 	int	DEFAULTMAPSIZE = 101;
	private long	id;
	
	TreeMap<Coord3D, ROIShape> 	roiShapes;
	AttachmentMap 				attachments;

	public ROI(long id)	
	{
		init(id);
	}
	
	public void addAttachment(AttachmentKey key, Attachment attachment)
	{
		attachments.addAttachment(key, attachment);
	}
	
	public Attachment getAttachment(AttachmentKey key)
	{
		return attachments.getAttachment(key);
	}
	
	public AttachmentMap getAttachmentMap()
	{
		return attachments;
	}
	
	public ROI(long id, Coord3D coord, ROIShape shape)
	{
		init(id);
		roiShapes.put(coord, shape);
	}
	
	private void init(long id)
	{
		this.id = id;
		roiShapes = new TreeMap<Coord3D, ROIShape>(new Coord3D());
	}
	
	public long getID()
	{
		return id;
	}
	
	public boolean containsKey(Coord3D coord)
	{
		return roiShapes.containsKey(coord);
	}
	
	public boolean containsKey(Coord3D start, Coord3D end)
	{
		for(int c = start.c; c < end.c ; c++)
			for(int t = start.t; t < end.t ; t++)
				for(int z = start.z; z < end.z ; z++)
					if(!roiShapes.containsKey(new Coord3D(c, t, z)))
						return false;
		return true;
	}
	
	public TreeMap<Coord3D, ROIShape> getShapes()
	{
		return roiShapes;
	}
	
	public ROIShape getShape(Coord3D coord) throws NoSuchShapeException
	{
		try 
		{
			return roiShapes.get(coord);
		}
		catch(Exception e)
		{
			throw new NoSuchShapeException(e);
		}
	}
	
	public Figure getFigure(Coord3D coord) throws NoSuchShapeException
	{
		return getShape(coord).getFigure();
	}
	
	public void addShape(ROIShape shape) 
												throws ROIShapeCreationException
	{
		if(roiShapes.containsKey(shape.getCoord3D()))
			throw new ROIShapeCreationException();
		roiShapes.put(shape.getCoord3D(), shape);
	}
	
	public void deleteShape(Coord3D coord) throws NoSuchShapeException
	{
		try
		{
			roiShapes.remove(coord);
		}
		catch(Exception e)
		{
			throw new NoSuchShapeException(e);
		}
	}
	
	public void deleteShape(Coord3D start, Coord3D end) throws NoSuchShapeException
	{
		for(int c = start.c; c < end.c ; c++)
			for(int t = start.t; t < end.t ; t++)
				for(int z = start.z; z < end.z ; z++)
					deleteShape(new Coord3D(c, t, z));
	}

	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, Coord3D end) 
												throws ROIShapeCreationException, 
													   NoSuchShapeException
	{
		ROIShape shape = getShape(selectedShape);
		for(int c = start.c; c < end.c ; c++)
			for(int t = start.t; t < end.t ; t++)
				for(int z = start.z; z < end.z ; z++)
				{
					Coord3D newCoord = new Coord3D(c, t, z);
					ROIShape newShape = new ROIShape(this, newCoord, shape);
					this.addShape(newShape);
				}
	}
	
}


