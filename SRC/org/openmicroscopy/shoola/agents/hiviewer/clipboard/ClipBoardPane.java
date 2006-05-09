/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardTab
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
