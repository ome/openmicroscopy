/*
 * org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.tagging.util;




//Java imports
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;

/** 
 * Basic renderer used to display the icon corresponding to a tag 
 * or a tag group.
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
public class TagCellRenderer 	
	extends DefaultListCellRenderer
{

	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_OTHER_OWNER_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		TAG_ICON = icons.getIcon(IconManager.TAG);
		TAG_OTHER_OWNER_ICON = icons.getIcon(IconManager.TAG_OTHER_OWNER);
	}
	
	/** The id of the currently logged in user. */
	private long		userID;
	
	/** Creates a new instance. */
	public TagCellRenderer()
	{
		this(-1);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param userID The id of the user currently logged in
	 */
	public TagCellRenderer(long userID)
	{
		this.userID = userID;
	}
	
	/**
	 * Overridden to set the icon corresponding to a tag or a tag set.
	 * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object, 
	 * 													int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus)
	{
		
		super.getListCellRendererComponent(list, value, index, isSelected, 
										cellHasFocus);
		if (value instanceof TagItem) {
			TagItem v = (TagItem) value;
			setText(v.getObjectName());
			if (userID == v.getOwnerID()) setIcon(TAG_ICON);
			else setIcon(TAG_OTHER_OWNER_ICON);
		}
		
		return this;
	}
	
}
