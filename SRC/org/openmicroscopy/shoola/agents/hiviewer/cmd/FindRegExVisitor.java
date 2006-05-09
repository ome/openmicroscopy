/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExVisitor
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
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindData;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.RegExFactory;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Top class. All visitors using dealing with regular expressions 
 * should extend this class.
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
class FindRegExVisitor
    extends HiViewerVisitor
{
    
    /** The highlighted color. */
    private Color       color = Color.RED;
    
    /** Collection of found nodes. */
    private Set         foundNodes;
    
    /** The pattern to find. */
    private Pattern     pattern;
    
    /** The context of the find. */
    private FindData    findContext;
    
    /**
     * Returns the name of the specified object.
     * 
     * @param userObject The <code>DataObject</code> hosted by the visited node.
     * @return See above.
     */
    private String getName(Object userObject)
    {
        if (userObject instanceof ProjectData) 
            return ((ProjectData) userObject).getName();
        else if (userObject instanceof DatasetData) 
            return ((DatasetData) userObject).getName();
        else if (userObject instanceof ImageData) 
            return ((ImageData) userObject).getName();
        else if (userObject instanceof CategoryGroupData) 
            return ((CategoryGroupData) userObject).getName();
        else if (userObject instanceof CategoryData) 
            return ((CategoryData) userObject).getName();
        return null;
    }
    
    /**
     * Returns the description of the specified object.
     * 
     * @param userObject The <code>DataObject</code> hosted by the visited node.
     * @return See above.
     */
    private String getDescription(Object userObject)
    {
        if (userObject instanceof ProjectData) 
            return ((ProjectData) userObject).getDescription();
        else if (userObject instanceof DatasetData) 
            return ((DatasetData) userObject).getDescription();
        else if (userObject instanceof ImageData) 
            return ((ImageData) userObject).getDescription();
        else if (userObject instanceof CategoryGroupData) 
            return ((CategoryGroupData) userObject).getDescription();
        else if (userObject instanceof CategoryData) 
            return ((CategoryData) userObject).getDescription();
        return null;
    }
    
    /**
     * Returns the annotation for the specified object.
     * 
     * @param userObject The <code>DataObject</code> hosted by the visited node.
     * @return See above.
     */
    private String getAnnotation(Object userObject)
    {
        Set annotations = null;
        if (userObject instanceof DatasetData)
            annotations = ((DatasetData) userObject).getAnnotations();
        else if (userObject instanceof ImageData)
            annotations = ((DatasetData) userObject).getAnnotations();
        if (annotations == null || annotations.size() == 0) return null;
        AnnotationData data = (AnnotationData) (annotations.toArray()[0]);
        if (data == null) return null;
        return data.getText();
    }
    
    /**
     * Finds the pattern in the specified node.
     * 
     * @param node The node to visit.
     */
    private void setFoundNode(ImageDisplay node)
    {
        Object userObject = node.getHierarchyObject();
        Color c = null;
        if (findContext.nameSelected) {
            String name = getName(userObject);
            if (name != null) {
                if (RegExFactory.find(pattern, name)) {
                    foundNodes.add(node);
                    c = color;
                }
            }
        }
        if (findContext.descriptionSelected) {
            String description = getDescription(userObject);
            if (description != null) {
                if (RegExFactory.find(pattern, description)) {
                    foundNodes.add(node);
                    c = color;
                }
            } 
        }
        if (findContext.annotationSelected) {
            String s = getAnnotation(userObject);
            if (s == null) return;
            if (RegExFactory.find(pattern, s)) {
                foundNodes.add(node);
                c = color;
            }
        }
        node.setHighlight(c);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        Reference to the model.
     *                      Mustn't be <code>null</code>.
     * @param pattern       The pattern to find. Mustn't be <code>null</code>.
     * @param findContext   The context of the find.
     *                      Mustn't be <code>null</code>.
     */
    FindRegExVisitor(HiViewer viewer, Pattern pattern, FindData findContext)
    {
        super(viewer);
        if (pattern == null)
            throw new IllegalArgumentException("No pattern.");
        if (findContext == null)
            throw new IllegalArgumentException("No context.");
        this.pattern = pattern;
        this.findContext = findContext;
        foundNodes = new HashSet();
    }
    
    /** 
     * Returns the nodes found. 
     * 
     * @return See above.
     */
    public Set getFoundNodes() { return foundNodes; }
    
    /**
     * Finds the pattern in the specified node. If the pattern is found,
     * the node is highlighted.
     * @see HiViewerVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) { setFoundNode(node); }

    /** 
     * Finds the pattern in the specified node. If the pattern is found,
     * the node is highlighted.
     * @see HiViewerVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) { setFoundNode(node); }

}
