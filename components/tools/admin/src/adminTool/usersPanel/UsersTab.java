/*
 * adminTool.UsersTab 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.usersPanel;

// Java imports

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import ome.model.meta.Experimenter;

import layout.TableLayout;

import src.adminTool.model.Model;
import src.adminTool.ui.GroupList;
import src.adminTool.ui.ImageFactory;
import src.adminTool.ui.UserGroupMembershipList;
import src.adminTool.ui.UserList;
import src.adminTool.ui.messenger.MessageBox;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class UsersTab extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -4955413583430334254L;

    private UserList userList;

    private GroupList groupList;

    private UserGroupMembershipList userGroupMembershipList;

    private JLabel firstNameLabel;

    private JLabel middleNameLabel;

    private JLabel lastNameLabel;

    private JLabel institutionLabel;

    private JLabel userNameLabel;

    private JLabel emailLabel;

    private JLabel userListLabel;

    private JLabel groupListLabel;

    private JLabel userGroupListLabel;

    private JTextField firstName;

    private JTextField middleName;

    private JTextField lastName;

    private JTextField institution;

    private JTextField userName;

    private JTextField email;

    private JButton saveBtn;

    private JButton resetPasswordBtn;

    private JButton setDefaultBtn;

    private JButton setSystemBtn;

    private JButton addUserBtn;

    private JButton removeUserBtn;

    private JButton addToGroupBtn;

    private JButton removeFromGroupBtn;

    private Model model;

    private UsersTabController controller;

    public String getUserName() {
        return userName.getText();
    }

    public String getFirstName() {
        return firstName.getText();
    }

    public String getMiddleName() {
        return middleName.getText();
    }

    public String getLastName() {
        return lastName.getText();
    }

    public String getInstitution() {
        return institution.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    public ArrayList getGroups() {
        return new ArrayList();
    }

    public void clearForm() {
        userName.setText("");
        firstName.setText("");
        middleName.setText("");
        lastName.setText("");
        institution.setText("");
        email.setText("");
        userName.setText("");
        userGroupMembershipList.clear();
    }

    public void refresh() {
        userGroupMembershipList.clear();
        userList.refresh();
        groupList.filterOff();
        groupList.refresh();
    }

    public void setToTop() {
        userName.requestFocusInWindow();
    }

    public void showErrorMessage(String errorMsg) {
        MessageBox msg = new MessageBox(this.getLocationOnScreen(), "Warning",
                errorMsg);
        setToTop();
    }

    public void setUserDetails(Experimenter user) {
        userName.setText(user.getOmeName());
        firstName.setText(user.getFirstName());
        middleName.setText(user.getMiddleName());
        lastName.setText(user.getLastName());
        institution.setText(user.getInstitution());
        email.setText(user.getEmail());
        userGroupMembershipList.setUser(user.getOmeName());
        groupList.filterGroupsByUser(user.getOmeName());
    }

    public void setUserNameEditable(boolean isEditable) {
        userName.setEnabled(isEditable);
    }

    public String getSelectedUser() {
        return userList.getSelectedUser();
    }

    public String getSelectedUserGroup() {
        return userGroupMembershipList.getSelectedUserGroup();
    }

    public String getSelectedGroup() {
        return groupList.getSelectedGroup();
    }

    public UsersTab(Model model) {
        this.model = model;
        createUIElements();
        setPermissions();
        buildUI();
    }

    public void setController(UsersTabController controller) {
        this.controller = controller;
        userList.setController(controller);
        addToGroupBtn.addActionListener(new GroupAction(GroupAction.ADD,
                controller));
        removeFromGroupBtn.addActionListener(new GroupAction(
                GroupAction.REMOVE, controller));
        addUserBtn
                .addActionListener(new UserAction(UserAction.ADD, controller));
        removeUserBtn.addActionListener(new UserAction(UserAction.REMOVE,
                controller));
        saveBtn.addActionListener(new SaveAction(controller));
        resetPasswordBtn.addActionListener(new PasswordAction(controller));
        setDefaultBtn.addActionListener(new SetDefaultAction(controller));
        setSystemBtn.addActionListener(new SetSystemAction(controller));
    }

    void createUIElements() {
        createUserList();
        createGroupList();
        createLabels();
        createTextFields();
        createActionButtons();

    }

    void createUserList() {
        userList = new UserList(model);
    }

    void createGroupList() {
        groupList = new GroupList(model);
        userGroupMembershipList = new UserGroupMembershipList(model);
    }

    void createLabels() {
        firstNameLabel = new JLabel("First Name");
        middleNameLabel = new JLabel("Middle Name");
        lastNameLabel = new JLabel("Last Name");
        userNameLabel = new JLabel("Username");
        institutionLabel = new JLabel("Institution");
        emailLabel = new JLabel("e-mail");
        groupListLabel = new JLabel("Groups");
        groupListLabel.setHorizontalAlignment(SwingConstants.LEFT);
        userListLabel = new JLabel("Users");
        userListLabel.setHorizontalAlignment(SwingConstants.LEFT);
        userGroupListLabel = new JLabel("User belongs to ");
        userGroupListLabel.setHorizontalAlignment(SwingConstants.LEFT);
    }

    void createTextFields() {
        firstName = new JTextField("Firstname");
        middleName = new JTextField("Middlename");
        lastName = new JTextField("Lastname");
        email = new JTextField("user@domain.suffix");
        institution = new JTextField("Institution");
        userName = new JTextField("Username");
    }

    void createActionButtons() {
        ImageIcon addUserIcon = ImageFactory.get().image(ImageFactory.ADD_USER);
        ImageIcon removeUserIcon = ImageFactory.get().image(
                ImageFactory.REMOVE_USER);
        ImageIcon addGroupIcon = ImageFactory.get().image(
                ImageFactory.RIGHT_ARROW);
        ImageIcon removeGroupIcon = ImageFactory.get().image(
                ImageFactory.LEFT_ARROW);
        ImageIcon defaultGroupIcon = ImageFactory.get().image(
                ImageFactory.DEFAULT_GROUP);
        saveBtn = new JButton("Save");
        resetPasswordBtn = new JButton("Reset Password");
        setSystemBtn = new JButton("Set System User");
        addUserBtn = new JButton(addUserIcon);
        removeUserBtn = new JButton(removeUserIcon);
        addToGroupBtn = new JButton(addGroupIcon);
        removeFromGroupBtn = new JButton(removeGroupIcon);
        setDefaultBtn = new JButton(defaultGroupIcon);
        setDefaultBtn.setToolTipText("Set Default Group");
    }

    void setPermissions() {
        if (!model.isSystemUser()) {
            saveBtn.setEnabled(false);
            addUserBtn.setEnabled(false);
            removeUserBtn.setEnabled(false);
            addToGroupBtn.setEnabled(false);
            removeFromGroupBtn.setEnabled(false);
            setDefaultBtn.setEnabled(false);
            setSystemBtn.setEnabled(false);
        } else {
            saveBtn.setEnabled(true);
            addUserBtn.setEnabled(true);
            removeUserBtn.setEnabled(true);
            addToGroupBtn.setEnabled(true);
            removeFromGroupBtn.setEnabled(true);
            setDefaultBtn.setEnabled(true);
            setSystemBtn.setEnabled(true);
        }
    }

    JPanel createLabelText(JLabel label, JTextField text) {
        double size[][] = { { 0.2, 0.05, TableLayout.FILL }, { 20 } };

        text.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JPanel panel = new JPanel();
        panel.setLayout(new TableLayout(size));
        panel.add(label, "0, 0");
        panel.add(text, " 2, 0");
        return panel;
    }

    void buildUI() {
        // Data Entry elements : Textfields.
        JPanel userDataEntryPanel = new JPanel();
        userDataEntryPanel.setLayout(new BoxLayout(userDataEntryPanel,
                BoxLayout.Y_AXIS));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(userNameLabel, userName));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(firstNameLabel, firstName));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(middleNameLabel, middleName));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(lastNameLabel, lastName));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(emailLabel, email));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        userDataEntryPanel.add(createLabelText(institutionLabel, institution));
        userDataEntryPanel.add(Box.createVerticalStrut(5));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalStrut(100));
        buttonPanel.add(resetPasswordBtn);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(setSystemBtn);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(saveBtn);
        userDataEntryPanel.add(buttonPanel);

        // buttons controlling which group user belongs to.
        JPanel userGroupButtonPanel = new JPanel();
        userGroupButtonPanel.setLayout(new BoxLayout(userGroupButtonPanel,
                BoxLayout.Y_AXIS));
        userGroupButtonPanel.add(addToGroupBtn);
        userGroupButtonPanel.add(removeFromGroupBtn);
        userGroupButtonPanel.add(setDefaultBtn);

        // label and list box containing group user belongs to.
        JPanel userGroupListPanel = new JPanel();
        userGroupListPanel.setLayout(new BoxLayout(userGroupListPanel,
                BoxLayout.Y_AXIS));
        userGroupListPanel.add(userGroupListLabel);
        userGroupListPanel.add(userGroupMembershipList);

        // panel holding user group list and buttons.
        JPanel userGroupPanel = new JPanel();
        userGroupPanel
                .setLayout(new BoxLayout(userGroupPanel, BoxLayout.X_AXIS));
        userGroupPanel.add(Box.createHorizontalStrut(30));
        userGroupPanel.add(userGroupButtonPanel);
        userGroupPanel.add(Box.createHorizontalStrut(30));
        userGroupPanel.add(userGroupListPanel);

        // Panel holding all user details, data entry and group list.
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.add(userDataEntryPanel);
        userInfoPanel.add(Box.createVerticalStrut(15));
        userInfoPanel.add(userGroupPanel);

        // List of all groups.
        JPanel groupListPanel = new JPanel();
        groupListPanel
                .setLayout(new BoxLayout(groupListPanel, BoxLayout.X_AXIS));
        groupListPanel.add(Box.createHorizontalStrut(55));
        groupListPanel.add(groupList);

        // Buttons for user panel.
        JPanel userListButtonPanel = new JPanel();
        userListButtonPanel.setLayout(new BoxLayout(userListButtonPanel,
                BoxLayout.Y_AXIS));
        userListButtonPanel.add(addUserBtn);
        userListButtonPanel.add(removeUserBtn);

        // User List panel.
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.add(userListLabel);
        userListPanel.add(userList);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
        userPanel.add(userListButtonPanel);
        userPanel.add(userListPanel);

        // Lists of all groups and users.
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.add(userPanel);
        listContainer.add(Box.createVerticalStrut(30));
        listContainer.add(groupListLabel);
        listContainer.add(groupListPanel);

        // Adding userlist and group list to left of form and user details to
        // right.
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(listContainer);
        container.add(Box.createHorizontalStrut(50));
        container.add(userInfoPanel);
        // container.add(Box.createHorizontalStrut(10));
        this.setLayout(new BorderLayout());
        this.add(container);
    }

    /**
     * @return
     */
    public boolean confirmUserDeletion() {
        int answer = JOptionPane
                .showConfirmDialog(
                        this,
                        "Are you sure you wish to delete this user.\n"
                                + "This operation may not work as the server will not\n"
                                + "delete users who own data in the server.",
                        "Delete user?", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
}
