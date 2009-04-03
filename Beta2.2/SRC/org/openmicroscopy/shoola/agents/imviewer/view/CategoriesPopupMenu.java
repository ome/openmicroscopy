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


//Java imports
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.CategoryData;

/** 
 * Menu displaying the category this image belongs to.
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
	implements ActionListener, PropertyChangeListener
{

	/** Text indicating to create a new category. */
	private static final String CREATION = "Add category to image";
	
	/** Text indicating to browse a given category. */
	private static final String BROWSE = "Browse category";
	
	/** Text indicating to remove from a given category. */
	private static final String REMOVE = "Remove category";
	
	/** Action command identifying the creation of a new category. */
	private static final int	CREATION_ID = 1;
	
	/** Reference to the View. */
	private ImViewerUI 		view;
	
	/** Reference to the Model. */
	private ImViewerModel	model; 
	
	/**
	 * Formats the passed item.
	 * 
	 * @param item The item to format.
	 */
	private void formatItem(JComponent item)
	{
		item.setBorder(null);
        item.setFont((Font) ImViewerAgent.getRegistry().lookup(
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
    	
    	List categories = model.getCategories();
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
        		item = new BasicCategoryMenuItem(this, 
        				BasicCategoryMenuItem.BROWSE, data);
        		formatItem(item);
        		menu.add(item);
        		//menuItem = new CategoryMenuItem(data);
        		//formatItem(menuItem);
        		//menuItem.addPropertyChangeListener(this);
        		//add(menuItem);
    		}
        	add(menu);
        	menu = new JMenu(REMOVE);
    		menu.setIcon(icons.getIcon(IconManager.CANCEL));
    		i = categories.iterator();
        	while (i.hasNext()) {
        		data = (CategoryData) i.next();
        		item = new BasicCategoryMenuItem(this, 
        				BasicCategoryMenuItem.REMOVE, data);
        		formatItem(item);
        		item.addPropertyChangeListener(this);
        		menu.add(item);
        		//menuItem = new CategoryMenuItem(data);
        		//formatItem(menuItem);
        		//menuItem.addPropertyChangeListener(this);
        		//add(menuItem);
    		}
        	add(menu);
        	//add(new JSeparator());
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
     * @param view	Reference to the View. Mustn't be <code>null</code>.
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	CategoriesPopupMenu(ImViewerUI view, ImViewerModel model)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.view = view;
		this.model = model;
		initComponents();
	}

	/**
     * Browses the specified category.
     * 
     * @param categoryID 	The id of the category.
     * @param userID		The id of the user.
     */
    void browse(long categoryID, long userID)
    {
    	EventBus bus = ImViewerAgent.getRegistry().getEventBus();
    	bus.post(new Browse(categoryID, Browse.CATEGORY, 
    			model.getUserDetails(), view.getBounds()));  
    }
    
    /**
     * Removes the images from the passed category.
     * 
     * @param categoryID The category's ID.
     */
    void declassify(long categoryID)
    {
    	view.declassify(categoryID);
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
	 * Browses the category when the item is selected or 
	 * creates a new category.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int id = -1;
		try {
			id = Integer.parseInt(e.getActionCommand());
		} catch (Exception ex) {}
		
		switch (id) {
			case CREATION_ID:
				view.createCategory();
				return;
		}
	}

	/**
	 * Removes the image from the category or
	 * browses the category.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (CategoryMenuItem.REMOVE_PROPERTY.equals(name)) {
			CategoryData item = (CategoryData) evt.getNewValue();
			view.declassify(item.getId());
		} else if (CategoryMenuItem.BROWSE_PROPERTY.equals(name)) {
			CategoryData item = (CategoryData) evt.getNewValue();
			browse(item.getId(), item.getOwner().getId());
		}
	}

}
