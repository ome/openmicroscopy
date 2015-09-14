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
package org.openmicroscopy.shoola.agents.treeviewer.util;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.PermissionsPane;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Displays the parameters to create the group.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class GroupPane 
	extends DataPane
{

	/** Component used when creating the owner of the group.*/
	private ExperimenterPane expPane;
	
    /** The mandatory name. */
    private JTextField descriptionArea;
    
    /** The component displaying the permissions options. */
    private PermissionsPane permissions;

    /** Initializes the components.
     * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
     */
    private void initComponents(boolean admin)
    {
    	permissions = new PermissionsPane(admin);
    	permissions.setBorder(
				BorderFactory.createTitledBorder("Permissions"));
    	descriptionArea = new JTextField();
    	expPane = new ExperimenterPane(false, null, null);
    	expPane.setBorder(
				BorderFactory.createTitledBorder("Owner"));
    	expPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (AdminDialog.ENABLE_SAVE_PROPERTY.equals(
						evt.getPropertyName()))
				firePropertyChange(AdminDialog.ENABLE_SAVE_PROPERTY,
						evt.getOldValue(), evt.getNewValue());
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
        content.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	JComponent label = EditorUtil.getLabel("Name", true);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;
        c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 0;
        content.add(label, c);
        c.gridx++;
        add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(nameArea, c); 
        c.gridy++;
        label = UIUtilities.setTextFont("Description");
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.weightx = 1.0;  
		c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;  
        c.gridx = 0;
        content.add(label, c);
        c.gridx++;
        add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(descriptionArea, c);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.weightx = 1.0;  
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
        c.gridy++;
        add(buildContentPanel(), c);
        c.gridy++;
        add(permissions, c);
        c.gridy++;
        add(expPane, c);
    }
    
    /**
     * Creates a new instance
     * 
     * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
     */
	GroupPane(boolean admin)
	{
		initComponents(admin);
		buildGUI();
	}
	/**
	 * Returns <code>true</code> if the login name has been populated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRequiredFields()
	{
		int count = 0;
		//if (expPane.hasLoginCredentials()) count++;
		if (isNameValid()) count++;
		return count == 1;
	}
	
	/**
	 * Returns the object to save.
	 * 
	 * @return See above.
	 */
	AdminObject getObjectToSave()
	{
		GroupData data = new GroupData();
		data.setName(nameArea.getText().trim());
		data.setDescription(descriptionArea.getText().trim());
		Map<ExperimenterData, UserCredentials> 
		m = new HashMap<ExperimenterData, UserCredentials>();
		if (expPane.hasLoginCredentials())
			m = expPane.getObjectToSave();
		AdminObject object = new AdminObject(data, m, AdminObject.CREATE_GROUP);
		object.setPermissions(permissions.getPermissions());
		return object;
	}

}
