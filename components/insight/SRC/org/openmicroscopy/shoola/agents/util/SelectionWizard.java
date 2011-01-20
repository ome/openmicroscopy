/*
 * org.openmicroscopy.shoola.agents.util.SelectionWizard 
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.TagAnnotationData;

/** 
 * A modal dialog to select collection of objects.
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
public class SelectionWizard 
	extends JDialog 
	implements ActionListener, DocumentListener
{
	
	/** Bound property indicating the selected items. */
	public static final String		SELECTED_ITEMS_PROPERTY = "selectedItems";
	
	/** Action command ID to add a field to the result table. */
	private static final int 		ADD = 0;
	
	/** Action command ID to remove a field from the result table. */
	private static final int 		REMOVE = 1;
	
	/** Action command ID to add all fields to the result table. */
	private static final int 		ADD_ALL = 2;
	
	/** Action command ID to remove all fields from the result table. */
	private static final int 		REMOVE_ALL = 3;
	
	/** Action command ID to Accept the current field selection. */
	private static final int 		ACCEPT = 4;

	/** Action command ID to cancel the wizard. */
	private static final int 		CANCEL = 5;
	
	/** Action command ID to reset the current field selection. */
	private static final int 		RESET = 6;
	
	/** Action command ID to add new object to the selection. */
	private static final int 		ADD_NEW = 7;
	
	/** The default size. */
	private static final Dimension 	DEFAULT_SIZE = new Dimension(500, 500);
	
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
	
	/** The button to accept the current selection. */
	private JButton 			acceptButton;
	
	/** The button to reset the current selection. */
	private JButton 			resetButton;

	/** The button to cancel the current selection. */
	private JButton 			cancelButton;
	
	/** Sorts the object. */
	private ViewerSorter		sorter;
	
	/** The type to handle. */
	private Class				type;
	
	/** Button to add new tag to the selection. */
	private JButton				addNewButton;
	
	/** The component used to add new objects. */
	private JTextField			addField;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		sorter = new ViewerSorter();
		availableItemsListbox = new JList();
		DataObjectListCellRenderer rnd = new DataObjectListCellRenderer();
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
		acceptButton = new JButton("Accept");
		acceptButton.setToolTipText("Accept the selection.");
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Cancel the selection.");
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset the selection.");
		
		addNewButton = new JButton("Add");
		addNewButton.setEnabled(false);
		addNewButton.setToolTipText("Add the new elements to the selection.");
		addNewButton.setActionCommand(""+ADD_NEW);
		addNewButton.addActionListener(this);
		
		addButton.setActionCommand(""+ADD);
		addButton.addActionListener(this);
		addAllButton.setActionCommand(""+ADD_ALL);
		addAllButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		removeButton.addActionListener(this);
		removeAllButton.setActionCommand(""+REMOVE_ALL);
		removeAllButton.addActionListener(this);
		acceptButton.setActionCommand(""+ACCEPT);
		acceptButton.addActionListener(this);
		acceptButton.setEnabled(false);
		resetButton.setEnabled(false);
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		resetButton.setActionCommand(""+RESET);
		resetButton.addActionListener(this);
		
		
		//Field creation
		addField = new JTextField(20);
		addField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						addNewObjects();
				}
			}
		});
		addField.getDocument().addDocumentListener(this);
		//getRootPane().setDefaultButton(cancelButton);
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
		setButtonsEnabled();
	}
	
	/** Sorts the lists. */
	private void sortLists()
	{
		if (availableItems != null) 
			availableItems = sorter.sort(availableItems);
		if (selectedItems != null) selectedItems = sorter.sort(selectedItems);
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
		setButtonsEnabled();
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
		setButtonsEnabled();
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
		setButtonsEnabled();
	}
	
	/**
	 * Sets the enabled flag of the {@link #acceptButton} and
	 * {@link #resetButton}
	 */
	private void setButtonsEnabled()
	{
		if (originalSelectedItems.size() != selectedItems.size()) {
			acceptButton.setEnabled(true);
			resetButton.setEnabled(true);
		} else {
			boolean b = false;
			int n = 0;
			Iterator i = selectedItems.iterator();
			while (i.hasNext()) {
				if (originalSelectedItems.contains(i.next())) n++;
			}
			b = (n != originalSelectedItems.size());
			acceptButton.setEnabled(b);
			resetButton.setEnabled(b);
		}
	}
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Fires a property change with the selected items. */
	private void accept()
	{
		Map<Class, Collection<Object>> 
			r = new HashMap<Class, Collection<Object>>();
		r.put(type, selectedItems);
		firePropertyChange(SELECTED_ITEMS_PROPERTY, null, r);
		cancel();
	}
	
	/** Resets the selection to the original selection. */
	private void reset()
	{
		availableItems.clear();
		selectedItems.clear();
		for (Object item : originalItems)
			availableItems.add(item);
		for (Object item : originalSelectedItems)
			selectedItems.add(item);
		
		populateAvailableItems();
		populateSelectedItems();
		setButtonsEnabled();
	}
	
	/**
	 * Builds and lays out the selection components.
	 * 
	 * @return See above.
	 */
	private JPanel layoutSelectionPane()
	{
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		double[][] size = {{TableLayout.FILL, 40, TableLayout.FILL},
				{TableLayout.FILL}};
		container.setLayout(new TableLayout(size));
		container.add(createAvailableItemsPane(), "0, 0");
		container.add(createSelectionPane(), "1, 0, c, c");
		container.add(createSelectedItemsPane(), "2, 0");
		return container;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	private void buildUI(boolean addCreation)
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		
		if (!addCreation || !TagAnnotationData.class.equals(type)) 
			c.add(layoutSelectionPane(), BorderLayout.CENTER);
		else {
			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
			container.add(layoutSelectionPane());
			container.add(createAdditionPane());
			c.add(container, BorderLayout.CENTER);
		}
		c.add(createControlsPane(), BorderLayout.SOUTH);
	}
	
	/**
	 * Builds and lays out the components hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel createControlsPane()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setOpaque(false);
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(resetButton);
		controlPanel.add(acceptButton);
		controlPanel.add(cancelButton);
		return UIUtilities.buildComponentPanelRight(controlPanel);
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
	 * Builds and lays out the component to add new objects to the selection.
	 * 
	 * @return See above.
	 */
	private JPanel createAdditionPane()
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		String text = null;
		String tip = null;
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		if (TagAnnotationData.class.equals(type)) {
			tip = "Enter the new Tags, use comma to separate them.";
			text = "New Tag: ";
		}
		if (tip != null) {
			p.add(UIUtilities.buildComponentPanel(new JLabel(tip)));
		}
		if (text != null) {
			JPanel pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
			pane.add(UIUtilities.setTextFont(text));
			pane.add(addField);
			pane.add(addNewButton);
			p.add(pane);
		}
		return UIUtilities.buildComponentPanel(p);
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
	
	/** Updates the currentFields list box. */
	private void populateAvailableItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : availableItems)
			listModel.addElement(item);
		availableItemsListbox.setModel(listModel);
	}
	
	/**
	 * Returns <code>true</code> if an object object of the same type 
	 * already exist in the list, <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	private boolean doesTagExist(TagAnnotationData object)
	{
		Iterator<Object> i = availableItems.iterator();
		TagAnnotationData ob;
		String value = object.getTagValue();
		while (i.hasNext()) {
			ob = (TagAnnotationData) i.next();
			if (ob.getTagValue().equals(value)) return true;
		}
		i = selectedItems.iterator();
		while (i.hasNext()) {
			ob = (TagAnnotationData) i.next();
			if (ob.getTagValue().equals(value)) return true;
		}
		return false;
	}
	
	/** Adds new objects to the selection list. */
	private void addNewObjects()
	{
		if (TagAnnotationData.class.equals(type)) {
			String text = addField.getText();
			if (text == null || text.trim().length() == 0) return;
			String[] names = text.split(SearchUtil.COMMA_SEPARATOR);
			TagAnnotationData data;
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null && names[i].length() > 0) {	
					data = new TagAnnotationData(names[i].trim());
					if (!doesTagExist(data))
						selectedItems.add(data);
				}
			}
		}
		sortLists();	
		populateSelectedItems();
		addField.setText("");
		setButtonsEnabled();
	}
	
	/** Updates the remaining fields list box. */
	private void populateSelectedItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : selectedItems)
			listModel.addElement(item);
		
		selectedItemsListbox.setModel(listModel);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner		The owner of this dialog.
	 * @param available	The collection of available tags.
	 * @param type		The type of object to handle.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
						Class type)
	{
		this(owner, available, null, type);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner			The owner of this dialog.
	 * @param available		The collection of available tags.
	 * @param type			The type of object to handle.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
						Class type, boolean addCreation)
	{
		this(owner, available, null, type, addCreation);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner		The owner of this dialog.
	 * @param available	The collection of available items.
	 * @param selected	The collection of selected items.
	 * @param type		The type of object to handle.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
							Collection<Object> selected, Class type)
	{
		this(owner, available, selected, type, false);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner		    The owner of this dialog.
	 * @param available	    The collection of available items.
	 * @param selected	    The collection of selected items.
	 * @param type			The type of object to handle.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
							Collection<Object> selected, Class type, 
							boolean addCreation)
	{
		super(owner);
		setModal(true);
		if (selected == null) selected = new ArrayList<Object>();
		if (available == null) available = new ArrayList<Object>();
		this.availableItems = available;
		this.selectedItems = selected;
		this.type = type;
		createOriginalSelections();
		initComponents();
		sortLists();
		buildUI(addCreation);
		setSize(DEFAULT_SIZE);
	}

	/**
	 * Sets the title, the text and the icon displayed in the header.
	 * 
	 * @param title		The title to set.
	 * @param text		The text to set.
	 */
	public void setTitle(String title, String text)
	{
		setTitle(title, text, null);
	}
	
	/**
	 * Sets the title, the text and the icon displayed in the header.
	 * 
	 * @param title		The title to set.
	 * @param text		The text to set.
	 * @param titleIcon	The icon to set.
	 */
	public void setTitle(String title, String text, Icon titleIcon)
	{
		setTitle(title);
		if (titleIcon == null) {
			IconManager icons = IconManager.getInstance();
			titleIcon = icons.getIcon(IconManager.WIZARD_48);
		}
		TitlePanel titlePanel = new TitlePanel(title, text, titleIcon);
		getContentPane().add(titlePanel, BorderLayout.NORTH);
	}
	
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
				break;
			case ACCEPT:
				accept();
				break;
			case CANCEL:
				cancel();
				break;
			case RESET:
				reset();
				break;
			case ADD_NEW:
				addNewObjects();
		}
	}

	/**
	 * Sets the text of the {@link #acceptButton}.
	 * 
	 * @param text The value to set.
	 */
	public void setAcceptButtonText(String text)
	{
		if (acceptButton != null) acceptButton.setText(text);
	}
	
	/**
	 * Sets the enabled flag of the {@link #addNewButton}.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		String text = addField.getText();
		boolean b = (text != null && text.trim().length() > 0);
		addNewButton.setEnabled(b);
	}

	/**
	 * Sets the enabled flag of the {@link #addNewButton}.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		String text = addField.getText();
		boolean b = (text != null && text.trim().length() > 0);
		addNewButton.setEnabled(b);
	}

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
