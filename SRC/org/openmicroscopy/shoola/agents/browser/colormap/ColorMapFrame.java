/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapFrame
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
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.browser.ui.StandaloneFrame;

/**
 * JFrame wrapper for the color map UI component.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class ColorMapFrame extends StandaloneFrame
                           implements ColorMapWrapper
{
    private ColorMapUI colorMapUI;
    
    /**
     * Constructs a color map frame with a new UI.
     */
    public ColorMapFrame()
    {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setTitle("View Phenotypes");
        colorMapUI = new ColorMapUI();
        buildUI();
    }
    
    /**
     * Responds to closing the color map.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperClosed()
     */
    public void wrapperClosed()
    {
        colorMapUI.fireModeCancel();
    }
    
    /**
     * Responds to opening (or reopening) the color map legend
     * window.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperOpened()
     */
    public void wrapperOpened()
    {
        colorMapUI.fireModeReactivate();
    }
    
    /**
     * Do nothing in particular.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperSelected()
     */
    public void wrapperSelected()
    {
        // nothing
    }
    
    /**
     * Two points for redundancy.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#select()
     */
    public void select() {
        // do nothing
    }
    
    /**
     * Returns the internal color map UI wrapped in the JFrame.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapWrapper#getColorMapUI()
     */
    public ColorMapUI getColorMapUI()
    {
        return colorMapUI;
    }
    
    // construct the wrapper UI.
    private void buildUI()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(colorMapUI);
    }
}
