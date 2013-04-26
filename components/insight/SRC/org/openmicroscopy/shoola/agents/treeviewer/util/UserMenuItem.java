/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.UserMenuItem
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import javax.swing.JCheckBox;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;
import org.openmicroscopy.shoola.agents.util.EditorUtil;

/**
 * Hosts the experimenter to add to the menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class UserMenuItem 
	extends JCheckBox
{

	/** The user to host.*/
	private ExperimenterData user;
	
	/** Flag indicating if the item can be enabled or not.*/
	private boolean canBeEnabled;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param user The user to host.
	 * @param canBeEnabled Flag indicating if the item can be enabled or not.
	 */
	public UserMenuItem(ExperimenterData user, boolean canBeEnabled)
	{
		if (user == null) 
			throw new IllegalArgumentException("No user");
		setText(EditorUtil.formatExperimenter(user));
		this.canBeEnabled = canBeEnabled;
		this.user = user;
		setEnabled(true);
	}
	
	/**
	 * Returns the experimenter.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getExperimenter() { return user; }
	
	/**
	 * Overridden to set the enabled flag.
	 * @see #setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		if (!canBeEnabled) enabled = false;
		super.setEnabled(enabled);
	}

}
