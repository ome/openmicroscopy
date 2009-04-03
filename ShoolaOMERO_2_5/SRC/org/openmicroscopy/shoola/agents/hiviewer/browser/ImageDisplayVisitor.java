/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines an interface for operations that have to be performed on a 
 * visualization tree.
 * <p>This interface allows you to define arbitrary operations that can then 
 * be applied to the tree by calling the 
 * {@link ImageDisplay#accept(ImageDisplayVisitor) accept} method of 
 * {@link ImageDisplay}, usually on the root node.  
 * or by calling the
 * {@link ImageDisplay#accept(ImageDisplayVisitor, int) accept} method of 
 * {@link ImageDisplay}.
 * When a node is visited, the corresponding <code>visit</code> method is
 * called, passing a reference to the node.  That is, if an {@link ImageNode}
 * is being visited, then the {@link #visit(ImageNode)} method is called. 
 * Otherwise the {@link #visit(ImageSet)} is called for {@link ImageSet} nodes.
 * </p>
 * <p>As an example think of highlighting all images that have been annotated.
 * You would define a class that implements this interface to perform the
 * highlight operation and then pass an instance to the <code>accept</code>
 * method of the root node.  The {@link #visit(ImageSet)} method would have a
 * no-op implementation and the {@link #visit(ImageNode)} method would be
 * implemented along the lines of (pseudo code):</p>
 * <pre>
 *   img = node.getHierarchyObject()
 *   if img has annotation
 *   then node.setHighlight(color)
 * </pre>
 * 
 * @see ImageDisplay
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
public interface ImageDisplayVisitor
{

    /** 
     * Indicates that only the {@link ImageNode} nodes will be visited.
     */
    public static final int IMAGE_NODE_ONLY = 0;
    
    /** 
     * Indicates that only the {@link ImageSet} nodes will be visited.
     */
    public static final int IMAGE_SET_ONLY = 1;
    
    /**
     * Indicates that {@link ImageNode} and {@link ImageSet} nodes will be
     * visited.
     */
    public static final int ALL_NODES = 2;
    
    /**
     * Visits the specified {@link ImageNode}. 
     * 
     * @param node The node to visit.
     */
    public void visit(ImageNode node);
    
    /**
     * Visits the specified {@link ImageSet}. 
     * 
     * @param node The node to visit.
     */
    public void visit(ImageSet node);
    
}
