/*
 * org.openmicroscopy.shoola.agents.spots.ui.java2d.TrajectoryCanvas;
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
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventManager;
import org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel;
import org.openmicroscopy.shoola.agents.spots.ui.TrajectoriesPanel;

/** 
 * An abstract canvas for drawing of a trajectory 
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

public abstract class TrajectoryCanvas extends JPanel implements
	MouseListener {
	
	
	
	/**
	 * The layer for the canvas. 
	 */
	//protected PLayer layer;
	
	
	/**
	 *layer for the grid
	 *
	 */
	//protected PLayer gridLayer;
	
	
	protected GridDimensions dimensions;
	
	private double horizScale=1.0;
	private double vertScale = 1.0;

	// as list of all trajectories
	protected Vector trajectories = new Vector();


	
	private AxisBoundedRangeModel horiz;
	private AxisBoundedRangeModel vert;
	
	// the top-lvel panel that holds this canvas
	private TrajectoriesPanel panel;
	
	
	public TrajectoryCanvas(double horizMax,double vertMax) {
		super();
		setBackground(Color.WHITE);
		addMouseListener(this);
	}
	
	
	public void setBounds(int x,int y,int w,int h) {
		
		super.setBounds(x,y,w,h);
		if (dimensions != null)
			dimensions.setDimensions(w,h);
	}
	
	public void addTrajectories(SpotsTrajectorySet tSet) {
		Iterator iter = tSet.iterator();
		SpotsTrajectory t;
		
		while (iter.hasNext()) {
			t = (SpotsTrajectory) iter.next();
			Trajectory2D p = getTrajectoryRendering(t);
			trajectories.add(p);
		}
	}
	
	protected abstract Trajectory2D getTrajectoryRendering(SpotsTrajectory t);
	
	public void setModels(AxisBoundedRangeModel horiz,
			AxisBoundedRangeModel vert) {
		this.horiz = horiz;
		this.vert = vert;
	}
	
	public void setPanel(TrajectoriesPanel panel){
		this.panel = panel;
	}
	
	public void adjustScale(boolean adjusting) {
		dimensions.setExtents(horiz.getValue(),horiz.getMax(),
					vert.getValue(),vert.getMax());
		if (adjusting == true)
			repaint();
		else
			repaintSiblings();
	}
	
	public void repaintSiblings() {
		if (panel != null)
			panel.repaintCanvases();
	}
	
	public void paintComponent(Graphics g) {
		
	
		Graphics2D g2 = (Graphics2D) g;
		
		super.paintComponent(g);
		drawAxes(g2);
		Iterator iter = trajectories.iterator();
		Trajectory2D p;
		while (iter.hasNext()) {
			p = (Trajectory2D) iter.next();
			p.paint(g2,horiz,vert);
		}
	}
	
	private void drawAxes(Graphics2D g) {
		if (dimensions == null) 
			return;
		dimensions.drawAxes(g,horiz.getLowTickString(),
			horiz.getHighTickString(),
			vert.getLowTickString(),
			vert.getHighTickString());
	}
	

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		Trajectory2D p=null;
		
		
		Iterator iter = trajectories.iterator();
		while (iter.hasNext()) {
			p = (Trajectory2D) iter.next();
			if (p.isAt(x,y)) {
				return;
			}
		}
		// nothing was selected
		TrajectoryEventManager.clearSelection();
	
	}
	
	public void  mousePressed(MouseEvent e) {
	}
	
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}

	public int getTrajectoryCount() {
		return trajectories.size();
	}
	
	public int getVisibleCount() {
		Iterator iter = trajectories.iterator();
		Trajectory2D t2;
		SpotsTrajectory t;
		int n = 0;
		while (iter.hasNext()) {
			t2 = (Trajectory2D) iter.next();
			t = t2.getTrajectory();
			if (t.isVisible())
				n++;
		}
		return n;
	}
}