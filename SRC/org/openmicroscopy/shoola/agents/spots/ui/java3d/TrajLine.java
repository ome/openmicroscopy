/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.TrajLine
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
import com.sun.j3d.utils.picking.PickTool;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Node;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;


/** 
 * The line that connects points in trajectories. 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */




public class TrajLine extends TrajShape {


	private LineGeometry line;
	
	public TrajLine(SpotsTrajectory t,Extents extents) {
		super();
		line = new LineGeometry(t,extents);
		shape.setGeometry(line);
		
		Appearance app = new Appearance();
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		ColoringAttributes ca = new ColoringAttributes();
		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		ca.setColor(TrajSwitch.POINT_COLOR);
		app.setColoringAttributes(ca);
		shape.setAppearance(app);
		PickTool.setCapabilities((Node) shape,PickTool.INTERSECT_FULL);
	
		addChild(shape);
	}
	
	public void adjustToFit(int axis,float low,float high) {
		line.adjustToFit(axis,low,high);
	}
	
	
	
	public void setSelected(boolean v) {
		Appearance app = shape.getAppearance();
		ColoringAttributes ca = app.getColoringAttributes();
		if (v == true)
			ca.setColor(TrajSwitch.HIGHLIGHT_COLOR);
		else
			ca.setColor(TrajSwitch.POINT_COLOR);
		app.setColoringAttributes(ca);
		shape.setAppearance(app);
	}
}