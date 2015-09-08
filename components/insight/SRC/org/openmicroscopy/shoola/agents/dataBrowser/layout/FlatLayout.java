/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.layout;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import omero.gateway.model.DataObject;

/** 
 * Recursively lays out all nodes regardless of the container.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
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
    private ViewerSorter sorter;

    /** Collection of {@link ImageNode}s to lay out. */
    private List<ImageNode> images;

    /** The root of all nodes. */
    private ImageSet root;

    /** Collection of nodes previously displayed. */
    private Set oldNodes;

    /** The number of items per row. */
    private int itemsPerRow;

    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     * 
     * @param sorter A {@link ViewerSorter sorter} to order nodes.
     */
    FlatLayout(ViewerSorter sorter)
    {
        images =  new ArrayList<ImageNode>();
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
            if (oldNodes == null || oldNodes.size() != l.size())
                LayoutUtils.doSquareGridLayout(root, l);
            else 
                LayoutUtils.redoLayout(root, null, l, oldNodes);
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

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setOldNodes(Set)
     */
    public void setOldNodes(Set oldNodes) { this.oldNodes = oldNodes; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setImagesPerRow(int)
     */
    public void setImagesPerRow(int number) { itemsPerRow = number; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getImagesPerRow()
     */
    public int getImagesPerRow() { return itemsPerRow; }

}
