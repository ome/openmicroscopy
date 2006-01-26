/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.EditVisitor
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Retrieves the nodes hosting the same <code>DataObject</code> than the 
 * specified {@link #originalNode}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class EditVisitor
	extends BrowserVisitor
{

    /** The ID of original node. */
    private int 				originalNodeID;
    
    /** The original node. */
    private Object              originalNode;
    
    /** The set of nodes found. */
    private Set					foundNodes;
    
    /**
     * Returns the id of the specified object, <code>-1</code> if it's the 
     * root node.
     * 
     * @param userObject The object to analyse.
     * @return See above.
     */
    private int getNodeID(Object userObject)
    {
        if (userObject instanceof ProjectData)
            return ((ProjectData) userObject).getId();
        else if (userObject instanceof DatasetData)
            return ((DatasetData) userObject).getId();
        else if (userObject instanceof ImageData)
            return ((ImageData) userObject).getId();
        else if (userObject instanceof CategoryData)
            return ((CategoryData) userObject).getId();
        else if (userObject instanceof CategoryGroupData)
            return ((CategoryGroupData) userObject).getId();
        return -1; //root
    }
    
    /**
     * Analyses the specified node.
     * 
     * @param node The node to analyse.
     */
    private void analyse(TreeImageDisplay node)
    {
        Object object = node.getUserObject();
        if (object.getClass().equals(originalNode.getClass()) && 
            originalNodeID == getNodeID(object)) {
            foundNodes.add(node);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     * 				Mustn't be <code>null</code>.
     * @param originalNode 	The object hosted by the tree node.
     * 						Mustn't be <code>null</code>.
     */
    public EditVisitor(Browser model, Object originalNode)
    {
        super(model);
        if (originalNode == null) 
            throw new IllegalArgumentException("No node.");
        this.originalNode = originalNode;
        originalNodeID = getNodeID(originalNode);
        foundNodes = new HashSet();
    }
    
    /**
     * Returns the collection of found nodes.
     * 
     * @return See above.
     */
    public Set getFoundNodes() { return foundNodes; }
    
    /**
     * Retrieves the nodes hosting a <code>DataObject</code> with the same ID
     * than {@link #originalNodeID}.
     * 
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { analyse(node); }
    
    /**
     * Retrieves the nodes hosting a <code>DataObject</code> with the same ID
     * than {@link #originalNodeID}.
     * 
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node) { analyse(node); }
    
}
