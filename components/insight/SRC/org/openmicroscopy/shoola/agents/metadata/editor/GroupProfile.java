/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.PermissionsPane;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Displays information about the currently selected group.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class GroupProfile 
	extends AnnotationUI 
	implements DocumentListener, PropertyChangeListener
{

    /** The name of the <code>Group</code>. */
    private JTextField namePane;
    
    /** The description of the <code>Group</code>. */
    private JTextField descriptionPane;
    
    /** Component displaying the permissions status. */
    private PermissionsPane permissionsPane;
    
    /** The original permissions level. */
    private int level;
    
    /** Flag indicating if the name can be edited or not.*/
    private boolean canEdit;
    
    /** The group object displayed.*/
    private GroupData ref;
    
    /** Save changes.*/
    private JButton saveButton;
    
    /** Reference to the view.*/
    private EditorUI view;
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	ref = (GroupData) model.getRefObject();
    	namePane = new JTextField();
    	descriptionPane = new JTextField();
    	final boolean mayChangePermissions = model.isAdministrator() || model.isGroupLeader(ref);
    	//permission level

    	permissionsPane = new PermissionsPane(ref.getPermissions(),
    			UIUtilities.BACKGROUND_COLOR, model.isAdministrator());
    	level = permissionsPane.getPermissions();
    	permissionsPane.allowDowngrade(!mayChangePermissions);
    	permissionsPane.setBorder(
    			BorderFactory.createTitledBorder("Permissions"));
    	//permissionsPane.displayWarningText();
    	permissionsPane.addPropertyChangeListener(this);
    	namePane.setText(ref.getName());
    	descriptionPane.setText(ref.getDescription());
    	canEdit = mayChangePermissions && !model.isSystemGroup(ref.getId());
    	namePane.setEditable(canEdit);
    	namePane.setEnabled(canEdit);
    	descriptionPane.setEditable(canEdit);
    	permissionsPane.setEnabled(canEdit);
    	if (canEdit) {
    		namePane.getDocument().addDocumentListener(this);
    		descriptionPane.getDocument().addDocumentListener(this);
    	}
    	saveButton = new JButton("Save");
    	saveButton.setEnabled(false);
    	saveButton.setBackground(UIUtilities.BACKGROUND_COLOR);
    	saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	view.saveData(true); 
            }
        });
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
        JComponent label;
        content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		label = EditorUtil.getLabel("Name", true);
		label.setBackground(UIUtilities.BACKGROUND_COLOR);
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
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
        content.add(namePane, c);  
        c.gridx = 0;
        c.gridy++;
        label = UIUtilities.setTextFont("Description");
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
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
        content.add(descriptionPane, c);  
        c.gridx = 0;
        c.gridy++;
        label = UIUtilities.setTextFont(EditorUtil.MANDATORY_DESCRIPTION,
        		Font.ITALIC);
        label.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
        c.weightx = 0.0;  
        content.add(label, c);
        return content;
    }
    
    /**
     * Builds and lays out the component displaying the owners of the group.
     * 
     * @return See above.
     */
    private JPanel buildOwnersPane()
    {
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	
    	if (model.getRefObject() instanceof GroupData) {
        	GroupData group = (GroupData) model.getRefObject();
        	Set<ExperimenterData> leaders = group.getLeaders();
        	if (leaders == null || leaders.size() == 0) return p;
        	
        	Iterator<ExperimenterData> i = leaders.iterator();
        	ExperimenterData exp;
        	while (i.hasNext()) {
    			exp = (ExperimenterData) i.next();
    			p.add(new JLabel(EditorUtil.formatExperimenter(exp)));
    		}
    	}
    	else {
    	  return p;
    	}
    
    	JPanel content = UIUtilities.buildComponentPanel(p);
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createTitledBorder("Owners"));
    	content.add(p);
    	return content;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model	Reference to the model. Mustn't be <code>null</code>. 
     * @param view 	Reference to the control. Mustn't be <code>null</code>.
     */
	GroupProfile(EditorModel model, EditorUI view)
	{
		super(model);
		if (view == null)
			throw new IllegalArgumentException("No view");
		this.view = view;
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
    
	/** 
	 * Returns the object to update.
	 * 
	 * @return See above.
	 */
	AdminObject getAdminObject()
	{
		if (!canEdit) return null;
		GroupData data = (GroupData) model.getRefObject();
		String v = namePane.getText();
		v = v.trim();
		if (!data.getName().equals(v)) {
			if (model.doesGroupExist(data, v)) {
				UserNotifier un = 
					MetadataViewerAgent.getRegistry().getUserNotifier();
					un.notifyInfo("Update Group", "A group with the " +
							"same name already exists.");
					return null;
			}
			data.setName(v);
		}
		saveButton.setEnabled(false);
		//check description
		v = descriptionPane.getText();
		v = v.trim();
		String description = data.getDescription();
		if (description == null) description = "";
		if (!description.equals(v)) data.setDescription(v); 
		AdminObject o = new AdminObject(data, null, AdminObject.UPDATE_GROUP);
		if (level != permissionsPane.getPermissions())
			o.setPermissions(permissionsPane.getPermissions());
		return o;
	}
	
	/**
	 * Overridden to lay out the UI.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
    	initComponents();
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.weightx = 1.0;  
		p.add(buildContentPanel(), c);
    	c.gridy++;
    	p.add(permissionsPane, c);
		c.gridy++;
		p.add(buildOwnersPane(), c);
		c.gridy++;
		JPanel buttonPanel = UIUtilities.buildComponentPanel(saveButton);
    	buttonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(buttonPanel, c);
		setLayout(new BorderLayout(0, 0));
		add(p, BorderLayout.NORTH);
	}

	/**
	 * Removes all components.
	 * @see AnnotationUI#clearData(Object)
	 */
	protected void clearData(Object oldObject)
	{
		if (namePane != null) namePane.setText("");
		if (descriptionPane != null) descriptionPane.setText("");
		revalidate();
		repaint();
	}

	/**
	 * Removes all components.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() {}
	
	/**
	 * No-operation implementation in our case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<Object> getAnnotationToRemove()
	{ 
		return new ArrayList<Object>();  
	}

	/**
	 * No-operation implementation in our case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{ 
		return new ArrayList<AnnotationData>(); 
	}

	/**
	 * Returns the title associated to this component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return "Group"; }

	/**
	 * Returns <code>true</code> if user's info has been modified, 
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (saveButton != null) saveButton.setEnabled(false);
		if (namePane == null) return false;
		String v = namePane.getText();
		v = v.trim();
		if (v.length() == 0) {
			saveButton.setEnabled(false);
			return false;
		}
		if (!ref.getName().equals(v)) {
			if (saveButton != null) saveButton.setEnabled(true);
			return true;
		}
		//check description
		v = descriptionPane.getText();
		v = v.trim();
		String description = ref.getDescription();
		if (description == null) description = "";
		if (!description.equals(v)) {
			if (saveButton != null) saveButton.setEnabled(true);
			return true;
		}
		boolean b = level != permissionsPane.getPermissions();
		if (saveButton != null) saveButton.setEnabled(b);
		return b;
	}

	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle() {}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}
	
	/** 
	 * Listens to property fired by the {@link #permissionsPane}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (PermissionsPane.PERMISSIONS_CHANGE_PROPERTY.equals(name))
			firePropertyChange(EditorControl.SAVE_PROPERTY, 
					Boolean.valueOf(false), Boolean.valueOf(true));
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
