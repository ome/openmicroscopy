/*
 * org.openmicroscopy.shoola.agents.viewer.controls.XYZNavigatorManager
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

package org.openmicroscopy.shoola.agents.viewer.controls;

//Java imports
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
class XYZNavigatorManager
	implements ActionListener, FocusListener, ChangeListener
{
	private NavigationPaletteManager 		manager;
	private XYZNavigator 					view;	
	
	private int 							curZ, maxZ;
	XYZNavigatorManager(XYZNavigator view, NavigationPaletteManager manager,
						int sizeZ, int z)
	{
		this.manager = manager;
		this.view = view;
		curZ = z;
		maxZ = sizeZ;
	}
	
	/** Attach the listeners. */
	void attachListeners()
	{
		//textfield
		JTextField zField = view.getZField();
		zField.addActionListener(this);
		zField.addFocusListener(this);
		//slider
		view.getZSlider().addChangeListener(this);
	}
	/** 
	 * Synchronizes the slider, the text field and the current Z.
	 * 
	 * @param v	The value that the slider, text field and the current Z will 
	 * 			be set to.
	 */
	private void synch(int v)
	{
		curZ = v;
		JSlider zSlider = view.getZSlider(); 
		//Remove temporarily the listener otherwise an event is fired.
		zSlider.removeChangeListener(this);
		zSlider.setValue(v);
		zSlider.addChangeListener(this);
		view.getZField().setText(""+v);  
		view.repaint();         
		manager.onZChange(curZ);
	}
	
	/** Listen to action events fired by the Z text field. */
	public void actionPerformed(ActionEvent e)
	{
		boolean valid = false;
		int val = 0;
		try {
			val = Integer.parseInt(view.getZField().getText());
			if (0 <= val && val <= maxZ) valid = true;
		} catch(NumberFormatException nfe) {}
		if (valid) synch(val);  // will notify the controller
		else {
			view.getZField().selectAll();
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	
	/** Listen event fired by the slider. */
	public void stateChanged(ChangeEvent e)
	{	
		int val = view.getZSlider().getValue();
		if (val != curZ) synch(val);
	}
	
	/** 
	 * Handles the lost of focus on the Z text field.
	 * If focus is lost while editing, then we don't consider the text 
	 * currently displayed in the text field and we reset it to the current Z.
	 */
	public void focusLost(FocusEvent e)
	{
		String val = view.getZField().getText(), z = ""+curZ;
		if (val == null || !val.equals(z)) view.getZField().setText(z);
	}
	
	/** 
 	 * Required by I/F but not actually needed in our case, no op 
 	 * implementation.
 	 */
	public void focusGained(FocusEvent e) {}
	
}
