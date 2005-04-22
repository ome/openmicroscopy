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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * 
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
public class FindRegExAnnotationVisitor
    extends FindRegExVisitor
{

    public FindRegExAnnotationVisitor(Browser browser, String regEx, int index)
    {
        super(browser, regEx, index);
    }
    
    /** 
     * Highlight the titleBar of the imageNode 
     * if the annotation contains the specified regular expression.
     */
    public void visit(ImageNode node)
    {
        if (!(levelIndex == FindRegExVisitor.CONTAINER_LEVEL)) {
            AnnotationData data = 
                ((ImageSummary) node.getHierarchyObject()).getAnnotation();
            if (data != null) {
                boolean b = RegExFactory.find(pattern, data.getAnnotation());
                if (b) {
                    if (node.equals(browser.getSelectedDisplay())) 
                        node.setHighlight(Colors.REGEX_ANNOTATION);
                    else node.setHighlight(Colors.REGEX_ANNOTATION_LIGHT);
                }
            }
        }
    }

    /** 
     * Highlight the titleBar of the container 
     * if the annotation contains the specified regular expression.
     */
    public void visit(ImageSet node)
    {
        if (!(levelIndex == FindRegExVisitor.IMAGE_LEVEL)) {
            Object ho = node.getHierarchyObject();
            if (ho instanceof DatasetSummaryLinked) {
                AnnotationData 
                    data = ((DatasetSummaryLinked) ho).getAnnotation();
                if (data != null) {
                    boolean b = RegExFactory.find(pattern, 
                            data.getAnnotation());
                    if (b) {
                        if (node.equals(browser.getSelectedDisplay()))
                            node.setHighlight(Colors.REGEX_ANNOTATION);
                        else node.setHighlight(Colors.REGEX_ANNOTATION_LIGHT);
                    }
                }
            }
        }
    }

}

