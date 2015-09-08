/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import omero.gateway.SecurityContext;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Dialog used to create <code>Group</code> or <code>Experimenter</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AdminDialog
extends JDialog
implements ActionListener, PropertyChangeListener
{

    /** Bound property indicating to create the object. */
    public static final String CREATE_ADMIN_PROPERTY = "createAdmin";

    /** Bound property indicating to enable of not the save property. */
    static final String ENABLE_SAVE_PROPERTY = "enableSave";

    /** The title of the dialog if the object to create is a group. */
    private static final String TITLE_GROUP = "Create Group";

    /** The title of the dialog if the object to create is an group.. */
    private static final String TITLE_EXPERIMENTER = "Create User";

    /** The text displayed if the object to create is a group. */
    private static final String TEXT_GROUP = "Create a new group";

    /** The text displayed if the object to create is a group. */
    private static final String TEXT_EXPERIMENTER = "Create a new User.";

    /** Action ID to close the dialog. */
    private static final int CANCEL = 0;

    /** Action ID to save the data. */
    private static final int SAVE = 1;

    /** Button to close the dialog. */
    private JButton cancel;

    /** Button to close the dialog. */
    private JButton save;

    /** The type of object to create. */
    private Class<?> type;

    /** The security context.*/
    private SecurityContext ctx;

    /** 
     * The main component displaying parameters required to 
     * create a group or an experimenter. 
     */
    private DataPane body;

    /** Initializes the components. */
    private void initComponents()
    {
        cancel = new JButton("Cancel");
        cancel.setActionCommand(""+CANCEL);
        cancel.addActionListener(this);
        save = new JButton("Create");
        save.setActionCommand(""+SAVE);
        save.addActionListener(this);
        save.setEnabled(false);
    }

    /** Closes and disposes of the dialog. */
    private void cancel()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Returns <code>true</code> if the name of the group already exists,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    private boolean isExistingObject(String name, boolean group)
    {
        AdminService svc = TreeViewerAgent.getRegistry().getAdminService();
        try {
            if (group) return svc.lookupGroup(ctx, name) != null;
            return svc.lookupExperimenter(ctx, name) != null;
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.print(e);
            TreeViewerAgent.getRegistry().getLogger().debug(this, msg);
        }
        return false;
    }

    /**
     * Notifies that a group with same name already exists if the passed
     * value is <code>true</code>, that an experimenter with the same name
     * already exists if the passed value is <code>false</code>.
     *
     * @param group Pass <code>true</code> to indicate that the message is
     *              related to a group, <code>false</code> if related to an
     *              experimenter.
     */
    private void notifyUser(boolean group)
    {
        UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        StringBuffer text = new StringBuffer();
        if (group) text.append("This Group name already exists.");
        else text.append("This User name already exists.");
        text.append("\nPlease choose another name.");
        un.notifyInfo("Admin Error", text.toString());
    }

    /** Saves the data. */
    private void save()
    {
        AdminObject object = null;
        if (body instanceof GroupPane) {
            object = ((GroupPane) body).getObjectToSave();
        } else if (body instanceof ExperimenterPane) {
            ExperimenterPane pane = (ExperimenterPane) body;
            Map<ExperimenterData, UserCredentials> m = pane.getObjectToSave();
            if (m.size() == 0) return;
            object = new AdminObject(m, AdminObject.CREATE_EXPERIMENTER);
            object.setGroups(pane.getSelectedGroups());
        }
        if (object == null) return;
        //Check if group already exist.
        Entry<ExperimenterData, UserCredentials> entry;
        Iterator<Entry<ExperimenterData, UserCredentials>> i;
        UserCredentials uc;
        Map<ExperimenterData, UserCredentials> map;
        boolean b = false;
        switch (object.getIndex()) {
        case AdminObject.CREATE_GROUP:
            b = isExistingObject(object.getGroup().getName(), true);
            if (b) {
                notifyUser(true);
                return;
            }
            map = object.getExperimenters();
            if (map != null) {
                i = map.entrySet().iterator();
                String password;
                StringBuffer text;
                UserNotifier un = 
                        TreeViewerAgent.getRegistry().getUserNotifier();
                while (i.hasNext()) {
                    entry = i.next();
                    uc = (UserCredentials) entry.getValue();
                    b = isExistingObject(uc.getUserName(), false);
                    if (!b) {
                        password = uc.getPassword();
                        if (password == null || password.length() == 0) {
                            text = new StringBuffer();
                            text.append("No Password set for the new User.");
                            text.append("\nPlease enter one.");
                            un.notifyInfo("Admin Error", text.toString());
                            return;
                        }
                    }
                }
            }
            break;
        case AdminObject.CREATE_EXPERIMENTER:
            map = object.getExperimenters();
            i = map.entrySet().iterator();
            while (i.hasNext()) {
                entry = i.next();
                uc = (UserCredentials) entry.getValue();
                b = isExistingObject(uc.getUserName(), false);
                if (b) {
                    notifyUser(false);
                    return;
                }
            }
        }
        firePropertyChange(CREATE_ADMIN_PROPERTY, null, object);
        cancel();
    }


    /**
     * Sets the property of the dialog.
     *
     * @param type The type to handle.
     */
    private void setProperties(Class<?> type)
    {
        setModal(true);
        if (GroupData.class.equals(type)) setTitle(TITLE_GROUP);
        else if (ExperimenterData.class.equals(type)) 
            setTitle(TITLE_EXPERIMENTER);
    }

    /**
     * Creates the header.
     *
     * @return See above.
     */
    private TitlePanel createHeader()
    {
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = null;
        if (GroupData.class.equals(type)) {
            tp = new TitlePanel(getTitle(), TEXT_GROUP,
                    icons.getIcon(IconManager.OWNER_GROUP_48));
        } else if (ExperimenterData.class.equals(type)) {
            String text = TEXT_EXPERIMENTER;
            tp = new TitlePanel(getTitle(), text,
                    icons.getIcon(IconManager.OWNER_48));
        }
        return tp;
    }

    /**
     * Builds and lays out the buttons.
     *
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.add(save);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        bar.add(cancel);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        return UIUtilities.buildComponentPanelRight(bar);
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(createHeader(), BorderLayout.NORTH);
        c.add(body, BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }

    /** 
     * Creates a new instance.
     * 
     * @param owner The owner of the frame.
     * @param ctx The security context.
     * @param type The type of object to create.
     * @param parent The parent of the data object or <code>null</code>.
     * @param groups The groups to add the experimenter to.
     */
    public AdminDialog(JFrame owner, SecurityContext ctx, Class<?> type,
            Object parent, Collection<DataObject> groups)
    {
        super(owner);
        setProperties(type);
        this.ctx = ctx;
        this.type = type;
        AdminService svc = TreeViewerAgent.getRegistry().getAdminService();
        if (ExperimenterData.class.equals(type)) {
            List<DataObject> selected = null;
            if (parent instanceof GroupData) {
                DataObject p = (DataObject) parent;
                selected = new ArrayList<DataObject>();
                selected.add(p);
                //Remove from the groups.
                Iterator<DataObject> i = groups.iterator();
                DataObject data;
                while (i.hasNext()) {
                    data = i.next();
                    if(data.getId() == p.getId() || svc.isSecuritySystemGroup(data.getId(), GroupData.USER)) {
                        i.remove();
                    }
                }
                i = selected.iterator();
                while (i.hasNext()) {
                    data = i.next();
                    if(svc.isSecuritySystemGroup(data.getId(), GroupData.USER)) {
                        i.remove();
                    }
                }
            }
            body = new ExperimenterPane(true, groups, selected);
        } else if (GroupData.class.equals(type)) {
            body = new GroupPane(TreeViewerAgent.isAdministrator());
        }
        body.addPropertyChangeListener(this);
        initComponents();
        buildGUI();
        pack();
        // workaround: weird size of the dialog, have to manually set a reasonable size
        setSize(new Dimension(330,650));
    }

    /**
     * Reacts to click on control.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        switch (index) {
        case CANCEL:
            cancel();
            break;
        case SAVE:
            save();
        }
    }

    /**
     * Reacts to property fired the component used to edit a group or
     * an experimenter.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (ExperimenterPane.EXPERIMENTER_ENABLE_SAVE_PROPERTY.equals(name)) {
            save.setEnabled((Boolean) evt.getNewValue());
        } else if (ENABLE_SAVE_PROPERTY.equals(name)) {
            if (body instanceof GroupPane) {
                Boolean b = (Boolean) evt.getNewValue();
                if (b.booleanValue()) {
                    save.setEnabled(((GroupPane) body).hasRequiredFields());
                } else {
                    save.setEnabled(false);
                }
            }
        }
    }

}
