/*
 * org.openmicroscopy.shoola.agents.util.ui.PermissionsPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.ui.RefWindow;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.GroupData;
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
	implements ActionListener
{

	/** Bound property indicating that the permissions have been modified. */
	public static final String  PERMISSIONS_CHANGE_PROPERTY = 
		"permissionsChange";
	
	/** Warning message. */
	private static final String WARNING_TITLE = "Permissions Downgrade";

	/** Warning message. */
	private static final String WARNING =
	        " Changing group to Private unlinks data from other users'\n" +
	        " containers and unlinks other users' annotations from data.\n" +
	        " The change to Private will abort if different users' data\n" +
	        " is too closely related to be separated. Make the change?";

	/** ReadWrite warning message */
    private static final String RW_WARNING = "Read-Write groups allow members to delete other"
            + " members' data.\nSee documentation about 'OMERO permissions' for full details.";

	/** Indicate that the group has <code>RWRA--</code>. */
    //private JRadioButton		collaborativeGroupBox;
    
    /** Indicate that the group has <code>RW----</code>. */
    private JRadioButton		readOnlyGroupBox;
    
    /** Indicate that the group has <code>RWRA--</code>. */
    private JRadioButton		readAnnotateGroupBox;
    
    /** Indicate that the group has <code>RWRW--</code>. */
    private JRadioButton		readWriteGroupBox;
    
    /** Indicate that the group will be private. */
    private JRadioButton		privateBox;
    
    /** Indicate that the group will be private. */
    private JRadioButton		publicBox;

    /** 
     * Indicates if the group is <code>Read Only</code> or 
     * <code>Read Write</code>.
     */
    private JCheckBox			readOnlyPublicBox;
    
    /** The label hosting the default text. */
    private JLabel				label;

    /**
     * Flag indicating if the current user is allowed to downgrade to 
     * private or not.
     */
    private boolean allowDowngrade;
    
    /** Flag indicating if the current user is an admin */
    private boolean admin;
    
    /** The original permissions level.*/
    private int originalPermissions;
    
    /** The current permissions level.*/
    private int currentPermissions;
    
    /** Internal flag making sure the RW warning message is only shown once */
    private boolean warningMessageShown = false;
    
    /**
     * Sets the controls depending on the specified permissions.
     * 
     * @param permissions The permissions to handle.
     */
    private void setPermissions(int permissions)
    {
    	currentPermissions = permissions;
    	switch (permissions) {
		case GroupData.PERMISSIONS_PRIVATE:
			privateBox.setSelected(true);
			break;
		case GroupData.PERMISSIONS_GROUP_READ:
			readOnlyGroupBox.setSelected(true);
			break;
		case GroupData.PERMISSIONS_GROUP_READ_LINK:
			readAnnotateGroupBox.setSelected(true);
			break;
		case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			readWriteGroupBox.setSelected(true);
			break;
		case GroupData.PERMISSIONS_PUBLIC_READ:
			readOnlyPublicBox.setSelected(true);
			publicBox.setSelected(true);
			break;
		case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
			publicBox.setSelected(true);
			readOnlyPublicBox.setEnabled(true);
		}
    }
    
    /** 
     * Initializes the components.
     * 
     * @param permissions The permissions level.
     */
    private void initComponents(int permissions)
    {
    	originalPermissions = permissions;
    	allowDowngrade = true;
    	label = new JLabel();
    	Font f = label.getFont();
    	label.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
    	readOnlyGroupBox = new JRadioButton(
    			GroupData.PERMISSIONS_GROUP_READ_SHORT_TEXT);
    	readOnlyGroupBox.setToolTipText(
    			GroupData.PERMISSIONS_GROUP_READ_TEXT);
    	readAnnotateGroupBox = new JRadioButton(
    			GroupData.PERMISSIONS_GROUP_READ_LINK_SHORT_TEXT);
    	readAnnotateGroupBox.setToolTipText(
    			GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT);
    	readWriteGroupBox = new JRadioButton(
    			GroupData.PERMISSIONS_GROUP_READ_WRITE_SHORT_TEXT);
    	readWriteGroupBox.setToolTipText(
    			GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT);
    	
        readWriteGroupBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (readWriteGroupBox.isSelected() && !warningMessageShown) {
                    // for some reason can't use NotificationDialog here, just shows
                    // a black panel instead of the text, using JOptionPane therefore
                    JOptionPane.showMessageDialog(
                            PermissionsPane.this,
                            RW_WARNING,
                            "Warning",
                            JOptionPane.WARNING_MESSAGE,
                            IconManager.getInstance().getIcon(
                                    IconManager.INFO_32));
                    warningMessageShown = true;
                }
            }
        });
    	
    	readOnlyGroupBox.setSelected(true);//default
    	readOnlyPublicBox = new JCheckBox(
    			GroupData.PERMISSIONS_GROUP_READ_SHORT_TEXT);
        privateBox = new JRadioButton(EditorUtil.PRIVATE);
        publicBox = new JRadioButton(EditorUtil.PUBLIC);
        ButtonGroup group = new ButtonGroup();
        group.add(privateBox);
        group.add(publicBox);
        
        //group = new ButtonGroup();
        group.add(readOnlyGroupBox);
        group.add(readAnnotateGroupBox);
        group.add(readWriteGroupBox);
        
        setPermissions(permissions);
        
        
        readOnlyGroupBox.setBackground(getBackground());
        readOnlyPublicBox.setBackground(getBackground());
        privateBox.setBackground(getBackground());
        readAnnotateGroupBox.setBackground(getBackground());
        readWriteGroupBox.setBackground(getBackground());
        
        publicBox.setBackground(getBackground());
        label.setBackground(getBackground());
      
        publicBox.addActionListener(this);
        privateBox.addActionListener(this);
        readOnlyGroupBox.addActionListener(this);
        readAnnotateGroupBox.addActionListener(this);
    }
    
    /**
     * Builds and lays out the components used to select the level of the
     * collaborative group.
     * 
     * @return See above.
     */
    private JPanel buildCollaborative()
    {
    	JPanel p = new JPanel(new GridLayout(2, 2));
    	p.setBackground(getBackground());
    	p.add(readOnlyGroupBox);
    	p.add(readAnnotateGroupBox);
    	p.add(readWriteGroupBox);
    	return p;
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
		c.gridwidth = GridBagConstraints.RELATIVE;//next-to-last
		c.fill = GridBagConstraints.NONE;//reset to default
		c.weightx = 0.0;
		JPanel row = new JPanel();
		row.setBackground(getBackground());
		row.add(privateBox);
		p.add(row, c);
    	c.gridx = 0;
		//c.gridy++;
		//p.add(collaborativeGroupBox, c);
		c.gridy++;
    	c.gridwidth = GridBagConstraints.REMAINDER;//end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	//c.weightx = 0.75;
    	p.add(buildCollaborative(), c);
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

    /**
     * Initializes and builds the UI
     * 
     * @param permissions The permissions level.
     * @param background The background color or <code>null</code>.
     * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
     */
    private void initialize(int permissions, Color background, boolean admin)
    {
    	if (background != null) 
    	    setBackground(background);
    	this.admin = admin;
		initComponents(permissions);
		buildGUI();
    }
    
	/** Creates a new instance.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
     */
	public PermissionsPane(boolean admin)
	{
		this(GroupData.PERMISSIONS_PRIVATE, admin);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param background	The background color or <code>null</code>.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
	 */
	public PermissionsPane(Color background, boolean admin)
	{
		this(GroupData.PERMISSIONS_PRIVATE, background, admin);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions The permissions level.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
	 */
	public PermissionsPane(int permissions, boolean admin)
	{
		this(permissions, null, admin);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
	 */
	public PermissionsPane(int permissions, Color background, boolean admin)
	{
		initialize(permissions, background, admin);
	}

	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
	 */
	public PermissionsPane(PermissionData permissions, boolean admin)
	{
		this(permissions, null, admin);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param permissions 	The permissions level.
	 * @param background	The background color or <code>null</code>.
	 * @param admin
     *            Pass <code>true</code> to enable admin-only permission changes
	 */
	public PermissionsPane(PermissionData permissions, Color background, boolean admin)
	{
		int level = GroupData.PERMISSIONS_PRIVATE;
		if (permissions != null)
			level = permissions.getPermissionsLevel();
		initialize(level, background, admin);
	}
	
	/**
	 * Resets the permissions level.
	 * 
	 * @param permissions The level to handle.
	 */
	public void resetPermissions(PermissionData permissions)
	{
	    if (permissions == null) return;
	    removeAll();
	    initialize(permissions.getPermissionsLevel(), getBackground(), admin);
	    revalidate();
	    repaint();
	}
	
	/**
	 * Returns the selected permissions level i.e. one of the constants defined
	 * by <code>AdminObject</code>.
	 * 
	 * @return See above.
	 */
	public int getPermissions()
	{
		if (privateBox.isSelected())
		    return GroupData.PERMISSIONS_PRIVATE;
		if (readAnnotateGroupBox.isSelected())
			return GroupData.PERMISSIONS_GROUP_READ_LINK;
		if (readOnlyGroupBox.isSelected())
			return GroupData.PERMISSIONS_GROUP_READ;
		if (readOnlyPublicBox.isSelected())
			return GroupData.PERMISSIONS_PUBLIC_READ;
		if (readWriteGroupBox.isSelected())
            return GroupData.PERMISSIONS_GROUP_READ_WRITE;
		return -1;
	}

	/** 
	 * Displays the warning text.
	 * 
	 * @param text The warning text.
	 */
	public void displayWarningText()
	{
		label.setText(WARNING);
		repaint();
	}
	
	/**
	 * Allows the user to downgrade to private.
	 * 
	 * @param downgrade Pass <code>true</code> to allow, <code>false</code>
	 *                  otherwise.
	 */
	public void allowDowngrade(boolean downgrade)
	{
		this.allowDowngrade = downgrade;
	}
	
	/** Disables all the controls. */
	public void disablePermissions()
	{
		publicBox.removeActionListener(this);
		//collaborativeGroupBox.removeChangeListener(this);
		privateBox.removeActionListener(this);
		
		readOnlyGroupBox.setEnabled(false);
		readOnlyPublicBox.setEnabled(false);
    	readAnnotateGroupBox.setEnabled(false);
    	readWriteGroupBox.setEnabled(false);
        privateBox.setEnabled(false);
        publicBox.setEnabled(false);
        
        //collaborativeGroupBox.addChangeListener(this);
        publicBox.addActionListener(this);
        privateBox.addActionListener(this);
        
        removeAll();
        JPanel p;
        switch (getPermissions()) {
			case GroupData.PERMISSIONS_PRIVATE:
				add(privateBox);
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				p = buildCollaborative();
				add(p);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				p = new JPanel();
				p.setBackground(getBackground());
				p.add(publicBox);
				p.add(readOnlyPublicBox);
				add(p);
		}
	}
	
	/**
	 * Overridden to set the flag for all components composing the display.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		publicBox.removeActionListener(this);
		//collaborativeGroupBox.removeChangeListener(this);
		privateBox.removeActionListener(this);
		readOnlyGroupBox.removeActionListener(this);
		readAnnotateGroupBox.removeActionListener(this);
		readWriteGroupBox.removeActionListener(this);
		if (privateBox != null) privateBox.setEnabled(enabled);
		if (publicBox != null) publicBox.setEnabled(enabled);
		//if (collaborativeGroupBox != null)
		//	collaborativeGroupBox.setEnabled(enabled);
		if (readOnlyGroupBox != null) readOnlyGroupBox.setEnabled(enabled);
		if (readOnlyPublicBox != null) readOnlyPublicBox.setEnabled(enabled);
		if (readWriteGroupBox != null)
			readWriteGroupBox.setEnabled(admin);
		if (readAnnotateGroupBox != null)
			readAnnotateGroupBox.setEnabled(enabled);
		publicBox.addActionListener(this);
		//collaborativeGroupBox.addChangeListener(this);
		privateBox.addActionListener(this);
		readOnlyGroupBox.addActionListener(this);
		readAnnotateGroupBox.addActionListener(this);
		readWriteGroupBox.addActionListener(this);
	}

	/**
	 * Turns the controls on/off
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		//turn controls on/off
		if (!allowDowngrade && privateBox == src && 
				originalPermissions > GroupData.PERMISSIONS_PRIVATE) {
			MessageBox d = new MessageBox(new RefWindow(), WARNING_TITLE,
					WARNING);
			if (d.centerMsgBox() == MessageBox.YES_OPTION) {
				firePropertyChange(PERMISSIONS_CHANGE_PROPERTY, -1,
						getPermissions());
			} else {
				setPermissions(currentPermissions);
			}
			currentPermissions = getPermissions();
			return;
		}
		currentPermissions = getPermissions();
 		if (readOnlyGroupBox == src || readAnnotateGroupBox == src ||
				privateBox == src || readWriteGroupBox == src) {
			firePropertyChange(PERMISSIONS_CHANGE_PROPERTY, -1,
					getPermissions());
			return;
		}
		readWriteGroupBox.setEnabled(false);
		readOnlyGroupBox.setEnabled(false);
		readAnnotateGroupBox.setEnabled(false);

		firePropertyChange(PERMISSIONS_CHANGE_PROPERTY, -1, getPermissions());
	}
	
}