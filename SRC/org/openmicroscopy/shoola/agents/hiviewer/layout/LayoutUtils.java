/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutUtils
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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;

/** 
 * A collection of <code>static</code> methods to support common computations 
 * during the layout of visualization trees. 
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
public class LayoutUtils
{

    /**
     * Sets the size of the specified {@link ImageSet} node if it doesn't
     * have child.
     * 
     * @param node The {@link ImageSet} node to lay out.
     */
    public static void noChildLayout(ImageDisplay node)
    {
        node.getInternalDesktop().setPreferredSize(
                node.getTitleBar().getMinimumSize());
        node.setVisible(true);
    }
    
    /**
     * Returns the object with the largest associated area. 
     * This method calculates the <code>area = width x height</code> of each
     * <code>Dimension</code> object and then returns the one having the
     * largest area.
     *  
     * @param a The first dimensions.  Mustn't be <code>null</code>.
     * @param b The second dimensions.  Mustn't be <code>null</code>.
     * @return The one between <code>a</code> and <code>b</code> with the
     *         largest associated area.
     */
    public static Dimension max(Dimension a, Dimension b)
    {
        int areaA = a.width*a.height, areaB = b.width*b.height;
        if (areaA < areaB) return b;
        return a;
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
     * @param node The parent node.  Mustn't be <code>null</code>.
     * @return The dimensions of the child having the largest area.
     * @see #max(Dimension, Dimension)
     */
    public static Dimension maxChildDim(ImageDisplay node)
    {
        Dimension maxDim = new Dimension(0, 0);
        Iterator children = node.getChildrenDisplay().iterator();
        ImageDisplay child;
        while (children.hasNext()) {
            child = (ImageDisplay) children.next();
            maxDim = max(maxDim, child.getPreferredSize());
        }
        return maxDim;  //[0, 0] if no children.
    }
    
    /**
     * Sorts the children of the specified <code>node</code> by their 
     * preferred width.
     * This method queries the preferred size of each child node in order to
     * determine its preferred width.  If the ascending order is specified,
     * a child with a smaller width will preceed a child with a larger width
     * in the returned array.  If the descending order is specified instead,
     * a child with a larger width will preceed a child with a smaller width.
     * 
     * @param node The parent node.  Mustn't be <code>null</code>.
     * @param ascending Pass <code>true</code> to have the returned array sorted
     *                  in the ascending order; pass <code>false</code> for the
     *                  descending order.
     * @return An array containing all the children of <code>node</code>,
     *         sorted in the specified order.  The returned array will have
     *         <code>0</code>-length if <code>node</code> has no children.
     */
    public static ImageDisplay[] sortChildrenByPrefWidth(ImageDisplay node,
                                                        final boolean ascending)
    {
        List children = new ArrayList(node.getChildrenDisplay());
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Dimension d1 = ((ImageDisplay) o1).getPreferredSize(),
                          d2 = ((ImageDisplay) o2).getPreferredSize();
                //NOTE: o1, o2 can't be null b/c an ImageDisplay doesn't allow
                //you to set null children.
                if (ascending) return d1.width-d2.width;
                return -(d1.width-d2.width);
            }
        };
        Collections.sort(children, c);
        return (ImageDisplay[]) children.toArray(new ImageDisplay[] {});
    }
    
    /**
     * Lays out all child nodes in the specified parent <code>node</code>
     * in a square grid. 
     * The size of each cell in the grid will be that of the largest child
     * in the parent <code>node</code>.
     * 
     * @param node The parent node. Mustn't be <code>null</code>.
     * @param sorter The sorter.
     */
    public static void doSquareGridLayout(ImageDisplay node,
                                        ViewerSorter sorter)
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
        n = (int) Math.floor(Math.sqrt(n)) + 1;  //See note.
        
        //Finally do layout.
        Dimension d;
        List l = sorter.sort(node.getChildrenDisplay());
        //Iterator children = node.getChildrenDisplay().iterator();
        Iterator children = l.iterator();
        ImageDisplay child;
        
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
            Rectangle bounds = node.getContentsBounds();
            d = bounds.getSize();
            node.getInternalDesktop().setSize(d);
            node.getInternalDesktop().setPreferredSize(d);
        }
    }
    //NOTE: Let A be the function that calculates the area of a Dimension,
    //sz the number of children, and r = sqr(sz).
    //The required area for the layout mustn't be less than sz*A(maxDim).
    //B/c: r < [r]+1  =>  sz=r^2 < ([r]+1)^2  
    //Then: sz*A(maxDim) < [([r]+1)^2]*A(maxDim)
    
}
