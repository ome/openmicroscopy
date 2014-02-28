/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.plugins;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.agents.events.AddOnRegisteredEvent;
import org.openmicroscopy.shoola.agents.events.OpenWithAddOnEvent;
import org.openmicroscopy.shoola.agents.events.RegisteredAddOnEvent;
import org.openmicroscopy.shoola.agents.plugins.util.AddOnMenuItem;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AddOnInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.TaskBar;

/**
 * This agent handles the registration of client side applications.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class PluginAgent
    implements Agent, AgentEventListener, PropertyChangeListener
{

    /** The name of the file. */
    private static final String FILE_NAME = "externalApplication.txt";

    /** The terms used to separate the file ID from the external application. */
    private static final String SEPARATOR = "=";

    /** Reference to the registry. */
    private static Registry registry;

    /** The registered applications.*/
    private Map<String, ApplicationData> applications;

    /**
     * Helper method.
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }

    /** Reads the file hosting the external applications. */
    private void readExternalApplications()
    {
        applications = new HashMap<String, ApplicationData>();

        Environment env = (Environment) getRegistry().lookup(LookupNames.ENV);
        String name = FilenameUtils.concat(env.getOmeroHome(), FILE_NAME);

        File f = new File(name);
        if (!f.exists()) return;
        try {
            BufferedReader input = new BufferedReader(new FileReader(f));
            try {
                String line = null;
                String[] values;
                int index;
                StringBuffer buffer;
                while ((line = input.readLine()) != null) {
                    if (line.contains(SEPARATOR)) {
                        values = line.split(SEPARATOR);
                        if (values.length >= 2) {
                            index = 1;
                            buffer = new StringBuffer();
                            for (int i = 1; i < values.length; i++) {
                                buffer.append(values[i]);
                                if (index != values.length-1)
                                    buffer.append(SEPARATOR);
                                index++;
                            }
                            File application = new File(buffer.toString());
                            ApplicationData data = new ApplicationData(application);
                            applications.put(data.getApplicationName().toLowerCase(),
                                    data);
                        }
                    }
                }
            } finally {
                input.close();
            }
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.print("An error occurred while reading the external " +
                    "applications file.");
            msg.print(e);
            getRegistry().getLogger().error(this, msg);
        }
    }

    /**
     * Returns the application data if it is already registered.
     *
     * @param name The name of the application.
     * @return See above.
     */
    private ApplicationData getApplication(String name)
    {
        Entry<String, ApplicationData> e;
        Iterator<Entry<String, ApplicationData>> i =
                applications.entrySet().iterator();
        while (i.hasNext()) {
            e = i.next();
            if (e.getKey().startsWith(name.toLowerCase()))
                return e.getValue();
        }
        return null;
    }

    /** Registers the agent with the tool bar.*/
    private void register()
    {
        List<AddOnInfo> infos = (List<AddOnInfo>)
                registry.lookup("/addOns");
        if (CollectionUtils.isEmpty(infos)) return;
        //Check if already registered
        Iterator<AddOnInfo> i = infos.iterator();
        AddOnInfo info;
        List<String> scripts;
        Iterator<String> j;
        List<JMenuItem> components = new ArrayList<JMenuItem>();
        AddOnMenuItem item;
        ApplicationData data;
        while (i.hasNext()) {
            info = i.next();
            data = getApplication(info.getName());
            scripts = info.getScripts();
            if (CollectionUtils.isEmpty(scripts)) {
                item = new AddOnMenuItem(info);
                item.setApplicationData(data);
                item.addPropertyChangeListener(this);
                components.add(item);
            } else {
                JMenu menu = new JMenu(info.getName());
                j = scripts.iterator();
                while (j.hasNext()) {
                    item = new AddOnMenuItem(info, j.next());
                    item.setApplicationData(data);
                    menu.add(item);
                }
                components.add(menu);
            }
        }
        TaskBar tb = registry.getTaskBar();
        tb.getMenu(TaskBar.ADD_ON).removeAll();
        Iterator<JMenuItem> k = components.iterator();
        while (k.hasNext()) {
            tb.addToMenu(TaskBar.ADD_ON, k.next());
        }
        getRegistry().getEventBus().post(new AddOnRegisteredEvent());
    }

    /** Creates a new instance. */
    public PluginAgent() {}

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master) {}

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
     */
    public void terminate() {}

    /** 
     * Implemented as specified by {@link Agent}.
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, RegisteredAddOnEvent.class);
        readExternalApplications();
        register();
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

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
        if (e instanceof RegisteredAddOnEvent) {
            RegisteredAddOnEvent evt = (RegisteredAddOnEvent) e;
            ApplicationData data = evt.getData();
            if (data == null) return;
            applications.put(data.getApplicationName().toLowerCase(), data);
            register();
        }
    }

    /**
     * Handles the selection of AddOnInfo.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (AddOnMenuItem.ADD_ON_SELECTION_PROPERTY.equals(name)) {
            AddOnMenuItem item = (AddOnMenuItem) evt.getNewValue();
            ApplicationData data = item.getApplicationData();
            data.setScript(item.getScript());
            getRegistry().getEventBus().post(new OpenWithAddOnEvent(data,
                    item.getAddOnName()));
        }
    }
}
