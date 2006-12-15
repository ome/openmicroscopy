/*
 * adminTool.Model 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.model;

// Java imports
import java.util.ArrayList;
import java.util.List;

import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import src.adminTool.omero.OMEROMetadataStore;

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
public class Model {
    private long currentUser;

    private long currentGroup;

    private IAdmin iAdmin;

    private String loggedInUser;

    public Model(OMEROMetadataStore store, String user) throws IAdminException,
            UnknownException, PermissionsException {
        currentUser = -1;
        currentGroup = -1;
        loggedInUser = user;
        try {
            iAdmin = store.getAdminService();
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public boolean isDefaultGroup(String name) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            return iAdmin.getDefaultGroup(currentUser).getId() == getGroupID(name);
        } catch (ome.conditions.ApiUsageException e) {
            ExceptionHandler.get().catchException(e);
        }
        return false;
    }

    public boolean isDefaultGroup(long groupID) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            return iAdmin.getDefaultGroup(currentUser).getId() == groupID;
        } catch (ome.conditions.ApiUsageException e) {
            ExceptionHandler.get().catchException(e);
        }
        return false;
    }

    public String getAdmin() {
        return loggedInUser;
    }

    public boolean isSystemUser() {
        return isSystemUser(loggedInUser);
    }

    public boolean isSystemUser(String userName) {
        boolean foundUser = findUserByName(userName);
        if (!foundUser) {
            return false;
        }

        List<String> groupNames = new ArrayList<String>();
        try {
            ExperimenterGroup[] groups = iAdmin.containedGroups(currentUser);
            for (ExperimenterGroup e : groups) {
                groupNames.add(e.getName());
            }
        } catch (ome.conditions.ApiUsageException e) {
        }

        boolean systemUser = false;
        for (String group : groupNames) {
            if (group.equals("system")) {
                systemUser = true;
            }
        }
        return systemUser;
    }

    public void setSystemUser() throws IAdminException, UnknownException,
            PermissionsException {
        try {
            ExperimenterGroup group = iAdmin.lookupGroup("system");
            iAdmin.addGroups(iAdmin.getExperimenter(currentUser), group);
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public void setDefaultGroup(long groupID) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            iAdmin.setDefaultGroup(getUser(currentUser), getGroup(groupID));
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public void changePassword(String password) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            iAdmin.changeUserPassword(getUserName(currentUser), password);
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public Experimenter getUser(long i) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            return iAdmin.getExperimenter(i);
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
        return null;
    }

    public String getUserName(long i) {
        return iAdmin.getExperimenter(i).getOmeName();
    }

    public ExperimenterGroup getGroup(long i) {
        return iAdmin.getGroup(i);
    }

    public long addUser(Experimenter user) {
        long id;
        try {
            id = iAdmin.createUser(user, "default");
        } catch (ome.conditions.ApiUsageException e) {
            id = -1;
        }
        return id;
    }

    public long addGroup(ExperimenterGroup group) {

        currentGroup = iAdmin.createGroup(group);
        return currentGroup;
    }

    public void update(Experimenter user, String name) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            Experimenter old = iAdmin.lookupExperimenter(name);
            if (old == null) {
                return;
            }
            old.setOmeName(user.getOmeName());
            old.setFirstName(user.getFirstName());
            old.setMiddleName(user.getMiddleName());
            old.setLastName(user.getLastName());
            old.setEmail(user.getEmail());
            old.setInstitution(user.getInstitution());
            currentUser = old.getId();
            iAdmin.updateExperimenter(old);
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public void update(ExperimenterGroup group, String name)
            throws IAdminException, UnknownException, PermissionsException {
        try {
            ExperimenterGroup old = iAdmin.lookupGroup(name);
            if (old == null) {
                return;
            }
            old.setName(group.getName());
            old.setDescription(old.getDescription());
            currentGroup = old.getId();
            iAdmin.updateGroup(old);
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public ExperimenterGroup createNewGroup() {
        ExperimenterGroup group = new ExperimenterGroup();
        group.setDescription(new String(""));
        group.setName(new String("NewGroup"));
        return group;
    }

    public Experimenter createNewUser() {
        Experimenter user = new Experimenter();
        user.setOmeName(new String("NewUser"));
        user.setFirstName(new String(""));
        user.setMiddleName(new String(""));
        user.setLastName(new String(""));
        user.setInstitution(new String(""));
        user.setEmail(new String(""));
        return user;
    }

    /**
     * @return
     */
    public List<String> getUserList() {
        List<Experimenter> users = iAdmin.lookupExperimenters();
        List<String> userNames = new ArrayList<String>(users.size());
        for (Experimenter e : users) {
            if (!systemUser(e.getOmeName())) {
                userNames.add(e.getOmeName());
            }
        }
        return userNames;
    }

    public List<String> getGroupsList() {
        List<ExperimenterGroup> groups = iAdmin.lookupGroups();
        List<String> groupNames = new ArrayList<String>(groups.size());
        for (ExperimenterGroup g : groups) {
            if (!systemGroup(g.getName())) {
                groupNames.add(g.getName());
            }
        }
        return groupNames;
    }

    public boolean systemUser(String name) {
        if (name.equals("root")) {
            return true;
        }
        return false;
    }

    public boolean systemGroup(String name) {
        if (name.equals("system")) {
            return true;
        }
        if (name.equals("user")) {
            return true;
        }
        if (name.equals("default")) {
            return true;
        }
        return false;
    }

    public List<String> iAdminContainedExperimenters(long groupID) {
        List<String> userNames = new ArrayList<String>();
        try {
            Experimenter[] users = iAdmin.containedExperimenters(groupID);
            for (Experimenter e : users) {
                if (!systemUser(e.getOmeName())) {
                    userNames.add(e.getOmeName());
                }
            }
        } catch (ome.conditions.ApiUsageException e) {
        }
        return userNames;
    }

    // RENAME to getGroupMembers and remove all refs.
    public List<String> getUserGroups(long l) {
        return iAdminContainedExperimenters(l);
    }

    public List<String> iAdminContainedGroups(long userID) {
        List<String> groupNames = new ArrayList<String>();
        try {
            ExperimenterGroup[] groups = iAdmin.containedGroups(userID);
            for (ExperimenterGroup e : groups) {
                if (!systemGroup(e.getName())) {
                    groupNames.add(e.getName());
                }
            }
        } catch (ome.conditions.ApiUsageException e) {
        }
        return groupNames;
    }

    // RENAME to getGroupsContainingMembership and remove all refs.
    public List<String> getUserGroupMembership(long l) {
        return iAdminContainedGroups(l);
    }

    public void setCurrentUser(String name) {
        try {
            Experimenter user = iAdmin.lookupExperimenter(name);
            currentUser = user.getId();
        } catch (ome.conditions.ApiUsageException e) {
        }
    }

    public long getGroupID(String name) {
        try {
            ExperimenterGroup group = iAdmin.lookupGroup(name);
            return group.getId();
        } catch (ome.conditions.ApiUsageException e) {
        }
        return -1;
    }

    public Experimenter getCurrentUser() {
        try {
            return iAdmin.getExperimenter(currentUser);
        } catch (ome.conditions.ApiUsageException e) {
            return null;
        }
    }

    public void setCurrentGroup(String name) {
        try {
            ExperimenterGroup group = iAdmin.lookupGroup(name);
            currentGroup = group.getId();

        } catch (ome.conditions.ApiUsageException e) {
        }
    }

    public ExperimenterGroup getCurrentGroup() {
        try {
            return iAdmin.getGroup(currentGroup);
        } catch (ome.conditions.ApiUsageException e) {
        }
        return null;
    }

    public long getNumUsers() {
        List users = getUserList();
        return users.size();
    }

    public long getNumGroups() {
        List groups = getGroupsList();
        return groups.size();
    }

    public void addUsersToGroup(ExperimenterGroup group, ArrayList userList)
            throws IAdminException, UnknownException, PermissionsException {
        for (int i = 0; i < userList.size(); i++) {
            try {
                Experimenter user = iAdmin.lookupExperimenter((String) userList
                        .get(i));
                iAdmin.addGroups(user, group);
            } catch (Exception e) {
                ExceptionHandler.get().catchException(e);
            }
        }
    }

    public void addGroupToUser(long group) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            if (!userBelongsToGroup(currentUser, group)) {
                iAdmin.addGroups(iAdmin.getExperimenter(currentUser), iAdmin
                        .getGroup(group));
            }
            List<String> groups = getUserGroupMembership(currentUser);
            if (groups.size() == 1) {
                iAdmin.setDefaultGroup(getUser(currentUser), getGroup(group));
            }
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public boolean userBelongsToGroup(long userID, long groupID) {
        ExperimenterGroup grp = iAdmin.getGroup(groupID);
        List<String> groups = iAdminContainedGroups(userID);
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(grp.getName())) {
                return true;
            }
        }
        return false;
    }

    public void removeGroupFromUser(long group) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            if (userBelongsToGroup(currentUser, group)) {
                iAdmin.removeGroups(iAdmin.getExperimenter(currentUser), iAdmin
                        .getGroup(group));
            }
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public void removeUser(long userID) throws IAdminException,
            UnknownException, PermissionsException {
        try {
            iAdmin.deleteExperimenter(iAdmin.getExperimenter(userID));
        } catch (Exception e) {
            ExceptionHandler.get().catchException(e);
        }
    }

    public long getCurrentUserID() {
        return currentUser;
    }

    public long getCurrentGroupID() {
        return currentGroup;
    }

    /**
     * @param currentGroup
     * @return
     */
    public List<String> getGroupMembership(String currentGroup) {
        ArrayList<String> memberArray = new ArrayList();
        long groupID;
        ExperimenterGroup group = iAdmin.lookupGroup(currentGroup);
        groupID = group.getId();
        List<String> users = iAdminContainedExperimenters(groupID);
        return users;
    }

    public List<String> getGroupsUserNotMemberOf(String userName) {
        List<String> groupList = getGroupsList();
        List<String> userGroupList = getUserGroupMembership(iAdmin
                .lookupExperimenter(userName).getId());
        List<String> notInGroup = new ArrayList();
        for (int i = 0; i < groupList.size(); i++) {
            String groupName = groupList.get(i);
            boolean found = false;
            for (int j = 0; j < userGroupList.size(); j++) {
                String userGroup = userGroupList.get(j);
                if (userGroup.equals(groupName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                notInGroup.add(groupName);
            }
        }
        return notInGroup;
    }

    /**
     * @param currentGroup
     * @return
     */
    public List<String> getUsersNotInGroup(String currentGroup) {
        List<String> userList = getUserList();
        List<String> groupList = getGroupMembership(currentGroup);
        List<String> notInGroup = new ArrayList();
        for (int i = 0; i < userList.size(); i++) {
            String userName = userList.get(i);
            boolean found = false;
            for (int j = 0; j < groupList.size(); j++) {
                String userInGroup = groupList.get(j);
                if (userInGroup.equals(userName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                notInGroup.add(userName);
            }
        }
        return notInGroup;
    }

    public void removeGroup(String name) {
        // NOT AVAILABLE.
    }

    public boolean findUserByName(String name) {
        boolean found = true;
        Experimenter user = null;
        try {
            user = iAdmin.lookupExperimenter(name);
        } catch (ome.conditions.ApiUsageException e) {
            found = false;
        }
        if (found) {
            currentUser = user.getId();
        }
        return found;
    }

    public boolean findGroupByName(String name) {
        boolean found = true;
        ExperimenterGroup group = null;
        try {
            group = iAdmin.lookupGroup(name);
        } catch (ome.conditions.ApiUsageException e) {
            found = false;
        }
        if (found) {
            currentGroup = group.getId();
        }
        return found;
    }

}
