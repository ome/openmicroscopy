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
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.PlaneSlicingDef;

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
	/** ID to handle events. */
	private static final int		STATIC = 0;
	private static final int		DYNAMIC = 1;
	private static final int 		RANGE = 2;
	
	/** Graphical constants. */
	private static final int    	topBorder = PlaneSlicingPanel.topBorder,
									leftBorder = PlaneSlicingPanel.leftBorder,
									square = PlaneSlicingPanel.square,
									rightBorder = PlaneSlicingPanel.rightBorder;
	private static final int 		lS = leftBorder+square, 
									tS = topBorder+square;
	private static final int 		triangleW = PlaneSlicingPanel.triangleW;
	
	/** 
	 * Coefficient of the map: y = coeff*x+b to convert
	 * a real value into a coordinate.
	 */
	private static final double		coeff = square/255;	

	/** 
	 * Coefficient of the map: y = rCoeff*x+b to convert
	 * a graphical value into a real value in the range [0, 255].
	 */
	private static final double		rCoeff = 255/square;
				
	private Rectangle				boxOutputStart, boxOutputEnd;
	private boolean					dragging, isSelected;
	
	private PlaneSlicingDialog		view;
	private QuantumMappingManager	control;
	private PlaneSlicingDef			psDef;
	/**
	 * Create a new instance.
	 * @param view			
	 * @param control
	 */
	PlaneSlicingDialogManager(PlaneSlicingDialog view, 
								QuantumMappingManager control, 
								PlaneSlicingDef psDef)
	{
		this.view = view;
		this.control = control;
		this.psDef = psDef;
		isSelected = true;
		boxOutputStart = new Rectangle();
		boxOutputEnd = new Rectangle();
	}
	
	/** Attach listeners to the graphics. */
	void attachListeners()
	{
		JRadioButton 	radioStatic = view.getRadioStatic(),
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

	/** 
	 * Convert a real value into a coordinate.
	 * 
	 * @param value		real value.
	 * @return	See above.
	 */
	int convertRealIntoGraphics(int x)
	{
		return	(int) (tS-coeff*x);
	}
	
	/** 
	 * Convert into a real value i.e. value in the range [0, 255] 
	 * 
	 * @param y		y-coordinate.
	 * @return
	 */
	int convertGraphicsIntoReal(int x)
	{
		return (int) (rCoeff*(tS-x));
	}
	
	/** Handles event fired by the radio button and the comboBox. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int index = Integer.parseInt(s);
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
			}// end switch  
		//impossible if IDs are set correctly 
		} catch(NumberFormatException nfe) {
				throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** Handles events fired the cursors. */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		if (!dragging) {
			dragging = true;
			if (boxOutputStart.contains(p) && p.y <= tS && p.y >= topBorder 
				&& isSelected)
				setLowerLimit(p.y);
			if (boxOutputEnd.contains(p) && p.y >= topBorder && p.y <= tS 
				&& isSelected)
				setUpperLimit(p.y);
		 }  //else dragging already in progress 
	}
	
	/** Handles events fired the cursors. */    
	public void mouseDragged(MouseEvent e)
	{
		Point   p = e.getPoint();
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
		dragging = false;
	}
	
	/**
	 * Set the plane slice index.
	 * 
	 * @param index		plane's index.
	 */
	private void setPlaneIndex(int index)
	{
		//psDef.setPlaneIndex(index);
		//Forward event to control
	}
	
	/**
	 * Set the lower limit.
	 * @param x			x-coordinate.
	 */
	private void setLowerLimit(int x)
	{
		setOutputStartBox(x);
		view.getPSPanel().updateOutputStart(x);
		//psDef.setLowerLimit(convertGraphicsIntoReal(x));
		//Forward event to control
	}
	
	/**
	 * Set the upper limit.
	 * @param x			x-coordinate.
	 */
	private void setUpperLimit(int x)
	{
		setOutputEndBox(x);
		view.getPSPanel().updateOutputEnd(x);
		//psDef.setUpperLimit(convertGraphicsIntoReal(x));
		//Forward event to control
	}
	
	/** 
	 * Resize the rectangle which controls the start cursor.
	 * 
	 * @param y	 y-coordinate.
	 */
	void setOutputStartBox(int y)
	{
		boxOutputStart.setBounds(0, y-triangleW, leftBorder, tS);
	}
	
	/** 
	 * Resize the rectangle which controls the end cursor.
	 * 
	 * @param y	 y-coordinate.
	 */
	void setOutputEndBox(int y)
	{
		boxOutputEnd.setBounds(lS, y-triangleW, 3*rightBorder, tS);
	}
	
	/**
	 * Sets the selection control.
	 *
	 */
	private void activateDynamic()
	{
		view.getPSPanel().setIsSelected(true);
		view.getPSSPanel().setIsSelected(false);
	}
	
	/**
	 * Sets the selection control.
	 *
	 */
	private void activateStatic()
	{
		view.getPSPanel().setIsSelected(false);
		view.getPSSPanel().setIsSelected(true);
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
