/*
 * adminTool.GroupsTabController 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.groupPanel;

// Java imports
import java.awt.Point;
import java.util.ArrayList;
import ome.model.meta.ExperimenterGroup;

import src.adminTool.model.IAdminException;
import src.adminTool.model.Model;
import src.adminTool.model.PermissionsException;
import src.adminTool.model.UnknownException;
import src.adminTool.ui.ErrorDialog;
import src.adminTool.ui.GroupListController;
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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class GroupsTabController implements UserListController,
        GroupListController {
    private Model model;

    private GroupsTab view;

    private String errorMsg;

    public GroupsTabController(Model model, GroupsTab view) {
        this.model = model;
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see adminTool.UserListController#userSelected(int)
     */
    public void userSelected(String index) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see adminTool.GroupListController#groupSelected(int)
     */
    public void groupSelected(String name) {
        if (model.getNumGroups() == 0) {
            view.showErrorMessage("You need to add groups.");
            return;
        }

        model.setCurrentGroup(name);
        view.setGroupDetails(model.getCurrentGroup());
        if (view.getGroupName().equals("NewGroup"))
            view.setGroupNameEditable(true);
        else
            view.setGroupNameEditable(false);
    }

    public void addNewGroup() {
        if (model.findGroupByName("NewGroup")) {
            view.showErrorMessage("A group called NewGroup already exists. "
                    + "Please rename this group to the group you "
                    + "wish to create.");
            return;
        }
        ExperimenterGroup group = model.createNewGroup();
        model.addGroup(group);

        view.clearForm();
        view.setToTop();
        view.refresh();
        view.selectCurrentGroup();
    }

    public void removeGroup() {
        String selectedGroup = view.getSelectedGroup();
        if (selectedGroup == null) {
            view.showErrorMessage("Please select a group to remove.");
            return;
        }
        model.removeGroup(selectedGroup);
        view.clearForm();
        view.refresh();
    }

    void saveGroup() {
        if (!isValidGroup()) {
            view.showErrorMessage(errorMsg);
            return;
        }
        ExperimenterGroup group = createGroupFromView();

        ArrayList users = view.getUsers();

        try {
            model.update(group, view.getSelectedGroup());
        } catch (Exception e) {
            handleException(e, "save current group");
        }
        // model.addUsersToGroup(group, users);
        view.refresh();
    }

    boolean isValidGroup() {
        String value;
        value = view.getGroupName();
        if (value.length() == 0) {
            errorMsg = new String("Groupname should not be empty.");
            return false;
        }
        if (value.equals("NewGroup")) {
            errorMsg = new String("Must change groupname "
                    + "from NewGroup to something else.");
            return false;
        }

        if (view.getSelectedGroup().equals("NewGroup")
                && model.findGroupByName(value)) {
            errorMsg = new String("A group with this name already exists.");
            return false;
        }
        value = view.getGroupDescription();
        if (value.length() == 0) {
            errorMsg = new String("Group description should not be empty");
            return false;
        }
        return true;
    }

    ExperimenterGroup createGroupFromView() {
        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(view.getGroupName());
        group.setDescription(view.getGroupDescription());
        return group;
    }

    public void addUser() {
        try {
            String selectedUser = view.getSelectedUser();
            if (selectedUser == null) {
                view.showErrorMessage("Please select a user to add.");
                return;
            }

            if (!model.findUserByName(selectedUser))
                return;

            String groupSelected = view.getSelectedGroup();
            if (groupSelected == null) {
                view
                        .showErrorMessage("Please select the group you wish add to user.");
                return;
            }

            if (!model.findGroupByName(groupSelected))
                return;

            model.addGroupToUser(model.getCurrentGroupID());

            view.setGroupDetails(model.getCurrentGroup());
            // view.refresh();
            view.refreshMembershipList();
        } catch (Exception e) {
            handleException(e, "add user to a group");
        }

    }

    public void removeUser() {
        try {
            String selectedUser = view.getSelectedMember();
            if (selectedUser == null) {
                view.showErrorMessage("Please select a user to remove.");
                return;
            }
            if (!model.findUserByName(selectedUser))
                return;

            String groupSelected = view.getSelectedGroup();
            if (groupSelected == null) {
                view
                        .showErrorMessage("Please select a group to remove from user.");
                return;
            }

            if (!model.findGroupByName(groupSelected))
                return;
            model.removeGroupFromUser(model.getCurrentGroupID());
            view.setGroupDetails(model.getCurrentGroup());
            view.refreshMembershipList();
        } catch (Exception e) {
            handleException(e, "save current group");
        }

    }

    void handleException(Exception e, String operation) {
        if (e instanceof IAdminException || e instanceof PermissionsException) {
            ErrorDialog dlg;
            if (e instanceof PermissionsException) {
                String reason = "You do not have sufficient privileges to "
                        + operation;
                Point location = view.getLocationOnScreen();
                location.x += view.getWidth() / 2 - 200;
                location.y += view.getHeight() / 2 - 150;
                dlg = new ErrorDialog(location, "Permissions Failure", e,
                        reason);
            } else {
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
