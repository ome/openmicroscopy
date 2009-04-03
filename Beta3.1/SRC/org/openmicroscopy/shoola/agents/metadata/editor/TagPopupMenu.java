/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TagPopupMenu 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;

/** 
 * Pop up menu used to handle the tags.
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
class TagPopupMenu 
	extends JPopupMenu
	implements ActionListener
{

	/** Action to delete the selected elements. */
	private static final int DELETE = 0;
	
	/** Action to browse the selected elements. */
	private static final int BROWSE = 1;
	
	/** Action to edit the selected elements. */
	private static final int EDIT = 2;
	
	/** Button to delete the selected elements. */
	private JMenuItem 	delete;
	
	/** Button to browse the selected elements. */
	private JMenuItem 	browse;
	
	/** Button to edit the selected element. */
	private JMenuItem 	edit;
	
	/** Reference to the view. */
	private TagsUI 		uiDelegate;
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		delete = new JMenuItem("Remove");
		delete.addActionListener(this);
		delete.setActionCommand(""+DELETE);
		delete.setIcon(icons.getIcon(IconManager.REMOVE));
		delete.setToolTipText("Remove the selected tags.");
		browse = new JMenuItem("Browse");
		browse.addActionListener(this);
		browse.setActionCommand(""+BROWSE);
		browse.setIcon(icons.getIcon(IconManager.BROWSE));
		browse.setToolTipText("Browse the selected tags.");
		edit = new JMenuItem("Describe");
		edit.addActionListener(this);
		edit.setActionCommand(""+EDIT);
		edit.setIcon(icons.getIcon(IconManager.EDIT));
		edit.setToolTipText("Describe the selected tags.");
		//edit.setEnabled(uiDelegate.getSelectedTagsCount() == 1);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(edit);
		add(delete);
		//add(browse);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param uiDelegate The view.
	 */
	TagPopupMenu(TagsUI uiDelegate)
	{
		this.uiDelegate = uiDelegate;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Delete, edit or browse the selected elements.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE:
				uiDelegate.removeSelectedTags(); 
				break;
			case EDIT:
				uiDelegate.editSelectedTags(); 
				break;
			case BROWSE:
				uiDelegate.browseSelectedTags(); 
				break;
		}
		
	}
	
}
