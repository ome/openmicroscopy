/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TagPopupMenu 
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
package org.openmicroscopy.shoola.agents.util.tagging.view;



//Java imports
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.tagging.util.TaggedMenuItem;
import org.openmicroscopy.shoola.util.ui.IconManager;
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
public class TagPopupMenu 	
	extends JPopupMenu
	implements ActionListener
{

	/** Text indicating to create a new category. */
	private static final String CREATION = "Add tag to image";
	
	/** Text indicating to browse a given category. */
	private static final String BROWSE = "Browse tag";
	
	/** Text indicating to remove from a given category. */
	private static final String REMOVE = "Remove tag";
	
	/** Action command identifying the creation of a new category. */
	private static final int	CREATION_ID = 1;
	
	/** Reference to the controller. */
	private TaggerControl	controller;
	
	/** Reference to the Model. */
	private TaggerModel		model; 
	
	/**
	 * Formats the passed item.
	 * 
	 * @param item The item to format.
	 */
	private void formatItem(JComponent item)
	{
		item.setBorder(null);
        item.setFont((Font) TaggerFactory.getRegistry().lookup(
                        "/resources/fonts/Labels"));
	}
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	/*
    	IconManager icons = IconManager.getInstance();
    	Icon icon = icons.getIcon(IconManager.CANCEL);
    	setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    	JMenuItem item;
    	List categories = model.getCategories();
    	CategoryData data;
    	if (categories != null && categories.size() > 0) {
    		Iterator i = categories.iterator();
        	while (i.hasNext()) {
        		data = (CategoryData) i.next();
        		item = new CategoryItem(data);
        		formatItem(item);
        		item.addPropertyChangeListener(this);
        		item.setIcon(icon);
        		add(item);
    		}
        	add(new JSeparator());
    	}
    	item = new JMenuItem(CREATION);
    	item.setActionCommand(""+CREATION_ID);
    	item.addActionListener(this);
    	formatItem(item);
    	add(item);
    	
    	*/
    	
    	List categories = model.getTags();
    	CategoryData data;
    	JMenuItem item;
    	IconManager icons = IconManager.getInstance();
    	if (categories != null && categories.size() > 0) {
    		
    		JMenu menu = new JMenu(BROWSE);
    		menu.setIcon(icons.getIcon(IconManager.BROWSE));
    		//CategoryMenuItem menuItem;
    		Iterator i = categories.iterator();
        	while (i.hasNext()) {
        		data = (CategoryData) i.next();
        		item = new TaggedMenuItem(this, TaggedMenuItem.BROWSE, data);
        		formatItem(item);
        		menu.add(item);
    		}
        	add(menu);
        	menu = new JMenu(REMOVE);
    		menu.setIcon(icons.getIcon(IconManager.CANCEL));
    		i = categories.iterator();
        	while (i.hasNext()) {
        		data = (CategoryData) i.next();
        		item = new TaggedMenuItem(this, TaggedMenuItem.REMOVE, data);
        		formatItem(item);
        		item.addPropertyChangeListener(controller);
        		menu.add(item);
    		}
        	add(menu);
    	}
    	item = new JMenuItem(CREATION);
    	item.setIcon(icons.getIcon(IconManager.TRANSPARENT));
    	item.setActionCommand(""+CREATION_ID);
    	item.addActionListener(this);
    	formatItem(item);
    	add(item);
    }

    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     * @param model 	 Reference to the Model. Mustn't be <code>null</code>.
     */
	TagPopupMenu(TaggerControl controller, TaggerModel model)
	{
		if (controller == null)
			throw new IllegalArgumentException("No controller.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.controller = controller;
		this.model = model;
		initComponents();
	}
    
	/**
	 * Shows the menu. Wraps the call b/c we might need to use a different
	 * component.
	 * 
	 * @param source	The invoker.
	 * @param x			The x-coordinate.
	 * @param y			The y-coordinate.
	 */
	void showMenu(Component source, int x, int y)
	{
		show(source, x, y);
		/*
		Point p = source.getLocationOnScreen();
		setLocation(p.x+x, p.y+y);
		setVisible(true);
		pack();
		*/
	}
	
	/**
     * Browses the specified category.
     * 
     * @param tag 	The tag to browse.
     */
    public void browse(CategoryData tag)
    {
    	controller.browseTag(tag);
    }
    
	/**
	 * Browses the category when the item is selected or 
	 * creates a new category.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int id = Integer.parseInt(e.getActionCommand());
		
		switch (id) {
			case CREATION_ID:
				//view.createCategory();
				return;
		}
	}

}
