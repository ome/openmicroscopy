/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorPaletteManager
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

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
class ColorPaletteManager
	implements MouseListener, MouseMotionListener
{
	
	private static final int 			leftBorder = ColorPalette.leftBorder;
	private static final int 			topBorder = ColorPalette.topBorder;
	private static final int 			widthBar = ColorPalette.WIDTH_BAR;
	private static final int			heightBar = ColorPalette.HEIGHT_BAR;
	private static final int			triangleH = ColorPalette.triangleH;
	private static final int			heightPanel = ColorPalette.HEIGHT_PANEL;
	
	/** HSB component of the selected color. */
	private float       				hue, saturation, brightness;
	
	/** Dragging control. */
	private boolean 					dragging;
	
	/** Rectangle used to control the mouse events. */
	private Rectangle 					boxB, boxH, boxS;
	
	/** Reference to the view. */
	private ColorPalette				view;
	private ColorChooserManager 		ccManager;
	
	ColorPaletteManager(ColorPalette view, ColorChooserManager ccManager) 
	{
		this.view = view;
		this.ccManager = ccManager;
		initBoxes();
	}
	
	/** 
	 * Initializes the hue, saturation and brightness components.
	 * 
	 * @param h		hue, value in range the [0, 1].
	 * @param s 	saturation, value in range the [0, 1].
	 * @param b		brightness, value in the range [0, 1].
	 * 
	 */
	void setHSB(float h, float s, float b)
	{
		hue = h;
		saturation = s;
		brightness = b;
	}
	
	/** 
	 * Initializes the rectangles used to control the knobs.
	 *
	 */
	void initBoxes()
	{
		boxH = new Rectangle(leftBorder, topBorder, widthBar, 
					heightBar+triangleH);
		boxS = new Rectangle(leftBorder, topBorder+heightPanel, widthBar, 
					heightBar+triangleH);
		boxB = new Rectangle(leftBorder, topBorder+2*heightPanel, widthBar, 
					heightBar+triangleH);	
	}
	
	/** Attach the listeners. */
	void attachListeners()
	{
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
	}
	
	/** Handles event fired by the mouse pressed. */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (!dragging) {
			dragging = true;
			if (boxH.contains(p)) updateHue(p.x);
			else if (boxS.contains(p)) updateSaturation(p.x);
			else if (boxB.contains(p)) updateBrightness(p.x);
		 }  //else dragging already in progress 
	}
	
	/** Handles event fired by the mouse dragged. */
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		if (dragging) {
			if (boxH.contains(p)) updateHue(p.x);
		    else if (boxS.contains(p)) updateSaturation(p.x);
			else if (boxB.contains(p)) updateBrightness(p.x);
		}
	}
	
	/** Update the hue. */
	void updateHue(int x)
	{
		setHue(x-leftBorder);
		view.getBarH().setLineLocation(x);
		view.repaint();
	}
	
	/** Update the saturation. */
	void updateSaturation(int x)
	{
		setSaturation(x-leftBorder);
		view.getBarS().setLineLocation(x);
		view.repaint();
	}
	
	/** Update the brightness. */
	void updateBrightness(int x)
	{
		setBrightness(x-leftBorder);
		view.getBarB().setLineLocation(x);
		view.repaint();
	}
	
	/**
	 * Compute the hue value, create the colors and update the color bars
	 * accordingly.
	 * 
	 * @param x		x-coordinate value in the range [0, widthBar]
	 */
	private void setHue(int x)
	{
		hue = (float) x/widthBar;
		//cD: 	color selected, c: colorBar.
		Color cD = Color.getHSBColor(hue, saturation, brightness);
		Color c = Color.getHSBColor(hue, 1f, 1f);
		view.getBarS().setColor(c);
		view.getBarB().setColor(c);
		ccManager.setColorPanel(cD);
	}
	
	/**
	 * Compute the saturation value and create the colors accordingly.
	 * 
	 * @param x		x-coordinate value in the range [0, widthBar]
	 */
	private void setSaturation(int x)
	{
		saturation = (float) x/widthBar;
		Color cD = Color.getHSBColor(hue, saturation, brightness);
		ccManager.setColorPanel(cD);	
	}
	
	/**
	 * Compute the Brightness value and create the colors accordingly.
	 * 
	 * @param x		x-coordinate value in the range [0, widthBar]
	 */
	private void setBrightness(int x)
	{
		brightness =(float) x/widthBar;
		Color cD = Color.getHSBColor(hue, saturation, brightness);
		ccManager.setColorPanel(cD);
	}
	
	/**  Release the mouse and set the dragging control to false. */
	public void mouseReleased(MouseEvent e) { dragging = false; }
		
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */   
	public void mouseMoved(MouseEvent e) {}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */     
	public void mouseClicked(MouseEvent e) {}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseEntered(MouseEvent e) {}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseExited(MouseEvent e) {}

}

