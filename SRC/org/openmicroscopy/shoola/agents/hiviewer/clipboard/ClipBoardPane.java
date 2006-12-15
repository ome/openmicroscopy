/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardTab
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;


//Java imports
import javax.swing.Icon;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

/** 
 * The abstract class that each component composing the clip board extends.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class ClipBoardPane
    extends JPanel
{
    
    /** The {@link ClipBoard} model. */
    protected ClipBoard    model;
    
    /**
     * Subclasses should override the method.
     * 
     * @param selectedDisplay   The selected {@link ImageDisplay} node in the
     *                          <code>Browser</code>.
     */
    public abstract void onDisplayChange(ImageDisplay selectedDisplay);
    
    /** 
     * Returns the name of the panel.
     * 
     * @return See above.
     */
    public abstract String getPaneName();
    
    /**
     * Returns the icon related to this panel.
     * 
     * @return See above.
     */
    public abstract Icon getPaneIcon();
    
    /**
     * Returns the index of the panel.
     * 
     * @return See above.
     */
    public abstract int getPaneIndex();
    
    /**
     * Returns the description of the pane.
     * 
     * @return See above.
     */
    public abstract String getPaneDescription();
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public ClipBoardPane(ClipBoard model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
    }

}
