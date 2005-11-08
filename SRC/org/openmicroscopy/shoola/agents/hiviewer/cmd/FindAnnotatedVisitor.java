/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindAnnotatedVisitor
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
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Highlights all annotated DataObject i.e. images or datasets.
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
class FindAnnotatedVisitor
    extends HiViewerVisitor
{
    
    private Colors colors;
    
    /** Set containing the nodes found. */
    private Set         foundNodes;
    
    /** Sets the color of the titleBar of the specified node. */
    private void setHighlight(ImageDisplay node)
    {
        foundNodes.add(node);
        if (node.equals(model.getBrowser().getSelectedDisplay()))
            node.setHighlight(
                    colors.getColor(Colors.ANNOTATED_HIGHLIGHT));
        else node.setHighlight(colors.getColor(Colors.ANNOTATED));
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer Reference to the model. Mustn't be <code>null</code>.
     */
    FindAnnotatedVisitor(HiViewer viewer)
    {
        super(viewer);
        colors = Colors.getInstance();
        foundNodes = new HashSet();
    }
    
    /** Returns the set of nodes found. */
    public Set getFoundNodes() { return foundNodes; }
    
    /** Highlights the annotated image.*/
    public void visit(ImageNode node)
    {
        Object ho = node.getHierarchyObject();
        if (ho instanceof ImageData) {
            Set annotations = ((ImageData) ho).getAnnotations();
            if (annotations != null && annotations.size() > 0)
                setHighlight(node);   
        }
    }

    /** Highlights the annotated dataset.*/
    public void visit(ImageSet node)
    {
        Object ho = node.getHierarchyObject();
        if (ho instanceof DatasetData) {
            Set annotations = ((DatasetData) ho).getAnnotations();
            if (annotations != null && annotations.size() > 0)
                setHighlight(node);  
        }
    }
    
}
