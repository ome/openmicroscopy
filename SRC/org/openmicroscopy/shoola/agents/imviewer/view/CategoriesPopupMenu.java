/*
 * org.openmicroscopy.shoola.agents.imviewer.view.CategoriesPopupMenu 
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;

import pojos.CategoryData;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
class CategoriesPopupMenu
	extends JPopupMenu
	implements ActionListener
{

	/** Reference to the View. */
	private ImViewerUI view;
	
	/**
	 * Creates a menu item and sets the defaults.
	 * 
	 * @param data The category object hosted by the menu item.
	 * @return See above.
	 */
    private JMenuItem initMenuItem(CategoryData data)
    {
    	JMenuItem item = new JMenuItem(data.getName());
        item.setBorder(null);
        item.setFont((Font) ImViewerAgent.getRegistry().lookup(
                        "/resources/fonts/Labels"));
        item.setToolTipText(data.getDescription());
		item.setActionCommand(""+data.getId());
        item.addActionListener(this);
        return item;
    }
    
    /** 
     * Initializes the components composing the display. 
     * 
     * @param categories The categories to display.
     */
    private void initComponents(List categories)
    {
    	setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    	Iterator i = categories.iterator();
    	CategoryData data;
    	while (i.hasNext()) {
    		data = (CategoryData) i.next();
    		add(initMenuItem(data));
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view
     * @param categories
     */
	CategoriesPopupMenu(ImViewerUI view, List categories)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.view = view;
		initComponents(categories);
	}

	/**
	 * Browses the category when the item is selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		long id = Long.parseLong(e.getActionCommand());
		view.browse(id);
	}
	
}
