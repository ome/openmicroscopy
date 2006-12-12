/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.FlatLayout
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

package org.openmicroscopy.shoola.agents.hiviewer.layout;



//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import pojos.DataObject;

/** 
 * Recursively lays out all nodes regardless of the container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class FlatLayout
    implements Layout
{
    
    //NOTE: The algorithm for this layout *relies* on the fact that
    //visualization trees are visited in a depth-first fashion.
    //When we'll implement iterators to visit a tree, then this class
    //will ask for a depth-first iterator.
    
    /** Textual description of this layout. */
    static final String DESCRIPTION = "Layout the images in a square grid.";

    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    sorter;
    
    /** Collection of {@link ImageNode} to lay out. */
    private List            images;
    
    /** The root of all nodes. */
    private ImageSet        root;
    
    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     * 
     * @param sorter A {@link ViewerSorter sorter} to order nodes.
     */
    FlatLayout(ViewerSorter sorter)
    {
        images =  new ArrayList();
        this.sorter = sorter;
    }
    
    /**
     * Lays out the images.
     * @see Layout#doLayout()
     */
    public void doLayout()
    {
        if (root != null) {
            List l = sorter.sort(images);
            LayoutUtils.doSquareGridLayout(root, l);
        }  
    }
    
    /**
     * Retrieves the images.
     * @see Layout#visit(ImageNode)
     */
    public void visit(ImageNode node) { images.add(node); }

    /**
     * Retrieves the root node.
     * @see Layout#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (!(node.getHierarchyObject() instanceof DataObject) && 
                node.getParentDisplay() == null) root = node;
    }
    
    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getDescription()
     */
    public String getDescription() { return DESCRIPTION; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getIndex()
     */
    public int getIndex() { return LayoutFactory.FLAT_LAYOUT; }
   
}
