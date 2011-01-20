 /*
 * org.openmicroscopy.shoola.agents.editor.browser.EditingModeUI 
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
package org.openmicroscopy.shoola.agents.editor.browser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a simple UI panel that allows the editing of the editing mode
 * between {@link Browser#EDIT_EXPERIMENT} and {@link Browser#EDIT_PROTOCOL}.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditingModeUI 
	extends JPanel 
	implements ActionListener{
	
	/** The browser to edit */
	private BrowserControl				browser;
	
	/** Action command for edit-protocol */
	public static final String			EDIT_PRO = "Edit Protocol";
	
	/** Action command for edit-experiment */
	public static final String			EDIT_EXP = "Edit Experiment";
	
	/** Button to select protocol */
	JRadioButton protocolButton;
	
	/** Button to select experiment */
	JRadioButton experimentButton;
	
	/**
	 * Creates an instance. 
	 * 
	 * @param browser	The browser to edit. 
	 */
	public EditingModeUI(BrowserControl browser) {
		
		this.browser = browser;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		ButtonGroup group = new ButtonGroup();
		
		
		protocolButton = new JRadioButton(EDIT_PRO);
		protocolButton.setBackground(null);
		protocolButton.setActionCommand(EDIT_PRO);
		group.add(protocolButton);
		
		experimentButton = new JRadioButton(EDIT_EXP);
		experimentButton.setBackground(null);
		experimentButton.setActionCommand(EDIT_EXP);
		group.add(experimentButton);
		
		protocolButton.addActionListener(this);
		add(protocolButton);
		experimentButton.addActionListener(this);
		add(experimentButton);

		// update UI according to current status 
		refresh();
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Responds to changes in selection. 
	 */
	public void actionPerformed(ActionEvent e) {
		int editingMode = Browser.EDIT_PROTOCOL;
		String actionCmd = e.getActionCommand();
		if (EDIT_EXP.equals(actionCmd)) 
			editingMode = Browser.EDIT_EXPERIMENT;
		
		browser.setEditingMode(editingMode);
	}
	
	/**
	 * Refreshes the UI according to the model. 
	 */
	public void refresh() 
	{
		protocolButton.removeActionListener(this);
		experimentButton.removeActionListener(this);
		
		switch (browser.getEditingMode()) {
		case Browser.EDIT_EXPERIMENT:
			experimentButton.setSelected(true);
			break;
		default:
			protocolButton.setSelected(true);
			break;
		}
		
		protocolButton.addActionListener(this);
		experimentButton.addActionListener(this);
	}

}
