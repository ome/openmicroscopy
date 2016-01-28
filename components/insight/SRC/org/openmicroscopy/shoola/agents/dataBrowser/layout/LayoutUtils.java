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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import omero.gateway.model.DataObject;

/** 
 * Helper class providing methods to lay out the nodes.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class LayoutUtils
{

    /** The default number of items per row. */
    public static final int DEFAULT_PER_ROW = 10;

    /** The minimum value for dimension.*/
    private static final int MIN = 1;

    /**
     * Scales the thumbnail of the specified new node.
     * 
     * @param newNode The new node to handle.
     * @param oldNode The original node.
     */
    private static void scaleImage(ImageNode newNode, ImageNode oldNode)
    {
        Thumbnail th = newNode.getThumbnail();
        th.scale(oldNode.getThumbnail().getScalingFactor());
    }

    /**
     * Sets the size of the specified {@link ImageSet} node if it doesn't
     * have child.
     * 
     * @param node The {@link ImageSet} node to lay out.
     */
    static void noChildLayout(ImageDisplay node)
    {
        node.getInternalDesktop().setPreferredSize(
                node.getTitleBar().getMinimumSize());
        node.setVisible(true);
    }

    /**
     * Finds out the dimensions of the largest child node in the specified
     * {@link ImageDisplay}.
     * This method calculates the area of each child node and then returns
     * the dimensions of the child having the largest area.
     * Note that the returned <code>Dimension</code> object can have 
     * <code>0</code>-width and height.  In particular this is always the
     * case if the specified parent <code>node</code> has no children.
     * 
     * @param node The parent node. Mustn't be <code>null</code>.
     * @return The dimensions of the child having the largest area.
     * @see #max(Dimension, Dimension)
     */
    static Dimension maxChildDim(ImageDisplay node)
    {
        Dimension maxDim = new Dimension(0, 0);
        Component[] comps = node.getInternalDesktop().getComponents();
        Component c;
        for (int i = 0; i < comps.length; i++) {
            c = comps[i];
            if (c instanceof ImageDisplay) {
                maxDim = max(maxDim, c.getPreferredSize());
            }
        }
        return maxDim;
    }

    /**
     * Finds out the dimensions of the largest child node in the specified
     * collection.
     * This method calculates the area of each child node and then returns
     * the dimensions of the child having the largest area.
     * Note that the returned <code>Dimension</code> object can have 
     * <code>0</code>-width and height. In particular this is always the
     * case if the specified parent <code>node</code> has no children.
     * 
     * @param images The collection to handle.
     * @return See above,
     */
    static Dimension maxChildDim(Collection images)
    {
        Dimension maxDim = new Dimension(0, 0);
        Iterator children = images.iterator();
        ImageDisplay child;
        while (children.hasNext()) {
            child = (ImageDisplay) children.next();
            maxDim = max(maxDim, child.getPreferredSize());
        }
        return maxDim;  //[0, 0] if no children.
    }

    /**
     * Lays out the specified images in a square grid.
     * 
     * @param root The root node.
     * @param imageNodes The collection of images to lay out.
     */
    static void doSquareGridLayout(ImageDisplay root, List imageNodes)
    {
        Iterator children = imageNodes.iterator();
        ImageDisplay child;
        Dimension maxDim = maxChildDim(imageNodes);
        int n = imageNodes.size();
        n = (int) Math.floor(Math.sqrt(n))+1;
        Dimension d;

        try {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (!children.hasNext()) //Done, less than n^2 children.
                        return;  //Go to finally.
                    child = (ImageDisplay) children.next();
                    d = child.getPreferredSize();
                    child.setBounds(j*maxDim.width, i*maxDim.height, d.width,
                            d.height);
                }
            }
        } finally {
            Rectangle bounds = root.getContentsBounds();
            d = bounds.getSize();
            root.getInternalDesktop().setSize(d);
            root.getInternalDesktop().setPreferredSize(d);
        }
    }

    /**
     * Lays out all child nodes in the specified parent <code>node</code>
     * in a square grid. 
     * The size of each cell in the grid will be that of the largest child
     * in the parent <code>node</code>.
     * 
     * @param node The parent node. Mustn't be <code>null</code>.
     * @param sorter The node sorter.
     * @param itemsPerRow The number of items per row.
     */
    static void doSquareGridLayout(ImageDisplay node, ViewerSorter sorter, int
            itemsPerRow)
    {
        //First find out the max dim among children.
        Dimension maxDim = maxChildDim(node);
        //Then figure out the number of columns, which is the same as the
        //number of rows.
        int n = node.getChildrenDisplay().size();
        if (n == 0) {   //Node with no children.
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }

        Component[] comps = node.getInternalDesktop().getComponents();

        List l = new ArrayList();
        for (int i = 0; i < comps.length; i++) 
            if (comps[i] instanceof ImageDisplay)
                l.add(comps[i]);
        Dimension dd = node.getSize();
        if (dd.width == 0 || dd.height == 0 && node.getParentDisplay() != null)
            dd = node.getParentDisplay().getSize();
        l = sorter.sort(l);
        if (dd.width >= MIN && dd.height >= MIN) {
            if (maxDim.width != 0) 
                n = dd.width/maxDim.width;
            if (n == 0) {
                n = DEFAULT_PER_ROW;
            }
            if (itemsPerRow >= 1) n = itemsPerRow;
        } else {
            if (itemsPerRow >= 1) {
                n = itemsPerRow; 
            } else {
                n = l.size();
                if (n > DEFAULT_PER_ROW)
                    n = (int) Math.floor(Math.sqrt(n))+1;  //See note.
            }
        }

        //Finally do layout.
        Dimension d;
        ImageDisplay child;
        Iterator children = l.iterator();
        try {
            int i = 0;
            while (children.hasNext()) {
                for (int j = 0; j < n; j++) {
                    if (!children.hasNext()) //Done, less than n^2 children.
                        return;  //Go to finally.
                    child = (ImageDisplay) children.next();
                    d = child.getPreferredSize();
                    child.setBounds(j*maxDim.width, i*maxDim.height, d.width,
                            d.height);
                }
                i++;
            }
        } finally {
            Rectangle bounds = node.getContentsBounds();
            d = bounds.getSize();
            node.getInternalDesktop().setSize(d);
            node.getInternalDesktop().setPreferredSize(d);
        }
    }

    /**
     * Relays out the node when refreshing the display.
     * 
     * @param node The node to refresh.
     * @param oldNode The node previously displayed.
     * @param newNodes The nodes to layout.
     * @param oldNodes The previously displayed out nodes.
     */
    static void redoLayout(ImageSet node, ImageSet oldNode, Collection newNodes,
            Collection oldNodes)
    {
        int n = newNodes.size();
        if (n == 0) {   //Node with no children.
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }

        Iterator children = newNodes.iterator();
        ImageDisplay child, oldChild;
        Object ho, oho, pho, poho;
        Iterator j;
        long id, pid;
        Class klass, pKlass;
        while (children.hasNext()) {
            child = (ImageDisplay) children.next();
            ho = child.getHierarchyObject();
            klass = ho.getClass();
            pho = child.getParentDisplay().getHierarchyObject();
            pKlass = pho.getClass();
            if (ho instanceof DataObject) {
                if (pho instanceof DataObject) {
                    j = oldNodes.iterator();
                    id = ((DataObject) ho).getId();
                    pid = ((DataObject) pho).getId();
                    while (j.hasNext()) {
                        oldChild = (ImageDisplay) j.next();
                        oho = oldChild.getHierarchyObject();
                        if (oldChild.getParentDisplay() != null) {
                            poho =
                               oldChild.getParentDisplay().getHierarchyObject();
                            if (oho instanceof DataObject) {
                                if (((DataObject) oho).getId() == id && 
                                        oho.getClass().equals(klass)) {
                                    if (((DataObject) poho).getId() == pid && 
                                            poho.getClass().equals(pKlass)) {
                                        if (child instanceof ImageNode) {
                                            scaleImage((ImageNode) child,
                                                    (ImageNode) oldChild);
                                        }
                                        child.setBounds(oldChild.getBounds());
                                    }
                                }
                            }
                        }
                    }
                } else { //pho not a dataobject
                    j = oldNodes.iterator();
                    id = ((DataObject) ho).getId();
                    while (j.hasNext()) {
                        oldChild = (ImageDisplay) j.next();
                        oho = oldChild.getHierarchyObject();
                        if (oldChild.getParentDisplay() != null) {
                            if (oho instanceof DataObject) {
                                if (((DataObject) oho).getId() == id && 
                                        oho.getClass().equals(klass)) {
                                    if (child instanceof ImageNode) {
                                        scaleImage((ImageNode) child,
                                                (ImageNode) oldChild);
                                    }
                                    child.setBounds(oldChild.getBounds());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (oldNode == null) {
            Rectangle bounds = node.getContentsBounds();
            Dimension d = bounds.getSize();
            node.getInternalDesktop().setSize(d);
            node.getInternalDesktop().setPreferredSize(d);
        } else {
            Dimension d = oldNode.getRestoreSize();
            if (oldNode.isCollapsed()) {
                Rectangle r = oldNode.getBounds();
                Rectangle bounds = new Rectangle(r.x, r.y, d.width, d.height);
                node.setBounds(bounds);
                node.setPreferredSize(d);
                node.getInternalDesktop().setSize(d);
                node.getInternalDesktop().setPreferredSize(d);
                node.setCollapsed(true);
            } else {
                node.setBounds(oldNode.getBounds());
                node.getInternalDesktop().setSize(d);
                node.getInternalDesktop().setPreferredSize(d);
            }
        }
    }

    /**
     * Returns the object with the largest associated area.
     * This method calculates the <code>area = width x height</code> of each
     * <code>Dimension</code> object and then returns the one having the
     * largest area.
     *  
     * @param a The first dimensions. Mustn't be <code>null</code>.
     * @param b The second dimensions. Mustn't be <code>null</code>.
     * @return The one between <code>a</code> and <code>b</code> with the
     *         largest associated area.
     */
    public static Dimension max(Dimension a, Dimension b)
    {
        int w = a.width;
        int h = a.height;
        if (b.width > w) w = b.width;
        if (b.height > h) h = b.height;
        return new Dimension (w, h);
    }

    //NOTE: Let A be the function that calculates the area of a Dimension,
    //sz the number of children, and r = sqr(sz).
    //The required area for the layout mustn't be less than sz*A(maxDim).
    //B/c: r < [r]+1  =>  sz=r^2 < ([r]+1)^2  
    //Then: sz*A(maxDim) < [([r]+1)^2]*A(maxDim)

    /*
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            if (!children.hasNext()) //Done, less than n^2 children.
                return;  //Go to finally.
            child = (ImageDisplay) children.next();
            d = child.getPreferredSize();
            child.setBounds(j*maxDim.width, i*maxDim.height, d.width, 
                                d.height);
        }
    }  
     */  

}
