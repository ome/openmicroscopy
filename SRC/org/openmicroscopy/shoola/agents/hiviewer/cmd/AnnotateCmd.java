/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Command to annotate a given dataset or image.
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
public class AnnotateCmd
    implements ActionCmd
{
    
    /** Reference to the model. */
    private HiViewer        model;
    
    /** The selected {@link ImageDisplay} node. */
    private ImageDisplay    node;
    
    /**
     * Creates a new instance.
     * 
     * @param model A reference to the model. Mustn't be <code>null</code>.
     * @param node The {@link ImageDisplay} node to annotate.
     */
    public AnnotateCmd(HiViewer model, ImageDisplay node)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        this.model = model;
        this.node = node;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        boolean b = false;
        Browser browser = model.getBrowser();
        
        if (node == null && browser != null) {
            node = browser.getLastSelectedDisplay();
            b = true;
        }     
        if (node == null) return;
        if (node.getHierarchyObject() == null) return;
        if (b) {
        	Set nodes = browser.getSelectedDisplays();
        	if (nodes.size() > 1) 
        		model.annotateDataObjects(nodes);
        }

        DataObject hierarchyObject = (DataObject) node.getHierarchyObject();
        if ((hierarchyObject instanceof DatasetData) ||
                (hierarchyObject instanceof ImageData)) {
        	ClipBoard cb = model.getClipBoard();
            if (b) {
            	if (cb != null)
                	cb.setSelectedPane(ClipBoard.ANNOTATION_PANE, null);
            } else {
            	if (cb != null) cb.setSelectedPane(ClipBoard.ANNOTATION_PANE, 
            									node);
                if (browser != null) browser.setSelectedDisplay(node);
            }
        } 
    }

}
