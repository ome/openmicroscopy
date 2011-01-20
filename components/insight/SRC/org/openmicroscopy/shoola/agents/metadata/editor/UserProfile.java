/*
 * org.openmicroscopy.shoola.agents.metadata.editor.UserProfile 
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
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.GroupsRenderer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.PermissionData;

/** 
 * Component displaying the user details.
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
class UserProfile 	
	extends JPanel
	implements ActionListener, DocumentListener
{
    
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_OLD = "Old password";
	
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_NEW = "New password";
	
	/** Text of the label in front of the confirmation password area. */
	private static final String		PASSWORD_CONFIRMATION = "Confirm password";
	
	/** The title of the dialog displayed if a problem occurs. */
	private static final String		PASSWORD_CHANGE_TITLE = "Change Password";
	
    /** The editable items. */
    private Map<String, JTextField>	items;
    
    /** UI component displaying the groups, the user is a member of. */
    private JComboBox				groups;

    /** Password field to enter the new password. */
    private JPasswordField			passwordNew;
    
    /** Password field to confirm the new password. */
    private JPasswordField			passwordConfirm;
    
    /** Hosts the old password. */
    private JPasswordField			oldPassword;

    /** Modify password. */
    private JButton					passwordButton;
    
	/** Reference to the Model. */
    private EditorModel				model;
    
    /** The original index. */
    private int						originalIndex;
    
    /** The currently selected index. */
    private int						selectedIndex;
    
    /** The user's details. */
    private Map						details;
    
    /** The groups the user is a member of. */
    private GroupData[] 			groupData;

    /** Indicates if the <code>DataObject</code> is only visible by owner. */
    private JRadioButton 		privateBox;
    
    /** 
     * Indicates if the <code>DataObject</code> is only visible by members
     * of the group the user belongs to. 
     */
    private JRadioButton 		groupBox;
    
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
       	p.setBorder(
				BorderFactory.createTitledBorder("Permission for all data"));
       	
        return p;
    }
    
    /** Modifies the existing password. */
    private void changePassword()
    {
    	UserNotifier un;
    	StringBuffer buf = new StringBuffer();
        buf.append(passwordNew.getPassword());
        String newPass = buf.toString();
        
        String pass = buf.toString();
        buf = new StringBuffer();
        buf.append(passwordConfirm.getPassword());
        String confirm = buf.toString();

        buf = new StringBuffer();
        buf.append(oldPassword.getPassword());
        String old = buf.toString();
        if (old == null || old.trim().length() == 0) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(PASSWORD_CHANGE_TITLE, 
        				"Please specify your old password.");
        	oldPassword.requestFocus();
        	return;
        }
        if (newPass == null || newPass.length() == 0) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(PASSWORD_CHANGE_TITLE, 
        			"Please enter your new password.");
        	passwordNew.requestFocus();
        	return;
        }

        if (pass == null || confirm == null || confirm.length() == 0 ||
        	!pass.equals(confirm)) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo(PASSWORD_CHANGE_TITLE, 
            			"The passwords entered do not match. " +
            			"Please try again.");
            passwordNew.setText("");
            passwordConfirm.setText("");
            passwordNew.requestFocus();
            return;
        }
        model.changePassword(old, confirm);
    }
    
    /** Upgrades the permissions of all data within the selected group. */
    private void upgradePermissions()
    {
    	GroupData data = (GroupData) groups.getSelectedItem();
    	//ask Question to user.
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
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	boolean isOwner = model.isCurrentUserOwner(model.getRefObject());
    	passwordButton =  new JButton("Change password");
    	passwordButton.setBackground(UIUtilities.BACKGROUND_COLOR);
    	passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	changePassword(); 
            }
        });
    	passwordNew = new JPasswordField();
    	passwordNew.setBackground(UIUtilities.BACKGROUND_COLOR);
    	passwordConfirm = new JPasswordField();
    	passwordConfirm.setBackground(UIUtilities.BACKGROUND_COLOR);
    	oldPassword = new JPasswordField();
    	oldPassword.setBackground(UIUtilities.BACKGROUND_COLOR);
    	items = new HashMap<String, JTextField>();
    	ExperimenterData user = (ExperimenterData) model.getRefObject();
    	List userGroups = user.getGroups();
    	GroupData defaultGroup = user.getDefaultGroup();
		long groupID = defaultGroup.getId();
		//Build the array for box.
		Iterator i = userGroups.iterator();
		//Remove not visible group
		GroupData g;
		
		List<GroupData> validGroups = new ArrayList<GroupData>();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (model.isValidGroup(g))
				validGroups.add(g);
		}
		groupData = new GroupData[validGroups.size()];
		int selectedIndex = 0;
		int index = 0;
		i = validGroups.iterator();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			groupData[index] = g;
			if (g.getId() == groupID) originalIndex = index;
			index++;
		}
		selectedIndex = originalIndex;
		//sort by name
		groups = EditorUtil.createComboBox(groupData, 0);
		groups.setRenderer(new GroupsRenderer());
		if (groupData.length != 0)
			groups.setSelectedIndex(selectedIndex);
		if (isOwner) {
			groups.addActionListener(this);
			groups.setEnabled(true);
		} else groups.setEnabled(false);
		
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
    }
    
    /**
     * Builds the panel hosting the user's details.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
    	ExperimenterData user = (ExperimenterData) model.getRefObject();
    	boolean editable = model.isCurrentUserOwner(user);
    	details = EditorUtil.convertExperimenter(user);
        JPanel content = new JPanel();
        content.setBorder(
				BorderFactory.createTitledBorder("Profile"));
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
            if (key.equals(EditorUtil.LAST_NAME) || 
            		key.equals(EditorUtil.EMAIL)) 
            	label = UIUtilities.setTextFont(
            			key+EditorUtil.MANDATORY_SYMBOL);
            else label = UIUtilities.setTextFont(key);
            area = new JTextField(value);
            area.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (editable) {
            	area.setEditable(editable);
            	area.getDocument().addDocumentListener(this);
            }
            items.put(key, area);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
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
        label = UIUtilities.setTextFont(EditorUtil.DEFAULT_GROUP);
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
        content.add(groups, c);  
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
    
    /** 
     * Builds the UI component hosting the UI component used to modify 
     * the password.
     * 
     * @return See above.
     */
    private JPanel buildPasswordPanel()
    {
    	JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	Registry reg = MetadataViewerAgent.getRegistry();
    	String ldap = (String) reg.lookup(LookupNames.USER_AUTHENTICATION);
    	if (ldap != null && ldap.length() > 0) {
    		content.setBorder(
    				BorderFactory.createTitledBorder("LDAP Authentication"));
    		content.setLayout(new FlowLayout(FlowLayout.LEFT));
    		content.add(new JLabel(ldap));
    		return content;
    	}
    	content.setBorder(
				BorderFactory.createTitledBorder("Change Password"));
    	
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;  
    	content.add(UIUtilities.setTextFont(PASSWORD_OLD), c);
    	c.gridx++;
    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	content.add(oldPassword, c);
    	c.gridy++;
    	c.gridx = 0;
    	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;  
    	content.add(UIUtilities.setTextFont(PASSWORD_NEW), c);
    	c.gridx++;
    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	content.add(passwordNew, c);
    	c.gridy++;
    	c.gridx = 0;
    	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;  
    	content.add(UIUtilities.setTextFont(PASSWORD_CONFIRMATION), c);
    	c.gridx++;
    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	content.add(passwordConfirm, c);
    	c.gridy++;
    	c.gridx = 0;
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.add(content);
    	JPanel buttonPanel = UIUtilities.buildComponentPanel(passwordButton);
    	buttonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.add(buttonPanel);
    	return p;
    }

    /** Message displayed when one of the required fields is left blank. */
    private void showRequiredField()
    {
    	UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
        un.notifyInfo("Edit Profile", "The required fields cannot be left " +
        		"blank.");
        return;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model	Reference to the model. Mustn't be <code>null</code>. 
     * @param view 	Reference to the control. Mustn't be <code>null</code>.                     
     */
	UserProfile(EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
 
	/**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    void buildGUI()
    {
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
    	if (model.isCurrentUserOwner(model.getRefObject())) {
    		//GroupData group = (GroupData) groups.getSelectedItem();
    		//c.gridy++;
    		//add(buildPermissions(group.getPermissions()), c); 
    		c.gridy++;
    		add(Box.createVerticalStrut(5), c); 
    		c.gridy++;
    		add(buildPasswordPanel(), c);
    	}
    }
    
	/** Clears the password fields. */
	void passwordChanged()
	{
		oldPassword.setText("");
		passwordNew.setText("");
        passwordConfirm.setText("");
	}

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		if (selectedIndex != originalIndex) return true;
		if (details == null) return false;
		Entry entry;
		Iterator i = details.entrySet().iterator();
		String key;
		String value;
		JTextField field;
		String v;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			field = items.get(key);
			v = field.getText();
			if (v != null) {
				v = v.trim();
				value = (String) entry.getValue();
				if (value != null && !v.equals(value))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns the experimenter to save.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenterToSave()
	{
		ExperimenterData original = (ExperimenterData) model.getRefObject();
    	//Required fields first
    	JTextField f = items.get(EditorUtil.LAST_NAME);
    	String v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	original.setLastName(v);
    	f = items.get(EditorUtil.EMAIL);
    	v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	original.setEmail(v);
    	f = items.get(EditorUtil.INSTITUTION);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setInstitution(v.trim());
    	f = items.get(EditorUtil.FIRST_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setFirstName(v.trim());
    	
    	//set the groups
    	if (selectedIndex != originalIndex) {
    		GroupData g = null;
    		if (selectedIndex < groupData.length)
    			g = groupData[selectedIndex];
    		ExperimenterData user = (ExperimenterData) model.getRefObject();
    		List userGroups = user.getGroups();
    		List<GroupData> newGroups = new ArrayList<GroupData>();
    		if (g != null) newGroups.add(g);
    		Iterator i = userGroups.iterator();
    		long id = -1;
    		if (g != null) id = g.getId();
    		GroupData group;
    		while (i.hasNext()) {
				group = (GroupData) i.next();
				if (group.getId() != id)
					newGroups.add(group);
			}
    		//Need to see what to do b/c no ExperimenterGroupMap
    		original.setGroups(newGroups);
    	}
		return original;//newOne;
	}
	
	/** 
	 * Fires a property change event when a index is selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		selectedIndex = groups.getSelectedIndex();
		buildGUI();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
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
