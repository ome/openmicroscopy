/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame
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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;

/**
 * Wraps a BrowserView in a JInternalFrame for use in MDI applications.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2
 */
public class BrowserInternalFrame extends InternalFrame
                                  implements BrowserWrapper
{
    private BrowserController controller;
    private BrowserManager manager;
    
    /**
     * Constructs a JInternalFrame that wraps the BrowserController and its
     * BrowserView (which ultimately extends JPanel)
     * 
     * @param controller The controller to wrap.
     * @throws IllegalArgumentException If theController is null.
     */
    public BrowserInternalFrame(BrowserController theController)
    {
        if(theController != null)
        {
            this.controller = theController;
        }
        else
        {
            throw new IllegalArgumentException("Null controller");
        }
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        this.manager = env.getBrowserManager();
        buildUI();
    }
    
    private void buildUI()
    {
        setSize(600,600);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(new BrowserMenuBar(controller.getBrowserModel()));
        setTitle(controller.getName());
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(new BrowserPanel(controller),BorderLayout.CENTER);
    }
    
    /**
     * Returns the embedded controller.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#getController()
     */
    public BrowserController getController()
    {
        return controller;
    }
    
    /**
     * Indicates to the browser application that this window has been
     * selected.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperSelected()
     */
    public void wrapperSelected()
    {
        manager.setActiveBrowser(this);
    }
    
    /**
     * Indicate to the browser application that this window has been
     * closed.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperClosed()
     */
    public void wrapperClosed()
    {
        manager.removeBrowser(this);
    }
    
    /**
     * Indicate to the browser application that this window has been
     * opened.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperOpened()
     */
    public void wrapperOpened()
    {
        // manager.addBrowser(controller);
    }

    /**
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#select()
     */
    public void select()
    {
        try
        {
            setLayer(JDesktopPane.PALETTE_LAYER);
            setSelected(true);
        }
        catch(PropertyVetoException ex) {}
    }

    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#setBrowserTitle(java.lang.String)
     */
    public void setBrowserTitle(String title)
    {
        setTitle(title);
    }
}
