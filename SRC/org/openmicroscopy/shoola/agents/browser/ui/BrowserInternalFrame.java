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

import javax.swing.JInternalFrame;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserView;

/**
 * Wraps a BrowserView in a JInternalFrame for use in MDI applications.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserInternalFrame extends JInternalFrame
                                  implements UIWrapper
{
    private IntraDragAdapter dragAdapter;
    private BrowserController controller;
    private BrowserView embeddedView;
    private BrowserEnvironment env;
    
    /**
     * Constructs a JInternalFrame that wraps the BrowserController and its
     * BrowserView (which ultimately extends JPanel)
     * 
     * @param controller The controller to wrap.
     */
    public BrowserInternalFrame(BrowserController controller)
    {
        setSize(600,600);
        setTitle("Image Browser--"); // TODO: get name of dataset
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
        if(controller != null)
        {
            this.controller = controller;
            this.embeddedView = controller.getView();
            this.env = BrowserEnvironment.getInstance();
        }
        
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(embeddedView,BorderLayout.CENTER);
        if(controller.getStatusView() != null)
        {
            container.add(controller.getStatusView(),BorderLayout.SOUTH);
        }
        this.addFocusListener(new CommonFocusAdapter(this));

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

}
