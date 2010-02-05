/*
 * org.openmicroscopy.shoola.agents.admin.util.ExperimenterPane 
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Displays the fields necessary to collect details about the user to create.
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
class ExperimenterPane 
	extends DataPane
{

	/** Indicates that the user is an administrator. */
	private static final String STATUS_ADMIN = "Administrator";
	
	/** Indicates that the user is visible or not. */
	private static final String STATUS_VISIBLE = "Active";
	
	/** Password field to enter password. */
    private JPasswordField passwordField;
    
    /** The items that can be edited. */
    private Map<String, JTextField>	items;
    
    /** The user's details. */
    private Map<String, String>		details;
   
    /** Select to indicate that the user is an administrator. */
    private JCheckBox				adminBox;
    
    /** Select to indicate that the user is active. */
    private JCheckBox				activeBox;
    
    /** 
     * Initializes the components composing this display. 
     * 
     * @param administrator Pass <code>true</code> to indicate that the
     * 						user is an administrator, <code>false</code>
     * 						otherwise.
     */
    private void initComponents(boolean administrator)
    {
    	passwordField = new JPasswordField();
    	passwordField.getDocument().addDocumentListener(this);
    	details = EditorUtil.convertExperimenter(null);
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
		adminBox = new JCheckBox();
		adminBox.setSelected(administrator);
		adminBox.setEnabled(!administrator);
    }
    
    /**
     * Builds the panel hosting the user's details.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	Entry entry;
    	Iterator i = details.entrySet().iterator();
        JLabel label;
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
            if (key.equals(EditorUtil.DISPLAY_NAME)) {
            	label = UIUtilities.setTextFont(
            			key+EditorUtil.MANDATORY_SYMBOL);
            	area.getDocument().addDocumentListener(this);
            	nameArea = area;
            } else label = UIUtilities.setTextFont(key);
            items.put(key, area);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        c.gridx = 0;
        c.gridy++;
        label = UIUtilities.setTextFont("Password"+EditorUtil.MANDATORY_SYMBOL);
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
        label = UIUtilities.setTextFont("Active");
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
        content.add(activeBox, c);  
        c.gridx = 0;
        c.gridy++;
        label = UIUtilities.setTextFont("Administrator");
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
        c.gridx = 0;
        c.gridy++;
        content.add(Box.createHorizontalStrut(10), c); 
        c.gridy++;
        label = UIUtilities.setTextFont(EditorUtil.MANDATORY_DESCRIPTION,
        		Font.ITALIC);
        c.weightx = 0.0;  
        content.add(label, c);
        return content;
    }
    
    private JPanel buildStatus()
    {
    	JPanel p = new JPanel();
    	return p;
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
    }

    /** 
     * Creates a new instance. 
     * 
     * @param administrator Pass <code>true</code> to indicate that the
     * 						user is an administrator, <code>false</code>
     * 						otherwise.
     */
    ExperimenterPane(boolean administrator)
    {
    	initComponents(administrator);
    	buildGUI();
    }

	/**
	 * Returns the experimenter to save.
	 * @see DataPane#getObjectToSave()
	 */
	DataObject getObjectToSave()
	{
		ExperimenterData data = new ExperimenterData();
		JTextField field = items.get(EditorUtil.FIRST_NAME);
		data.setFirstName(field.getText().trim());
		field = items.get(EditorUtil.LAST_NAME);
		data.setLastName(field.getText().trim());
		field = items.get(EditorUtil.MIDDLE_NAME);
		//data.set(field.getText().trim());
		field = items.get(EditorUtil.EMAIL);
		data.setEmail(field.getText().trim());
		field = items.get(EditorUtil.INSTITUTION);
		data.setInstitution(field.getText().trim());
		return data;
	}
    
}
