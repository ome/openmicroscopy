/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapInternalFrame
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.BorderLayout;
import java.awt.Container;

import org.openmicroscopy.shoola.agents.browser.ui.InternalFrame;

/**
 * The internal frame wrapper for the heat map.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class HeatMapInternalFrame extends InternalFrame
                                  implements HeatMapWrapper
{
    private HeatMapUI heatMapUI;
    
    /**
     * Heat map constructor.
     */
    public HeatMapInternalFrame()
    {
        super("HeatMap: [no data]");
        heatMapUI = new HeatMapUI();
        buildUI();
    }
    
    /**
     * Responds to a close event.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperClosed()
     */
    public void wrapperClosed()
    {
        heatMapUI.fireModeCancel();
    }
    
    /**
     * Responds to an open event.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperOpened()
     */
    public void wrapperOpened()
    {
        heatMapUI.fireModeReactivate();
    }
    
    /**
     * Do nothing.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperSelected()
     */
    public void wrapperSelected() {
        // do nothing
    }
    
    /**
     * Do nothing.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#select()
     */
    public void select() {
        // do jack.
    }
    
    /**
     * Returns a reference to the wrapped heat map UI.
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapWrapper#getHeatMapUI()
     */
    public HeatMapUI getHeatMapUI()
    {
        return heatMapUI;
    }
    
    /**
     * Sets the title of the window to the specified text.
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapWrapper#setWindowTitle(java.lang.String)
     */
    public void setWindowTitle(String title)
    {
        setTitle(title);
    }
    
    private void buildUI()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(heatMapUI);
        pack();
    }
    
}
