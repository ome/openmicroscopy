 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveAsProtocolAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.ui.MessageBox;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveAsProtocolAction 
	extends EditorAction
	implements PropertyChangeListener
{
	/** 
	 * Implement this method to disable the Save Action if no file is open
	 * or the current file is not an Experiment. 
	 * @see EditorAction#onStateChange()
	 */
	protected void onStateChange()
	{
		int state = model.getState();
		setEnabled(state == Editor.READY);
		
		if (!model.isExperiment()) setEnabled(false);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public SaveAsProtocolAction(Editor model)
	{
		super(model);
		setEnabled(false);
		setName("Save As Protocol");
		setDescription("Save an Experiment file as a Protocol.");
		setIcon(IconManager.SAVE_ICON);
		model.registerBrowserListener(this);
	}
	
	/**
	 * Implemented as specified by the {@link ActionListener} interface. 
	 * Removes Experiment info and Step Notes and Saves the currently 
	 * edited file (as a protocol). 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// if not an experiment 
		if (!model.isExperiment())		return;
		
		JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
		MessageBox msg = new MessageBox(f, "Save As Protocol...", 
				"This will remove the Experiment Info and any Step Notes\n" +
				"and save the file as a new Protocol file.");
		msg.setYesText("OK");
		msg.setNoText("Cancel");
		
		int option = msg.centerMsgBox();
		if (option != MessageBox.YES_OPTION) return;
		
		model.deleteExperimentInfo();
		ActionCmd save = new SaveNewCmd(model);
		save.execute();
	}
	
	/**
	 * Reacts to property fired by the <code>Browser</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.BROWSER_EDIT_PROPERTY.equals(name)) 
			onStateChange();
	}

}
