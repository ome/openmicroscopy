/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
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
        if (node == null) {
            node = model.getBrowser().getLastSelectedDisplay();
            b = true;
        }     
        if (node == null) return;
        if (node.getHierarchyObject() == null) return;
        DataObject hierarchyObject = (DataObject) node.getHierarchyObject();
        if ((hierarchyObject instanceof DatasetData) ||
                (hierarchyObject instanceof ImageData)) {
            if (b) {
                model.getClipBoard().setSelectedPane(
                        ClipBoard.ANNOTATION_PANE, null);
            } else {
                model.getClipBoard().setSelectedPane(
                        ClipBoard.ANNOTATION_PANE, node);
                model.getBrowser().setSelectedDisplay(node);
            }
        } 
    }

}
