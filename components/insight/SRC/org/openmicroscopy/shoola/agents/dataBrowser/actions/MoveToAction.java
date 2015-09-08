/*
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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.events.treeviewer.MoveToEvent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.GroupData;

/** 
 * Indicates to move the data to the selected group.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MoveToAction
	extends DataBrowserAction
{

	/** The name of the action.*/
	public static final String NAME = "Move to";
	
	/** The name of the action.*/
	public static final String DESCRIPTION = "Select the group where to" +
			" move the data.";
	
	/** The group to move the data to.*/
	private GroupData group;
	
	/** 
	 * Sets the icon and tool tip text according to the permissions of the 
	 * group.
	 */
	private void setPermissions()
	{
		IconManager im = IconManager.getInstance();
		Icon icon = im.getIcon(IconManager.PERSONAL);
        String desc = "";
        switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
				icon = im.getIcon(IconManager.PRIVATE_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
				icon = im.getIcon(IconManager.READ_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
				icon = im.getIcon(IconManager.READ_LINK_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
				icon = im.getIcon(IconManager.READ_WRITE_GROUP);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
		}
        
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param group The selected group.
	 */
	public MoveToAction(DataBrowser model, GroupData group)
	{
		super(model);
		if (group == null)
			throw new IllegalArgumentException("No group.");
		this.group = group;
		setEnabled(true);
		putValue(Action.NAME, group.getName()+"...");
		setPermissions();
	}

	/**
     * Moves the selected objects to the group.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser b = model.getBrowser();
    	if (b == null) return;
    	MoveToEvent evt = new MoveToEvent(group, (List<DataObject>) 
    			b.getSelectedDataObjects());
    	DataBrowserAgent.getRegistry().getEventBus().post(evt);
    }

}
