/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExTitleAndAnnotationVisitor
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
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FindRegExTitleAndAnnotationVisitor
    extends FindRegExVisitor 
{
    
    private Colors colors;
    
    private boolean inAnno;
    
    FindRegExTitleAndAnnotationVisitor(HiViewer viewer, String regEx)
    {
        super(viewer, regEx);
        colors = Colors.getInstance();
        inAnno = false;
    }
    
    /** Set the color of the titleBar of the specified node. */
    private void setHighlight(ImageDisplay node)
    {
        if (!RegExFactory.find(pattern, node.getTitle())) return;
        foundNodes.add(node);
        if (node.equals(model.getBrowser().getSelectedDisplay())) {
            node.setHighlight(colors.getColor(Colors.REGEX_TITLE_HIGHLIGHT));
        }
        else 
            if (inAnno) {
                node.setHighlight(colors.getColor(
                        Colors.REGEX_TITLE_AND_ANNOTATION));
            } else {
                node.setHighlight(colors.getColor(Colors.REGEX_TITLE));
            }
    }     
    
    /** Set the color of the titleBar of the specified node. */
    private void setHighlight(ImageDisplay node, AnnotationData data)
    {
        if (!RegExFactory.find(pattern, data.getAnnotation())) return;
        foundNodes.add(node);
        if (node.equals(model.getBrowser().getSelectedDisplay()))
            node.setHighlight(
                    colors.getColor(Colors.REGEX_ANNOTATION_HIGHLIGHT));
        else node.setHighlight(colors.getColor(Colors.REGEX_ANNOTATION));
        inAnno = true;
    }
    
    
    /** 
     * Highlight the titleBar of the imageNode 
     * if the title contains the specified regular expression.
     */
    public void visit(ImageNode node) 
    { 
        inAnno = false;
        AnnotationData data = 
            ((ImageSummary) node.getHierarchyObject()).getAnnotation();
        if (data != null) setHighlight(node, data);
        setHighlight(node); 
    }

    
    /** 
     * Highlight the titleBar of the container 
     * if the title contains the specified regular expression.
     */
    public void visit(ImageSet node) 
    { 
        Object ho = node.getHierarchyObject();
        if (ho instanceof DatasetSummaryLinked) {
            AnnotationData 
                data = ((DatasetSummaryLinked) ho).getAnnotation();
            if (data != null) setHighlight(node, data);
        }
        setHighlight(node); 
    }
    
    

}
