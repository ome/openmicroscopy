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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.GroupsRenderer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
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
    
    private GroupData[] 			groupData;
    
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
        UserNotifier un;
        if (old == null || old.trim().length() == 0) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo(PASSWORD_CHANGE_TITLE, 
        				"Please specify your old password.");
        	return;
        }
        if (pass == null || confirm == null || !pass.equals(confirm)) {
        	un = MetadataViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo(PASSWORD_CHANGE_TITLE, 
            			"The passwords entered do not match. " +
            			"Please try again.");
            passwordNew.setText("");
            passwordConfirm.setText("");
            return;
        }
        model.changePassword(old, confirm);
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	boolean isOwner = model.isCurrentUserOwner(model.getRefObject());
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
		groups = new JComboBox(groupData);
		groups.setRenderer(new GroupsRenderer());
		if (groupData.length != 0)
			groups.setSelectedIndex(selectedIndex);
		if (isOwner) {
			groups.addActionListener(this);
			groups.setEnabled(true);
		} else groups.setEnabled(false);
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
        boolean isOwner = model.isCurrentUserOwner(model.getRefObject());
        while (i.hasNext()) {
            key = (String) i.next();
            value = (String) details.get(key);
            if (key.equals(EditorUtil.LAST_NAME) || 
            		key.equals(EditorUtil.EMAIL)) 
            	label = UIUtilities.setTextFont(
            			key+EditorUtil.MANDATORY_SYMBOL);
            else label = UIUtilities.setTextFont(key);
            area = new JTextField(value);
            
            j = index-1;
            content.add(new JLabel(""), "0, "+j+", 1, "+j);
            content.add(label, "0, "+index);
            if (editable) {
            	 area.setEditable(editable);
            	 area.getDocument().addDocumentListener(this);
            }
            area.setEnabled(isOwner);
            label.setLabelFor(area);
            content.add(area, "1, "+index);
            items.put(key, area);
            index = index+2;
        }
        label = UIUtilities.setTextFont(EditorUtil.DEFAULT_GROUP);
        j = index-1;
        content.add(new JLabel(""), "0, "+j+", 1, "+j);
        content.add(label, "0, "+index);
        content.add(groups, "1, "+index);
        index = index+2;
        content.add(new JLabel(""), "0, "+j+", 1, "+j);
        content.add(UIUtilities.setTextFont(EditorUtil.MANDATORY_DESCRIPTION,
        		Font.ITALIC), "0, "+index+", 1, "+index);
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
	}
 
	/**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    void buildGUI()
    {
    	removeAll();
    	initComponents();
    	//setBorder(new EtchedBorder());
    	JPanel contentPanel = buildContentPanel();
    	double[][] tl = {{TableLayout.FILL}, 
    					{TableLayout.PREFERRED, TableLayout.PREFERRED}}; 
    	setLayout(new TableLayout(tl));
    	add(contentPanel, "0, 0, f, t");
    	if (model.isCurrentUserOwner(model.getRefObject()))
    			add(buildPasswordPanel(), "0, 1, f, t");
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
		Iterator i = details.keySet().iterator();
		String key;
		String value;
		JTextField field;
		String v;
		while (i.hasNext()) {
			key = (String) i.next();
			field = items.get(key);
			v = field.getText().trim();
			value = (String) details.get(key);
			if (value != null && !v.equals(value))
				return true;
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
    	ExperimenterData newOne = new ExperimenterData();
    	JTextField f = items.get(EditorUtil.LAST_NAME);
    	String v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	newOne.setLastName(v);
    	f = items.get(EditorUtil.EMAIL);
    	v = f.getText();
    	if (v == null || v.trim().length() == 0) showRequiredField();
    	newOne.setEmail(v);
    	f = items.get(EditorUtil.INSTITUTION);
    	v = f.getText();
    	if (v == null) v = "";
    	newOne.setInstitution(v.trim());
    	f = items.get(EditorUtil.FIRST_NAME);
    	v = f.getText();
    	if (v == null) v = "";
    	newOne.setFirstName(v.trim());
    	newOne.setId(original.getId());
    	//set the groups
    	if (selectedIndex != originalIndex) {
    		GroupData g = groupData[selectedIndex];
    		ExperimenterData user = (ExperimenterData) model.getRefObject();
    		List userGroups = user.getGroups();
    		List<GroupData> newGroups = new ArrayList<GroupData>();
    		newGroups.add(g);
    		Iterator i = userGroups.iterator();
    		GroupData group;
    		while (i.hasNext()) {
				group = (GroupData) i.next();
				if (group.getId() != g.getId())
					newGroups.add(group);
			}
    		newOne.setGroups(newGroups);
    	}
		return newOne;
	}
	
	/** 
	 * Fires a property change event when a index is selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		selectedIndex = groups.getSelectedIndex();
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
