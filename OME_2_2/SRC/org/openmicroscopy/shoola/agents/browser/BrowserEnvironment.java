/*
 * org.openmicroscopy.shoola.agents.browser.BrowserEnvironment
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import org.openmicroscopy.shoola.agents.browser.colormap.ColorMapManager;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapManager;

/**
 * Singleton class which contains references to all universally referencable
 * browser components, such as the OMEBrowserAgent and OMEDataAgent.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class BrowserEnvironment
{
    private static boolean initialized = false;
    private static BrowserEnvironment environment = null;

    private BrowserAgent browserAgent;

    private BrowserManager browserManager;
    private HeatMapManager heatMapManager;
    private ColorMapManager colorMapManager;
    
    private IconManager iconManager;
    // private static OMEDataAgent dataAgent;
    private MessageHandler messageHandler;
    private BrowserPreferences browserPreferences;

    private BrowserEnvironment()
    {
        // dummy singleton constructor
        initialized = true;
    }

    /**
     * Returns the environment's OMEBrowserAgent.
     * 
     * @return The OME browser agent of the environment.
     */
    public BrowserAgent getBrowserAgent()
    {
        return browserAgent;
    }

    /**
     * Sets the environment's OMEBrowserAgent.
     * 
     * @param agent The OME browser agent of the environment.
     */
    public void setBrowserAgent(BrowserAgent agent)
    {
        this.browserAgent = agent;
    }

    /**
     * Gets the environment's browser manager.
     * @return See above.
     */
    public BrowserManager getBrowserManager()
    {
        return browserManager;
    }

    /**
     * Sets the environment's browser manager.
     * 
     * @param manager The new manager for this environment.
     */
    public void setBrowserManager(BrowserManager manager)
    {
        this.browserManager = manager;
        if(heatMapManager != null)
        {
            manager.addSelectionListener(heatMapManager);
        }
        if(colorMapManager != null)
        {
            manager.addSelectionListener(colorMapManager);
        }
    }

    /**
     * Gets the message handler for the environment.
     * 
     * @return The message handler of the environment.
     */
    public MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    /**
     * Sets the message handler for the environment.
     * 
     * @param handler The message handler to use.
     */
    public void setMessageHandler(MessageHandler handler)
    {
        this.messageHandler = handler;
    }
    
    /**
     * Gets the icon manager for the environment.
     * @return The icon manager of the environment.
     */
    public IconManager getIconManager()
    {
        return iconManager;
    }
    
    /**
     * Sets the icon manager for the environment.
     * 
     * @param manager The icon manager to use.
     */
    public void setIconManager(IconManager manager)
    {
        this.iconManager = manager;
    }
    
    /**
     * Gets the heat map manager of the environment.
     * @return The heat map manager of the environment.
     */
    public HeatMapManager getHeatMapManager()
    {
        return heatMapManager;
    }
    
    /**
     * Sets the heat map manager for the environment.
     * @param manager The heat map manager to use.
     */
    public void setHeatMapManager(HeatMapManager manager)
    {
        if(browserManager != null)
        {
            browserManager.removeSelectionListener(heatMapManager);
        }
        this.heatMapManager = manager;
        if(browserManager != null)
        {
            browserManager.addSelectionListener(manager);
        }
    }
    
    /**
     * Gets the (classification) color map manager for the environment.
     * @return See above.
     */
    public ColorMapManager getColorMapManager()
    {
        return colorMapManager;
    }
    
    /**
     * Sets the color map manager of the environment.
     * @param manager The color map manager to use.
     */
    public void setColorMapManager(ColorMapManager manager)
    {
        if(browserManager != null)
        {
            browserManager.removeSelectionListener(colorMapManager);
        }
        this.colorMapManager = manager;
        if(browserManager != null)
        {
            browserManager.addSelectionListener(colorMapManager);
        }
    }
    
    /**
     * Returns the browser display preferences.
     * @return See above.
     */
    public BrowserPreferences getBrowserPreferences()
    {
        return browserPreferences;
    }
    
    /**
     * Sets the application's default set of browser preferences to the specified
     * object.
     * @param prefs The set of browser preferences.
     */
    public void setBrowserPreferences(BrowserPreferences prefs)
    {
        if(prefs != null)
        {
            browserPreferences = prefs;
        }
    }

    /**
     * Gets the BrowserEnvironment instance.
     * @return
     */
    public static BrowserEnvironment getInstance()
    {
        if (!initialized)
        {
            environment = new BrowserEnvironment();
        }
        return environment;
    }
}
