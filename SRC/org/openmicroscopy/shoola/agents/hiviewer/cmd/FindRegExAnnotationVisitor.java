/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExAnnotationVisitor
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
import java.util.Set;
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Finds a regular expression in the annotation if any.
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
class FindRegExAnnotationVisitor
    extends FindRegExVisitor
{
    
    private Colors colors;
    
    /**
     * Sets the color of the titleBar of the specified node.
     * 
     * @param node The specified node.
     * @param annotations The annotations.
     */
    private void setHighlight(ImageDisplay node, Set annotations)
    {
        if (annotations != null && annotations.size() > 0) {
            AnnotationData data = (AnnotationData) (annotations.toArray()[0]);
            if (data != null) {
                if (!RegExFactory.find(pattern, data.getText())) return;
                foundNodes.add(node);
                if (node.equals(model.getBrowser().getSelectedDisplay()))
                    node.setHighlight(
                            colors.getColor(Colors.REGEX_ANNOTATION_HIGHLIGHT));
                else 
                    node.setHighlight(colors.getColor(Colors.REGEX_ANNOTATION));  
            }
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    Reference to the model. Mustn't be <code>null</code>.
     * @param pattern   The pattern. Mustn't be <code>null</code>.
     */
    FindRegExAnnotationVisitor(HiViewer viewer, Pattern pattern)
    {
        super(viewer, pattern);
        colors = Colors.getInstance();
    }
  
    /** 
     * Highlights the titleBar of the imageNode 
     * if the annotation contains the specified regular expression.
     * @see FindRegExVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        Set annotations = 
            ((ImageData) node.getHierarchyObject()).getAnnotations();
        setHighlight(node, annotations);
    }

    /** 
     * Highlights the titleBar of the container 
     * if the annotation contains the specified regular expression.
     * @see FindRegExVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        Object obj = node.getHierarchyObject();
        if (obj instanceof DatasetData)
            setHighlight(node, ((DatasetData) obj).getAnnotations());
    }

}

