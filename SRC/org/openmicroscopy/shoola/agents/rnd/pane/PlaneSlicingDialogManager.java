/*
 * org.openmicroscopy.shoola.agents.rnd.pane.PlaneSlicingDialogManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.PlaneSlicingContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
class PlaneSlicingDialogManager
	implements ActionListener, MouseListener, MouseMotionListener
{
	
	/** ID to map biPlane index and value. */
	static final int				B_ONE = 0;
	static final int				B_TWO = 1;
	static final int				B_THREE = 2;
	static final int				B_FOUR = 3;
	static final int				B_FIVE = 4;
	static final int				B_SIX = 5;
	static final int				B_SEVEN = 6;

	private static final int 		B_ZERO = -1;

	/** ID to handle events. */
	private static final int		STATIC = 0;
	private static final int		DYNAMIC = 1;
	private static final int 		RANGE = 2;
	
	private static final HashMap	bitPlanes;
	
	static {
		bitPlanes = new HashMap();
		bitPlanes.put(new Integer(B_ZERO), 
							new Integer(PlaneSlicingContext.BIT_ZERO));
		bitPlanes.put(new Integer(B_ONE), 
					new Integer(PlaneSlicingContext.BIT_ONE));
		bitPlanes.put(new Integer(B_TWO), 
							new Integer(PlaneSlicingContext.BIT_TWO));
		bitPlanes.put(new Integer(B_THREE), 
							new Integer(PlaneSlicingContext.BIT_THREE));
		bitPlanes.put(new Integer(B_FOUR), 
							new Integer(PlaneSlicingContext.BIT_FOUR));
		bitPlanes.put(new Integer(B_FIVE), 
							new Integer(PlaneSlicingContext.BIT_FIVE));
		bitPlanes.put(new Integer(B_SIX), 
							new Integer(PlaneSlicingContext.BIT_SIX));
		bitPlanes.put(new Integer(B_SEVEN), 
							new Integer(PlaneSlicingContext.BIT_SEVEN));
	}
	
	/** Graphic constants. */
	private static final int    	topBorder = PlaneSlicingPanel.topBorder,
									leftBorder = PlaneSlicingPanel.leftBorder,
									square = PlaneSlicingPanel.square,
									rightBorder = PlaneSlicingPanel.rightBorder;
	private static final int 		lS = leftBorder+square, 
									tS = topBorder+square;
				
	private Rectangle				boxOutputStart, boxOutputEnd;
	
	/** Used to control mouse pressed and dragged events. */
	private boolean					dragging;
	
	/** Control with graphic view is selected. */
	private boolean					isSelected;
	
	/** Controls to determine which knob has been selected. */
	private boolean					outputStartKnob, outputEndKnob;
	
	private int						curRealValue;
									
	/** Reference to the view. */
	private PlaneSlicingDialog		view;
	
	/** Reference to the main control {@link QuantumPaneManager}. */
	private QuantumPaneManager		control;

	private PlaneSlicingContext		ctx;
	
	/**
	 * Create a new instance.
	 * @param view			
	 * @param control
	 */
	PlaneSlicingDialogManager(PlaneSlicingDialog view, 
								QuantumPaneManager control,
								PlaneSlicingContext ctx)
	{
		this.view = view;
		this.control = control;
		this.ctx = ctx;
		isSelected = ctx.IsConstant();
		boxOutputStart = new Rectangle(0, 0, leftBorder, tS);
		boxOutputEnd = new Rectangle(lS, 0, 3*rightBorder, tS);
		outputStartKnob  = false;
		outputEndKnob  = false;
	}
	
	/** Attach listeners to the graphics. */
	void attachListeners()
	{
		JRadioButton radioStatic = view.getRadioStatic(),
					 radioDynamic = view.getRadioDynamic();
		radioStatic.addActionListener(this);
		radioStatic.setActionCommand(""+STATIC);
		radioDynamic.addActionListener(this);
		radioDynamic.setActionCommand(""+DYNAMIC);
		JComboBox cbx = view.getRange();
		cbx.addActionListener(this);
		cbx.setActionCommand(""+RANGE);
		
		// graphical listeners.	
		view.getPSPanel().addMouseListener(this);
		view.getPSPanel().addMouseMotionListener(this);
	}

	/** Convert a real value into a coordinate. */
	int convertGraphicsIntoReal(int x, int r, int b)
	{
		double a = (double) r/square;
		return (int) (a*x+b);
	}

	/** Convert  coordinate into a real value. */
	int convertRealIntoGraphics(int x, int r, int b)
	{
		double a = (double) square/r;
		return (int) (a*(x-b));
	}
	
	/** Handles event fired by the radio button and the comboBox. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		int index = Integer.parseInt(s);
		try {
			switch(index) { 
				case RANGE:
					JComboBox cbx = (JComboBox) e.getSource();
					setPlaneIndex(cbx.getSelectedIndex());
					break;
				case STATIC:
					activateStatic();
					break;
				case DYNAMIC:
					activateDynamic();
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Handles events fired the cursors. */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (!dragging) {
			dragging = true;
			if (boxOutputStart.contains(p) && p.y <= tS && p.y >= topBorder 
				&& isSelected) {
				outputStartKnob	= true;
				setLowerLimit(p.y);
			}	
			if (boxOutputEnd.contains(p) && p.y >= topBorder && p.y <= tS 
				&& isSelected) {
				outputEndKnob = true;
				setUpperLimit(p.y);
			}
		 }  //else dragging already in progress 
	}
	
	/** Handles events fired the cursors. */    
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		if (dragging) { 
			if (boxOutputStart.contains(p) && p.y <= tS && p.y >= topBorder 
				&& isSelected) 
				setLowerLimit(p.y);
			if (boxOutputEnd.contains(p) && p.y >= topBorder && p.y <= tS
				&& isSelected)
				setUpperLimit(p.y);
		}
	}
	
	/** Resets the dragging control to false. */     
	public void mouseReleased(MouseEvent e)
	{ 
		if (outputStartKnob) {
			ctx.setLowerLimit(curRealValue);
			control.updateCodomainMap(ctx);
		}
		if (outputEndKnob) {
			ctx.setUpperLimit(curRealValue);
			control.updateCodomainMap(ctx);
		}
		dragging = false;
		outputStartKnob = false;
		outputEndKnob = false;
	}
	
	/**
	 * Set the plane slice index.
	 * 
	 * @param index		plane's index.
	 */
	private void setPlaneIndex(int index)
	{
		//retrieve the level of the plane
		Integer psI = (Integer) bitPlanes.get(new Integer(index));
		int planeSelected = psI.intValue();
		int e = control.getCodomainEnd();
		int s = control.getCodomainStart();
		if (planeSelected > e || planeSelected < s) {
			String message = "The level of the plane selected is not a value" +
				"contained in the output interval. Please resize the output " +
				"window or select a new plane.";
			UserNotifier un = 
					control.getEventManager().getRegistry().getUserNotifier();
			un.notifyInfo("Plane Selection", message);
		} else {
			Integer ppI = (Integer) bitPlanes.get(new Integer(index-1));
			ctx.setPlanes(ppI.intValue(), planeSelected);
			control.updateCodomainMap(ctx);
		}
	}
	
	/**
	 * Set the lower limit.
	 * 
	 * @param y		y-coordinate.
	 */
	private void setLowerLimit(int y)
	{
		view.getPSPanel().updateOutputStart(y);
		int e = control.getCodomainEnd();
		int s = control.getCodomainStart();
		curRealValue = convertGraphicsIntoReal(y-topBorder, s-e, e);
	}
	
	/**
	 * Set the upper limit.
	 * 
	 * @param y		y-coordinate.
	 */
	private void setUpperLimit(int y)
	{
		view.getPSPanel().updateOutputEnd(y);
		int e = control.getCodomainEnd();
		int s = control.getCodomainStart();
		curRealValue = convertGraphicsIntoReal(y-topBorder, s-e, e);
	}

	/** Sets the selection control. */
	private void activateDynamic()
	{
		isSelected = true;
		view.getPSPanel().setIsSelected(true);
		view.getPSSPanel().setIsSelected(false);
		ctx.setConstant(isSelected);
		control.updateCodomainMap(ctx);
	}
	
	/** Sets the selection control. */
	private void activateStatic()
	{
		isSelected = false;
		view.getPSPanel().setIsSelected(false);
		view.getPSSPanel().setIsSelected(true);
		ctx.setConstant(isSelected);
		control.updateCodomainMap(ctx);
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
