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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.model.DataObject;

/** 
 * Retrieves the nodes hosting the same <code>DataObject</code> than the 
 * specified {@link #originalNode}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class EditVisitor
	extends BrowserVisitor
{

    /** The ID of original node. */
    private long                	originalNodeID;
    
    /** The original node. */
    private Object              	originalNode;
    
    /** The parent's data object. */
    private DataObject				parent;
    
    /** The collection of found {@link TreeImageDisplay nodes}. */
    private List<TreeImageDisplay>	foundNodes;
    
    /** The collection of found {@link TreeImageDisplay nodes}. */
    private List<TreeImageDisplay>	parentNodes;
    
    /**
     * Returns the id of the specified object, <code>-1</code> if it's the 
     * root node.
     * 
     * @param userObject The object to analyse.
     * @return See above.
     */
    private long getNodeID(Object userObject)
    {
        if (userObject instanceof DataObject)
            return ((DataObject) userObject).getId();
        return -1; //root
    }
    
    /**
     * Analyses the specified node.
     * 
     * @param node The node to analyse.
     */
    private void analyse(TreeImageDisplay node)
    {
    	if (originalNode != null) {
    		Object object = node.getUserObject();
            if (object.getClass().equals(originalNode.getClass()) && 
                originalNodeID == getNodeID(object)) {
                foundNodes.add(node);
            }
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the {@link Browser}.
     *                      Mustn't be <code>null</code>.
     * @param originalNode  The object hosted by the tree node.
     * 						Mustn't be <code>null</code>.
     * @param parent		The parent of the data object to create.
     */
    public EditVisitor(Browser model, Object originalNode, DataObject parent)
    {
        super(model);
        this.originalNode = originalNode;
        this.parent = parent;
        originalNodeID = getNodeID(originalNode);
        foundNodes = new ArrayList<TreeImageDisplay>();
        parentNodes = new ArrayList<TreeImageDisplay>();
    }
    
    /**
     * Returns the collection of found {@link TreeImageDisplay nodes}.
     * 
     * @return See above.
     */
    public List getFoundNodes() { return foundNodes; }
    
    /**
     * Returns the collection of found {@link TreeImageDisplay nodes}.
     * 
     * @return See above.
     */
    public List getParentNodes() { return parentNodes; }
    
    /**
     * Retrieves the nodes hosting a <code>DataObject</code> with the same ID
     * than {@link #originalNodeID}.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { analyse(node); }
    
    /**
     * Retrieves the nodes hosting a <code>DataObject</code> with the same ID
     * than {@link #originalNodeID}.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    { 
    	if (parent != null) {
    		Object n = node.getUserObject();
    		if (n instanceof DataObject) {
    			if (((DataObject) n).getId() == parent.getId() && 
    					n.getClass().equals(parent.getClass())) {
    				parentNodes.add(node);
    			}
    		}
    	}
    	analyse(node); 
    }
    
}
