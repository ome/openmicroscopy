/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ImageCanvasListener 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ImageCanvasListener
	implements MouseListener, MouseMotionListener, MouseWheelListener
{

	/** Reference to the Model. */
    private BrowserModel	model;
    
    /** The canvas this listener is for. */
    private ImageCanvas		canvas;
    
    /** 
     * Flag indicating that the mouse entered or not the area. Control
     * used to handle mouse wheel events.
     */
    private boolean			mouseOnCanvas;
    
    /** The image area. */
    private Rectangle		area;
    
    /**
     * Creates a new instance.
     * 
     * @param model		Reference to the Model. Mustn't be <code>null</code>.
     * @param canvas	Reference to the canvas this listener is for.
     * 					Mustn't be <code>null</code>.
     */
    ImageCanvasListener(BrowserModel model, ImageCanvas canvas)
    {
    	if (model == null) throw new NullPointerException("No model.");
    	if (canvas == null) throw new NullPointerException("No canvas.");
        this.model = model;
        this.canvas = canvas;
    	area = new Rectangle(0, 0, 0, 0);
    	canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
    }
    
    /**
     * Sets the size of the area to control.
     * 
     * @param width		The width of the {@link #area} rectangle.
     * @param height	The height of the {@link #area} rectangle.
     */
    void setAreaSize(int width, int height)
    {
    	area.setBounds(0, 0, width, height);
    }
    
    /**
	 * Selects a new z-section and timepoint.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		int pressedZ = -1;
		int pressedT = -1;
		int maxZ = model.getMaxZ();
		pressedZ = (p.y*maxZ)/area.height;
		if (pressedZ < 0) return;
		pressedZ = maxZ-pressedZ;
		if (pressedZ > model.getMaxZ()) pressedZ = -1;
		pressedT = (p.x*model.getMaxT())/area.width;
		if (pressedT < 0) return;
		if (pressedT > model.getMaxT())  return;
		model.setSelectedXYPlane(pressedZ, pressedT);
		canvas.setPaintedString(pressedZ, pressedT);
	}

	/**
	 * Displays on the image the currently selected z-section and timepoint.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		canvas.setPaintedString(model.getDefaultZ(), model.getDefaultT());
	}
	
	/**
	 * Removes the painted value.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		canvas.setPaintedString(-1, -1);
	}
	
	/**
	 * Determines the value of the z-section.
	 * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (!mouseOnCanvas) return;
		boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxZ()) {
                	model.setSelectedXYPlane(v,  -1);
                	canvas.setPaintedString(v,  model.getDefaultT());
                } else
                	canvas.setPaintedString(-1,  -1);
            } else { //moving down
                if (v >= 0) {
                	model.setSelectedXYPlane(v,  -1);
                	canvas.setPaintedString(v,  model.getDefaultT());
                } else
                	canvas.setPaintedString(-1,  -1);
            }
        } else {
     
        }
	}
	
	/**
	 * Sets the value of the {@link #mouseOnCanvas} flag.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) 
	{
		mouseOnCanvas =  area.contains(e.getPoint());
	}

	/**
	 * Sets the value of the {@link #mouseOnCanvas} flag to <code>false</code>.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) { mouseOnCanvas = false; }
	
	/**
	 * Required by the {@link MouseMotionListener} interface but no-op 
	 * implementation in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}

	/**
	 * Required by the {@link MouseMotionListener} interface but no-op 
	 * implementation in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

}
