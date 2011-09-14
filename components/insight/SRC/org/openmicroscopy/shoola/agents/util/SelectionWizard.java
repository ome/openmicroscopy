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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.DataObject;
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
	implements ActionListener, DocumentListener, PropertyChangeListener
{
	
	/** Bound property indicating the selected items. */
	public static final String		SELECTED_ITEMS_PROPERTY = "selectedItems";
	
	/** Bound property indicating to cancel the selection. */
	public static final String		CANCEL_SELECTION_PROPERTY = 
		"cancelSelection";

	/** Action command ID to Accept the current field selection. */
	private static final int 		ACCEPT = 0;

	/** Action command ID to cancel the wizard. */
	private static final int 		CANCEL = 1;
	
	/** Action command ID to reset the current field selection. */
	private static final int 		RESET = 2;
	
	/** Action command ID to add new object to the selection. */
	private static final int 		ADD_NEW = 3;
	
	/** The default size. */
	private static final Dimension 	DEFAULT_SIZE = new Dimension(500, 500);
	
	/** The button to accept the current selection. */
	private JButton 			acceptButton;
	
	/** The button to reset the current selection. */
	private JButton 			resetButton;

	/** The button to cancel the current selection. */
	private JButton 			cancelButton;
	
	/** The type to handle. */
	private Class				type;
	
	/** Button to add new tag to the selection. */
	private JButton				addNewButton;
	
	/** The component used to add new objects. */
	private JTextField			addField;
	
	/** The component displaying the selection. */
	private SelectionWizardUI 	uiDelegate;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param userID The id of the user currently logged in.
	 */
	private void initComponents()
	{
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
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Closes and disposes. */
	private void cancel()
	{
		close();
		firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}
	
	/** Fires a property change with the selected items. */
	private void accept()
	{
		Map<Class, Collection<Object>> 
			r = new HashMap<Class, Collection<Object>>();
		r.put(type, uiDelegate.getSelection());
		firePropertyChange(SELECTED_ITEMS_PROPERTY, null, r);
		close();
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
			c.add(uiDelegate, BorderLayout.CENTER);
		else {
			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
			container.add(uiDelegate);
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
	
	/** Adds new objects to the selection list. */
	private void addNewObjects()
	{
		if (TagAnnotationData.class.equals(type)) {
			String text = addField.getText();
			if (text == null || text.trim().length() == 0) return;
			String[] names = text.split(SearchUtil.COMMA_SEPARATOR);
			TagAnnotationData data;
			List<DataObject> objects = new ArrayList<DataObject>();
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null && names[i].length() > 0) {	
					objects.add(new TagAnnotationData(names[i].trim()));
				}
			}
			uiDelegate.addObjects(objects);
			addField.setText("");
		}
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner		The owner of this dialog.
	 * @param available	The collection of available tags.
	 * @param type		The type of object to handle.
	 * @param userID    The if of the current user.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
						Class type, long userID)
	{
		this(owner, available, null, type, userID);
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
	 * @param userID        The id of the current user.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
						Class type, boolean addCreation, long userID)
	{
		this(owner, available, null, type, addCreation, userID);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner		The owner of this dialog.
	 * @param available	The collection of available items.
	 * @param selected	The collection of selected items.
	 * @param type		The type of object to handle. 
	 * @param userID    The if of the current user.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
						Collection<Object> selected, Class type, long userID)
	{
		this(owner, available, selected, type, false, userID);
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
	 * @param userID        The if of the current user.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> available, 
							Collection<Object> selected, Class type, 
							boolean addCreation, long userID)
	{
		super(owner);
		setModal(true);
		uiDelegate = new SelectionWizardUI(available, selected, type, userID);
		uiDelegate.addPropertyChangeListener(this);
		this.type = type;
		initComponents();
		buildUI(addCreation);
		setSize(DEFAULT_SIZE);
	}

	/**
	 * Sets the collection of nodes that cannot be removed.
	 * 
	 * @param immutable The collection to set.
	 */
	public void setImmutableElements(Collection immutable)
	{
		uiDelegate.setImmutableElements(immutable);
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
			case ACCEPT:
				accept();
				break;
			case CANCEL:
				cancel();
				break;
			case RESET:
				uiDelegate.reset();
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

	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SelectionWizardUI.SELECTION_CHANGE.equals(name)) {
			Boolean b = (Boolean) evt.getNewValue();
			acceptButton.setEnabled(b.booleanValue());
			resetButton.setEnabled(b.booleanValue());
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
