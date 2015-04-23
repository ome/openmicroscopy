/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.GroupItem
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.util;



//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.SelectableMenu;
import pojos.ExperimenterData;
import pojos.GroupData;

/**
 * Hosts the group and its associated menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class GroupItem
    extends SelectableMenu
    implements PropertyChangeListener
{

    /** Bound property indicating that the users have been selected.*/
    public static final String USER_SELECTION_PROPERTY;

    /** Text indicating to select all the groups.*/
    public static final String ALL_GROUPS;

    /** Bound property indicating to select all the groups.*/
    public static final String ALL_GROUPS_SELECTION_PROPERTY;

    /** Bound property indicating to select all the groups.*/
    public static final String ALL_GROUPS_DESELECTION_PROPERTY;

    /** Bound property indicating to select all the users.*/
    public static final String ALL_USERS_SELECTION_PROPERTY;

    static {
        USER_SELECTION_PROPERTY = "userSelection";
        ALL_GROUPS = "All Groups";
        ALL_GROUPS_SELECTION_PROPERTY = "allGroupsSelection";
        ALL_GROUPS_DESELECTION_PROPERTY = "allGroupsDeselection";
        ALL_USERS_SELECTION_PROPERTY = "allUsersSelection";
    }

    /** The group hosted by this component.*/
    private GroupData group;

    /** The List of components hosting the user.*/
    private List<DataMenuItem> usersItem;

    /** The identifier of the user currently logged in.*/
    private long userID;

    /**
     * Creates a new instance.
     * 
     * @param group The group hosted by this component.
     * @param selected Pass <code>true</code> if the group is selected,
     *                 <code>false</code>.
     * @param selectable Pass <code>true</code> to allow user selection,
     *                   <code>false</code> otherwise.
     */
    public GroupItem(GroupData group, boolean selected, boolean selectable)
    {
        super(selected, group.getName(), selectable);
        this.group = group;
        addPropertyChangeListener(this);
    }

    /**
     * Creates a new instance.
     * 
     * @param group The group hosted by this component.
     * @param selected Pass <code>true</code> if the group is selected,
     *                 <code>false</code>.
     */
    public GroupItem(GroupData group, boolean selected)
    {
        super(selected, group.getName(), true);
        this.group = group;
        addPropertyChangeListener(this);
    }

    /**
     * Creates a new default instance.
     *
     * @param selected Pass <code>true</code> if the item is selected,
     *                 <code>false</code>.
     */
    public GroupItem(boolean selected)
    {
        super(selected, ALL_GROUPS, true);
        addPropertyChangeListener(this);
    }

    /**
     * Sets the list of components hosting the users.
     * 
     * @param usersItem The value to set.
     */
    public void setUsersItem(List<DataMenuItem> usersItem)
    {
        this.usersItem = usersItem;
    }

    /**
     * Sets the identifier of the user currently logged in.
     * 
     * @param userID The value to set.
     */
    public void setUserID(long userID) { this.userID = userID; }

    /**
     * Returns the group.
     * 
     * @return See above.
     */
    public GroupData getGroup() { return group; }

    /**
     * Returns the selected users.
     * 
     * @return See above.
     */
    public List<ExperimenterData> getSeletectedUsers()
    {
        List<ExperimenterData> users = new ArrayList<ExperimenterData>();
        Iterator<DataMenuItem> i = usersItem.iterator();
        DataMenuItem item;
        Object ho;
        while (i.hasNext()) {
            item = i.next();
            ho = item.getDataObject();
            if (item.isChecked() && ho instanceof ExperimenterData)
                users.add((ExperimenterData) ho);
        }
        return users;
    }

    /**
     * Sets the enabled flag of the items hosting the users.
     *
     * @param enabled Pass <code>true</code> to enable (user display),
     *                <code>false</code> otherwise (group display).
     */
    public void setDisplay(boolean enabled)
    {
        Iterator<DataMenuItem> i = usersItem.iterator();
        DataMenuItem item;
        ExperimenterData exp;
        Object ho;
        while (i.hasNext()) {
            item = i.next();
            item.setEnabled(enabled);
            if (!enabled) {
                item.setChecked(true);
            } else {
                ho = item.getDataObject();
                if (ho instanceof ExperimenterData) {
                    exp = (ExperimenterData) ho;
                    item.setChecked(exp.getId() == userID);
                } else item.setChecked(false);
            }
        }
    }

    /** 
     * Selects the user currently logged in if no user already selected.
     *
     * @param all Pass <code>true</code> to select all the users or
     *            <code>false</code> to select the user currently logged in
     *            if no user already selected.
     * @param select Pass <code>true</code> to select the users,
     *               <code>false</code> to de-select.
     */
    public void selectUsers(boolean all, boolean select)
    {
        DataMenuItem data;
        Object ho;
        if (all) {
            Iterator<DataMenuItem> i = usersItem.iterator();
            while (i.hasNext()) {
                data = i.next();
                ho = data.getDataObject();
                if (ho instanceof ExperimenterData && data.isEnabled()) {
                    data.setChecked(select);
                    long id = ((ExperimenterData) ho).getId();
                    //always keep the user currently logged in even if 
                    //select is false
                    if (id == userID) {
                        data.setChecked(true);
                    }
                } else if (ho instanceof String) {
                    data.removePropertyChangeListener(this);
                    data.setChecked(select);
                    data.addPropertyChangeListener(this);
                }
            }
        } else {
            List<ExperimenterData> l = getSeletectedUsers();
            if (CollectionUtils.isEmpty(l)) {
                //select the user currently logged in
                Iterator<DataMenuItem> i = usersItem.iterator();
                
                while (i.hasNext()) {
                    data = i.next();
                    ho = data.getDataObject();
                    if (ho instanceof ExperimenterData && data.isEnabled()) {
                        long id = ((ExperimenterData) ho).getId();
                        if (id == userID) {
                            data.setChecked(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    /** Handles the selection of menu items.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (DataMenuItem.ITEM_SELECTED_PROPERTY.equals(name)) {
            DataMenuItem item = (DataMenuItem) evt.getNewValue();
            Object ho = item.getDataObject();
            ExperimenterData exp;
            Iterator<DataMenuItem> i;
            List<ExperimenterData> l;
            if (ho instanceof String) {
                String v = (String) ho;
                if (DataMenuItem.ALL_USERS_TEXT.equals(v)) {
                    selectUsers(true, item.isChecked());
                    i = usersItem.iterator();
                    boolean b = item.isChecked();
                    while (i.hasNext()) {
                        item = i.next();
                        ho = item.getDataObject();
                        if (ho instanceof ExperimenterData && item.isEnabled()) {
                            exp = (ExperimenterData) ho;
                            if (b) item.setChecked(b);
                            else {
                                if (exp.getId() != userID)
                                    item.setChecked(b);
                            }
                        }
                    }
                }
            } else {
                l = getSeletectedUsers();
              //check that if we keep the "show All users" selected
                boolean all = l.size() == usersItem.size()-1;
                i = usersItem.iterator();
                while (i.hasNext()) {
                    item = i.next();
                    ho = item.getDataObject();
                    if (ho instanceof String) {
                        String v = (String) ho;
                        if (DataMenuItem.ALL_USERS_TEXT.equals(v)) {
                            item.removePropertyChangeListener(this);
                            item.setChecked(all);
                            item.addPropertyChangeListener(this);
                        }
                    }
                }
            }
            l = getSeletectedUsers();
            if (getGroup() != null) {
                if (isSelectable())
                    setMenuSelected(CollectionUtils.isNotEmpty(l), false);
                firePropertyChange(USER_SELECTION_PROPERTY, null, this);
            } else {
                //no longer select the group.
                boolean selected = item.isChecked();
                if (!selected && isMenuSelected()) {
                    setMenuSelected(false, false);
                } else if (selected && !isMenuSelected()) {
                    setMenuSelected(true, false);
                }
                firePropertyChange(ALL_USERS_SELECTION_PROPERTY, null,
                        selected);
            }
        } else if (SelectableMenu.GROUP_SELECTION_PROPERTY.equals(name)) {
            GroupItem item = (GroupItem) evt.getNewValue();
            if (item != this) return;
            //Check if all groups need to be selected.
            GroupData group = item.getGroup();
            if (group == null && GroupItem.ALL_GROUPS.equals(item.getText())) {
                if (item.isMenuSelected())
                    firePropertyChange(ALL_GROUPS_SELECTION_PROPERTY, null,
                            this);
                else firePropertyChange(ALL_GROUPS_DESELECTION_PROPERTY, null,
                        this);
                return;
            }
            if (!item.isMenuSelected()) {
                firePropertyChange(USER_SELECTION_PROPERTY, null, this);
            } else {
                selectUsers(false, true);
                firePropertyChange(USER_SELECTION_PROPERTY, null, this);
            }
        }
    }

}
