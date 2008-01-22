/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.UserProfile 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.profile;


//Java imports
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.ui.GroupsRenderer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Displays the user's profile.
 * Allows user to edit his/her profile and modifies password.
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
{
    
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_OLD = "Old password";
	
	/** Text of the label in front of the new password area. */
	private static final String		PASSWORD_NEW = "New password";
	
	/** Text of the label in front of the confirmation password area. */
	private static final String		PASSWORD_CONFIRMATION = "Confirm password";
	
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
    
    /** Save the modification. */
    private JButton					saveButton;
    
    /** Modify password. */
    private JButton					passwordButton;
    
	/** Reference to the Model. */
    private ProfileEditorModel		model;

    /** Reference to the Control. */
    private ProfileEditorControl	controller;
    
    /**
     * Displays the specified string into a {@link JLabel} and sets 
     * the font to <code>italic</code>.
     * 
     * @param s The string to display.
     * @return See above.
     */
    private JLabel setFontToItalic(String s)
    {
    	 JLabel label = new JLabel(s);
         Font font = label.getFont();
         Font newFont = font.deriveFont(Font.ITALIC);
         label.setFont(newFont);
         return label;
    }
    
    /** 
     * Message displayed when one of the required fields is left blank.
     */
    private void showRequiredField()
    {
    	UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        un.notifyInfo("Edit Profile", "The required fields cannot be left " +
        		"blank.");
        return;
    }
    
    /** Saves the edited values. */
    private void save()
    {
    	ExperimenterData original = model.getUser();
    	//Required fields first
    	ExperimenterData newOne = new ExperimenterData();
    	JTextField f = items.get(ProfileEditorUtil.LAST_NAME);
    	String v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	newOne.setLastName(v);
    	f = items.get(ProfileEditorUtil.EMAIL);
    	v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	newOne.setEmail(v);
    	f = items.get(ProfileEditorUtil.INSTITUTION);
    	v = f.getText();
    	if (v == null) v = "";
    	newOne.setInstitution(v.trim());
    	f = items.get(ProfileEditorUtil.FIRST_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	newOne.setFirstName(v.trim());
    	newOne.setId(original.getId());
    	
    	//newOne.setDefaultGroup((GroupData) groups.getSelectedItem());
    	controller.save(newOne);
    }
    
    /** Modifies the existing password. */
    private void changePassword()
    {
    	StringBuffer buf = new StringBuffer();
        buf.append(passwordNew.getPassword());
        String pass = buf.toString();
        buf = new StringBuffer();
        buf.append(passwordConfirm.getPassword());
        String confirm = buf.toString();
        
        buf = new StringBuffer();
        buf.append(oldPassword.getPassword());
        String old = buf.toString();
        if (old == null || old.trim().length() == 0) {
        	UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(ProfileEditorUI.DIALOG_TITLE, 
        				"Please specify your old password.");
        	return;
        }
        if (pass == null || confirm == null || !pass.equals(confirm)) {
        	UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo(ProfileEditorUI.DIALOG_TITLE, 
            		"The passwords entered don't " +
            		"match. Please try again.");
            passwordNew.setText("");
            passwordConfirm.setText("");
            return;
        }
        controller.changePassword(old, confirm);
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	saveButton = new JButton("Save");
    	saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	save(); 
            }
        });
    	saveButton.setEnabled(model.isEditable());
    	passwordButton =  new JButton("Change password");
    	passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	changePassword(); 
            }
        });
    	passwordNew = new JPasswordField();
    	passwordConfirm = new JPasswordField();
    	oldPassword = new JPasswordField();
    	items = new HashMap<String, JTextField>();
    	ExperimenterData user = model.getUser();
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
			if (model.isValidGroup(g)) {
				validGroups.add(g);
			}
		}
		GroupData[] objects = new GroupData[validGroups.size()];
		int selectedIndex = 0;
		int index = 0;
		i = validGroups.iterator();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			objects[index] = g;
			if (g.getId() == groupID) selectedIndex = index;
			index++;
		}
		//sort by name
		groups = new JComboBox(objects);
		groups.setEnabled(model.isEditable());
		groups.setRenderer(new GroupsRenderer());
		if (objects.length != 0)
			groups.setSelectedIndex(selectedIndex);
    }
    
    /**
     * Builds the panel hosting the user's details.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
    	boolean editable = model.isEditable();
    	Map details = ProfileEditorUtil.manageExperimenterData(model.getUser());
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        double[] columns = {150, 200};
        double[] rows =  new double[2*(details.size()+2)];
        for (int i = 0; i < rows.length; i++) {
        	if (i%2 == 0) rows[i] = 5;
        	else rows[i] = TableLayout.FILL;
		}
        
        TableLayout layout = new TableLayout();
        layout.setColumn(columns);
        layout.setRow(rows);
        content.setLayout(layout);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        int index = 1;
        int j;
        while (i.hasNext()) {
            key = (String) i.next();
            value = (String) details.get(key);
            if (key.equals(ProfileEditorUtil.LAST_NAME) || 
            		key.equals(ProfileEditorUtil.EMAIL)) 
            	label = UIUtilities.setTextFont(
            			key+ProfileEditorUtil.MANDATORY_SYMBOL);
            else label = UIUtilities.setTextFont(key);
            area = new JTextField(value);
            j = index-1;
            content.add(new JLabel(""), "0, "+j+", 1, "+j);
            content.add(label, "0, "+index);
            
            area.setEditable(editable);
            //area.setEnabled(false);
            label.setLabelFor(area);
            content.add(area, "1, "+index);
            items.put(key, area);
            index = index+2;
        }
        label = UIUtilities.setTextFont(ProfileEditorUtil.DEFAULT_GROUP);
        j = index-1;
        content.add(new JLabel(""), "0, "+j+", 1, "+j);
        content.add(label, "0, "+index);
        content.add(groups, "1, "+index);
        index = index+2;
        content.add(new JLabel(""), "0, "+j+", 1, "+j);
        content.add(setFontToItalic(ProfileEditorUtil.MANDATORY_DESCRIPTION), 
        		"0, "+index+", 1, "+index);
        
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(content);
        p.add(UIUtilities.buildComponentPanel(saveButton));
        return p;
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
    	content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    	double[][] tl = {{150, 5, 100}, //columns
				{TableLayout.FILL, 5, TableLayout.FILL, 5, TableLayout.FILL} };
    	//rows
    	content.setLayout(new TableLayout(tl));
    	JLabel label =  UIUtilities.setTextFont(PASSWORD_OLD);
    	content.add(label, "0, 0, f, t");
    	label = new JLabel();
    	content.add(label, "1, 0");
    	content.add(oldPassword, "2, 0");
    	label = new JLabel();
    	content.add(label, "0, 1, 2, 1");
    	label = UIUtilities.setTextFont(PASSWORD_NEW);
    	content.add(label, "0, 2, f, t");
    	label = new JLabel();
    	content.add(label, "1, 2");
    	content.add(passwordNew, "2, 2");
    	label = new JLabel();
    	content.add(label, "0, 3, 2, 3");
    	label = UIUtilities.setTextFont(PASSWORD_CONFIRMATION);
    	content.add(label, "0, 4, f, t");
    	label = new JLabel();
    	content.add(label, "1, 4");
    	content.add(passwordConfirm, "2, 4");
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.add(content);
    	p.add(UIUtilities.buildComponentPanel(passwordButton));
    	return p;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
    	setBorder(new EtchedBorder());
    	JPanel contentPanel = buildContentPanel();
    	double[][] tl = {{TableLayout.FILL}, 
    					{TableLayout.PREFERRED, TableLayout.PREFERRED}}; 
    	setLayout(new TableLayout(tl));
    	add(contentPanel, "0, 0, f, t");
    	add(buildPasswordPanel(), "0, 1, f, t");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 		Reference to the model. Mustn't be <code>null</code>. 
     * @param controller 	Reference to the control. 
     * 						Mustn't be <code>null</code>.                     
     */
	UserProfile(ProfileEditorModel model, ProfileEditorControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		
		initComponents();
		buildGUI();
	}
 
	/** Clears the password fields. */
	void passwordChanged()
	{
		oldPassword.setText("");
		passwordNew.setText("");
        passwordConfirm.setText("");
	}
	
}
