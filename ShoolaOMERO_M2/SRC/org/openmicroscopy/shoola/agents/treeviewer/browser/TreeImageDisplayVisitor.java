/*
 * org.openmicroscopy.shoola.agents.treemng.browser.TreeImageDisplayVisitor
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;



//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines an interface for operations that have to be performed on a 
 * visualization tree.
 * <p>This interface allows you to define arbitrary operations that can then 
 * be applied to the tree by calling the 
 * {@link TreeImageDisplay#accept(TreeImageDisplayVisitor) accept} method of 
 * {@link TreeImageDisplay}, usually on the root node.  
 * or by calling the
 * {@link TreeImageDisplay#accept(TreeImageDisplayVisitor, int) accept} method
 * of {@link TreeImageDisplay}.
 * When a node is visited, the corresponding <code>visit</code> method is
 * called, passing a reference to the node. That is, if an {@link TreeImageNode}
 * is being visited, then the {@link #visit(TreeImageNode)} method is called. 
 * Otherwise the {@link #visit(TreeImageSet)} is called for {@link TreeImageSet}
 * nodes.
 * </p>
 * <p>As an example think of highlighting all images that have been annotated.
 * You would define a class that implements this interface to perform the
 * highlight operation and then pass an instance to the <code>accept</code>
 * method of the root node.  The {@link #visit(TreeImageSet)} method would have
 * a no-op implementation and the {@link #visit(TreeImageNode)} method would be
 * implemented along the lines of (pseudo code):</p>
 * <pre>
 *   img = node.getHierarchyObject()
 *   if img has annotation
 *   then node.setTooltip(text)
 * </pre>
 * 
 * @see TreeImageDisplay
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface TreeImageDisplayVisitor
{
    
    /** 
     * Indicates that only the {@link TreeImageNode} nodes will be visited.
     */
    public static final int TREEIMAGE_NODE_ONLY = 0;
    
    /** 
     * Indicates that only the {@link TreeImageSet} nodes will be visited.
     */
    public static final int TREEIMAGE_SET_ONLY = 1;
    
    /**
     * Indicates that {@link TreeImageNode} and {@link TreeImageSet} nodes will
     * be visited.
     */
    public static final int ALL_NODES = 2;
    
    /**
     * Visits the specified {@link TreeImageNode}. 
     * 
     * @param node The node to visit.
     */
    public void visit(TreeImageNode node);
    
    /**
     * Visits the specified {@link TreeImageSet}. 
     * 
     * @param node The node to visit.
     */
    public void visit(TreeImageSet node);
    
}
