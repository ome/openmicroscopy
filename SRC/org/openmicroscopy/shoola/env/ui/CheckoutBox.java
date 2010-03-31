/*
 * org.openmicroscopy.shoola.env.ui.CheckoutBox 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the instances of <code>Agent</code> that can be saved before
 * switching group or closing the application.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class CheckoutBox 
	extends MessageBox
{

	/** The elements to save. */
	private Map<Agent, AgentSaveInfo> map;
	
	/** Component to save all instances. */
	private JCheckBox				  saveAll;
	
	/** Initializes the display. */
	private void initComponents()
	{
		saveAll = new JCheckBox("Save unsaved changes");
		if (map != null && map.size() > 0) saveAll.setSelected(true);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		if (map == null || map.size() == 0) return;
		addBodyComponent(UIUtilities.buildComponentPanel(saveAll));
	}
	
	/**
	 * Creates  a new instance.
	 * 
	 * @param onwer		The parent window.
	 * @param title		The title to display on the title bar.
	 * @param message	The notification message.
	 * @param map 		Contains the instances to save.
	 */
	public CheckoutBox(JFrame owner, String title, String message, Icon icon,
			Map<Agent, AgentSaveInfo> map)
	{
		super(owner, title, message, icon);
		this.map = map;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Creates  a new instance.
	 * 
	 * @param onwer		The parent window.
	 * @param title		The title to display on the title bar.
	 * @param message	The notification message.
	 * @param map 		Contains the instances to save.
	 */
	public CheckoutBox(JFrame owner, String title, String message, 
			Map<Agent, AgentSaveInfo> map)
	{
		this(owner, title, message, null, map);
	}
	
	/**
	 * Returns the instances of <code>Agent</code> to save.
	 * 
	 * @return See above.
	 */
	Map<Agent, AgentSaveInfo> getInstancesToSave()
	{ 
		if (!saveAll.isSelected()) return null;
		//to be modified.
		return map; 
	}
	
}
