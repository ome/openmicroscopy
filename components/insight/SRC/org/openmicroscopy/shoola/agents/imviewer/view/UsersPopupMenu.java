/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.imviewer.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import omero.gateway.model.ExperimenterData;

/** 
 * Menu displaying the users who viewed the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class UsersPopupMenu
	extends JPopupMenu
	implements ActionListener
{

	/** Bound property indicating to set the rnd settings for a given user. */
	static final String USER_RNDSETTINGS_PROPERTY = "userRndSettings";
	
	/** Reference to the model. */
	private ImViewerModel	model;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		Map m = model.getRenderingSettings();
		if (m == null) return; //should not happen
		long userID = model.getAlternativeSettingsOwnerId();
		
		ViewerSorter sorter = new ViewerSorter();
		List list = sorter.sort(m.keySet());
		Iterator i = list.iterator();
		UserItem item;
		ExperimenterData exp;
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.USER);
		long id = model.getUserDetails().getId();
		if (userID != -1) id = userID;
		ButtonGroup group = new ButtonGroup();
		long ownerID = model.getOwnerID();
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (ownerID == exp.getId()) {
				item = new UserItem(exp, icon);
				item.setFont(item.getFont().deriveFont(Font.BOLD));
				item.setToolTipText("Image's owner");
				item.setSelected(id == exp.getId());
				group.add(item);
				item.addActionListener(this);
				add(item);
				add(new JSeparator());
			}
		}
		i = list.iterator();
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (ownerID != exp.getId()) {
				item = new UserItem(exp, icon);
				item.setSelected(id == exp.getId());
				group.add(item);
				item.addActionListener(this);
				add(item);
			}
		}
	}

	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	UsersPopupMenu(ImViewerModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
	}
	
	/**
	 * Sets the settings set by the selected user.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src instanceof UserItem) {
			UserItem item = (UserItem) src;
			firePropertyChange(USER_RNDSETTINGS_PROPERTY, null, 
								item.getExperimenter());
		}
	}
	
	/** Helper inner class displaying the experimenter. */
	class UserItem 
		extends JCheckBoxMenuItem
	{
		
		/** The experimenter to host. */
		private ExperimenterData exp;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param exp	The experimenter to host. 
		 * 				Mustn't be <code>null</code>.
		 * @param icon	The icon to set.
		 */
		UserItem(ExperimenterData exp, Icon icon) 
		{
			if (exp == null)
				throw new IllegalArgumentException("No experimenter.");
			this.exp = exp;
			setIcon(icon);
			setText(toString());
		}
		
		/**
		 * Returns the experimenter.
		 * 
		 * @return See above.
		 */
		ExperimenterData getExperimenter() { return exp; }
		
		/**
		 * Overridden to return the first and last name of the
		 * experimenter.
		 * @see Object#toString()
		 */
		public String toString() 
		{
			return exp.getFirstName()+" "+exp.getLastName();
		}
		
	}
	
}
