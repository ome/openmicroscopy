/*
 * org.openmicroscopy.shoola.agents.viewer3D.canvas.DrawingCanvasMng
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas;



//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DrawingCanvasMng
	implements MouseListener, MouseMotionListener
{
	
	private Viewer3DManager		control;
	private DrawingCanvas		view;
	
	private	Rectangle			drawingAreaXY, drawingAreaXZ, drawingAreaZY;
	
	/** Dragging control. */
	private boolean         	dragging;
	
	public DrawingCanvasMng(DrawingCanvas view, Viewer3DManager control)
	{
		this.view = view;
		this.control = control;
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		drawingAreaXY = new Rectangle();	
		drawingAreaXZ = new Rectangle();
		drawingAreaZY = new Rectangle();
	}

	public void setDrawingAreaXY(int x, int y, int w, int h)
	{
		drawingAreaXY.setBounds(x, y, w, h);
	}
	
	public void setDrawingAreaXZ(int x, int y, int w, int h)
	{
		drawingAreaXZ.setBounds(x, y, w, h);
	}
	
	public void setDrawingAreaZY(int x, int y, int w, int h)
	{
		drawingAreaZY.setBounds(x, y, w, h);
	}
	
	/** Draw the lines and update the XZimage and ZYimage. */ 
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (!dragging) {
			dragging = true;
			if (drawingAreaXY.contains(p)) drawXY(p);
			//if (drawingAreaXZ.contains(p)) view.drawXZ(p);
			//if (drawingAreaZY.contains(p)) view.drawZY(p);
		}
	}

	/** Draw the lines and update the XZimage and ZY image. */
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		if (dragging) {
			if (drawingAreaXY.contains(p)) drawXY(p);
			//if (drawingAreaXZ.contains(p)) view.drawXZ(p);
			//if (drawingAreaZY.contains(p)) view.drawZY(p);	
		}
	}

	private void drawXY(Point p)
	{
		control.onPlaneSelected(p.x, p.y);
		view.drawXY(p);
	}
	
	/** 
	 * Set the dragging control to <code>false</code> 
	 * and erase the shape drawn on each canvas.
	 */
	public void mouseReleased(MouseEvent e)
	{
		dragging = false;
		view.erase();
	}

	/** 
	 * Required by I/F but not actually needed in our case, 
	 * no op implementation.
	 */   
	public void mouseClicked(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, 
	 * no op implementation.
	 */   
	public void mouseEntered(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case,
	 * no op implementation.
	 */   
	public void mouseExited(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case,
	 * no op implementation.
	 */   
	public void mouseMoved(MouseEvent e) {}

}
