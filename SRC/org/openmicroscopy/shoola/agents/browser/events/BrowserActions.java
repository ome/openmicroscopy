/*
 * org.openmicroscopy.shoola.agents.browser.BrowserActions
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
package org.openmicroscopy.shoola.agents.browser.events;

import java.beans.PropertyVetoException;

import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.colormap.ColorMapManager;
import org.openmicroscopy.shoola.agents.browser.colormap.ColorMapUI;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapManager;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapUI;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/**
 * A collection of common browser actions.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class BrowserActions
{
    /**
     * Do nothing.
     */
    public static BrowserAction NOOP_ACTION = new BrowserAction()
    {
        public void execute()
        {
            // do nothing
        }
    };
    
    public static BrowserAction SHOW_HEATMAP_ACTION = new BrowserAction()
    {
        public void execute()
        {
            BrowserEnvironment env = BrowserEnvironment.getInstance();
            BrowserAgent agent = env.getBrowserAgent();
            TopFrame tf = agent.getTopFrame();
            
            HeatMapManager manager = env.getHeatMapManager();
            HeatMapUI ui = manager.getUI();
            ui.setClosable(true);
            ui.setIconifiable(true);
            ui.setResizable(false);
            ui.setMaximizable(false);
            if(!ui.isShowing())
            {
                tf.addToDesktop(ui,TopFrame.PALETTE_LAYER);
                ui.show();
            }
            else
            {
                try
                {
                    ui.setSelected(true);
                }
                catch(PropertyVetoException ex) {}
            }
        }
    };
    
    public static BrowserAction SHOW_COLORMAP_ACTION = new BrowserAction()
    {
        public void execute()
        {
            BrowserEnvironment env = BrowserEnvironment.getInstance();
            BrowserAgent agent = env.getBrowserAgent();
            TopFrame tf = agent.getTopFrame();
            
            ColorMapManager manager = env.getColorMapManager();
            ColorMapUI ui = manager.getUI();
            ui.setClosable(true);
            ui.setIconifiable(true);
            ui.setResizable(false);
            ui.setMaximizable(false);
            if(!ui.isShowing())
            {
                tf.addToDesktop(ui,TopFrame.PALETTE_LAYER);
                ui.show();
            }
            else
            {
                try
                {
                    ui.setSelected(true);
                }
                catch(PropertyVetoException ex) {}
            }
        }
    };
}
