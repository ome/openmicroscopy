/*
 * org.openmicroscopy.shoola.agents.metadata.editor.GroupProfile 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.PermissionData;

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
 * @since 3.0-Beta4
 */
class GroupProfile 
	extends JPanel
	implements DocumentListener
{

    /** Indicates if the <code>DataObject</code> is only visible by owner. */
    private JRadioButton 			privateBox;
    
    /** 
     * Indicates if the <code>DataObject</code> is only visible by members
     * of the group the user belongs to. 
     */
    private JRadioButton 			groupBox;
    
	/** Reference to the Model. */
    private EditorModel				model;
    
    /** The component hosting the name of the <code>DataObject</code>. */
    private JTextField				namePane;
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	groupBox = new JRadioButton(EditorUtil.GROUP_VISIBLE);
    	groupBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	groupBox.setToolTipText(EditorUtil.GROUP_DESCRIPTION);
    	//groupBox.setEnabled(false);
    	privateBox =  new JRadioButton(EditorUtil.PRIVATE);
    	privateBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	privateBox.setSelected(true);
    	//privateBox.setEnabled(false);
    	ButtonGroup group = new ButtonGroup();
    	group.add(privateBox);
    	group.add(groupBox);
    	groupBox.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			upgradePermissions();
    		}
    	});
    	namePane = new JTextField();
    	namePane.setEditable(false);
    	GroupData data = (GroupData) model.getRefObject();
    	namePane.setText(data.getName());
    }
    
    /** Upgrades the permissions of all data within the selected group. */
    private void upgradePermissions()
    {
    	//Ask Question to user if already users.
    	MessageBox msg = new MessageBox(
    			MetadataViewerAgent.getRegistry().getTaskBar().getFrame(), 
    			"Permissions update", 
		"Upgrading the permissions cannot be undone. \nAre you sure you " +
		"want to continue?");
		msg.setYesText("Upgrade");
		int option = msg.centerMsgBox();
		if (option == MessageBox.YES_OPTION)
			model.upgradePermissions();
		else privateBox.setSelected(true);
    }

    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(PermissionData permissions)
    {
        JPanel content = new JPanel();
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        boolean b = true;;
       	if (permissions != null && permissions.isGroupRead()) {
       		//groupBox.setSelected(true);
       		//b = false;
       	}
   		groupBox.setEnabled(b);
   		privateBox.setEnabled(b);
       	content.add(privateBox);
       	content.add(groupBox);
       	JPanel p = UIUtilities.buildComponentPanel(content, 0, 0);
       	p.setBackground(UIUtilities.BACKGROUND_COLOR);
       	p.setBorder(BorderFactory.createTitledBorder("Permission"));
       	
        return p;
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
        JLabel label;
        content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		label = UIUtilities.setTextFont("Name"+EditorUtil.MANDATORY_SYMBOL);
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(namePane);
        c.gridx++;
        content.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(namePane, c);  

        c.gridx = 0;
        c.gridy++;
        label = UIUtilities.setTextFont(EditorUtil.MANDATORY_DESCRIPTION,
        		Font.ITALIC);
        c.weightx = 0.0;  
        content.add(label, c);
        return content;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model	Reference to the model. Mustn't be <code>null</code>. 
     * @param view 	Reference to the control. Mustn't be <code>null</code>.                     
     */
	GroupProfile(EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/** Builds and lays out the UI. */
    void buildUI()
    {
    	GroupData group = (GroupData) model.getRefObject();
    	removeAll();
    	initComponents();
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
    	add(Box.createVerticalStrut(5), c); 
		c.gridy++;
		add(buildPermissions(group.getPermissions()), c);
    }
    
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		return false;
	}
	
	/**
	 * Returns the group to save.
	 * 
	 * @return See above.
	 */
	GroupData getDataToSave()
	{
		return null;
	}
	
	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
