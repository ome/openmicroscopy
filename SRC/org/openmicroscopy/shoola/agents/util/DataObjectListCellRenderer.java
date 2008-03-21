/*
 * org.openmicroscopy.shoola.agents.metadata.util.DataObjectListCellRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;


//Java imports
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;

import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

/** 
 * Renderer used to display various kind of <code>DataObject</code>s in 
 * a table.
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
public class DataObjectListCellRenderer 
	extends DefaultListCellRenderer
{

	/** Helper reference to the icon manager. */
	private IconManager icons;
	
	/** The id of the user currently logged in. */
	private long		currentUserID;
	
	/** Creates a new instance. */
	public DataObjectListCellRenderer()
	{
		this(-1);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 *  @param currentUserID The id of the user currently logged in.
	 */
	public DataObjectListCellRenderer(long currentUserID)
	{
		this.currentUserID = currentUserID;
		icons = IconManager.getInstance();
	}
	
	/**
	 * Overridden to set the text and icon corresponding to the selected object.
	 * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object,
	 * 								int, boolean, boolean)
	 */
	public Component getListCellRendererComponent (JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus)
	{
		super.getListCellRendererComponent(list, value, index, isSelected, 
										cellHasFocus);
		if (value instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) value;
			setText(tag.getTagValue());
			setIcon(icons.getIcon(IconManager.TAG));
		} else if (value instanceof FileAnnotationData) {
			FileAnnotationData fad = (FileAnnotationData) value;
			setText(fad.getFileName());
			setIcon(icons.getIcon(IconManager.FILE));
		}
		return this;
	}
	
}
