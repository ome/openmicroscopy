/*
 * adminTool.UserTabController 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.usersPanel;

// Java imports
import java.awt.Point;
import java.util.ArrayList;

import ome.model.meta.Experimenter;
import src.adminTool.model.IAdminException;
import src.adminTool.model.Model;
import src.adminTool.model.PermissionsException;
import src.adminTool.model.UnknownException;
import src.adminTool.ui.ErrorDialog;
import src.adminTool.ui.PasswordDialog;
import src.adminTool.ui.UserListController;
import src.adminTool.ui.messenger.DebugMessenger;

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
public class UsersTabController implements UserListController {
    private Model model;

    private UsersTab view;

    private String errorMsg;

    public UsersTabController(Model model, UsersTab view) {
        this.model = model;
        this.view = view;
    }

    void changePassword() {
        String user = view.getSelectedUser();
        if (user == null) {
            view.showErrorMessage("You must select a user before you can"
                    + " change their password.");
            return;
        }

        PasswordDialog passwordDialog = new PasswordDialog();
        Point loc = view.getLocationOnScreen();
        int x = loc.x + view.getWidth() / 2 - passwordDialog.getWidth() / 2;
        int y = loc.y + view.getHeight() / 2 - passwordDialog.getHeight() / 2;
        passwordDialog.setLocation(x, y);
        passwordDialog.setVisible(true);
        if (passwordDialog.OKSelected()) {
            model.setCurrentUser(user);
            try {
                model.changePassword(passwordDialog.getPassword());
            } catch (Exception e) {
                handleException(e, "Change User password");
            }
        }
    }

    void changeLoggedInUserPassword() {

    	PasswordDialog passwordDialog = new PasswordDialog();
        Point loc = view.getLocationOnScreen();
        int x = loc.x + view.getWidth() / 2 - passwordDialog.getWidth() / 2;
        int y = loc.y + view.getHeight() / 2 - passwordDialog.getHeight() / 2;
        passwordDialog.setLocation(x, y);
        passwordDialog.setVisible(true);
        if (passwordDialog.OKSelected()) {
            try {
                model.changeLoggedInUserPassword(passwordDialog.getPassword());
            } catch (Exception e) {
                handleException(e, "Change User password");
            }
        }
    }
    void setSystemUser() {
        String selectedUser = view.getSelectedUser();
        try {
            if (selectedUser == null) {
                view.showErrorMessage("You must select a user before you set "
                        + "default group.");
                return;
            }
            // Check user exists.
            if (!model.findUserByName(selectedUser)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find user in "
                                        + "Database. Admintool may be out of sync with Database."));
            }
        } catch (Exception e) {
            handleException(e,
                    "Admintool seems to be out of sync with database."
                            + "Try restarting Admintool.");
        }

        // Change the default group for the user to the one selected.
        try {
            model.setSystemUser();
            view.refresh();
            view.setUserDetails(model.getCurrentUser());
        } catch (Exception e) {
            handleException(e, "Set Current User as System user.");
        }
    }

    void setDefaultGroup() {
        String selectedUser = view.getSelectedUser();
        try {
            if (selectedUser == null) {
                view.showErrorMessage("You must select a user before you set "
                        + "default group.");
                return;
            }
            // Check user exists.
            if (!model.findUserByName(selectedUser)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find user in "
                                        + "Database. Admintool may be out of sync with Database."));
            }

            // check that the groupslist returns a non-null string.
            String groupSelected = view.getSelectedUserGroup();
            if (groupSelected == null) {
                view.showErrorMessage("You must select a group before you set "
                        + "default group.");
                return;
            }
            // Check that group exists.
            if (!model.findGroupByName(groupSelected)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find group in "
                                        + "Database. Admintool may be out of sync with Database."));
            }
        } catch (Exception e) {
            handleException(e,
                    "Admintool seems to be out of sync with database."
                            + "Try restarting Admintool.");
        }

        // Change the default group for the user to the one selected.
        try {
            model.setDefaultGroup(model.getCurrentGroupID());
            view.refresh();
            view.setUserDetails(model.getCurrentUser());
        } catch (Exception e) {
            handleException(e, "Set Current Group as Users Default Group");
        }
    }

    void addNewUser() {
        if (model.findUserByName("NewUser")) {
            view
                    .showErrorMessage("You have already created a user called \'NewUser\'."
                            + "This is the user you should rename/modify.");
            return;
        }
        try {
            Experimenter user = model.createNewUser();
            model.addUser(user);
        } catch (Exception e) {
            handleException(e, "Add New User to server");
        }
        view.clearForm();
        view.setToTop();

        view.refresh();
    }

    void saveUser() {

        if (!isValidUser()) {
            view.showErrorMessage(errorMsg);
            return;
        }
        Experimenter user = createUserFromView();
        try {
            model.update(user, view.getSelectedUser());
            view.refresh();
            view.setUserDetails(model.getCurrentUser());
        } catch (Exception e) {
            handleException(e, "Update user details");
        }
    }

    boolean isValidUser() {
        String value;
        value = view.getUserName();
        if (value.length() == 0) {
            errorMsg = new String("Cannot save user as username should not "
                    + "be empty.");
            return false;
        }
        if (value.equals("NewUser")) {
            errorMsg = new String("Cannot save user as you should change "
                    + "username from NewUser to something else.");
            return false;
        }
        if (view.getSelectedUser().equals("NewUser")
                && model.findUserByName(value)) {
            errorMsg = new String("A user with this name already Exists.");
            return false;
        }
        value = view.getFirstName();
        if (value.length() == 0) {
            errorMsg = new String("Cannot save user as first name should not "
                    + "be empty");
            return false;
        }
        value = view.getLastName();
        if (value.length() == 0) {
            errorMsg = new String("Cannot save user as last name should not "
                    + "be empty");
            return false;
        }
        ArrayList groups = view.getGroups();
        if (groups.size() != 0) {
            return false;
        }
        return true;
    }

    Experimenter createUserFromView() {
        Experimenter user = new Experimenter();
        user.setOmeName(view.getUserName());
        user.setFirstName(view.getFirstName());
        user.setMiddleName(view.getMiddleName());
        user.setLastName(view.getLastName());
        user.setInstitution(view.getInstitution());
        user.setEmail(view.getEmail());
        return user;
    }

    public void userSelected(String userName) {
        try {
            if (model.getNumUsers() == 0) {
                return;
            }
            model.setCurrentUser(userName);
            view.setUserDetails(model.getCurrentUser());
            if (view.getUserName().equals("NewUser")) {
                view.setUserNameEditable(true);
            } else {
                view.setUserNameEditable(false);
            }
        } catch (Exception e) {
            handleException(e, "Select User and Display details");
        }
    }

    public void addGroupToUser() {
        String selectedUser = view.getSelectedUser();
        try {
            if (selectedUser == null) {
                view.showErrorMessage("You must select a user before "
                        + "you add user to group.");
                return;
            }
            // Check user exists.
            if (!model.findUserByName(selectedUser)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find user in "
                                        + "Database.Admintool may be out of sync with Database."));
            }

            // check that the groupslist returns a non-null string.
            String groupSelected = view.getSelectedGroup();
            if (groupSelected == null) {
                view.showErrorMessage("You must select a group "
                        + "before adding user to group.");
                return;
            }
            // Check that group exists.
            if (!model.findGroupByName(groupSelected)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find group in "
                                        + "Database. Admintool may be out of sync with Database."));
            }
        } catch (Exception e) {
            handleException(e,
                    "Admintool seems to be out of sync with database."
                            + "Try restarting Admintool.");
        }
        try {
            model.addGroupToUser(model.getCurrentGroupID());
            view.setUserDetails(model.getCurrentUser());
        } catch (Exception e) {
            handleException(e, "Add Group to User");
        }
    }

    /**
     * Remove the group selected in the {@see UsersTab#groupList} from the users
     * list of groups. Once complete, redisplay the users new settings.
     */
    public void removeGroupFromUser() {
        // Check that userList returns a non-null string;
        String selectedUser = view.getSelectedUser();
        try {
            if (selectedUser == null) {
                view
                        .showErrorMessage("You must select a user before you can remove "
                                + "a user from group.");
                return;
            }
            // Check user exists.
            if (!model.findUserByName(selectedUser)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find user in "
                                        + "Database. Admintool may be out of sync with Database."));
            }

            // check that the groupslist returns a non-null string.
            String groupSelected = view.getSelectedUserGroup();
            if (groupSelected == null) {
                view
                        .showErrorMessage("You must select a group before you can remove "
                                + "user from group.");
                return;
            }
            // Check that group exists.
            if (!model.findGroupByName(groupSelected)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find group in "
                                        + "Database. Admintool may be out of sync with Database."));
            }
        } catch (Exception e) {
            handleException(e,
                    "Admintool seems to be out of sync with database. "
                            + "Try restarting Admintool.");
        }
        try {
            if (model.isDefaultGroup(model.getCurrentGroupID())) {
                view
                        .showErrorMessage("Cannot remove default group from user."
                                + " Change default group to another group and try again.");
                return;
            }
        } catch (Exception e) {
            handleException(e, "Problem find default group for User.");
        }
        try {
            // Remove the selected group from the user and refresh user details
            // on screen.
            model.removeGroupFromUser(model.getCurrentGroupID());
            view.setUserDetails(model.getCurrentUser());
        } catch (Exception e) {
            handleException(e, "Remove Group from User");
        }
    }

    public void addUser() {
        view.clearForm();
        try {
            if (model.findUserByName("NewUser")) {
                view
                        .showErrorMessage("A user called NewUser already exists. "
                                + "Please rename this user to the user you wish to create.");
                return;
            }
            addNewUser();
            view.refresh();
        } catch (Exception e) {
            handleException(e, "Add New User");
        }
    }

    public void removeUser() {
        String selectedUser = view.getSelectedUser();
        if (!view.confirmUserDeletion()) {
            return;
        }
        try {
            if (!model.findUserByName(selectedUser)) {
                throw new IAdminException(
                        new Exception(
                                "Cannot find user in "
                                        + "Database. Admintool may be out of sync with Database."));
            }
        } catch (Exception e) {
            handleException(e,
                    "Admintool seems to be out of sync with database."
                            + "Try restarting Admintool.");
        }
        try {
            model.removeUser(model.getCurrentUserID());
            view.clearForm();
            view.refresh();
        } catch (Exception e) {
            handleException(e, "Remove user from system");
        }
    }

    void handleException(Exception e, String operation) {
        if (e instanceof IAdminException || e instanceof PermissionsException) {
            ErrorDialog dlg;
            if (e instanceof PermissionsException) {
                PermissionsException p = (PermissionsException) e;
                String reason = "You do not have sufficient privileges to "
                        + operation;
                Point location = view.getLocationOnScreen();
                location.x += view.getWidth() / 2 - 200;
                location.y += view.getHeight() / 2 - 150;
                dlg = new ErrorDialog(location, "Permissions Failure", e,
                        reason);
            } else {
                IAdminException a = (IAdminException) e;
                String reason = "While trying to " + operation + " the server "
                        + "returned an error";
                Point location = view.getLocationOnScreen();
                location.x += view.getWidth() / 2 - 200;
                location.y += view.getHeight() / 2 - 150;
                dlg = new ErrorDialog(location,
                        "IAdmin Could not complete task", e, reason);
            }
        } else {
            if (e instanceof UnknownException) {
                UnknownException error = (UnknownException) e;
                DebugMessenger debug = new DebugMessenger(null,
                        "An Unexpected " + "Error has Occurred", true, error
                                .getException());
            } else {
                DebugMessenger debug = new DebugMessenger(null,
                        "An Unexpected " + "Error has Occurred", true, e);
            }
        }
    }

}
