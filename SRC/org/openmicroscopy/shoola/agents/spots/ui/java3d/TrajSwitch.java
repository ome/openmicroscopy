/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.TrajSwitch
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
import com.sun.j3d.utils.geometry.Primitive;
import java.util.Enumeration;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Switch;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEvent;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventListener;
import org.openmicroscopy.shoola.agents.spots.ui.Constants;

/** 
 * Switch that determines whether or not a trajectory is displayed 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */




public class TrajSwitch extends Switch implements TrajectoryEventListener {
	
	private static int AXIS_COUNT =3;
	public static final Color3f POINT_COLOR=new Color3f(
		Constants.POINT_RED,Constants.POINT_GREEN,Constants.POINT_BLUE);
	
	public static final Color3f HIGHLIGHT_COLOR=new Color3f(
			Constants.HIGHLIGHT_RED,Constants.HIGHLIGHT_GREEN,
			Constants.HIGHLIGHT_BLUE);
	
	private SpotsTrajectory traj;
	private Transform3D translate;
	private TransformGroup tg;
	
	private SpotsTrajectorySet tSet;
	private Extents extents;
	
	public TrajSwitch(SpotsTrajectory t,SpotsTrajectorySet tSet,Extents extents) {
		super();
		this.traj = t;	
		this.tSet = tSet;
		this.extents = extents;
		
		traj.getManager().addTrajectoryEventListener(this);
		
		Appearance appearance = new Appearance();
		appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
     	
     	PolygonAttributes polygonAttributes = new PolygonAttributes();
     	polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_READ);
     	polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_FILL);
        appearance.setPolygonAttributes( polygonAttributes );
        ColoringAttributes ca = new ColoringAttributes();
        ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        ca.setColor(POINT_COLOR);
        appearance.setColoringAttributes(ca);
        
        
        buildBoxes(t,appearance);
        setCapability(Group.ALLOW_CHILDREN_EXTEND);
        setCapability(Group.ALLOW_CHILDREN_READ);
        setCapability(Group.ALLOW_CHILDREN_WRITE);
        setCapability(Switch.ALLOW_SWITCH_WRITE);
        setCapability(Node.ENABLE_PICK_REPORTING);
		setCapability(TransformGroup.ALLOW_BOUNDS_READ);
		setCapability(Node.ALLOW_PICKABLE_READ);
		setCapability(Node.ALLOW_PICKABLE_WRITE);	
	    setCapability(GeometryArray.ALLOW_INTERSECT);
	    setCapability(Primitive.ENABLE_GEOMETRY_PICKING);
	
        
      //  setWhichChild(0);
        setWhichChild(Switch.CHILD_ALL);
	}
	

	
	private void buildBoxes(SpotsTrajectory t,Appearance app) {
		for (int i = 0; i < t.getLength(); i++) {
			SpotsTrajectoryEntry  p = t.getPoint(i);
		//	System.err.println("adding box at "+p.getX()+","+p.getY()+","+p.getZ());
 		
			// 	put coords into an array
			addChild(getBlock(p,app));
		}
		if (t.getLength() >1)
			addChild(new TrajLine(t,extents));
 	// add the line array.
	}
 
	private Node getBlock(SpotsTrajectoryEntry p,Appearance app) {
 	
		// make a box
		TrajPoint box = new TrajPoint(p,app,extents);
		return box;
	
		
	}
	
	// revise to do something for all children..
	
	public void adjustToFit(int axis,float low,float high) {
		boolean curVisible =  
			(low <= traj.getMin(axis) && high >= traj.getMax(axis));
		if (curVisible) {
			Enumeration enum= getAllChildren();
			TrajShape ts;
			while (enum.hasMoreElements()) {
				Object obj = enum.nextElement();
				if (obj instanceof TrajShape) {
					ts = (TrajShape) obj;
					ts.adjustToFit(axis,low,high);
				}
			}
		}
	}
	

	public void setVisible(boolean v) {
		if (v == true ) {
			setWhichChild(Switch.CHILD_ALL);
		}
		else {
			setWhichChild(Switch.CHILD_NONE);
		}
	}

	public  void setHighlighted(boolean v) { 
		//System.err.println("highlighting ..."+this +", "+v);
		Enumeration enum = getAllChildren();
		TrajShape ts;
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof TrajShape) {
				ts = (TrajShape) obj;
				ts.setSelected(v);
			}
		}
	}
	
	public void setSelected(boolean v) {
		traj.setSelected(v);
	}
		
	public void trajectoryChanged(TrajectoryEvent e) {
		boolean v = e.getStatus();
		int type = e.getType();
		if (e.getSource() == this) 
			return;
		if (type == TrajectoryEvent.SELECT) {
			setHighlighted(v);
		}
		else if (type == TrajectoryEvent.SCALE) 
			setScaled(v);
		else if (type == TrajectoryEvent.VISIBLE)  
			setVisible(v);
		else
			System.err.println("traj switch - unknown event type..");
	}
	
	public void drawShape(int shape) {
		Enumeration enum = getAllChildren();
		TrajPoint tp;
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof TrajPoint) {
				tp = (TrajPoint) obj;
				tp.drawShape(shape);
			}
		}
	}
	
	private void setScaled(boolean v) {

		Enumeration enum = getAllChildren();
		TrajPoint tp;
		
		float size = TrajPoint.BOX_SIDE;
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof TrajPoint) {
				tp = (TrajPoint) obj;

				if (v == true) {
					SpotsTrajectoryEntry ste = tp.getTrajectoryEntry();
					size = (float) (ste.getLogSize()/tSet.getLogMaxSize())*
							TrajPoint.BOX_SIDE;
				}
				tp.getGeometry(size);
			}
		}
	}
}