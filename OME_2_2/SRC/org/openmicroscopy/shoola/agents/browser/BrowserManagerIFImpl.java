/*
 * org.openmicroscopy.shoola.agents.browser.BrowserManagerIFImpl
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

import org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper;
import org.openmicroscopy.shoola.agents.browser.ui.InternalFrame;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * Internal frame implementation of the browser manager.
 * (deprecated-- internal frame should not appear in 2.2.1)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class BrowserManagerIFImpl extends BrowserManager
{
    /**
     * Package-private for your protection.
     * @param registry
     */
    BrowserManagerIFImpl(Registry registry)
    {
        super(registry);
    }
    
    /**
     * True.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#managesInternalFrames()
     */
    public boolean managesInternalFrames()
    {
        return true;
    }
    
    
    /**
     * Creates a BrowserInternalFrame, adds it to the manager, and returns.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#addBrowser(org.openmicroscopy.shoola.agents.browser.BrowserController)
     */
    public BrowserWrapper addBrowser(BrowserController controller)
    {
        if(controller == null) return null;
        
        BrowserInternalFrame bif = new BrowserInternalFrame(controller);
        activeBrowser = bif;
        activeWindow = bif;
        browserList.add(bif);
        
        /*
        TopFrame tf = registry.getTopFrame();
        tf.addToDesktop(bif,TopFrame.PALETTE_LAYER);
        */
        Icon windowIcon = iconManager.getSmallIcon(IconManager.BROWSER);
        bif.setFrameIcon(windowIcon);
        bif.setClosable(true);
        bif.setMaximizable(true);
        bif.setResizable(true);
        bif.setIconifiable(true);
        bif.show();
        return bif;
    }
    
    /**
     * Removes the browser from the manager and from the UI.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#removeBrowser(org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper)
     */
    public void removeBrowser(BrowserWrapper browser)
    {
        BrowserInternalFrame bif = (BrowserInternalFrame)browser;
        if(browser != null && browserList.contains(browser))
        {
            browserList.remove(bif);
            BrowserEnvironment env = BrowserEnvironment.getInstance();
            env.getBrowserAgent().interruptThread(browser.getController());
            /*
            TopFrame tf = registry.getTopFrame();
            tf.removeFromDesktop(bif);
            */
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
        InternalFrame iFrame = (InternalFrame)window;
        Icon windowIcon = iconManager.getSmallIcon(IconManager.BROWSER);
        iFrame.setFrameIcon(windowIcon);
        iFrame.setMaximizable(false);
        iFrame.setResizable(false);
        iFrame.setClosable(true);
        iFrame.setIconifiable(true);
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
        
        InternalFrame iFrame = (InternalFrame)window;
        iFrame.setFrameIcon(windowIcon);
        iFrame.setMaximizable(false);
        iFrame.setResizable(false);
        iFrame.setClosable(true);
        iFrame.setIconifiable(true);
    }
    
    /**
     * Displays a static window in the JDesktop.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#showStaticWindow(java.lang.String)
     */
    public void showStaticWindow(String windowKey)
    {
        InternalFrame iFrame = (InternalFrame)staticWindowMap.get(windowKey);
        if(iFrame == null)
        {
            return;
        }
        /*
        TopFrame tf = registry.getTopFrame();
        if(!iFrame.isShowing())
        {
            tf.addToDesktop(iFrame,TopFrame.PALETTE_LAYER);
            iFrame.show();
        }
        else
        {
            try
            {
                iFrame.setSelected(true);
            }
            catch(PropertyVetoException e) {}
        }*/
    }
    
    /**
     * Removes a static window from the manager and from the UI.  Generally should
     * not be called-- static windows should hide themselves on close, not dispose.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserManager#removeStaticWindow(java.lang.String)
     */
    public void removeStaticWindow(String windowKey)
    {
        InternalFrame iFrame = (InternalFrame)staticWindowMap.get(windowKey);
        staticWindowMap.remove(windowKey);
        /*
        TopFrame tf = registry.getTopFrame();
        tf.removeFromDesktop(iFrame);
        */
    }
}
