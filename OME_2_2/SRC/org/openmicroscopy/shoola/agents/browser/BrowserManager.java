/*
 * org.openmicroscopy.shoola.agents.browser.BrowserManager
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * A superclass for all browser manager functions, that contains the same
 * code that are used for both layout implementations.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public abstract class BrowserManager
{
    /**
     * The list of browser windows in the manager.
     */
    protected List browserList;
    
    /**
     * The active browser window.
     */
    protected BrowserWrapper activeBrowser;
    
    /**
     * The active component window (which may not be a browser)
     */
    protected UIWrapper activeWindow;
    
    /**
     * The application context.
     */
    protected Registry registry;
    
    /**
     * The application's icon manager.
     */
    protected IconManager iconManager;
    
    /**
     * The set of active window listeners.
     */
    protected Set activeWindowListeners;
    
    /**
     * A mapping between static window components and their actual implementations.
     */
    protected Map staticWindowMap;
    
    /**
     * Key for the color map UI component in the static window map.
     */
    public static final String COLORMAP_KEY = "colormap";
    
    /**
     * Key for the heat map UI component in the static window map.
     */
    public static final String HEATMAP_KEY = "heatmap";
    
    /**
     * Parent constructor that initializes common data structures.
     * 
     * @param registry The registry to extract application context from.
     */
    protected BrowserManager(Registry registry)
    {
        this.registry = registry;
        this.iconManager = IconManager.getInstance(registry);
        browserList = new ArrayList();
        activeWindowListeners = new HashSet();
        staticWindowMap = new HashMap();
    }
    
    /**
     * Adds a browser to the manager.  Uses a controller as the parameter to
     * maintain layout independence in the subclasses.  The concrete subclass
     * returned by this method will depend on the implementation of this method.
     * 
     * @param controller The controller of the browser to add.
     * @return A reference to the newly created browser component, if needed.
     */
    public abstract BrowserWrapper addBrowser(BrowserController controller);
    
    /**
     * Gets a references to the browser at the specified index.  This will return
     * null if the index is invalid, or if an internal error occurs.
     * 
     * @param index The index of the browser to access.
     * @return The accessed browser.
     */
    public BrowserWrapper getBrowser(int index)
    {
        try
        {
            return (BrowserWrapper)browserList.get(index);
        }
        // possible candidates: ArrayIndexOutOfBounds (most likely); NPE (maybe),
        // ClassCastException (if subclass code is screwed up)
        catch(Exception e)
        {
            System.err.println("Invalid browser index/return type");
            return null;
        }
    }
    
    /**
     * Gets the number of browsers in the manager.
     * @return See above.
     */
    public int getBrowserCount()
    {
        return browserList.size();
    }
    
    /**
     * Returns an unmodifiable list of all the browsers tracked by the manager.
     * @return See above.
     */
    public List getAllBrowsers()
    {
        return Collections.unmodifiableList(browserList);
    }
    
    /**
     * Removes the specified browser from the manager, if it is present, and
     * deletes it from the GUI.
     * 
     * @param browser The browser to remove.
     */
    public abstract void removeBrowser(BrowserWrapper browser);
    
    /**
     * Returns the browser window which reflects the contents of the dataset with
     * the specified ID, if such a browser is tracked in the manager.  If not, this
     * method will return null.
     * 
     * @param datasetID The ID of the dataset to retrieve the browser for.
     * @return A reference to that browser window, if one exists.
     */
    public BrowserWrapper getBrowserForDataset(int datasetID)
    {
        for(int i=0;i<browserList.size();i++)
        {
            BrowserWrapper browser = (BrowserWrapper)browserList.get(i);
            BrowserModel model = browser.getController().getBrowserModel();
            int theID = model.getDataset().getID();
            if(theID == datasetID)
            {
                return browser;
            }
        }
        return null;
    }
    
    /**
     * Returns the active window.  Does not return the controller, as the
     * controller does not have a window reference (but the window has a reference
     * to the controller).
     * 
     * @return The active browser window.
     */
    public BrowserWrapper getActiveBrowser()
    {
        return activeBrowser;
    }
    
    /**
     * Sets the active browser to the browser at the specified index.
     * 
     * @param browserIndex The index of the browser to make active.
     */
    public void setActiveBrowser(int browserIndex)
    {
        BrowserWrapper browser =
            (BrowserWrapper)browserList.get(browserIndex);
        setActiveBrowser(browser);
    }
    
    /**
     * Sets the specified browser to be the active browser.
     * @param browser The browser to make active.
     */
    public void setActiveBrowser(BrowserWrapper browser)
    {
        if(getActiveBrowser() == browser)
        {
            return;
        }
        browser.select();
        for(Iterator iter = activeWindowListeners.iterator(); iter.hasNext();)
        {
            ActiveWindowListener awl = (ActiveWindowListener)iter.next();
            awl.windowActive(browser);
        }
    }
    
    /**
     * Adds a static window to the layout-- that is, a window that should
     * have a single instance in the entire UI (like a palette).  This
     * window will have the default browser icon as its frame icon.
     * @param windowKey The key to identify the window by.
     * @param window The window to bind to that key.
     */
    public abstract void addStaticWindow(String windowKey, UIWrapper window);
    
    /**
     * Add a static window with a custom icon.  Otherwise, this method
     * is the same as addStaticWindow(String,UIWrapper).
     * 
     * @param windowKey The key to bind the window to.
     * @param window The window to add.
     * @param windowIcon The icon to display in that window's title bar.
     */
    public abstract void addStaticWindow(String windowKey, UIWrapper window,
                                         Icon windowIcon);
    
    /**
     * Returns a reference to the static window bound to the
     * specified key by addStaticWindow(windowKey,window).
     * 
     * @param windowKey The key of the window to retrieve.
     * @return A reference to the window bound to that key.
     */
    public UIWrapper getStaticWindow(String windowKey)
    {
        return (UIWrapper)staticWindowMap.get(windowKey);
    }
    
    /**
     * Show the static window bound to the specified key.
     * @param windowKey The key bound to the desired window.
     */
    public abstract void showStaticWindow(String windowKey);
    
    /**
     * Disposes of the window bound to the specified key.
     * @param windowKey The key of the static window to dispose.
     */
    public abstract void removeStaticWindow(String windowKey);
    
    /**
     * Adds a selection listener to the manager.
     * @param listener The listener to add.
     */
    public void addSelectionListener(ActiveWindowListener listener)
    {
        if(listener != null)
        {
            activeWindowListeners.add(listener);
        }
    }
    
    /**
     * Removes a selection listener from the manager.
     * @param listener The listener to remove.
     */
    public void removeSelectionListener(ActiveWindowListener listener)
    {
        if(listener != null)
        {
            activeWindowListeners.remove(listener);
        }
    }
    
    /**
     * Returns whether or not this manager manages internal or standalone frames.
     * @return See above.
     */
    public abstract boolean managesInternalFrames();
}
