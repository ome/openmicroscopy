/*
 * org.openmicroscopy.shoola.agents.browser.BrowserManagerSFImpl
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

import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.browser.ui.BrowserFrame;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper;
import org.openmicroscopy.shoola.agents.browser.ui.StandaloneFrame;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopWindowGroup;

/**
 * Standalone frame implementation of the browser manager.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class BrowserManagerSFImpl extends BrowserManager
{
    protected TopWindowGroup topWindowGroup;
    /**
     * Package-private for your protection.
     * @param registry
     */
    BrowserManagerSFImpl(Registry registry)
    {
        super(registry);
        topWindowGroup = new TopWindowGroup("Browser windows",
                         iconManager.getSmallIcon(IconManager.BROWSER),
                         registry.getTaskBar());
    }
    
    /**
     * False.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#managesInternalFrames()
     */
    public boolean managesInternalFrames()
    {
        return false;
    }
    
    /**
     * Creates a BrowserInternalFrame, adds it to the manager, and returns.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#addBrowser(org.openmicroscopy.shoola.agents.browser.BrowserController)
     */
    public BrowserWrapper addBrowser(BrowserController controller)
    {
        if(controller == null) return null;
        
        BrowserFrame bf = new BrowserFrame(controller);
        activeBrowser = bf;
        activeWindow = bf;
        browserList.add(bf);
        // temporary workaround: can you set the title later?
        topWindowGroup.add(bf,"Browser window",
                           iconManager.getSmallIcon(IconManager.BROWSER));
        bf.open();
        return bf;
    }
    
    /**
     * Removes the browser from the manager and from the UI.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#removeBrowser(org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper)
     */
    public void removeBrowser(BrowserWrapper browser)
    {
        BrowserFrame bf = (BrowserFrame)browser;
        if(browser != null && browserList.contains(bf))
        {
            browserList.remove(bf);
            topWindowGroup.remove(bf,true);
            BrowserEnvironment env = BrowserEnvironment.getInstance();
            env.getBrowserAgent().interruptThread(bf.getController());
            bf.setVisible(false);
            bf.dispose();
        }
    }
    
    /**
     * Adds (but does not show) a static window to the manager.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#addStaticWindow(java.lang.String, org.openmicroscopy.shoola.agents.browser.ui.UIWrapper)
     */
    public void addStaticWindow(String windowKey, UIWrapper window)
    {
        if(windowKey != null && window != null)
        {
            staticWindowMap.put(windowKey,window);
        }
        else return;
        
        // apply internal frame parameters/settings
        StandaloneFrame sFrame = (StandaloneFrame)window;
        topWindowGroup.add(sFrame,sFrame.getTitle(),
                           iconManager.getSmallIcon(IconManager.BROWSER));
    }
    
    /**
     * Adds (but does not show) a static window component with the specified
     * custom icon.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#addStaticWindow(java.lang.String, org.openmicroscopy.shoola.agents.browser.ui.UIWrapper, javax.swing.Icon)
     */
    public void addStaticWindow(String windowKey, UIWrapper window,
                                Icon windowIcon)
    {
        if(windowKey != null && window != null)
        {
            staticWindowMap.put(windowKey,window);
        }
        else return;
        
        // check null icon case (revert to default)
        if(windowIcon == null)
        {
            windowIcon = iconManager.getSmallIcon(IconManager.BROWSER);
        }
        
        StandaloneFrame sFrame = (StandaloneFrame)window;
        topWindowGroup.add(sFrame,sFrame.getTitle(),windowIcon);
    }
    
    /**
     * Displays a static window in the JDesktop.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#showStaticWindow(java.lang.String)
     */
    public void showStaticWindow(String windowKey)
    {
        StandaloneFrame sFrame = (StandaloneFrame)staticWindowMap.get(windowKey);
        if(sFrame == null) return;
        
        if(!sFrame.isShowing())
        {
            sFrame.show();
        }
    }
    
    /**
     * Removes a static window from the manager and from the UI.  Generally should
     * not be called-- static windows should hide themselves on close, not dispose.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#removeStaticWindow(java.lang.String)
     */
    public void removeStaticWindow(String windowKey)
    {
        StandaloneFrame sFrame = (StandaloneFrame)staticWindowMap.get(windowKey);
        topWindowGroup.remove(sFrame);
        sFrame.setVisible(false);
        sFrame.dispose();
    }
}
