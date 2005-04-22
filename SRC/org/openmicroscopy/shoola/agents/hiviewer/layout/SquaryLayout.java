/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.SquaryLayout
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
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;

/** 
 * Recursively lays out all nodes in a container display in a square grid.
 * The size of each cell in the grid is that of the largest child in the
 * container. 
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
class SquaryLayout
    implements Layout
{

    //NOTE: The algorithm for this layout *relies* on the fact that
    //visualization trees are visited in a depth-first fashion.
    //When we'll implement iterators to visit a tree, then this class
    //will ask for a depth-first iterator.
    
    /** Textual description of this layout. */
    static final String DESCRIPTION = "Recursively lays out all nodes in a "+
                                      "container display in a square grid. "+
                                      "The size of each cell in the grid "+
                                      "is that of the largest child in the "+
                                      "container.";
    
    
    /**
     * Returns the largest dimensions object among two given ones.
     * That is, it returns the dimension object with the largest area.
     *  
     * @param a The first dimensions.
     * @param b The second dimensions.
     * @return The largest dimensions.
     */
    private Dimension max(Dimension a, Dimension b)
    {
        int areaA = a.width*a.height, areaB = b.width*b.height;
        if (areaA < areaB) return b;
        return a;
    }
    
    /**
     * Finds out the dimensions of the largest child node in the specified
     * {@link ImageSet}.
     * 
     * @param node The parent node.
     * @return See above.
     */
    private Dimension maxChildDim(ImageSet node)
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
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     */
    SquaryLayout() {}

    /**
     * Lays out the current container display.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        //First find out the max dim among children.
        Dimension maxDim = maxChildDim(node);
        
        //Then figure out the number of columns, which is the same as the
        //number of rows.
        int n = node.getChildrenDisplay().size();
        if (n == 0) {   //node with no child
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }
        n = (int) Math.floor(Math.sqrt(n)) + 1;  //See note.
        
        //Finally do layout.
        Dimension d;
        Iterator children = node.getChildrenDisplay().iterator();
        ImageDisplay child;
        try {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (!children.hasNext()) //Done, less than n^2 children.
                        return;  //Go to finally.
                    child = (ImageDisplay) children.next();
                    d = child.getPreferredSize();
                    child.setBounds(i*maxDim.width, j*maxDim.height,
                                    d.width, d.height);
                    child.setVisible(true);
                }
            }    
        } finally {
            Rectangle bounds = node.getContentsBounds();
            d = bounds.getSize();
            node.getInternalDesktop().setPreferredSize(d);
            node.setVisible(true);
        }
    }
    //NOTE: Let A be the function that calculates the area of a Dimension,
    //sz the number of children, and r = sqr(sz).
    //The required area for the layout mustn't be less than sz*A(maxDim).
    //B/c: r < [r]+1  =>  sz=r^2 < ([r]+1)^2  
    //Then: sz*A(maxDim) < [([r]+1)^2]*A(maxDim)
    
    /**
     * No-op implementation, as we only layout container displays.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) {}

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getDescription()
     */
    public String getDescription() { return DESCRIPTION; }

}
