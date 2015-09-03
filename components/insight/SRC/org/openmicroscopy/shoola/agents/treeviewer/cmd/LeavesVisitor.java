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

import java.util.HashSet;
import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.model.ImageData;

/** 
 * Retrieves the nodes hosting {@link ImageData} objects. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class LeavesVisitor
    extends BrowserVisitor
{

    /** Set of nodes */
    private Set<TreeImageDisplay> 	nodes;
    
    /** Set of corresponding <code>DataObject</code>s IDs*/
    private Set<Long> 				objects;
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the {@link Browser}.
     *                      Mustn't be <code>null</code>.
     */
    public LeavesVisitor(Browser model)
    {
        super(model);
        nodes = new HashSet<TreeImageDisplay>();
        objects = new HashSet<Long>();
    }

    /**
     * Retrieves the node hosting an {@link ImageData} object.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node)
    { 
        Object uo = node.getUserObject();
        if (uo instanceof ImageData) {
            nodes.add(node);
            objects.add(Long.valueOf(((ImageData) uo).getId()));
        }
    }
    
    /**
     * Retrieves the node hosting an {@link ImageData} object.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    { 
        Object uo = node.getUserObject();
        if (uo instanceof ImageData) {
            nodes.add(node);
            objects.add(Long.valueOf(((ImageData) uo).getId()));
        }
    }
    
    /**
     * Returns the collection of images' id.
     * 
     * @return See above.
     */
    public Set getNodeIDs() { return objects; }
    
    /**
     * Returns the collection of {@link TreeImageNode}s.
     * 
     * @return See above.
     */
    public Set getNodes() { return nodes; }
    
    
}
