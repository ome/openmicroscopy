/*
 * org.openmicroscopy.shoola.agents.rnd.pane.GraphicsRepresentationManager
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

package org.openmicroscopy.shoola.agents.rnd.pane;

//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Handles events fired the graphical cursors drawn in
 * {@link GraphicsRepresentation}.
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
class GraphicsRepresentationManager
	implements MouseListener, MouseMotionListener
{
	/** Graphical constant. */
	private static final int	topBorder = GraphicsRepresentation.topBorder, 
								leftBorder = GraphicsRepresentation.leftBorder,
								square = GraphicsRepresentation.square, 
								bottomBorder = 
									GraphicsRepresentation.bottomBorder,
								triangleW = GraphicsRepresentation.triangleW, 
	   							lS = leftBorder+square, tS = topBorder+square,
								length = 2*triangleW, absMin = leftBorder+20;
							
	private boolean                 dragging;
	private Rectangle               boxStart, boxEnd, boxOutputStart,
									boxOutputEnd;
	private int                     maxStartX, minEndX, maxEndX, 
									maxStartOutputY, minEndOutputY;

	private QuantumMappingManager	control;
	private GraphicsRepresentation	view;
	
	private int 					type;
	
	GraphicsRepresentationManager(GraphicsRepresentation view, 
									QuantumMappingManager control, int type)
	{
		this.view = view;
		this.control = control;
		this.type = type;
		boxStart = new Rectangle();
		boxEnd = new Rectangle();
		boxOutputStart = new Rectangle();
		boxOutputEnd = new Rectangle();
		maxEndX = leftBorder+square/2; //only used if type = exponential
		attachListeners();
	}
	
	/** Attach listeners. */
	void attachListeners()
	{
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
	}
	
	/** Handles events fired the cursors. */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (!dragging) {
			dragging = true;
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS && 
				p.x >= maxStartX && p.x <= maxEndX && p.x >= absMin 
				&& type == QuantumMapping.EXPONENTIAL ) {
				setInputEndBox(p.x);
				//control.updateInputEnd(p.x);
			}
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS &&
				p.x >= maxStartX && type != QuantumMapping.EXPONENTIAL) {
				setInputEndBox(p.x);
				//control.updateInputEnd(p.x);
			}
			if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS &&
				p.x <= minEndX) {
				setInputStartBox(p.x);
				//control.updateInputStart(p.x);
			}
			if (boxOutputStart.contains(p) && p.y >= minEndOutputY &&
				 p.y <= tS) {
				setOutputStartBox(p.y);
				view.updateOutputStart(p);
				//control.updateOutputStart(p.y);
			}
			if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY &&
				 p.y >= topBorder) {
				setOutputEndBox(p.y);
				view.updateOutputEnd(p);
				//control.updateOutputStart(p.y);
			}
		 }  //else dragging already in progress 
	}
	
	/** Handles events fired the cursors. */    
	public void mouseDragged(MouseEvent e)
	{
		Point   p = e.getPoint();
		if (dragging) {
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS && 
				p.x >= maxStartX && p.x <= maxEndX && p.x >= absMin 
				&& type == QuantumMapping.EXPONENTIAL ) {
				setInputEndBox(p.x);
				//control.updateInputEnd(p.x);
			}
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS &&
				p.x >= maxStartX && type != QuantumMapping.EXPONENTIAL) {
				setInputEndBox(p.x);
				//control.updateInputEnd(p.x);
			}
			if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS &&
				p.x <= minEndX) {
				setInputStartBox(p.x);
				//control.updateInputStart(p.x);
			}
			if (boxOutputStart.contains(p) && p.y >= minEndOutputY 
				&& p.y <= tS) {
				setOutputStartBox(p.y);
				view.updateOutputStart(p);
				//control.updateOutputStart(p.x);
			}
			if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY && 
				p.y >= topBorder) {
				setOutputEndBox(p.y);
				view.updateOutputEnd(p);
				//control.updateOutputEnd(p.x);
			}
		}
	}
	
	/** Resets the dragging control to false. */    
	public void mouseReleased(MouseEvent e)
	{
		dragging = false;
	}
	
	/** 
	 * Sets the type. 
	 *
	 * @param t     family index.
	 * @param x     MaxEndX value.
	 */
	void setType(int type, int x)
	{
		this.type = type;
		maxEndX = x ;
	}
	
	/** 
	 * Sets  the MaxEndX value that is used to control the cursors' motions.
	 *
	 * @param x value.
	 */    
	void setMaxEndX(int x)
	{
		 maxEndX = x ;
	}
	
	/** 
	 * Sizes the rectangle used to listen to the outpuStart cursor.
	 *
	 * @param y     y-coordinate.
	 */ 
	void setOutputStartBox(int y)
	{
		maxStartOutputY = y-triangleW;
		boxOutputStart.setBounds(0, y-triangleW, leftBorder-triangleW-1,
										 length);
	}
	
	/** 
	 * Sizes the rectangle used to listen to the outputEnd cursor.
	 *
	 * @param y     y-coordinate.
	 */  
	void setOutputEndBox(int y)
	{
		minEndOutputY = y+triangleW;
		boxOutputEnd.setBounds(0, y-triangleW, leftBorder-triangleW-1, length);
	}
	
	/** 
	 * Sizes the rectangle used to listen to the inputStart cursor.
	 *
	 * @param x     x-coordinate.
	 */
	void setInputStartBox(int x)
	{
		maxStartX = x+triangleW;
		boxStart.setBounds(x-triangleW, tS+triangleW+1, length, bottomBorder);
	}
	
	/** 
	 * Sizes the rectangle used to listen to the inputEnd cursor.
	 *
	 * @param x     x-coordinate.
	 */  
	void setInputEndBox(int x)
	{
		minEndX = x-triangleW;
		boxEnd.setBounds(x-triangleW, tS+triangleW+1, length, bottomBorder);
	}
	
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
