/*
 * org.openmicroscopy.shoola.agents.rnd.pane.ContrastStretchingDialogManager
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
class ContrastStretchingDialogManager
	implements MouseListener, MouseMotionListener
{
	private static final int			topBorder = 
										ContrastStretchingPanel.topBorder, 
										leftBorder = 
										ContrastStretchingPanel.leftBorder,
										square = ContrastStretchingPanel.square, 
										bottomBorder = 
										ContrastStretchingPanel.bottomBorder, 
										lS = leftBorder+square, 
										tS = topBorder+square;
	private static final int 			triangleW = 	
										ContrastStretchingPanel.triangleW, 
										length = 2*triangleW;
								
	private boolean   					dragging;
	
	/** Rectangles controlling the cursors. */
	private Rectangle  					boxStart, boxEnd, boxOutputStart, 
										boxOutputEnd;
										
	private int 						maxStartX, minEndX, maxStartOutputY,
										minEndOutputY;
	/** Reference to the view. */
	private ContrastStretchingDialog	view;
	private QuantumMappingManager		control;
	
	ContrastStretchingDialogManager(ContrastStretchingDialog view,
									QuantumMappingManager control)
	{
		this.view = view;
		this.control = control;
	}
	
	/** Attach listeners. */
	void attachListeners()
	{
		view.getCSPanel().addMouseListener(this);
		view.getCSPanel().addMouseMotionListener(this);
	}
	
	
	int convertGraphicsIntoReal(int x, int r, int b)
	{
		double a = (double) r/square;
		return (int) (a*x+b);
	}
	
	int convertRealIntoGraphics(int x, int r, int b)
	{
		double a = (double) square/r;
		return (int) (a*(x-b));
	}
	
	/** Initializes the rectangles which control the cursors. */
	void setRectangles(int xStart, int xEnd, int yStart, int yEnd)
	{
		boxStart = new Rectangle(xStart-triangleW, tS, length, bottomBorder);
		boxEnd = new Rectangle(xEnd-triangleW, tS, length, bottomBorder);
		boxOutputStart = new Rectangle(0, yStart-triangleW, 
										leftBorder-triangleW-1, length);
		boxOutputEnd = new Rectangle(0, yEnd-triangleW, leftBorder-triangleW-1,
										length);
	}
	
	/** Handles events fired the cursors. */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (dragging) {  
			if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS 
				&& p.x <= minEndX)
				setInputStart(p.x);
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS
				&& p.x >= maxStartX)
				setInputEnd(p.x); 
			if (boxOutputStart.contains(p) && p.y >= minEndOutputY
				&& p.y <= tS)
				setOutputStart(p.y);
			if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY
				&& p.y >= topBorder) 
				setOutputEnd(p.y);
		}
	}
	
	/** Handles events fired the cursors. */
	public void mouseDragged(MouseEvent e)
	{
		Point   p = e.getPoint();
		if (dragging) {  
			if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS 
				&& p.x <= minEndX)
				setInputStart(p.x);
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS
				&& p.x >= maxStartX)
				setInputEnd(p.x); 
			if (boxOutputStart.contains(p) && p.y >= minEndOutputY
				&& p.y <= tS)
				setOutputStart(p.y);
			if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY
				&& p.y >= topBorder) 
				setOutputEnd(p.y);
		}
	}
	
	/** Resets the dragging control to false. */
	public void mouseReleased(MouseEvent e)
	{
		dragging = false;
	}
	
	private void setInputStart(int x)
	{
		setInputStartBox(x);
		view.getCSPanel().updateStartCursor(x);
		int s  = control.getCurOutputStart();
		int xReal = convertGraphicsIntoReal(x-leftBorder, 
											control.getCurEnd()-s, s);
		//Forward event to control
	}
	
	private void setInputEnd(int x)
	{
		setInputEndBox(x);
		view.getCSPanel().updateEndCursor(x);
		int s  = control.getCurOutputStart();
		int xReal = convertGraphicsIntoReal(x-leftBorder,
											control.getCurOutputEnd()-s, s);
		//Forward event to control
	}
	
	private void setOutputStart(int y)
	{
		setOutputStartBox(y);
		view.getCSPanel().updateStartOutputCursor(y);
		int e = control.getCurOutputEnd();
		int yReal = convertGraphicsIntoReal(y-topBorder, 
										e-control.getCurOutputStart(), e);
		
		//Forward event to control
	}
	
	private void setOutputEnd(int y)
	{
		setOutputEndBox(y);
		view.getCSPanel().updateEndOutputCursor(y);
		int e = control.getCurOutputEnd();
		int yReal = convertGraphicsIntoReal(y-topBorder, 
										e-control.getCurOutputStart(), e);
		//Forward event to control
	}
	
	/** 
	 * Sizes the rectangle used to listen to the inputStart cursor.
	 *
	 * @param x     x-coordinate.
	 */
	private void setInputStartBox(int x)
	{
		maxStartX = x+triangleW;
		boxStart.setBounds(x-triangleW, tS, length, bottomBorder);
	}
	
	/** 
	 * Sizes the rectangle used to listen to the inputEnd cursor.
	 *
	 * @param x     x-coordinate.
	 */
	private void setInputEndBox(int x)
	{
		minEndX = x-triangleW;
		boxEnd.setBounds(x-triangleW, tS, length, bottomBorder);
	}
	
	/** 
	 * Sizes the rectangle used to listen to the outputStart cursor.
	 *
	 * @param y     y-coordinate.
	 */
	private void setOutputStartBox(int y)
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
	private void setOutputEndBox(int y)
	{
		minEndOutputY = y+triangleW;
		boxOutputEnd.setBounds(0, y-triangleW, leftBorder-triangleW-1, 
							length);
	}
	
	/** 
	 * Required by I/F but not actually needed in our case, 
	 * no op implementation. 
	 */   
	public void mouseMoved(MouseEvent e) {}
	
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
	
}
