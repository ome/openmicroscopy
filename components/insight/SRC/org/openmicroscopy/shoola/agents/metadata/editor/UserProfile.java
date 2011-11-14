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
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.util.UploadPictureDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.PermissionsPane;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

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
	implements ActionListener, ChangeListener, DocumentListener, 
	PropertyChangeListener
{
    
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_OLD = "Old password";
	
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_NEW = "New password";
	
	/** Text of the label in front of the confirmation password area. */
	private static final String		PASSWORD_CONFIRMATION = "Confirm password";
	
	/** The title of the dialog displayed if a problem occurs. */
	private static final String		PASSWORD_CHANGE_TITLE = "Change Password";
	
	/** The default user's photo.*/
	private static final Image		USER_PHOTO;
	
	static {
		IconManager icons = IconManager.getInstance();
		USER_PHOTO = icons.getImageIcon(IconManager.USER_PHOTO_32).getImage();
	}
	
    /** The items that can be edited. */
    private Map<String, JTextField>	items;
    
    /** UI component displaying the groups, the user is a member of. */
    private JComboBox				groups;

    /** Displayed the current group.*/
    private JLabel					groupLabel;
    
    /** Password field to enter the new password. */
    private JPasswordField			passwordNew;
    
    /** Password field to confirm the new password. */
    private JPasswordField			passwordConfirm;
    
    /** Hosts the old password. */
    private JPasswordField			oldPassword;

    /** Modify password. */
    private JButton					passwordButton;
    
    /** Button to add the user to group. */
    private JButton					manageButton;
    
    /** Box to make the selected user an administrator. */
    private JCheckBox				adminBox;
    
    /** Box to make the user active or not. */
    private JCheckBox				activeBox;
    
    /** Box to make the selected user the owner of the group is a member of. */
    private JCheckBox				ownerBox;
    
	/** Reference to the Model. */
    private EditorModel				model;
    
    /** The original index. */
    private int						originalIndex;

    /** The user's details. */
    private Map						details;
    
    /** The groups the user is a member of. */
    private GroupData[] 			groupData;

    /** Flag indicating that the selected user is an owner of the group. */
    private boolean					groupOwner;
    
    /** Indicates that the user is an administrator. */
    private boolean					admin;
    
    /** Indicates that the user is active or not. */
    private boolean					active;
 
    /** Component displaying the permissions status. */
    private PermissionsPane			permissionsPane;
    
    /** The field hosting the login name. */
    private JTextField				loginArea;
    
    /** Component displaying the photo of the user. */
    private UserProfileCanvas		userPicture;
    
    /** Component used to change the user's photo.*/
    private JLabel					changePhoto;
    
    /** Component used to delete the user's photo.*/
    private JButton					deletePhoto;
    
    /** Modifies the existing password. */
    private void changePassword()
    {
    	UserNotifier un;
    	if (!oldPassword.isVisible()) {
    		StringBuffer buf = new StringBuffer();
            buf.append(passwordNew.getPassword());
            String newPass = buf.toString();
            if (newPass == null || newPass.length() == 0) {
            	un = MetadataViewerAgent.getRegistry().getUserNotifier();
            	un.notifyInfo(PASSWORD_CHANGE_TITLE, 
            			"Please enter the new password.");
            	passwordNew.requestFocus();
            	return;
            }
            un = MetadataViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(PASSWORD_CHANGE_TITLE, "Password changed.");
            passwordNew.setText("");
            model.resetPassword(newPass);
    		return;
    	}
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
        if (old.equals(newPass)) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(PASSWORD_CHANGE_TITLE, 
        			"Your new and old passwords are the same.\n" +
        			"Please enter a new password.");
        	passwordNew.setText("");
        	passwordConfirm.setText("");
        	passwordNew.requestFocus();
        	return;
        }

        if (pass == null || confirm == null || confirm.length() == 0 ||
        	!pass.equals(confirm)) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo(PASSWORD_CHANGE_TITLE, 
            			"The passwords entered do not match.\n" +
            			"Please try again.");
            passwordNew.setText("");
            passwordConfirm.setText("");
            passwordNew.requestFocus();
            return;
        }
        model.changePassword(old, confirm);
    }
    
    /** 
     * Loads the collection of existing groups and 
     * select where to add the user to or remove the user from.
     */
    private void manageGroup()
    {
    	
    }
    
    /**
     * Returns <code>true</code> if the user can modify the photo, 
     * <code></code> otherwise.
     * 
     * @return See above.
     */
    private boolean canModifyPhoto()
    {
    	Object object = model.getRefObject();
    	if (!(object instanceof ExperimenterData)) return false;
    	ExperimenterData exp = (ExperimenterData) object;
    	ExperimenterData user = MetadataViewerAgent.getUserDetails();
    	return exp.getId() == user.getId();
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
		admin = false;
		active = false;
		groupOwner = false;

    	userPicture = new UserProfileCanvas();
    	userPicture.setBackground(UIUtilities.BACKGROUND_COLOR);
    	//userPicture.setToolTipText("Click to upload your photo.");
    	
    	IconManager icons = IconManager.getInstance();
    	changePhoto = new JLabel("Change Photo");
    	changePhoto.setToolTipText("Upload your photo.");
    	changePhoto.setForeground(UIUtilities.HYPERLINK_COLOR);
    	Font font = changePhoto.getFont();
    	changePhoto.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
    	changePhoto.setBackground(UIUtilities.BACKGROUND_COLOR);
    	deletePhoto = new JButton(icons.getIcon(IconManager.DELETE_12));
    	boolean b = canModifyPhoto();
    	changePhoto.setVisible(b);
    	deletePhoto.setToolTipText("Delete the photo.");
    	deletePhoto.setBackground(UIUtilities.BACKGROUND_COLOR);
    	UIUtilities.unifiedButtonLookAndFeel(deletePhoto);
    	deletePhoto.setVisible(false);
    	loginArea = new JTextField();
    	boolean a = MetadataViewerAgent.isAdministrator();
    	loginArea.setEnabled(a);
    	loginArea.setEditable(a);
    	adminBox = new JCheckBox();
    	adminBox.setVisible(false);
    	adminBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	ownerBox = new JCheckBox();
    	ownerBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	activeBox = new JCheckBox();
    	activeBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	activeBox.setVisible(false);
    	passwordButton =  new JButton("Change password");
    	passwordButton.setEnabled(false);
    	passwordButton.setBackground(UIUtilities.BACKGROUND_COLOR);
    	passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	changePassword(); 
            }
        });
    	manageButton = new JButton("Group");
    	manageButton.setEnabled(false);
    	manageButton.setBackground(UIUtilities.BACKGROUND_COLOR);
    	manageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	manageGroup(); 
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
    	GroupData defaultGroup = user.getDefaultGroup();

    	permissionsPane = new PermissionsPane(defaultGroup.getPermissions(), 
    			UIUtilities.BACKGROUND_COLOR);
    	permissionsPane.disablePermissions();
    	groupLabel = new JLabel(defaultGroup.getName());
    	groupLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
    	
    	
		long groupID = defaultGroup.getId();
		boolean owner = false;
		/*
		if (defaultGroup.getLeaders() != null)
			owner = setGroupOwner(defaultGroup);
		else {
			GroupData g = model.loadGroup(groupID);
			if (g != null)
				owner = setGroupOwner(g);
		}
		*/
		Object parentRootObject = model.getParentRootObject();
		if (parentRootObject instanceof GroupData) {
			owner = setGroupOwner((GroupData) parentRootObject);
		}
		//Build the array for box.
		/*
		Iterator i = userGroups.iterator();
		GroupData g;
		
		List<GroupData> validGroups = new ArrayList<GroupData>();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (model.isValidGroup(g))
				validGroups.add(g);
		}
		groupData = new GroupData[validGroups.size()];
		admin = false;
		active = false;
		int selectedIndex = 0;
		int index = 0;
		i = validGroups.iterator();
		boolean owner = false;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			groupData[index] = g;
			if (g.getId() == groupID) {
				if (g.getLeaders() != null) {
					owner = setGroupOwner(g);
					originalIndex = index;
				}
			}
			index++;
		}
		selectedIndex = originalIndex;
		groups = EditorUtil.createComboBox(groupData, 0);
		groups.setEnabled(false);
		groups.setRenderer(new GroupsRenderer());
		if (groupData.length != 0)
			groups.setSelectedIndex(selectedIndex);
		*/
		
		
		if (MetadataViewerAgent.isAdministrator()) {
			//Check that the user is not the one currently logged.
			oldPassword.setVisible(false);
			owner = true;
			adminBox.setVisible(true);
			activeBox.setVisible(true);
			adminBox.addChangeListener(this);
			active = user.isActive();
			activeBox.setSelected(active);
			activeBox.setEnabled(!model.isSelf());
			activeBox.addChangeListener(this);
			//indicate if the user is an administrator.a
			admin = isUserAdministrator();
			adminBox.setSelected(admin);
			ownerBox.addChangeListener(this);
			//admin = false;
			//Now check if the user is the last administrator
		} else {
			ownerBox.setEnabled(false);
			passwordConfirm.getDocument().addDocumentListener(
					new DocumentListener() {
				
			   /**
				* Allows the user to interact with the password controls
				* depending on the value entered.
				* @see DocumentListener#removeUpdate(DocumentEvent)
				*/
				public void removeUpdate(DocumentEvent e)
				{
					handlePasswordEntered();
				}
				
				/**
				 * Allows the user to interact with the password controls
				 * depending on the value entered.
				 * @see DocumentListener#insertUpdate(DocumentEvent)
				 */
				public void insertUpdate(DocumentEvent e)
				{
					handlePasswordEntered();
				}
				
				/**
				 * Required by the {@link DocumentListener} I/F but 
				 * no-operation implementation in our case.
				 * @see DocumentListener#changedUpdate(DocumentEvent)
				 */
				public void changedUpdate(DocumentEvent e) {}
			});
		}
		passwordNew.getDocument().addDocumentListener(
				new DocumentListener() {
			
		   /**
			* Allows the user to interact with the password controls
			* depending on the value entered.
			* @see DocumentListener#removeUpdate(DocumentEvent)
			*/
			public void removeUpdate(DocumentEvent e)
			{
				handlePasswordEntered();
			}
			
			/**
			 * Allows the user to interact with the password controls
			 * depending on the value entered.
			 * @see DocumentListener#insertUpdate(DocumentEvent)
			 */
			public void insertUpdate(DocumentEvent e)
			{
				handlePasswordEntered();
			}
			
			/**
			 * Required by the {@link DocumentListener} I/F but 
			 * no-operation implementation in our case.
			 * @see DocumentListener#changedUpdate(DocumentEvent)
			 */
			public void changedUpdate(DocumentEvent e) {}
		});
		ExperimenterData logUser = MetadataViewerAgent.getUserDetails();
		if (user.getId() == logUser.getId()) {
			MouseAdapter adapter = new MouseAdapter() {
				
	    		/** Brings up a chooser to load the user image. */
				public void mouseReleased(MouseEvent e)
				{
					uploadPicture();
				}
				
			};
			//userPicture.addMouseListener(adapter);
			changePhoto.addMouseListener(adapter);
			deletePhoto.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	model.deletePicture();
	            	setUserPhoto(null);
	            }
	        });
		}
    }
    
    /**
     * Returns <code>true</code> if the user is an administrator, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    private boolean isUserAdministrator()
    {
    	ExperimenterData user = (ExperimenterData) model.getRefObject();
    	ExperimenterData loggedInUser = MetadataViewerAgent.getUserDetails();
    	if (user.getId() == loggedInUser.getId())
    		return MetadataViewerAgent.isAdministrator();
    	List<GroupData> groups = user.getGroups();
    	Iterator<GroupData> i = groups.iterator();
    	GroupData g;
    	while (i.hasNext()) {
			g = i.next();
			if (GroupData.SYSTEM.equals(g.getName()))
				return true;
		}
    	return false;
    }
    /**
     * Sets the enabled flag of some password controls depending on the
     * text entered.
     */
    private void handlePasswordEntered()
    {
    	char[] values = passwordNew.getPassword();
    	if (oldPassword.isVisible()) {
    		char[] oldValues = oldPassword.getPassword();
    		char[] confirmValues = passwordConfirm.getPassword();
    		if (values != null && oldValues != null && confirmValues != null) {
    			passwordButton.setEnabled(values.length > 0 && 
    					oldValues.length > 0
    					&& confirmValues.length == values.length);
    		}
    	} else {
    		passwordButton.setEnabled(values != null && values.length > 0);
    	}
    }
    
    /** Brings up the dialog to choose the photo to upload. */
    private void uploadPicture()
    {
    	UploadPictureDialog d = new UploadPictureDialog(
    			MetadataViewerAgent.getRegistry().getTaskBar().getFrame());
    	d.addPropertyChangeListener(this);
    	d.pack();
    	UIUtilities.centerAndShow(d);
    }
    
    /**
     * Selects or not the {@link #ownerBox} if the selected user 
     * is an owner. Returns <code>true</code> if the currently logged in 
     * user is an owner of the group.
     * 
     * @param group The group to handle.
     */
    private boolean setGroupOwner(GroupData group)
    {
    	ExperimenterData ref = (ExperimenterData) model.getRefObject();
    	long userID = MetadataViewerAgent.getUserDetails().getId();
    	Set leaders = group.getLeaders();
    	ExperimenterData exp;
    	boolean isOwner = false;
    	if (leaders != null) {
    		Iterator i = leaders.iterator();
        	while (i.hasNext()) {
    			exp = (ExperimenterData) i.next();
    			if (exp.getId() == ref.getId()) {
    				groupOwner = true;
    				ownerBox.setSelected(true);
    			}
    			if (exp.getId() == userID)
    				isOwner = true;
    		}
    	}
    	return isOwner;
    }
    
    /**
     * Returns the component displayed the user photo.
     * 
     * @return See above.
     */
    private JPanel buildProfileCanvas()
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.add(userPicture);
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	JPanel bar = new JPanel();
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.setLayout(new FlowLayout(FlowLayout.LEFT));
    	bar.add(changePhoto);
    	bar.add(deletePhoto);
    	p.add(bar);
    	return p;
    }
    
    /**
     * Builds the panel hosting the user's details.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
    	ExperimenterData user = (ExperimenterData) model.getRefObject();
    	boolean editable = model.isUserOwner(user);
    	if (!editable) 
    		editable = model.isGroupLeader() || 
    		MetadataViewerAgent.isAdministrator();
    	details = EditorUtil.convertExperimenter(user);
        JPanel content = new JPanel();
        content.setBorder(
				BorderFactory.createTitledBorder("Profile"));
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
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
		//Add log in name but cannot edit.
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(buildProfileCanvas(), c);
        c.gridy++;
        c.gridx = 0;
        label = EditorUtil.getLabel(EditorUtil.DISPLAY_NAME, true);
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
        loginArea.setText(user.getUserName());
        loginArea.getDocument().addDocumentListener(this);
        content.add(loginArea, c);  
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            entry = (Entry) i.next();
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            label = EditorUtil.getLabel(key, false);
            area = new JTextField(value);
            area.setBackground(UIUtilities.BACKGROUND_COLOR);
            area.setEditable(editable);
            area.setEnabled(editable);
            if (editable)
            	area.getDocument().addDocumentListener(this);
            items.put(key, area);
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
            content.add(area, c);  
        }
        c.gridx = 0;
        c.gridy++;
        label = EditorUtil.getLabel(EditorUtil.DEFAULT_GROUP, false); 
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
        content.add(groupLabel, c);
        c.gridy++;
        content.add(permissionsPane, c);
        
        //content.add(groups, c); 
        
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
        if (activeBox.isVisible()) {
        	c.gridx = 0;
            c.gridy++;
            label = EditorUtil.getLabel(EditorUtil.ACTIVE, false);   
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
        }
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
		if (MetadataViewerAgent.isAdministrator()) {
	    	content.add(UIUtilities.setTextFont(PASSWORD_NEW), c);
	    	c.gridx++;
	    	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	    	c.fill = GridBagConstraints.HORIZONTAL;
	    	c.weightx = 1.0;
	    	content.add(passwordNew, c);
		} else {
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
		}
    	
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
 
	/** Builds and lays out the UI. */
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
    	if (model.isUserOwner(model.getRefObject()) || 
    			MetadataViewerAgent.isAdministrator()) {
    		c.gridy++;
    		add(Box.createVerticalStrut(5), c); 
    		c.gridy++;
    		add(buildPasswordPanel(), c);
    	}
    	ExperimenterData exp = (ExperimenterData) model.getRefObject();
    	BufferedImage photo = model.getUserPhoto(exp.getId());
    	if (photo == null) setUserPhoto(null);
    	else setUserPhoto(photo);
    	deletePhoto.setVisible(photo != null && canModifyPhoto());
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
		String text = loginArea.getText();
		if (text == null || text.trim().length() == 0) return false;
		text = text.trim();
		ExperimenterData original = (ExperimenterData) model.getRefObject();
		if (!text.equals(original.getUserName())) return true;
		//if (selectedIndex != originalIndex) return true;
		if (details == null) return false;
		Entry entry;
		Iterator i = details.entrySet().iterator();
		String key;
		String value;
		JTextField field;
		String v;
		if (items.size() > 0) {
			while (i.hasNext()) {
				entry = (Entry) i.next();
				key = (String) entry.getKey();
				field = items.get(key);
				if (field != null) {
					v = field.getText();
					if (v != null) {
						v = v.trim();
						value = (String) entry.getValue();
						if (value != null && !v.equals(value))
							return true;
					}
				}
			}
		}
		
		Boolean b = ownerBox.isSelected();
		if (b.compareTo(groupOwner) != 0) return true;
		if (adminBox.isVisible()) {
			b = adminBox.isSelected();
			if (b.compareTo(admin) != 0) return true;
		}
		if (activeBox.isVisible()) {
			b = activeBox.isSelected();
			if (b.compareTo(active) != 0) return true;
		}
		return false;
	}

	/**
	 * Returns the experimenter to save.
	 * 
	 * @return See above.
	 */
	Object getExperimenterToSave()
	{
		ExperimenterData original = (ExperimenterData) model.getRefObject();
    	//Required fields first
		
    	String v = loginArea.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	original.setLastName(v);
    	JTextField f = items.get(EditorUtil.EMAIL);
    	v = f.getText();
    	if (v == null || v.trim().length() == 0) v = "";//showRequiredField();
    	original.setEmail(v);
    	f = items.get(EditorUtil.INSTITUTION);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setInstitution(v.trim());
    	f = items.get(EditorUtil.LAST_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setLastName(v.trim());
    	
    	f = items.get(EditorUtil.FIRST_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setFirstName(v.trim());
    	
    	f = items.get(EditorUtil.MIDDLE_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	original.setMiddleName(v.trim());
    	
    	//set the groups
    	GroupData g = null;
    	/*
    	
    	if (selectedIndex != originalIndex) {
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
    	*/
    	String value = loginArea.getText().trim();
    	UserCredentials uc = new UserCredentials(value, "");
    	Boolean b = ownerBox.isSelected();
    	//if (g == null) g = original.getDefaultGroup();
    	boolean a = false;
    	if (b.compareTo(groupOwner) != 0) {
    		a = true;
    		uc.setOwner(b);
    		Object parent = model.getParentRootObject();
    		if (parent instanceof GroupData) {
    			Map<GroupData, Boolean> map = new HashMap<GroupData, Boolean>();
    			map.put((GroupData) parent, b);
    			uc.setGroupsOwner(map);
    		}
    	}
    	if (adminBox.isVisible()) {
    		b = adminBox.isSelected();
    		if (b.compareTo(admin) != 0) {
        		a = true;
        		uc.setAdministrator(b);
        	}
    	}
    	if (activeBox.isVisible()) {
    		b = activeBox.isSelected();
    		if (b.compareTo(active) != 0) {
    			a = true;
    			uc.setActive(b);
    		}
    	}
    	if (!original.getUserName().equals(value)) a = true;
    	//if admin 
    	if (MetadataViewerAgent.isAdministrator()) a = true;
    	if (a) {
    		Map<ExperimenterData, UserCredentials> m = 
    			new HashMap<ExperimenterData, UserCredentials>();
    		m.put(original, uc);
    		AdminObject object = new AdminObject(g, m, 
    				AdminObject.UPDATE_EXPERIMENTER);
    		return object;
    	}
		return original;//newOne;
	}
	
	/**
	 * Sets the photo of the user.
	 * 
	 * @param image The image to set.
	 */
	void setUserPhoto(BufferedImage image)
	{
		if (image == null) {
			userPicture.setImage(USER_PHOTO);
			deletePhoto.setVisible(false);
			return;
		}
		BufferedImage img = Factory.scaleBufferedImage(image,
				UserProfileCanvas.WIDTH);
		userPicture.setImage(img);
		deletePhoto.setVisible(canModifyPhoto());
		repaint();
	}
	
	/** Sets the parent of the node. */
	void setParentRootObject()
	{
		Object parentRootObject = model.getParentRootObject();
		if (parentRootObject instanceof GroupData) {
			setGroupOwner((GroupData) parentRootObject);
		}
	}
	
	/** 
	 * Fires a property change event when a index is selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		buildGUI();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Fires property indicating that some values have changed.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Uploads the photo.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (UploadPictureDialog.UPLOAD_PHOTO_PROPERTY.equals(name)) {
			List l = (List) evt.getNewValue();
			if (l == null || l.size() != 2) return;
			File f = (File) l.get(0);
			if (f == null) return;
			model.uploadPicture(f, (String) l.get(1));
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
