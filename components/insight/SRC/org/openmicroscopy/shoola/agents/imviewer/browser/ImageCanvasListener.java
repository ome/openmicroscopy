/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ImageCanvasListener 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the listeners added to an {@link GLImageCanvas}.
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

	/** The default point. */
	private static final Point DEFAULT_POINT = new Point(0, 0);
	
	/** Reference to the Model. */
    private BrowserModel model;
    
    /** Reference to the View. */
    private BrowserUI view;
    
    /** The canvas this listener is for. */
    private JComponent canvas;
    
    /** The image area. */
    private Rectangle area;
    
    /** The location of the mouse pressed. */
    private Point pressedPoint;
    
    /** 
     * Flag indicating if having the <code>Shift</code> and <code>Alt</code>
     * keys down is handled or not.
     */
    private boolean handleKeyDown;

    /** Flag indicating that the image was dragged.*/
    private boolean dragged;

    /**
     * Pans to the new location.
     * 
     * @param p The location of the mouse.
     * @param load Passed <code>true</code> to load the tile, 
     * <code>false</code> otherwise.
     */
    private void pan(Point p, boolean load)
    {
    	if (p == null) return;
		view.pan(p.x-pressedPoint.x, p.y-pressedPoint.y, load);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view		Reference to the View. Mustn't be <code>null</code>.
     * @param model		Reference to the Model. Mustn't be <code>null</code>.
     * @param canvas	Reference to the canvas this listener is for.
     * 					Mustn't be <code>null</code>.
     */
    ImageCanvasListener(BrowserUI view, BrowserModel model,
    		JComponent canvas)
    {
    	if (model == null) throw new NullPointerException("No Model.");
    	if (canvas == null) throw new NullPointerException("No canvas.");
    	if (view == null) throw new NullPointerException("No View.");
        this.model = model;
        this.canvas = canvas;
        this.view = view;
    	area = new Rectangle(0, 0, 0, 0);
		installListeners(true);
		handleKeyDown = false;
		pressedPoint = new Point(0, 0);
    }
    
    /**
     * Add or remove the <code>MouseListener</code> and a
     * <code>MouseMotionListener</code> depending on the specified value.
     * 
     * @param add Pass <code>true</code> to attach the listeners,
	 * <code>false</code> otherwise.
     */
    void installListeners(boolean add)
    {
    	if (add) {
            // prevent adding the listeners multiple times
            if (!Arrays.asList(canvas.getMouseListeners()).contains(this)) {
                canvas.addMouseListener(this);
                canvas.addMouseMotionListener(this);
                canvas.addMouseWheelListener(this);
            }
    	} else {
    		canvas.removeMouseListener(this);
    		canvas.removeMouseMotionListener(this);
    		canvas.removeMouseWheelListener(this);
    	}
    }
    
    /**
     * Sets to <code>true</code> if having the <code>Shift</code> and 
     * <code>Alt</code> keys down is handled, <code>false</code> otherwise.
     * 
     * @param handleKeyDown	The value to set.
     */
    void setHandleKeyDown(boolean handleKeyDown)
    { 
    	this.handleKeyDown = handleKeyDown; 
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
	 * Zooms in and out the image if the <code>Shift</code> key is down,
	 * pans if the <code>Alt</code> key is down.
	 * Selects a new z-section and time-point otherwise.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		if (handleKeyDown) {
			if (e.isShiftDown()) {
				if (model.isBigImage()) {
					pan(p, false);
					return;
				}
				if (p.y < pressedPoint.y) model.zoom(true);
				else if (p.y > pressedPoint.y) model.zoom(false);
				pressedPoint = p;
				return;
			} else if (e.isAltDown()) {
				pan(p, false);
				return;
			}
		}
		if (model.isBigImage()) { //panning
			dragged = true;
			pan(p, false);
			return;
		}
		int maxZ = model.getMaxZ();
		int maxT = model.getMaxT();
		if (maxZ <= 0 && maxT <= 0) return;
		int pressedZ = -1;
		int pressedT = -1;
		pressedZ = (p.y*maxZ)/area.height;
		if (pressedZ < 0) return;
		pressedZ = maxZ-pressedZ;
		if (pressedZ > maxZ) pressedZ = -1;
		pressedT = (p.x*maxT)/area.width;
		if (pressedT < 0) return;
		if (pressedT > maxT)  return;
		model.setSelectedXYPlane(pressedZ, pressedT);
		if (canvas instanceof BrowserBICanvas)
			((BrowserBICanvas) canvas).setPaintedString(pressedZ, pressedT);
	}

	/**
	 * Displays on the image the currently selected z-section and time-point.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		pressedPoint = e.getPoint();
		canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (canvas instanceof BrowserBICanvas)
			((BrowserBICanvas) canvas).setPaintedString(model.getDefaultZ(),
					model.getRealSelectedT());
	}
	
	/**
	 * Removes the painted value.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		canvas.setCursor(Cursor.getDefaultCursor());
		Point p = e.getPoint();
		if ((handleKeyDown && e.isAltDown()) || 
			(model.isBigImage() && dragged)) {
			pan(p, true);
		}
		dragged = false;
		pressedPoint = DEFAULT_POINT;
		SwingUtilities.convertPointToScreen(pressedPoint, canvas);
		if (canvas instanceof BrowserBICanvas)
			((BrowserBICanvas) canvas).setPaintedString(-1, -1);
	}
	
	/**
	 * Determines the value of the z-section.
	 * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.isAltDown() || e.isShiftDown() || e.isControlDown() ||
			model.isBigImage()) { //zooming
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				int v = e.getWheelRotation();
				model.zoom(v < 0); //zoom in and out.
			}
			return;
		}
		//change the z-section.
		int maxZ = model.getMaxZ();
		int maxT = model.getMaxT();
		if (maxZ <= 0 && maxT <= 0) return;
		boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= maxZ) {
                	model.setSelectedXYPlane(v, -1);
                	if (canvas instanceof BrowserBICanvas)
            			((BrowserBICanvas) canvas).setPaintedString(v,
            					model.getRealSelectedT());
                } else {
                	if (canvas instanceof BrowserBICanvas)
            			((BrowserBICanvas) canvas).setPaintedString(-1, -1);
                }
            } else { //moving down
                if (v >= 0) {
                	model.setSelectedXYPlane(v, -1);
                	if (canvas instanceof BrowserBICanvas)
            			((BrowserBICanvas) canvas).setPaintedString(v,
            					model.getDefaultT());
                } else {
                	if (canvas instanceof BrowserBICanvas)
            			((BrowserBICanvas) canvas).setPaintedString(-1, -1);
                }
            }
        } 
	}
	
	/**
	 * Required by the {@link MouseMotionListener} interface but no-operation 
	 * implementation in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Required by the {@link MouseMotionListener} interface but no-operation 
	 * implementation in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}
	
	/**
	 * Required by the {@link MouseMotionListener} interface but no-operation 
	 * implementation in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}

	/**
	 * Required by the {@link MouseMotionListener} interface but no-operation 
	 * implementation in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}
	
}
