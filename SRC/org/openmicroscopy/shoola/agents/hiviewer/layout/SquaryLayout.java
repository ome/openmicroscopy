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
import java.awt.Toolkit;

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
    
    
    /** Maximum width used to displayed the thumbnail. */
    private int browserW;
    
    /** Maximum width of the BrowserView.*/
    private void setBrowserSize()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        browserW = 8*(screenSize.width/10);
    }
  
    /** Visit an ImageSet node that contains imageSet nodes. */
    private void visitContainerNode(ImageSet node)
    {
        //Then figure out the number of columns, which is the same as the
        //number of rows.
        int n = node.getChildrenDisplay().size();        
        if (n == 0) {   //node with no child
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }

        //Finally do layout.
        ImageDisplay[] children = 
            LayoutUtils.sortChildrenByPrefWidth(node, false);
        Dimension d;
        int maxY = 0;
        int x = 0, y = 0;
        for (int i = 0; i < children.length; i++) {
            d = children[i].getPreferredSize();
            children[i].setBounds(x, y, d.width, d.height);
            children[i].setVisible(true);
            children[i].setCollapsed(false);
            if (x+d.width <= browserW) {
                x += d.width;
                maxY = Math.max(maxY, d.height); 
            } else {
                x = 0;
                //we didn't enter the previous.
                if (maxY == 0) y += d.height; 
                else y += maxY;
                maxY = 0;
            } 
        }
        Rectangle bounds = node.getContentsBounds();
        d = bounds.getSize();
        node.getInternalDesktop().setPreferredSize(d);
        node.setCollapsed(false);
        node.setVisible(true);
    }

    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     */
    SquaryLayout()
    {
        setBrowserSize();
    }

    /**
     * Lays out the current container display.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (node.isSingleViewMode()) return;
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }
        if (node.containsImages()) LayoutUtils.doSquareGridLayout(node);
        else visitContainerNode(node);
    }

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
