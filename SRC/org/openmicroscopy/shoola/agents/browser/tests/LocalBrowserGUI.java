/*
 * org.openmicroscopy.shoola.agents.browser.tests.LocalBrowserGUI
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
package org.openmicroscopy.shoola.agents.browser.tests;

import java.awt.BorderLayout;
import java.awt.Container;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame;

/**
 * A local browser GUI (testbed for thumbnail/browser stuff)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class LocalBrowserGUI extends JDesktopPane
{
    /**
     * The constructor for the LocalBrowserGUI.
     *
     */
    public LocalBrowserGUI()
    {
        super();
    }
    
    /**
     * Not sure if this is the right call yet.
     * @param frame
     */
    public void addBrowser(BrowserInternalFrame frame)
    {
        frame.setIconifiable(true);
        frame.setClosable(true);
        frame.setMaximizable(true);
        frame.setResizable(true);
        add(frame,new Integer(0));
        try
        {
        	frame.setSelected(true);
        }
        catch(PropertyVetoException e) {}
        frame.show();
    }
    
    /**
     * Ditto this.
     * @param frame
     */
    public void removeBrowser(BrowserInternalFrame frame)
    {
        remove(frame);
    }
}
