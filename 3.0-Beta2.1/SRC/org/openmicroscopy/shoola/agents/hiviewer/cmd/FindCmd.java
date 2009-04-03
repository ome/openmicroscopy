/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindCmd
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
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Command to select the find pane.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FindCmd
    implements ActionCmd
{

    /** Reference to the model. */
    private HiViewer        model;

    /**
     * Creates a new instance.
     * 
     * @param model A reference to the model. Mustn't be <code>null</code>.
     */
    public FindCmd(HiViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	Browser browser = model.getBrowser();
    	if (browser ==  null) return;
        ImageDisplay node = browser.getLastSelectedDisplay();
        if (node == null) return;
        if (node.getHierarchyObject() == null) return;
        DataObject ho = (DataObject) node.getHierarchyObject();
        if (!(ho instanceof ImageData)) {
        	ClipBoard cb = model.getClipBoard();
        	if (cb == null) return;
        	cb.setSelectedPane(ClipBoard.FIND_PANE, null);
        }
            
    }
    
}
