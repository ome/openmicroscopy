/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ImgDisplayAnnotationVisitor
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

import org.openmicroscopy.shoola.agents.hiviewer.HiTranslator;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.HiViewerVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;

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
public class ImgDisplayAnnotationVisitor
    extends HiViewerVisitor
{
 
    /** The annotation. */
    protected AnnotationData    data;
    
    /** The id of the selected hierarchy object. */
    protected int               hierarchyObjectID;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param data The annotation.
     * @param hierarchyObjectID The id of the hierarchy object.
     */
    public ImgDisplayAnnotationVisitor(HiViewer model, AnnotationData data,
                                        int hierarchyObjectID)
    {
        super(model);
        if (hierarchyObjectID < 0)
            throw new IllegalArgumentException("ID not valid.");
        this.hierarchyObjectID = hierarchyObjectID;
        this.data = data;
    }
    
    /**
     * Sets the annotation of the selected {@link ImageNode} and updates 
     * the tooltip.
     */
    public void visit(ImageNode node)
    {
        ImageData is = (ImageData) node.getHierarchyObject();
        if (is.getId() == hierarchyObjectID) {
            HashSet set = new HashSet(1);
            set.add(data);
            is.setAnnotations(set);
            HiTranslator.formatToolTipFor(node, data);
        }       
    }  
    
    /**
     * Sets the annotation of the selected {@link ImageSet} and updates 
     * the tooltip.
     */
    public void visit(ImageSet node)
    {
        Object ho = node.getHierarchyObject();
        if (ho instanceof DatasetData) {
            DatasetData ds = (DatasetData) ho;
            if (ds.getId() == hierarchyObjectID) {
                HashSet set = new HashSet(1);
                set.add(data);
                ds.setAnnotations(set);     
                HiTranslator.formatToolTipFor(node, data);
            }    
        }
    }

}
