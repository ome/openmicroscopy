/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.LayoutCmd
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.cmd;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Lays out the {@link ImageDisplay} objects according to the specified index.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LayoutCmd
    implements ActionCmd
{
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** One of the constants defined by the {@link LayoutFactory}. */
    private int         layoutIndex;
    
    /**
     * Checks if the index of the layout is supported.
     * 
     * @param index The passed index.
     */
    private void checkIndex(int index)
    {
        switch (index) {
            case LayoutFactory.SQUARY_LAYOUT:
            case LayoutFactory.FLAT_LAYOUT:    
                return;
            default:
                    throw new IllegalArgumentException("Index not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param layoutIndex The index of the layout.
     */
    public LayoutCmd(HiViewer model, int layoutIndex)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        checkIndex(layoutIndex);
        this.model = model;
        this.layoutIndex = layoutIndex;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	Browser browser = model.getBrowser();
    	if (browser != null) {
    		if (browser.getSelectedLayout() == layoutIndex) return; 
    	}
        model.setLayout(layoutIndex);
    }

}
