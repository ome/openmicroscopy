/*
 * org.openmicroscopy.shoola.agents.spots.ui.java2d.Trajectory2D;
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

package org.openmicroscopy.shoola.agents.spots.ui.java2d;

//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Shape;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEvent;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventListener;
import org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel;
import org.openmicroscopy.shoola.agents.spots.ui.Constants;

/** 
 * Abstract 2D trajectory 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */



public abstract class Trajectory2D implements TrajectoryEventListener  { 
	
	private static final Color TRAJ_COLOR =  new 
		Color(Constants.POINT_RED,Constants.POINT_GREEN,Constants.POINT_BLUE);
	private static final Color HIGHLIGHT_COLOR= 
		new Color(Constants.HIGHLIGHT_RED,Constants.HIGHLIGHT_GREEN,
			Constants.HIGHLIGHT_BLUE);
	private static final int END_RADIUS=2;
	private static final int END_SIDE=4;
	private static final int ARROW_SIZE=3;
	private static final int PICK_RANGE=10;
	
	
	
	protected SpotsTrajectory trajectory;
	
	private Color currentColor=TRAJ_COLOR;
	
	private Shape[] shapes;

	
	private BasicStroke stroke = new BasicStroke(1,BasicStroke.CAP_BUTT,
		BasicStroke.JOIN_BEVEL);
		
	private boolean visible=true;
	
	
	private GridDimensions dimensions;
	private TrajectoryCanvas canvas;
	
	
	//private Shape shapes[];

	private Rectangle2D boundsRect;
	public Trajectory2D(TrajectoryCanvas canvas,SpotsTrajectory trajectory) {
		super();
		this.trajectory =trajectory;
		this.canvas = canvas;
		trajectory.getManager().addTrajectoryEventListener(this);
		shapes = new Shape[trajectory.getLength()];
		
	}
	
	
	
	
	public void setHighlighted(boolean v) {
		if (v == true) {
			currentColor = HIGHLIGHT_COLOR;
		}
		else {
			currentColor = TRAJ_COLOR;
		}
	}
	
	public void setDimensions(GridDimensions dimensions) {
		
		this.dimensions = dimensions;
		
	}
	
	
	
	public abstract float getHorizCoord(SpotsTrajectoryEntry pt, GridDimensions dimensions);
	
	public abstract float getVertCoord(SpotsTrajectoryEntry pt,GridDimensions dimensions);
	

	public void trajectoryChanged(TrajectoryEvent e) {
		
		boolean v = e.getStatus();
		int type = e.getType();
		if (e.getSource() == this) 
			return;
		if (type == TrajectoryEvent.SELECT) {
			setHighlighted(v);
			canvas.repaint();
		}
		else if (type == TrajectoryEvent.VISIBLE)  
			setVisible(v);
		
	}
	
	private void setVisible(boolean v) {
		visible =v;
	}
	
	
	/**
	 * @return
	 */
	public SpotsTrajectory getTrajectory() {
		return trajectory;
	}
	
	
	
	public void paint(Graphics2D g,AxisBoundedRangeModel horiz,
				AxisBoundedRangeModel vert) {
		if (visible == true && isInRange(horiz,vert)) {
			doPaint(g);
			
		}
	}
	
	private void doPaint(Graphics2D g) {
		SpotsTrajectoryEntry pt = trajectory.getPoint(0);
		float x = getHorizCoord(pt,dimensions);
		float y = getVertCoord(pt,dimensions);
		
		float oldx=x;
		float oldy = y;
		g.setColor(currentColor);
		 
		Rectangle2D.Float bulb = new Rectangle2D.Float(x-END_RADIUS,y-END_RADIUS,
			END_SIDE,END_SIDE);
		boundsRect = new Rectangle2D.Float(x,y,PICK_RANGE,PICK_RANGE);
		shapes[0] = bulb;
		g.fill(bulb);
		Rectangle2D rect;
		
		int len = trajectory.getLength();
		for (int i = 1; i < len; i++) {
			pt = trajectory.getPoint(i);
			x = getHorizCoord(pt,dimensions);
			y = getVertCoord(pt,dimensions);
			g.drawLine((int) oldx,(int)oldy,(int) x,(int)y);

			rect = new Rectangle2D.Float(x-END_RADIUS,y-END_RADIUS,END_SIDE,END_SIDE);
			boundsRect.add(rect);
			shapes[i] = rect;
			g.fill(rect);
			oldx =x;
			oldy=y;
		}
	}
	
	private boolean isInRange(AxisBoundedRangeModel horiz,
			AxisBoundedRangeModel vert) {
		if (horiz == null || vert == null)
			return false;
		int horizMin = horiz.getValue();
		int horizMax = horiz.getValue()+horiz.getExtent();
		if (getHorizMin() < horizMin || getHorizMax() > horizMax)
			return false;
		
		int vertMin = vert.getValue();
		int vertMax = vert.getValue()+vert.getExtent();
		
		if (getVertMin() < vertMin || getVertMax() > vertMin)
			return true;
		
		return true;
	}
	
	protected abstract double getVertMin();
	protected abstract double getVertMax();
	
	protected abstract double getHorizMin();
	protected abstract double getHorizMax();
	
	public boolean isAt(int x,int y) {
		
	//	if (boundsRect == null)
	//		return false;
		if (trajectory.isVisible() == false)
			return false;
		//System.err.println("checking.."+x+","+y+", bounds "+boundsRect);
	//	boolean res = boundsRect.intersects(x,y,PICK_RANGE,PICK_RANGE);
		boolean res = intersects(x,y);
		//System.err.println("result is "+res);
		if (res == true)
			trajectory.setSelected(res);
		
		return res;
	}
	
	private boolean intersects(int x,int y) {
		double left= x-PICK_RANGE/2;
		double top = y -PICK_RANGE/2;
		for (int i =0; i < shapes.length; i++) {
			if (shapes[i].intersects(left,top,PICK_RANGE,PICK_RANGE))
				return true;
		}
		return false;
	}
	
}