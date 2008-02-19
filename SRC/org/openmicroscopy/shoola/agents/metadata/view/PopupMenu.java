/*
 * org.openmicroscopy.shoola.agents.metadata.view.PopupMenu 
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * Pop-up menu for nodes in the browser display.
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
class PopupMenu
	extends JPopupMenu
{

	/** Button to browse a container or bring up the Viewer for an image. */
	private JMenuItem				view;
	
	/** Button to add a new element e.g. a new tag. */
	private JMenuItem				add;
	
	/** Button to remove an element e.g. a tag. */
	private JMenuItem				remove;
	
	/** Button to remove all elements of a given type. */
	private JMenuItem				removeAll;
	 
	/** Reference to the Control. */
	private MetadataViewerControl	controller;
	
	/** Helper method to create the menu items with the given actions. */
	private void createMenuItems()
	{
		view = new JMenuItem(
				controller.getAction(MetadataViewerControl.BROWSE));
		add = new JMenuItem(controller.getAction(MetadataViewerControl.ADD));
		remove = new JMenuItem(
				controller.getAction(MetadataViewerControl.REMOVE));
		removeAll = new JMenuItem(
				controller.getAction(MetadataViewerControl.REMOVE_ALL));
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(view);
		add(add);
		add(remove);
		add(removeAll);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 */
	PopupMenu(MetadataViewerControl	controller)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
		createMenuItems();
		buildGUI();
	}
	
}
