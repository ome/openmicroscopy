/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent 
 *
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
package org.openmicroscopy.shoola.agents.fsimporter;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenuItem;

//Third-party libraries





import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.events.treeviewer.ActivitiesEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.ChangeUserGroupEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.ExperimenterLoadedDataEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.SaveResultsEvent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.fsimporter.view.ImporterFactory;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * This agent interacts is used to import images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImporterAgent
	implements Agent, AgentEventListener
{

    /** Reference to the registry. */
    private static Registry registry;

    /** The selected browser type.*/
    private int browserType;
    
    /** The objects displayed.*/
    private Map<Long, Map<Long, List<TreeImageDisplay>>> objects;
    
    /** The group id if set.*/
    private long groupId;

    private boolean isMaster;

    /** The display mode.*/
    private int displayMode = -1;

    /**
     * Helper method.
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }

    /**
     * Returns <code>true</code> if the currently logged in user
     * is an administrator, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public static boolean isAdministrator()
    {
        Boolean b = (Boolean) registry.lookup(LookupNames.USER_ADMINISTRATOR);
        if (b == null) return false;
        return b.booleanValue();
    }

    /**
     * Helper method returning the current user's details.
     * 
     * @return See above.
     */
    public static ExperimenterData getUserDetails()
    { 
        return (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
    }

    /**
     * Returns how deep to scan when a folder is selected.
     * 
     * @return See above.
     */
    public static int getScanningDepth()
    {
        Integer value = (Integer) registry.lookup("/options/ScanningDepth");
        if (value == null || value.intValue() < 0) return 1;
        return value.intValue();
    }

    /**
     * Returns the available user groups.
     * 
     * @return See above.
     */
    public static Collection getAvailableUserGroups()
    {
        return (Collection) registry.lookup(LookupNames.USER_GROUP_DETAILS);
    }

    /**
     * Returns the identifier of the plugin to run.
     * 
     * @return See above.
     */
    public static int runAsPlugin()
    {
        Environment env = (Environment) registry.lookup(LookupNames.ENV);
        if (env == null) return -1;
        return env.runAsPlugin();
    }

    /**
     * Returns the default value from the configuration file.
     * 
     * @return See above.
     */
    private int getDefaultBrowser()
    {
        Environment env = (Environment) registry.lookup(LookupNames.ENV);
        if (env == null) return BrowserSelectionEvent.PROJECT_TYPE;
        switch (env.getDefaultHierarchy()) {
        case LookupNames.HCS_ENTRY:
            return BrowserSelectionEvent.SCREEN_TYPE;
        default:
            return 	BrowserSelectionEvent.PROJECT_TYPE;
        }
    }

    /**
     * Handles the {@link LoadImporter} event.
     * 
     * @param evt The event to handle.
     */
    private void handleLoadImporter(LoadImporter evt)
    {
        if (evt == null) return;
        long groupId = evt.getGroup();
        Importer importer = ImporterFactory.getImporter(groupId, displayMode);
        if (importer != null) {
            int t;
            switch (evt.getType()) {
            case BrowserSelectionEvent.PROJECT_TYPE:
            case BrowserSelectionEvent.SCREEN_TYPE:
                t = evt.getType();
                break;
            default:
                if (browserType == BrowserSelectionEvent.PROJECT_TYPE ||
                browserType == BrowserSelectionEvent.SCREEN_TYPE)
                    t = browserType;
                else t = getDefaultBrowser();
            }
            importer.activate(t, evt.getSelectedContainer(), null,
                    evt.getUser());
        }
    }

    /**
     * Removes all the references to the existing imports.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
        if (evt == null) return;
        ImporterFactory.onGroupSwitched(evt.isSuccessful());
    }

    /**
     * Indicates that it was possible to reconnect.
     * 
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
        if (evt == null) return;

        ImporterFactory.onReconnected();
        if (isMaster)
        {
            Importer importer = ImporterFactory.getImporter(
                    getUserDetails().getGroupId(), true, displayMode);
            importer.activate(browserType, null, null,
                    importer.getImportFor());
        }
    }

    /**
     * Returns the containers if available for the specified group.
     * 
     * @param groupId The id of the group.
     */
    private List<Object> handleContainers(long groupId)
    {
        if (objects == null) return null;
        Map<Long, List<TreeImageDisplay>> data = objects.get(groupId);
        if (data == null) return null;
        List<TreeImageDisplay> l = data.get(getUserDetails().getId());
        if (l == null) return null;
        Iterator<TreeImageDisplay> i = l.iterator();
        List<Object> values = new ArrayList<Object>();
        while (i.hasNext()) {
            values.add(i.next().getUserObject());
        }
        return values;
    }

    /**
     * Handles the fact that data were loaded.
     * 
     * @param evt The event to handle.
     */
    private void handleExperimenterLoadedDataEvent(
            ExperimenterLoadedDataEvent evt)
    {
        if (evt == null) return;

        Map<Long, Map<Long, List<TreeImageDisplay>>> map = evt.getData();
        objects = map;
        if (!ImporterFactory.doesImporterExist()) return;
        Importer importer = ImporterFactory.getImporter(groupId, displayMode);
        if (importer == null || map == null || map.size() == 0) return;
        GroupData group = importer.getSelectedGroup();
        if (group == null) return;
        List<Object> l = handleContainers(group.getId());
        if (CollectionUtils.isEmpty(l)) return;
        importer.setContainers(l, true, false, browserType,
                importer.getImportFor());
    }

    /**
     * Updates the view when the mode is changed.
     * 
     * @param evt The event to handle.
     */
    private void handleDisplayModeEvent(DisplayModeEvent evt)
    {
        if (evt == null) return;
        displayMode = evt.getDisplayMode();
        ImporterFactory.setDiplayMode(displayMode);
    }

    /**
     * Updates the view when the mode is changed.
     * 
     * @param evt The event to handle.
     */
    private void handleActivitiesEvent(ActivitiesEvent evt)
    {
        if (evt == null) return;
        ImporterFactory.setDiplayMode(displayMode);
    }

    /**
     * Imports and saved the results when completed.
     * 
     * @param evt The event to handle.
     */
    private void handleSaveResultsEvent(SaveResultsEvent evt)
    {
        if (evt == null) return;
        Importer importer = ImporterFactory.getImporter(groupId, displayMode);
        importer.importResults(evt.getObject(), evt.isFirstImport());
    }

    /** Registers the agent with the tool bar.*/
    private void register()
    {
        String description = "Open the Importer.";
        TaskBar tb = registry.getTaskBar();
        IconManager icons = IconManager.getInstance();
        JButton b = new JButton(icons.getIcon(IconManager.IMPORT));
        b.setToolTipText(description);
        ActionListener l = new ActionListener() {

            /** Posts an event to start the agent.*/
            public void actionPerformed(ActionEvent e) {
                EventBus bus = registry.getEventBus();
                ExperimenterData exp = (ExperimenterData) registry.lookup(
                        LookupNames.CURRENT_USER_DETAILS);
                if (exp == null) return;
                GroupData gp = null;
                try {
                    gp = exp.getDefaultGroup();
                } catch (Exception ex) {
                    //No default group
                }
                long id = -1;
                if (gp != null) id = gp.getId();
                if (groupId == -1) groupId = id;
                LoadImporter event = new LoadImporter(null, browserType);
                event.setGroup(groupId);
                event.setObjects(objects);
                bus.post(event);
            }
        };
        b.addActionListener(l);
        tb.addToToolBar(TaskBar.AGENTS, b);
        JMenuItem item = new JMenuItem(icons.getIcon(IconManager.IMPORT));
        item.setText("Import...");
        item.setToolTipText(description);
        item.addActionListener(l);
        tb.addToMenu(TaskBar.FILE_MENU, item);
    }

    /** Creates a new instance. */
    public ImporterAgent() {}

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master)
    {
        this.isMaster = master;
        if (master) {
            this.isMaster = master;
            ExperimenterData exp = (ExperimenterData) registry.lookup(
                    LookupNames.CURRENT_USER_DETAILS);
            if (exp == null) return;
            GroupData gp = null;
            try {
                gp = exp.getDefaultGroup();
            } catch (Exception e) {
                //No default group
            }
            long id = -1;
            if (gp != null) id = gp.getId();
            Importer importer = ImporterFactory.getImporter(id, true, displayMode);
            if (importer != null) {
                Environment env = (Environment) registry.lookup(LookupNames.ENV);
                int type = Importer.PROJECT_TYPE;
                if (env != null) {
                    switch (env.getDefaultHierarchy()) {
                    case LookupNames.PD_ENTRY:
                    default:
                        type = Importer.PROJECT_TYPE;
                        break;
                    case LookupNames.HCS_ENTRY:
                        type = Importer.SCREEN_TYPE;
                    }
                }
                importer.activate(type, null, null, importer.getImportFor());
            }
        }
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
     */
    public void terminate()
    {
        Environment env = (Environment) registry.lookup(LookupNames.ENV);
        if (env.isRunAsPlugin())
            ImporterFactory.terminate();
    }

    /** 
     * Implemented as specified by {@link Agent}.
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, LoadImporter.class);
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, ReconnectedEvent.class);
        bus.register(this, BrowserSelectionEvent.class);
        bus.register(this, ExperimenterLoadedDataEvent.class);
        bus.register(this, ChangeUserGroupEvent.class);
        bus.register(this, DisplayModeEvent.class);
        bus.register(this, ActivitiesEvent.class);
        bus.register(this, SaveResultsEvent.class);
        browserType = getDefaultBrowser();
        groupId = -1;
        register();
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#canTerminate()
     */
    public boolean canTerminate()
    { 
        if (!ImporterFactory.doesImporterExist()) return true;
        Importer importer = ImporterFactory.getImporter(groupId, displayMode);
        if (importer == null) return true;
        return !importer.hasOnGoingUpload();
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#getDataToSave()
     */
    public AgentSaveInfo getDataToSave() { return null; }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#save(List)
     */
    public void save(List<Object> instances) {}

    /**
     * Responds to an event fired trigger on the bus.
     * 
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof LoadImporter)
            handleLoadImporter((LoadImporter) e);
        else if (e instanceof UserGroupSwitched)
            handleUserGroupSwitched((UserGroupSwitched) e);
        else if (e instanceof ReconnectedEvent)
            handleReconnectedEvent((ReconnectedEvent) e);
        else if (e instanceof BrowserSelectionEvent) {
            BrowserSelectionEvent evt = (BrowserSelectionEvent) e;
            browserType = evt.getType();
        } else if (e instanceof ExperimenterLoadedDataEvent) {
            handleExperimenterLoadedDataEvent((ExperimenterLoadedDataEvent) e);
        } else if (e instanceof ChangeUserGroupEvent) {
            ChangeUserGroupEvent evt = (ChangeUserGroupEvent) e;
            groupId = evt.getGroupID();
        } else if (e instanceof DisplayModeEvent) {
            handleDisplayModeEvent((DisplayModeEvent) e);
        } else if (e instanceof ActivitiesEvent) {
            handleActivitiesEvent((ActivitiesEvent) e);
        } else if (e instanceof SaveResultsEvent) {
            handleSaveResultsEvent((SaveResultsEvent) e);
        }
    }

}
