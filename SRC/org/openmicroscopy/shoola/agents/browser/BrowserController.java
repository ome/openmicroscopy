/*
 * org.openmicroscopy.shoola.agents.browser.BrowserController
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

/**
 * The controller of the browser component MVC architecture.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal Version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserController
{
    private BrowserModel browserModel;
    private BrowserTopModel overlayModel;

    private BrowserView browserView;

    /**
     * Constructs a browser controller with an empty browser model.
     *
     */
    public BrowserController()
    {
        browserModel = new BrowserModel();
        overlayModel = new BrowserTopModel();
    }
    
    /**
     * Constructs a browser controller with a specific browser backing model.
     * @param model The browser model to back the controller.
     */
    public BrowserController(BrowserModel model)
    {
        this.browserModel = model;
        overlayModel = new BrowserTopModel();
        
    }
    
    /**
     * Gets the current view selected by this controller.
     * @return The current view.
     */
    public BrowserView getView()
    {
        return browserView;
    }
    
    /**
     * Sets the current view selected by this controller.
     * @param view The view for the controller to present the model by.
     */
    public void setView(BrowserView view)
    {
        if(view != null)
        {
            browserView = view;
        }
        view.repaint();
    }
}
