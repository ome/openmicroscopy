/*
 * org.openmicroscopy.shoola.agents.viewer.controls.ToolBarNavigatorMng
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
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
public class ToolBarManager
	implements ActionListener, FocusListener, ChangeListener
{

	/** Action command ID to be used with the timepoint text field. */
	private static final int   					T_FIELD_CMD = 0;
	
	/** Action command ID to be used with the play button. */
	private static final int   					PLAY_CMD = 1;
	
	/** Action command ID to be used with the stop button. */
	private static final int   					STOP_CMD = 2;
	
	/** Action command ID to be used with the rewind button. */
	private static final int   					REWIND_CMD = 3;
	
	/** Action command ID to be used with the rewind button. */
	private static final int   					RENDER_CMD = 4;
	
	/** Action command ID to be used with the inspector button. */
	private static final int   					INSPECTOR_CMD = 5;
	
	/** 
	 * Action command ID to be used to sync JSpinner 
	 * and the text field editor.
	 */
	private static final int   					EDITOR_CMD = 6;
	
	/** Action command ID to be used with the z-section text field. */
	private static final int					Z_FIELD_CMD = 7;
	
	/** Action command ID to be used with the saveAs button. */
	private static final int   					SAVEAS_CMD = 8;
	
	/** Action command ID to be used with the pause button. */
	private static final int   					PAUSE_CMD = 9;

	/** Action command ID to be used with the forward button. */
	private static final int   					FORWARD_CMD = 10;
	
	private int									curT, maxT, curR;
	private int									curZ, maxZ;
	
	private ViewerCtrl							control;
	
	private ToolBar								view;
	
	public ToolBarManager(ViewerCtrl control, ToolBar view, int sizeT, int t,
						  int sizeZ, int z)
	{
		this.control = control;
		this.view = view;
		maxT = sizeT;
		curT = t;
		maxZ = sizeZ;
		curZ = z;
	}
	
	/** Attach the listeners. */
	void attachListeners()
	{
		//textfield
		JTextField tField = view.getTField(), editor = view.getEditor(),
					zField = view.getZField();
		tField.setActionCommand(""+T_FIELD_CMD);  
		tField.addActionListener(this);
		tField.addFocusListener(this);
		editor.addActionListener(this);
		editor.setActionCommand(""+EDITOR_CMD);
		zField.setActionCommand(""+Z_FIELD_CMD);  
		zField.addActionListener(this);
		zField.addFocusListener(this);
		
		//button
		JButton	play = view.getPlay(), stop = view.getStop(), 
				rewind = view.getRewind(), render = view.getRender(), 
				inspector = view.getInspector(), saveAs = view.getSaveAs(), 
				pause = view.getPause(), forward = view.getForward();
		play.setActionCommand(""+PLAY_CMD);
		play.addActionListener(this);
		stop.setActionCommand(""+STOP_CMD); 
		stop.addActionListener(this);
		rewind.setActionCommand(""+REWIND_CMD);
		rewind.addActionListener(this);
		render.setActionCommand(""+RENDER_CMD);
		render.addActionListener(this);
		inspector.setActionCommand(""+INSPECTOR_CMD);
		inspector.addActionListener(this);
		saveAs.setActionCommand(""+SAVEAS_CMD);
		saveAs.addActionListener(this);
		pause.setActionCommand(""+PAUSE_CMD);
		pause.addActionListener(this);
		forward.setActionCommand(""+FORWARD_CMD);
		forward.addActionListener(this);
		//spinner
		view.getFPS().addChangeListener(this);
	}
	
	public void setMaxZ(int z) { maxZ = z; }
	
	public void setMaxT(int t) { maxT = t; }
	
	/** Update the TField when a new timepoint is selected using the slider. */
	public void onTChange(int t)
	{
		curT = t;
		view.getTField().setText(""+t);
	}
	
	/** Update the ZField when a z-section is selected using the slider. */
	public void onZChange(int z)
	{
		curZ = z;
		view.getZField().setText(""+z);
	}
	
	/** 
	 * Synchronizes the spinner, and the text editor.
	 * 
	 * @param val	The value that the slider, text field and the current 
	 * 				Scale will be set to.
	 */
	private void synchSpinner(int val)
	{ 
		curR = val;
		view.getFPS().setValue(new Integer(val));  
		view.getEditor().setText(""+val); 
	} 
	
	/** 
	 * Handles the action event fired by the editor text field when the 
	 * user enters some text. If the entered text can be  converted to a valid 
	 * blacklevel, the {@link #synch(int) synch} method is invoked in order to 
	 * set all elements to the new blacklevel value. 
	 * If that text doesn't evaluate to a valid blacklevel, then we simply 
	 * suggest the user to enter a valid one.
	 */      
	private void editorActionHandler()
	{
		//TODO implement
		boolean valid = false;
		int val = 0;
		try {
			val = Integer.parseInt(view.getEditor().getText());
			if (0 <= val && val <= maxT) {
				valid = true;
			} else if (val < 0) {
				val = 0;
				valid = true;
			} else if (val > maxT) {
				val = maxT;
				valid = true;
			}
		} catch(NumberFormatException nfe) {}
		if (valid) synchSpinner(val);  
		else {
			view.getEditor().selectAll();
			UserNotifier un = control.getRegistry().getUserNotifier();
			un.notifyInfo("Invalid value", "Please enter ");
		}
	} 
	
	/** 
	 * Handles the action event fired by the timepoint text field when the user 
	 * enters some text. 
	 * If that text doesn't evaluate to a valid timepoint, then we simply 
	 * suggest the user to enter a valid one.
	 */
	private void tFieldActionHandler() 
	{
		boolean valid = false;
		int val = 0;
		try {
			val = Integer.parseInt(view.getTField().getText());
			if (0 <= val && val <= maxT) valid = true;
		} catch(NumberFormatException nfe) {}
		if (valid) {
			curT = val;
			control.onTChange(curZ, curT); 
		} else {
			view.getTField().selectAll();
			UserNotifier un = control.getRegistry().getUserNotifier();
			un.notifyInfo("Invalid timepoint", 
				"Please enter a timepoint between 0 and "+maxT);
		}
	}
	
	/** 
	 * Handles the action event fired by the timepoint text field when the user 
	 * enters some text. 
	 * If that text doesn't evaluate to a valid timepoint, then we simply 
	 * suggest the user to enter a valid one.
	 */
	private void zFieldActionHandler() 
	{
		boolean valid = false;
		int val = 0;
		try {
			val = Integer.parseInt(view.getZField().getText());
			if (0 <= val && val <= maxZ) valid = true;
		} catch(NumberFormatException nfe) {}
		if (valid) {
			curZ = val;
			control.onZChange(curZ, curT);
		} else {
			view.getTField().selectAll();
			UserNotifier un = control.getRegistry().getUserNotifier();
			un.notifyInfo("Invalid z-section", 
				"Please enter a z-section between 0 and "+maxZ);
		}
	}
	
	/** Handle events fired byt text field and buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
		    switch (index) {
				case T_FIELD_CMD:
					tFieldActionHandler(); break;
				case Z_FIELD_CMD:
					zFieldActionHandler(); break;
				case RENDER_CMD:
					control.showRendering(); break;
				case INSPECTOR_CMD:
					control.showInspector(); break;
				case SAVEAS_CMD:
					control.showImageSaver(); break;
				/*
			   	case PLAY_CMD:  //not implemented yet
			   	case STOP_CMD:  //not implemented yet
			   	case REWIND_CMD:  //not implemented yet
			   	case FORWARD_CMD:
			   	case PAUSE_CMD:
			   	case EDITOR_CMD:
					editorActionHandler();
				   	break;
				  */
			}
		} catch(NumberFormatException nfe) { 
			throw new Error("Invalid Action ID "+index, nfe); 
		}
	}

	/** Handle events fired by the spinner. */
	public void stateChanged(ChangeEvent e)
	{
		//TODO: implement along with movie controls
		int v = ((Integer) view.getFPS().getValue()).intValue();
		view.getEditor().setText(""+v);
		if (v != curR) synchSpinner(v);
	}
	
	/** 
	 * Handles the lost of focus on the timepoint text field.
	 * If focus is lost while editing, then we don't consider the text 
	 * currently displayed in the text field and we reset it to the current
	 * timepoint.
	 */
	public void focusLost(FocusEvent e)
	{
		String tVal = view.getTField().getText(), t = ""+curT;
		String zVal = view.getZField().getText(), z = ""+curZ;
		String edit = view.getEditor().getText(), ed = ""+curR;
		if (tVal == null || !tVal.equals(t)) view.getTField().setText(t);
		if (zVal == null || !zVal.equals(z)) view.getZField().setText(z);
		if (edit == null || !edit.equals(ed)) view.getEditor().setText(ed);
	}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void focusGained(FocusEvent e) {}
	
}
