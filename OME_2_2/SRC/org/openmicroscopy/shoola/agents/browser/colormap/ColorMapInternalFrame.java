/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapInternalFrame
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

import org.openmicroscopy.shoola.agents.browser.ui.InternalFrame;

/**
 * The internal frame wrapper for the color map.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class ColorMapInternalFrame extends InternalFrame
                                   implements ColorMapWrapper
{
    private ColorMapUI colorMapUI;
    
    public ColorMapInternalFrame()
    {
        super("View Phenotypes");
        colorMapUI = new ColorMapUI();
        buildUI();
    }
    
    /**
     * Responds to an open command.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperOpened()
     */
    public void wrapperOpened()
    {
        colorMapUI.fireModeReactivate();
    }
    
    /**
     * Responds to a select command.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperSelected()
     */
    public void wrapperSelected()
    {
        // TODO Auto-generated method stub

    }
    
    /**
     * Responds to a close command.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#wrapperClosed()
     */
    public void wrapperClosed()
    {
        colorMapUI.fireModeCancel();
    }
    
    /**
     * Hmm, this may be redundant with wrapperSelected.
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#select()
     */
    public void select()
    {
        // do nothing
    }
    
    /**
     * Returns the internal ColorMapUI component.
     */
    public ColorMapUI getColorMapUI()
    {
        return colorMapUI;
    }

    // builds the UI.
    private void buildUI()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(colorMapUI);
        pack();
    }
}
