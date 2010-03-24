/*
 * org.openmicroscopy.shoola.agents.util.SelectionWizardUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.TagAnnotationData;


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
 * @since 3.0-Beta4
 */
public class SelectionWizardUI 
	extends JPanel
	implements ActionListener
{

	/** Bound property indicating that the selection has changed. */
	static final String SELECTION_CHANGE = "selectionChangeProperty";
	
	/** Action command ID to add a field to the result table. */
	private static final int 		ADD = 0;
	
	/** Action command ID to remove a field from the result table. */
	private static final int 		REMOVE = 1;
	
	/** Action command ID to add all fields to the result table. */
	private static final int 		ADD_ALL = 2;
	
	/** Action command ID to remove all fields from the result table. */
	private static final int 		REMOVE_ALL = 3;
	
	/** The original items before the user selects items. */
	private List<Object>		originalItems;
	
	/** The original selected items before the user selects items. */
	private List<Object>		originalSelectedItems;
	
	/** Collection of available items. */
	private Collection<Object>	availableItems;

	/** Collection of all the selected items. */
	private Collection<Object>	selectedItems;
	
	/** The list box showing the available items. */
	private JList				availableItemsListbox;
	
	/** The list box showing the selected items. */
	private JList				selectedItemsListbox;
	
	/** The button to move an item from the remaining items to current items. */
	private JButton 			addButton;
	
	/** The button to move an item from the current items to remaining items. */
	private JButton 			removeButton;
	
	/** The button to move all items to the current items. */
	private JButton 			addAllButton;
	
	/** The button to move all items to the remaining items. */
	private JButton 			removeAllButton;
	
	/** Sorts the object. */
	private ViewerSorter		sorter;
	
	/** The type to handle. */
	private Class				type;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param userID The id of the user currently logged in.
	 */
	private void initComponents(long userID)
	{
		sorter = new ViewerSorter();
		availableItemsListbox = new JList();
		DataObjectListCellRenderer rnd = new DataObjectListCellRenderer(userID);
		availableItemsListbox.setCellRenderer(rnd);
		selectedItemsListbox = new JList();
		selectedItemsListbox.setCellRenderer(rnd);
		IconManager icons = IconManager.getInstance();
		addButton = new JButton(icons.getIcon(IconManager.RIGHT_ARROW));
		removeButton = new JButton(icons.getIcon(IconManager.LEFT_ARROW));
		addAllButton = new JButton(
								icons.getIcon(IconManager.DOUBLE_RIGHT_ARROW));
		removeAllButton = new JButton(
								icons.getIcon(IconManager.DOUBLE_LEFT_ARROW));

		addButton.setActionCommand(""+ADD);
		addButton.addActionListener(this);
		addAllButton.setActionCommand(""+ADD_ALL);
		addAllButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		removeButton.addActionListener(this);
		removeAllButton.setActionCommand(""+REMOVE_ALL);
		removeAllButton.addActionListener(this);
	}
	
	/** Creates a copy of the original selections. */
	private void createOriginalSelections()
	{
		originalItems = new ArrayList<Object>();
		if (availableItems != null) {
			for (Object item : availableItems)
				originalItems.add(item);
		}
		
		originalSelectedItems  = new ArrayList<Object>();
		if (selectedItems != null) {
			for (Object item : selectedItems)
				originalSelectedItems.add(item);
		}
	}
	
	/** Adds all the items to the selection. */
	private void addAllItems()
	{
		//selectedItems.clear();
		for (Object item: availableItems)
			selectedItems.add(item);
		availableItems.clear();
		sortLists();
		populateAvailableItems();
		populateSelectedItems();
		setSelectionChange();
	}
	
	/** Removes an item from the selection. */
	private void removeItem()
	{
		if (selectedItemsListbox.getSelectedIndex() == -1) return;
		DefaultListModel model = (DefaultListModel)
									selectedItemsListbox.getModel();
		int [] indexes = selectedItemsListbox.getSelectedIndices();
		Object object; 
		TagAnnotationData tag;
		for (int i = 0 ; i < indexes.length ; i++) {
			object = model.getElementAt(indexes[i]);
			if (selectedItems.contains(object)) {
				selectedItems.remove(object);
				if (TagAnnotationData.class.equals(type)) {
					tag = (TagAnnotationData) object;
					if (tag.getId() > 0) availableItems.add(object);
				} else {
					availableItems.add(object);
				}
			}
		}
		
		sortLists();	
		populateAvailableItems();
		populateSelectedItems();
		setSelectionChange();
	}
	
	/** Removes all items from the selection. */
	private void removeAllItems()
	{
		if (TagAnnotationData.class.equals(type)) {
			TagAnnotationData tag;
			for (Object item: selectedItems) {
				tag = (TagAnnotationData) item;
				if (tag.getId() > 0)
					availableItems.add(item);
			}
		} else {
			for (Object item: selectedItems)
				availableItems.add(item);
		}
		
		selectedItems.clear();
		sortLists();
		populateAvailableItems();
		populateSelectedItems();
		setSelectionChange();
	}
	
	/** Adds an item to the list and then sorts the list to maintain order.  */
	private void addItem()
	{
		if (availableItemsListbox.getSelectedIndex() == -1) return;
		int [] indexes = availableItemsListbox.getSelectedIndices();
		DefaultListModel model = 
				(DefaultListModel) availableItemsListbox.getModel();
		Object object;
		for (int i = 0 ; i < indexes.length ; i++) {
			object = model.getElementAt(indexes[i]);
			if (availableItems.contains(object)) {
				selectedItems.add(object);
				availableItems.remove(object);
			}
		}
		sortLists();
		populateSelectedItems();
		populateAvailableItems();
		setSelectionChange();
	}
	
	/** Notifies that the selection has changed. */
	private void setSelectionChange()
	{
		boolean b = false;
		if (originalSelectedItems.size() != selectedItems.size()) {
			b = true;
		} else {
			int n = 0;
			Iterator i = selectedItems.iterator();
			while (i.hasNext()) {
				if (originalSelectedItems.contains(i.next())) n++;
			}
			b = (n != originalSelectedItems.size());
		}
		firePropertyChange(SELECTION_CHANGE, Boolean.valueOf(!b), 
				Boolean.valueOf(b));
	}
	
	/** Updates the remaining fields list box. */
	private void populateSelectedItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : selectedItems)
			listModel.addElement(item);
		
		selectedItemsListbox.setModel(listModel);
	}
	
	
	/** Updates the currentFields list box. */
	private void populateAvailableItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : availableItems)
			listModel.addElement(item);
		availableItemsListbox.setModel(listModel);
	}
	/** Sorts the lists. */
	private void sortLists()
	{
		if (availableItems != null) 
			availableItems = sorter.sort(availableItems);
		if (selectedItems != null) selectedItems = sorter.sort(selectedItems);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		double[][] size = {{TableLayout.FILL, 40, TableLayout.FILL},
				{TableLayout.FILL}};
		setLayout(new TableLayout(size));
		add(createAvailableItemsPane(), "0, 0");
		add(createSelectionPane(), "1, 0, CENTER, CENTER");
		add(createSelectedItemsPane(), "2, 0");
	}
	
	/**
	 * Builds and lays out the available tags.
	 * 
	 * @return See above.
	 */
	private JPanel createAvailableItemsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(UIUtilities.setTextFont("Available:"), BorderLayout.NORTH);
		p.add(new JScrollPane(availableItemsListbox), BorderLayout.CENTER);
		populateAvailableItems();
		return p;
	}

	/**
	 * Builds and lays out the buttons used to select tags.
	 * 
	 * @return See above.
	 */
	private JPanel createSelectionPane()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalStrut(30));
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(addAllButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeAllButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		return buttonPanel;
	}
	
	/**
	 * Builds and lays out the list of selected tags.
	 * 
	 * @return See above.
	 */
	private JPanel createSelectedItemsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(UIUtilities.setTextFont("Selected:"), BorderLayout.NORTH);
		p.add(new JScrollPane(selectedItemsListbox), BorderLayout.CENTER);
		populateSelectedItems();
		return p;
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param available	The collection of available items.
	 * @param type		The type of object to handle. 
	 * @param userID    The if of the current user.
	 */
	public SelectionWizardUI(Collection<Object> available, Class type, 
							long userID)
	{
		this(available, null, type, userID);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param available	The collection of available items.
	 * @param selected	The collection of selected items.
	 * @param type		The type of object to handle. 
	 * @param userID    The if of the current user.
	 */
	public SelectionWizardUI(Collection<Object> available, 
							Collection<Object> selected, Class type, 
							long userID)
	{
		if (selected == null) selected = new ArrayList<Object>();
		if (available == null) available = new ArrayList<Object>();
		this.availableItems = available;
		this.selectedItems = selected;
		this.type = type;
		createOriginalSelections();
		initComponents(userID);
		sortLists();
		buildGUI();
	}
	
	/** Resets the selection. */
	void reset()
	{
		availableItems.clear();
		selectedItems.clear();
		for (Object item : originalItems)
			availableItems.add(item);
		for (Object item : originalSelectedItems)
			selectedItems.add(item);
		
		populateAvailableItems();
		populateSelectedItems();
		setSelectionChange();
	}
	
	/**
	 * Returns <code>true</code> if an object object of the same type 
	 * already exist in the list, <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	private boolean doesObjectExist(DataObject object)
	{
		Iterator<Object> i = availableItems.iterator();
		if (object instanceof TagAnnotationData) {
			TagAnnotationData ob;
			String value = ((TagAnnotationData) object).getTagValue();
			while (i.hasNext()) {
				ob = (TagAnnotationData) i.next();
				if (ob.getTagValue().equals(value)) return true;
			}
			i = selectedItems.iterator();
			while (i.hasNext()) {
				ob = (TagAnnotationData) i.next();
				if (ob.getTagValue().equals(value)) return true;
			}
		}
		return false;
	}
	
	void addObjects(List<DataObject> toAdd)
	{
		if (toAdd == null || toAdd.size() == 0) return;
		Iterator<DataObject> i = toAdd.iterator();
		DataObject data;
		while (i.hasNext()) {
			data = i.next();
			if (!doesObjectExist(data)) {
				selectedItems.add(data);
			}
		}
		sortLists();	
		populateSelectedItems();
		setSelectionChange();
	}
	
	/**
	 * Returns the selected items.
	 * 
	 * @return See above.
	 */
	public Collection<Object> getSelection() { return selectedItems; }
	
	/**
	 * Reacts to event fired by the various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int id = Integer.parseInt(evt.getActionCommand());
		switch (id) {
			case ADD:
				addItem();
				break;
			case ADD_ALL:
				addAllItems();
				break;
			case REMOVE:
				removeItem();
				break;
			case REMOVE_ALL:
				removeAllItems();
		}
	}
	
}
