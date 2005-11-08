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
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

//Third-party libraries

//Application-internal dependencies

/** 
 * The interface that each tabbedpane should implement.
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
abstract class ClipBoardTab
    extends JPanel
{

    /** The {@link ClipBoardUI} view hosting this component. */
    protected ClipBoardUI       view;
    
    /** The {@link ClipBoardModel} model. */
    protected ClipBoardModel    model;
    
    /** The {@link ClipBoardControl} control. */
    protected ClipBoardControl  controller;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param view Reference to the view. Mustn't be <code>null</code>.
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     */
    ClipBoardTab(ClipBoardModel model, ClipBoardUI view, ClipBoardControl
                controller)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        if (controller == null) throw new NullPointerException("No control.");
        this.model = model;
        this.view = view;
        this.controller = controller;
    }
    
    protected void onDisplayChange(ImageDisplay selectedDisplay) {}
    
}
