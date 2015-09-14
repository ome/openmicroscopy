/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.finder;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

/** 
 * Retrieves the nodes that match a given pattern
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class RegExVisitor
	implements TreeImageDisplayVisitor
{

    /** The highlighted color. */
    private Color					color = Color.RED;
    
    /** Collection of found nodes. */
    private Set<TreeImageDisplay>	foundNodes;
    
    /** The pattern to find. */
    private Pattern 				pattern;
    
    /** Reference to {@link Finder}.*/
    private Finder					model;
    
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
        else if (userObject instanceof ImageData) 
            return ((ImageData) userObject).getName();
        else if (userObject instanceof PlateData) 
            return ((PlateData) userObject).getName();
        else if (userObject instanceof ScreenData) 
            return ((ScreenData) userObject).getName();
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
            annotations = ((ImageData) userObject).getAnnotations();
        if (annotations == null || annotations.size() == 0) return null;
        AnnotationData data = (AnnotationData) (annotations.toArray()[0]);
        if (data == null) return null;
        return null;//data.getText();
    }
    
    /**
     * Finds the pattern.
     * 
     * @param node The node to visit.
     */
    private void setFoundNode(TreeImageDisplay node)
    {
        Object userObject = node.getUserObject();
        Color c = null;
        int style = TreeImageDisplay.FONT_PLAIN;
        if (model.isNameSelected()) {
            String name = getName(userObject);
            if (name != null) {
                if (RegExFactory.find(pattern, name)) {
                    foundNodes.add(node);
                    c = color;
                    style = TreeImageDisplay.FONT_BOLD;
                }
            }
        }
        if (model.isDescriptionSelected()) {
            String description = getDescription(userObject);
            if (description != null) {
                if (RegExFactory.find(pattern, description)) {
                    foundNodes.add(node);
                    c = color;
                    style = TreeImageDisplay.FONT_BOLD;
                }
            } 
        }
        if (model.isAnnotationSelected()) {
            String s = getAnnotation(userObject);
            if (s == null) return;
            if (RegExFactory.find(pattern, s)) {
                foundNodes.add(node);
                c = color;
                style = TreeImageDisplay.FONT_BOLD;
            }
        }
        node.setHighLight(c);
        node.setFontStyle(style);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param pattern The pattern to find. Mustn't be <code>null</code>.
     */
    public RegExVisitor(Finder model, Pattern pattern)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (pattern == null) throw new IllegalArgumentException("No pattern.");
        this.model = model;
        this.pattern = pattern;
        foundNodes = new HashSet<TreeImageDisplay>();
    }
    
    /**
     * Returns the set of found nodes.
     * 
     * @return See above.
     */
    public Set getFoundNodes() { return foundNodes; }
    
    /**
     * Finds the pattern in the specified node. If the pattern is found,
     * the node is highlighted.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { setFoundNode(node); }

    /**
     * Finds the pattern in the specified node. If the pattern is found,
     * the node is highlighted.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node) { setFoundNode(node); }

}
