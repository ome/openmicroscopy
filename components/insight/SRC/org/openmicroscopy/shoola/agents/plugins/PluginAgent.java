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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.plugins.util.AddOnMenuItem;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.AddOnInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/**
 * This agent handles the registration of client side applications.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class PluginAgent
    implements Agent, AgentEventListener
{

    /** Reference to the registry. */
    private static Registry registry;

    /**
     * Helper method.
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }

    /** Registers the agent with the tool bar.*/
    private void register()
    {
        List<AddOnInfo> infos = (List<AddOnInfo>)
                registry.lookup("/addOns");
        if (CollectionUtils.isEmpty(infos)) return;
        Iterator<AddOnInfo> i = infos.iterator();
        AddOnInfo info;
        List<String> scripts;
        Iterator<String> j;
        List<JComponent> components = new ArrayList<JComponent>();
        AddOnMenuItem item;
        while (i.hasNext()) {
            info = i.next();
            scripts = info.getScripts();
            if (CollectionUtils.isEmpty(scripts)) {
                item = new AddOnMenuItem(info);
                components.add(item);
            } else {
                JMenu menu = new JMenu(info.getName());
                j = scripts.iterator();
                while (j.hasNext()) {
                    item = new AddOnMenuItem(info, j.next());
                    menu.add(item);
                }
                components.add(menu);
            }
        }
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
    public void eventFired(AgentEvent e) {
        // TODO Auto-generated method stub
        
    }
}
