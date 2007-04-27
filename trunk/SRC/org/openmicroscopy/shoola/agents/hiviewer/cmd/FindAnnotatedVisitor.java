/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindAnnotatedVisitor
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
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Replaces the hierarchy object by the specified <code>DataObject</code>
 * and repaints the node.
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
public class FindAnnotatedVisitor
    extends HiViewerVisitor
{
    
    /** The updated hierarchy object. */
    private DataObject  ho;
    
    /**
     * Creates a new instance. 
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param ho        The updated <code>DataObject</code>.
     *                  Mustn't be <code>null</code>.
     */
    public FindAnnotatedVisitor(HiViewer model, DataObject ho)
    {
        super(model);
        if (ho == null) throw new IllegalArgumentException("No DataObject.");
        this.ho = ho;
    }
    
    /**
     * Updates the <code>DataObject</code> hosted by the node and updates
     * icon.
     * @see HiViewerVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        Object object = node.getHierarchyObject();
        if (object instanceof ImageData) {
            if (((ImageData) object).getId() == ho.getId()) {
                node.setNodeDecoration();
                node.setHierarchyObject(ho);
                node.repaint();
            }
        }
    }

    /**
     * Updates the <code>DataObject</code> hosted by the node and updates
     * icon.
     * @see HiViewerVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        Object object = node.getHierarchyObject();
        if (object instanceof DatasetData) {
            if (((DatasetData) object).getId() == ho.getId()) {
                IconManager im = IconManager.getInstance();
                node.setHierarchyObject(ho);
                if (node.isAnnotated())
                    node.setFrameIcon(im.getIcon(
                                    IconManager.ANNOTATED_DATASET));
                else 
                    node.setFrameIcon(im.getIcon(IconManager.DATASET));
                node.repaint();
            }
        }
    }
    
}
