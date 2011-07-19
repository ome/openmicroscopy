/*
 * org.openmicroscopy.shoola.agents.util.ui.PermissionsPane 
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
package org.openmicroscopy.shoola.agents.util.ui;


//Java import
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.PermissionData;

/** 
 * Displays the permissions options.
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
public class PermissionsPane 
	extends JPanel
	implements ChangeListener
{

	/** Bound property indicating that the permissions have been modified. */
	public static final String  PERMISSIONS_CHANGE_PROPERTY = 
		"permissionsChange";
	
	/** Warning message. */
	private static final String WARNING = "Upgrading the permissions cannot" +
			" be undone.";
	
	/** Indicate that the group will have group visibility. */
    private JRadioButton		groupBox;
    
    /** Indicate that the group will be private. */
    private JRadioButton		privateBox;
    
    /** Indicate that the group will be private. */
    private JRadioButton		publicBox;
    
    /** 
     * Indicates if the group is <code>Read Only</code> or 
     * <code>Read Write</code>.
     */
    private JCheckBox			readOnlyGroupBox;
    
    /** 
     * Indicates if the group is <code>Read Only</code> or 
     * <code>Read Write</code>.
     */
    private JCheckBox			readOnlyPublicBox;
    
    /** The label hosting the default text. */
    private JLabel				label;
    
    /** 
     * Initializes the components. 
     * 
     * @param permissions The permissions level.
     */
    private void initComponents(int permissions)
    {
    	label = new JLabel();
    	Font f = label.getFont();
    	label.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
    	readOnlyGroupBox = new JCheckBox("Read-Only");
    	//readOnlyGroupBox.setSelected(true);
    	readOnlyGroupBox.setEnabled(false);
    	readOnlyPublicBox = new JCheckBox("Read-Only");
    	//readOnlyPublicBox.setSelected(true);
    	readOnlyPublicBox.setEnabled(false);
    	groupBox = new JRadioButton(EditorUtil.GROUP_VISIBLE);
        privateBox =  new JRadioButton(EditorUtil.PRIVATE); 
        publicBox =  new JRadioButton(EditorUtil.PUBLIC); 
        ButtonGroup group = new ButtonGroup();
        group.add(privateBox);
        group.add(groupBox);
        group.add(publicBox);
        switch (permissions) {
			case AdminObject.PERMISSIONS_PRIVATE:
				privateBox.setSelected(true);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ:
				privateBox.setEnabled(false);
				groupBox.setSelected(true);
				readOnlyGroupBox.setSelected(true);
				readOnlyGroupBox.setEnabled(true);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
				privateBox.setEnabled(false);
				groupBox.setSelected(true);
				readOnlyGroupBox.setSelected(false);
				readOnlyGroupBox.setEnabled(true);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ:
				privateBox.setEnabled(false);
				groupBox.setEnabled(false);
				readOnlyPublicBox.setSelected(true);
				publicBox.setSelected(true);
				readOnlyPublicBox.setEnabled(true);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				privateBox.setEnabled(false);
				groupBox.setEnabled(false);
				readOnlyPublicBox.setSelected(false);
				publicBox.setSelected(true);
				readOnlyPublicBox.setEnabled(true);
		}
        
        readOnlyGroupBox.setBackground(getBackground());
        readOnlyPublicBox.setBackground(getBackground());
        privateBox.setBackground(getBackground());
        groupBox.setBackground(getBackground());
        publicBox.setBackground(getBackground());
        label.setBackground(getBackground());
       
        readOnlyGroupBox.addChangeListener(this);
        readOnlyPublicBox.addChangeListener(this);
        groupBox.addChangeListener(this);
        publicBox.addChangeListener(this);
        privateBox.addChangeListener(this);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel p = new JPanel();
    	p.setBackground(getBackground());
    	p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
    	c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;  
		p.add(privateBox, c);
    	c.gridx = 0;
		c.gridy++;
		p.add(groupBox, c);
        c.gridx++;
    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	p.add(readOnlyGroupBox, c);
    	/*
    	 * TODO: Turn back on when implemented server side.
        c.gridy++;
    	c.gridx = 0;
    	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;  
		p.add(publicBox, c);
        c.gridx++;
    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	p.add(readOnlyPublicBox, c);
    	*/
    	JPanel content = new JPanel();
    	content.setBackground(getBackground());
    	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    	JPanel lp = UIUtilities.buildComponentPanel(label);
    	lp.setBackground(getBackground());
    	content.add(lp);
    	content.add(p);
    	setLayout(new FlowLayout(FlowLayout.LEFT));
    	add(content);
    }

	/** Creates a new instance. */
	public PermissionsPane()
	{
		this(AdminObject.PERMISSIONS_PRIVATE);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param background	The background color or <code>null</code>.
	 */
	public PermissionsPane(Color background)
	{
		this(AdminObject.PERMISSIONS_PRIVATE, background);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions The permissions level.
	 */
	public PermissionsPane(int permissions)
	{
		this(permissions, null);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 */
	public PermissionsPane(int permissions, Color background)
	{
		if (background != null) setBackground(background);
		initComponents(permissions);
		buildGUI();
	}

	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 */
	public PermissionsPane(PermissionData permissions)
	{
		this(permissions, null);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 */
	public PermissionsPane(PermissionData permissions, Color background)
	{
		int level = AdminObject.PERMISSIONS_PRIVATE;
		if (permissions != null) {
			if (permissions.isGroupRead()) {
	    		if (permissions.isGroupWrite()) 
	    			level = AdminObject.PERMISSIONS_GROUP_READ_LINK;
	    		else level = AdminObject.PERMISSIONS_GROUP_READ;
	    	} else if (permissions.isWorldRead()) {
	    		if (permissions.isGroupWrite()) 
	    			level = AdminObject.PERMISSIONS_PUBLIC_READ_WRITE;
	    		else level = AdminObject.PERMISSIONS_PUBLIC_READ;
	    	}
		}
    	if (background != null) setBackground(background);
		initComponents(level);
		buildGUI();
	}
	
	/**
	 * Returns the selected permissions level i.e. one of the constants defined
	 * by <code>AdminObject</code>.
	 * 
	 * @return See above.
	 */
	public int getPermissions()
	{
		if (privateBox.isSelected()) return AdminObject.PERMISSIONS_PRIVATE;
		if (groupBox.isSelected()) {
			if (readOnlyGroupBox.isSelected())
				return AdminObject.PERMISSIONS_GROUP_READ; 
			return AdminObject.PERMISSIONS_GROUP_READ_LINK; 
		}
		if (readOnlyPublicBox.isSelected())
			return AdminObject.PERMISSIONS_PUBLIC_READ; 
		return AdminObject.PERMISSIONS_PUBLIC_READ_WRITE; 
	}

	/** Displays the warning text. */
	public void displayWarningText()
	{
		label.setText(WARNING);
		repaint();
	}
	
	/** Disables all the controls. */
	public void disablePermissions()
	{
		publicBox.removeChangeListener(this);
		groupBox.removeChangeListener(this);
		readOnlyGroupBox.setEnabled(false);
		readOnlyPublicBox.setEnabled(false);
    	groupBox.setEnabled(false);
        privateBox.setEnabled(false);
        publicBox.setEnabled(false);
        
        groupBox.addChangeListener(this);
        publicBox.addChangeListener(this);
        removeAll();
        JPanel p;
        switch (getPermissions()) {
			case AdminObject.PERMISSIONS_PRIVATE:
				add(privateBox);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ:
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
				p = new JPanel();
				p.setBackground(getBackground());
				p.add(groupBox);
				p.add(readOnlyGroupBox);
				add(p);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ:
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				p = new JPanel();
				p.setBackground(getBackground());
				p.add(publicBox);
				p.add(readOnlyPublicBox);
				add(p);
		}
	}
	
	/**
	 * Sets the enabled flag of the {@link #readOnlyGroupBox} and
	 * {@link #readOnlyPublicBox}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (readOnlyGroupBox == src || readOnlyPublicBox == src) {
			firePropertyChange(PERMISSIONS_CHANGE_PROPERTY, 
					-1, getPermissions());
		} else {
			readOnlyGroupBox.setEnabled(false);
			readOnlyPublicBox.setEnabled(false);
			if (groupBox == src) readOnlyGroupBox.setEnabled(true);
			else if (publicBox == src) readOnlyPublicBox.setEnabled(true);
			firePropertyChange(PERMISSIONS_CHANGE_PROPERTY, 
					-1, getPermissions());
		}
	}
	
}
