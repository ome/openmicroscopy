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

//Third-party libraries

//Application-internal dependencies
import java.util.HashSet;
import java.util.Set;

import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * Highlights all annotated DataObject i.e. <code>ImageSummary</code> object or
 * <code>DatasetSummaryLinked</code> object.  
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
    
    FindAnnotatedVisitor(HiViewer viewer)
    {
        super(viewer);
        colors = Colors.getInstance();
        foundNodes = new HashSet();
    }

    /** Set the color of the titleBar of the specified node. */
    private void setHighlight(ImageDisplay node)
    {
        foundNodes.add(node);
        if (node.equals(model.getBrowser().getSelectedDisplay()))
            node.setHighlight(
                    colors.getColor(Colors.ANNOTATED_HIGHLIGHT));
        else node.setHighlight(colors.getColor(Colors.ANNOTATED));
    }
    
    /** Returns the set of nodes found. */
    public Set getFoundNodes() { return foundNodes; }
    
    /** Highlight the annotated image.*/
    public void visit(ImageNode node)
    {
        Object ho = node.getHierarchyObject();
        if (ho instanceof ImageSummary && 
                ((ImageSummary) ho).getAnnotation() != null)
            setHighlight(node);
    }

    /** Highlight the annotated dataset.*/
    public void visit(ImageSet node)
    {
        Object ho = node.getHierarchyObject();
        if (ho instanceof DatasetSummary && 
                ((DatasetSummary) ho).getAnnotation() != null) 
                setHighlight(node);
    }
    
}
