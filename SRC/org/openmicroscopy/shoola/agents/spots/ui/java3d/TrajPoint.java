/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.TrajPoint
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
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;


/** 
 * A point in a trajectory 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */



public class TrajPoint extends TrajShape {
	
	public static final float BOX_SIDE=.02f;
	private SpotsTrajectoryEntry pt;
	
	private Transform3D translate;
	private Vector3d v= new Vector3d();
	
	private Extents extents;
	
	public TrajPoint(SpotsTrajectoryEntry pt,Appearance app,Extents extents) {
		super();
		this.pt = pt;
		this.extents = extents;
		getGeometry(BOX_SIDE);
		PickTool.setCapabilities((Node)shape,PickTool.INTERSECT_FULL);
		
		shape.setAppearance(app);
	 	// 	make a transform to translate it
		translate = new Transform3D();
		// will need to fix this to work with arbitrary scales (i,e.,
		//	where scale of dataset is != scale of vis.
		extents.calculateTranslation(v,pt);
		
		translate.setTranslation(v); 
	 	
		setTransform(translate);
		
		addChild(shape);  
	}
	
	public SpotsTrajectoryEntry getTrajectoryEntry() {
		return pt;
	}
	
	
	// builds cube centered on 0,0 with appropriate side length.
	public void getGeometry(float side) {
		CubeGeometry cube = new CubeGeometry(side);
		shape.setGeometry(cube);
	}
	public void adjustToFit(int axis,float low,float high) {
		extents.adjustPointToFit(axis,v,pt,low,high);

		translate.setTranslation(v); 
		setTransform(translate);
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
	
	public void drawShape(int s) {
		Appearance app = shape.getAppearance();
		PolygonAttributes ats = app.getPolygonAttributes();
		if (s == Spots3DCanvas.CUBE) 
			  ats.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		else
			 ats.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		app.setPolygonAttributes(ats);
		shape.setAppearance(app);
	}
}