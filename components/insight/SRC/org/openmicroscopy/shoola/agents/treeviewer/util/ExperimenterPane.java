/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.SelectionWizardUI;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Displays the fields necessary to collect details about the user to create.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ExperimenterPane 
	extends DataPane
	implements PropertyChangeListener
{
	
	/** Bound property indicating to enable of not the save property. */
	static final String EXPERIMENTER_ENABLE_SAVE_PROPERTY = 
		"exprimenterEnableSave";
	
	/** Password field to enter password. */
    private JPasswordField 			passwordField;
    
    /** The items that can be edited. */
    private Map<String, JTextField>	items;
    
    /** The user's details. */
    private Map<String, String>		details;
   
    /** Select to indicate that the user is an administrator. */
    private JCheckBox				adminBox;
    
    /** Select to indicate that the user is active. */
    private JCheckBox				activeBox;
    
    /** Select to indicate that the user is an owner of the group. */
    private JCheckBox				ownerBox;
    
    /** The Button to display existing user as possible group owner. */
    private JButton					groupOwner;
    
    /** The component used to select the groups. */
    private SelectionWizardUI		selectionComponent;
    
    /** Flag indicating that the password is required. */
    private boolean 				passwordRequired;
    
    /** 
     * Initializes the components composing this display. 
     * 
     * @param available	The collection of groups is any available.
     * @param selected  The collection of selected groups.
     */
    private void initComponents(Collection available, Collection selected)
    {
    	passwordField = new JPasswordField();
    	passwordField.getDocument().addDocumentListener(this);
    	//details = EditorUtil.convertExperimenter(null);
    	details = new LinkedHashMap<String, String>();
    	details.put(EditorUtil.DISPLAY_NAME, "");
    	details.put(EditorUtil.FIRST_NAME, "");
    	details.put(EditorUtil.MIDDLE_NAME, "");
		details.put(EditorUtil.LAST_NAME, "");
		details.put(EditorUtil.EMAIL, "");
		details.put(EditorUtil.INSTITUTION, "");
		items = new LinkedHashMap<String, JTextField>();
		activeBox = new JCheckBox();
		activeBox.setSelected(true);
		activeBox.setEnabled(!passwordRequired);
		adminBox = new JCheckBox();
		//adminBox.setVisible(administrator);
		ownerBox = new JCheckBox();
		ownerBox.setEnabled(passwordRequired);
		ownerBox.setSelected(!passwordRequired);
		IconManager icons = IconManager.getInstance();
		groupOwner = new JButton(icons.getIcon(IconManager.USER_GROUP));
		groupOwner.setToolTipText("Select an existing user as owner");
		groupOwner.setVisible(passwordRequired);
		if (available == null && selected == null) return;
		selectionComponent = new SelectionWizardUI(null, available, selected,
				GroupData.class, TreeViewerAgent.getUserDetails());
		selectionComponent.addPropertyChangeListener(this);
		addPropertyChangeListener(this);
    }
    
    /**
     * Builds the panel hosting the user's details.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
    	//content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	Entry entry;
    	Iterator i = details.entrySet().iterator();
        JComponent label;
        JTextField area;
        String key, value;
        content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            entry = (Entry) i.next();
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            area = new JTextField(value);
            area.setEditable(true);
            if (EditorUtil.DISPLAY_NAME.equals(key)) {
            	label = EditorUtil.getLabel(key, true);
            	area = nameArea;
            } else if (EditorUtil.FIRST_NAME.equals(key) ||
            		EditorUtil.LAST_NAME.equals(key)) {
            	label = EditorUtil.getLabel(key, true);
            } else label = UIUtilities.setTextFont(key);
            items.put(key, area);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0.0;  
            content.add(label, c);

            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        c.gridx = 0;
        c.gridy++;
        label = EditorUtil.getLabel("Password", passwordRequired); 
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        c.gridx++;
        content.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(passwordField, c);  
        c.gridx = 0;
        c.gridy++;
        label = EditorUtil.getLabel(EditorUtil.GROUP_OWNER, false); 
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        c.gridx++;
        content.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(ownerBox, c); 
        if (adminBox.isVisible()) {
        	c.gridx = 0;
            c.gridy++;
            label = EditorUtil.getLabel(EditorUtil.ADMINISTRATOR, false);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(adminBox, c);  
        }
        c.gridx = 0;
        c.gridy++;
        content.add(Box.createHorizontalStrut(10), c); 
        c.gridy++;
        label = UIUtilities.setTextFont(EditorUtil.MANDATORY_DESCRIPTION,
        		Font.ITALIC);
        label.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
        c.weightx = 0.0;  
        content.add(label, c);
        return content;
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
    	c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.weightx = 1.0;  
    	add(buildContentPanel(), c);
    	if (selectionComponent != null) {
    		JPanel p = new JPanel();
    		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    		p.add(UIUtilities.buildComponentPanel(
    				UIUtilities.setTextFont("Select the group(s) to add the " +
    				"User to")));
    		p.add(selectionComponent);
    		c.gridy++;
    		add(new JSeparator(), c);
    		c.gridy++;
    		add(p, c);
    	}
    }

    /** 
     * Creates a new instance. 
     * 
     * @param passwordRequired 	Pass <code>true</code> to indicate that a
     * 							password is required, <code>false</code>
     * 							otherwise.
     * @param groups			The available groups.
     * @param selected			The selected groups.
     */
    ExperimenterPane(boolean passwordRequired, Collection<DataObject> available,
    		Collection<DataObject> selected)
    {
    	this.passwordRequired = true;//passwordRequired;
    	initComponents(available, selected);
    	buildGUI();
    }
    
	/**
	 * Returns the experimenter to save.
	 * 
	 * @return See above.
	 */
	Map<ExperimenterData, UserCredentials> getObjectToSave()
	{
		JTextField field = items.get(EditorUtil.DISPLAY_NAME);
		String s = field.getText().trim();
		
		ExperimenterData data = new ExperimenterData();
		field = items.get(EditorUtil.FIRST_NAME);
		String value = field.getText().trim();
		if (value.length() == 0) value = "";
		data.setFirstName(value);
		field = items.get(EditorUtil.LAST_NAME);
		value = field.getText().trim();
		if (value.length() == 0) value = "";
		data.setLastName(value);
		field = items.get(EditorUtil.MIDDLE_NAME);
		value = field.getText();
		if (value == null) value = "";
		data.setMiddleName(value.trim());
		field = items.get(EditorUtil.EMAIL);
		data.setEmail(field.getText().trim());
		field = items.get(EditorUtil.INSTITUTION);
		data.setInstitution(field.getText().trim());
		
		Map<ExperimenterData, UserCredentials> m = 
			new HashMap<ExperimenterData, UserCredentials>();
		
		StringBuffer buf = new StringBuffer();
		buf.append(passwordField.getPassword());
		
		if (s == null || s.length() == 0) return m;
		String pass = buf.toString();
		if (passwordRequired) {
			if (pass == null || pass.length() == 0) {
				UserNotifier un =
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Create Experimenter", "Please Enter a Password");
				return m;
			}
		}
		UserCredentials uc = new UserCredentials(s, pass);
		uc.setAdministrator(adminBox.isSelected());
		uc.setOwner(ownerBox.isSelected());
		m.put(data, uc);
		return m;
	}
    
	/**
	 * Returns the selected groups.
	 * 
	 * @return See above.
	 */
	List<GroupData> getSelectedGroups()
	{
		List<GroupData> groups = new ArrayList<GroupData>();
		if (selectionComponent != null) {
			Collection l = selectionComponent.getSelection();
			if (l != null) {
				Iterator i = l.iterator();
				Object o;
				while (i.hasNext()) {
					o = i.next();
					if (o instanceof GroupData)
						groups.add((GroupData) o);
				}
			}
		}
		return groups;
	}

	/**
	 * Returns <code>true</code> if the login name has been populated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasLoginName()
	{
		JTextField field = items.get(EditorUtil.DISPLAY_NAME);
		String s = field.getText().trim();
		return (s.length() != 0);
	}
	
	/**
	 * Returns <code>true</code> if the login name has been populated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasLoginCredentials()
	{
		JTextField field = items.get(EditorUtil.DISPLAY_NAME);
		String s = field.getText().trim();
		int count = 0;
		if (s.length() != 0) count++;
		field = items.get(EditorUtil.FIRST_NAME);
		s = field.getText().trim();
		if (s.length() != 0) count++;
		field = items.get(EditorUtil.LAST_NAME);
		s = field.getText().trim();
		if (s.length() != 0) count++;
		return count == 3;
	}
	
	/**
	 * Controls if criteria are met to create a new user.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (AdminDialog.ENABLE_SAVE_PROPERTY.equals(name) ||
				SelectionWizardUI.SELECTION_CHANGE.equals(name)) {
			int count = 0;
			if (hasLoginCredentials()) count++;
			StringBuffer buf = new StringBuffer();
			buf.append(passwordField.getPassword());
			String v = buf.toString();
			if (v.trim().length() > 0) count++;
			List<GroupData> groups = getSelectedGroups();
			if (groups != null && groups.size() > 0)
				count++;
			firePropertyChange(EXPERIMENTER_ENABLE_SAVE_PROPERTY, null, 
					count == 3);
		}
	}
	
}
