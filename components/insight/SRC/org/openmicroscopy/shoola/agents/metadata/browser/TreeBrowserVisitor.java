/*
 * org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.metadata.browser;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines an interface for operations that have to be performed on a 
 * visualization tree.
 * <p>This interface allows you to define arbitrary operations that can then 
 * be applied to the tree by calling the 
 * {@link TreeBrowserDisplay#doAccept(TreeBrowserVisitor) doAccept} method of 
 * {@link TreeBrowserDisplay}, usually on the root node.
 * When a node is visited, the corresponding <code>visit</code> method is
 * called, passing a reference to the node. That is, if an {@link TreeBrowserNode}
 * is being visited, then the {@link #visit(TreeBrowserNode)} method is called. 
 * Otherwise the {@link #visit(TreeBrowserSet)} is called for {@link TreeBrowserSet}
 * nodes.
 * </p>
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface TreeBrowserVisitor
{

	/** Indicates that only the {@link TreeBrowserNode} nodes will be visited. */
    public static final int NODE_ONLY = 0;
    
    /** Indicates that only the {@link TreeBrowserSet} nodes will be visited. */
    public static final int SET_ONLY = 1;
    
    /**
     * Indicates that {@link TreeBrowserNode} and {@link TreeBrowserSet} nodes will
     * be visited.
     */
    public static final int ALL_NODES = 2;
    
    /**
     * Visits the specified {@link TreeBrowserNode}. 
     * 
     * @param node The node to visit.
     */
    public void visit(TreeBrowserNode node);
    
    /**
     * Visits the specified {@link TreeBrowserSet}. 
     * 
     * @param node The node to visit.
     */
    public void visit(TreeBrowserSet node);
    
}
