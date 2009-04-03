/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.TrajShape
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.spots.ui.java3d;

//Java imports
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import com.sun.j3d.utils.geometry.Primitive;


//Third-party libraries

//Application-internal dependencies

/** 
 * Abstract superclasses for shapes used in 3D view 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public abstract class TrajShape extends TransformGroup {
	
	protected Shape3D shape = new Shape3D();
	
	public TrajShape() {
		super();
		setCapability(GeometryArray.ALLOW_INTERSECT);
		setCapability(Primitive.ENABLE_GEOMETRY_PICKING);
		setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		setCapability(TransformGroup.ALLOW_CHILDREN_READ);

		setCapability(TransformGroup.ALLOW_BOUNDS_READ);
		setCapability(Node.ALLOW_PICKABLE_READ);
		setCapability(Node.ALLOW_PICKABLE_WRITE);	

		setCapability(Node.ENABLE_PICK_REPORTING);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	}
	
	public abstract void adjustToFit(int axis,float low,float high);
	
	public abstract void setSelected(boolean v);
	
}