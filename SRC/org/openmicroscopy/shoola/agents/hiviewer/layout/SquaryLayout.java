/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.SquaryLayout
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------s
 */

package org.openmicroscopy.shoola.agents.hiviewer.layout;


//Java imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;

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
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    sorter;
    
    /** Maximum width used to displayed the thumbnail. */
    private int             browserW;
    
    /** Maximum width of the BrowserView.*/
    private void setBrowserSize()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        browserW = 8*(screenSize.width/10);
    }
    
    /**
     * Visits an {@link ImageSet} node that contains {@link ImageSet} nodes. 
     * 
     * @param node The parent {@link ImageSet} node.
     */
    private void visitContainerNode(ImageSet node)
    {
        //Then figure out the number of columns, which is the same as the
        //number of rows.    
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            LayoutUtils.noChildLayout(node);
            return;
        }

        Object[] children = sorter.sortArray(node.getChildrenDisplay());
        //Finally do layout.
        //ImageDisplay[] children = 
        //    LayoutUtils.sortChildrenByPrefWidth(node, false);
        Dimension d;
        int maxY = 0;
        int x = 0, y = 0;
        ImageDisplay child;
        for (int i = 0; i < children.length; i++) {
        	child = (ImageDisplay) children[i];
            d = child.getPreferredSize();
            child.setBounds(x, y, d.width, d.height);
            //children[i].setVisible(true);
            child.setCollapsed(false);
            if (x+d.width <= browserW) {
                x += d.width;
                maxY = Math.max(maxY, d.height); 
            } else {
                x = 0;
                if (maxY == 0) y += d.height; 
                else y += maxY;
                maxY = 0;
            } 
        }
       
        Rectangle bounds = node.getContentsBounds();
        node.getInternalDesktop().setPreferredSize(bounds.getSize());
        //node.validate();
        //node.repaint();
        node.setCollapsed(false);
        //node.setVisible(true);
    }

    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     * 
     * @param sorter A {@link ViewerSorter sorter} to order nodes.
     */
    SquaryLayout(ViewerSorter sorter)
    {
        setBrowserSize();
        this.sorter = sorter;
    }

    /**
     * Lays out the current container display.
     * @see Layout#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        //if (node.getParentDisplay() != null) return;
        node.restoreDisplay();
        if (node.isSingleViewMode()) return;
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            LayoutUtils.noChildLayout(node);
            return;
        }
        
        if (node.containsImages()) LayoutUtils.doSquareGridLayout(node, sorter);
        else visitContainerNode(node);
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
    public int getIndex() { return LayoutFactory.SQUARY_LAYOUT; }
    
    /**
     * No-op implementation, as we only layout container displays.
     * @see Layout#visit(ImageNode)
     */
    public void visit(ImageNode node) {}
    
    /**
     * No-op implementation in our case.
     * @see Layout#doLayout()
     */
    public void doLayout() {}

}
