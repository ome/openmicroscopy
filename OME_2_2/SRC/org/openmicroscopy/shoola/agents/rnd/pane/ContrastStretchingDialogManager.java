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
import org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingContext;

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
	
	/** Widget's contants. */
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
	
	/** Used to control mouse pressed and dragged events. */				
	private boolean   					dragging;
	
	/** Rectangles controlling the cursors. */
	private Rectangle  					boxStart, boxEnd, boxOutputStart, 
										boxOutputEnd;
										
	/** Control to position the cursors. */									
	private int 						maxStartX, minEndX, maxStartOutputY,
										minEndOutputY;
	/** Reference to the view. */
	private ContrastStretchingDialog	view;
	
	/** Controls to determine which knob has been selected. */
	private boolean						inputStartKnob, inputEndKnob, 
										outputStartKnob, outputEndKnob;
	
	private int							curRealValue;
										
	/** Reference to the main {@link QuantumPaneManager manager}. */
	private QuantumPaneManager			control;
	
	private ContrastStretchingContext	ctx;
	
	ContrastStretchingDialogManager(ContrastStretchingDialog view,
									QuantumPaneManager control, 
									ContrastStretchingContext ctx)
	{
		this.view = view;
		this.control = control;
		this.ctx = ctx;
		inputStartKnob = false;
		inputEndKnob = false; 
		outputStartKnob = false;
		outputEndKnob = false;
	}
	
	/** Attach listeners. */
	void attachListeners()
	{
		view.getCSPanel().addMouseListener(this);
		view.getCSPanel().addMouseMotionListener(this);
	}
	
	/** Convert a real value into coordinates. */
	int convertGraphicsIntoReal(int x, int r, int b)
	{
		double a = (double) r/square;
		return (int) (a*x+b);
	}
	
	/** Convert a coordinate into a real value. */
	int convertRealIntoGraphics(int x, int r, int b)
	{
		double a = (double) square/r;
		return (int) (a*(x-b));
	}
	
	/** Initializes the rectangles which control the cursors. */
	void setRectangles(int xStart, int xEnd, int yStart, int yEnd)
	{
		minEndX = xEnd-triangleW;
		maxStartX = xStart+triangleW;
		minEndOutputY =  yEnd+triangleW;
		maxStartOutputY = yStart-triangleW;
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
		if (!dragging) { 
			dragging = true; 
			if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS 
				&& p.x <= minEndX) {
				inputStartKnob = true;
				setInputStart(p.x);	
			}
			if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS
				&& p.x >= maxStartX) {
				inputEndKnob = true;	
				setInputEnd(p.x); 	
			}	
			if (boxOutputStart.contains(p) && p.y >= minEndOutputY
				&& p.y <= tS) {
				outputStartKnob = true;	
				setOutputStart(p.y);	
			}
			if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY
				&& p.y >= topBorder) {
				inputStartKnob = true;	
				setOutputEnd(p.y);	
			}	
		}
	}
	
	/** Handles events fired the cursors. */
	public void mouseDragged(MouseEvent e)
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
	
	/** Resets the dragging control to false. */
	public void mouseReleased(MouseEvent e)
	{ 
		if (inputStartKnob) {
			ctx.setXStart(curRealValue);
			control.updateCodomainMap(ctx);
		} else if (inputEndKnob) {
			ctx.setXEnd(curRealValue);
			control.updateCodomainMap(ctx);
		} else if (outputStartKnob) {
			ctx.setYStart(curRealValue);
			control.updateCodomainMap(ctx);
		} else if (outputEndKnob) {
			ctx.setYEnd(curRealValue);
			control.updateCodomainMap(ctx);
		}
		dragging = false;
		inputStartKnob = true;
		inputEndKnob = true;
		outputStartKnob = true;
		outputEndKnob = true;
	}
	
	/** Modify the x-coordinate of the control startPoint. */
	private void setInputStart(int x)
	{
		setInputStartBox(x);
		view.getCSPanel().updateStartKnob(x);
		int s  = control.getCodomainStart();
		curRealValue = convertGraphicsIntoReal(x-leftBorder, 
										control.getCodomainEnd()-s, s);	
	}
	
	/** Modify the x-coordinate of the control endPoint. */
	private void setInputEnd(int x)
	{
		setInputEndBox(x);
		view.getCSPanel().updateEndKnob(x);
		int s  = control.getCodomainStart();
		curRealValue = convertGraphicsIntoReal(x-leftBorder,
										control.getCodomainEnd()-s, s);
	}
	
	/** Modify the y-coordinate of the control startPoint. */
	private void setOutputStart(int y)
	{
		setOutputStartBox(y);
		view.getCSPanel().updateStartOutputKnob(y);
		int e = control.getCodomainEnd();
		curRealValue = convertGraphicsIntoReal(y-topBorder, 
										control.getCodomainStart()-e, e);
	}
	
	/** Modify the y-coordinate of the control endPoint. */
	private void setOutputEnd(int y)
	{
		setOutputEndBox(y);
		view.getCSPanel().updateEndOutputKnob(y);
		int e = control.getCodomainEnd();
		curRealValue = convertGraphicsIntoReal(y-topBorder, 
										control.getCodomainStart()-e, e);	}
	
	/** 
	 * Size the rectangle used to listen to the inputStart cursor.
	 *
	 * @param x     x-coordinate.
	 */
	private void setInputStartBox(int x)
	{
		maxStartX = x+triangleW;
		boxStart.setBounds(x-triangleW, tS, length, bottomBorder);
	}
	
	/** 
	 * Size the rectangle used to listen to the inputEnd cursor.
	 *
	 * @param x     x-coordinate.
	 */
	private void setInputEndBox(int x)
	{
		minEndX = x-triangleW;
		boxEnd.setBounds(x-triangleW, tS, length, bottomBorder);
	}
	
	/** 
	 * Size the rectangle used to listen to the outputStart cursor.
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
	 * Size the rectangle used to listen to the outputEnd cursor.
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
