/*
 * org.openmicroscopy.shoola.agents.imviewer.view.BasicCategoryMenuItem 
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
package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;


//Third-party libraries

//Application-internal dependencies
import pojos.CategoryData;

/** 
 * 
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
class BasicCategoryMenuItem
	extends JMenuItem
	implements ActionListener
{

	/** Action Id indicating to browse the category. */
	static final int			BROWSE = 0;
	
	/** Action Id indicating to browse the category. */
	static final int			REMOVE = 1;
	
	/** The category hosted by this node. */
	private CategoryData 		data;
	
	/** The parent. */
	private CategoriesPopupMenu	parent;
	
	/** One of the constants defined by this class. */
	private int					actionID;
	
	/**
	 * Controls if the passed id is supported.
	 * 
	 * @param id The action Id.
	 */
	private void checkActionID(int id)
	{
		switch (id) {
			case REMOVE:
			case BROWSE:
				return;
	
			default:
				throw new IllegalArgumentException("ID not valid.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The parent of this item. Mustn't be <code>null</code>.
	 * @param actionID	The action to handle.
	 * @param data		The data hosted by this node. 
	 * 					Mustn't be <code>null</code>.
	 */
	BasicCategoryMenuItem(CategoriesPopupMenu parent, int actionID,
						CategoryData data)
	{
		if (data == null)
			throw new IllegalArgumentException("No category specified.");
		if (parent == null)
			throw new IllegalArgumentException("No parent specified.");
		checkActionID(actionID);
		this.actionID = actionID;
		this.data = data;
		this.parent = parent;
		setText(data.getName());
		setToolTipText(data.getDescription());
		addActionListener(this);
	}

	/**
	 * Fires a property change when selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		switch (actionID) {
			case BROWSE:
				parent.browse(data.getId(), data.getOwner().getId());
				break;
			case REMOVE:
				parent.declassify(data.getId());
				break;
		}
	}
	
}
