/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.TreeLayout
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

//Third-party libraries

//Application-internal dependencies
import java.awt.Dimension;
import java.awt.Rectangle;

import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;

/** 
 * Lays out all the container nodes in a tree.
 * Node containing images are collapsed, so images are not showing.
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
class TreeLayout
    implements Layout
{  //TODO: Implement layout algo!

    /** Textual description of this layout. */
    static final String         DESCRIPTION = "Lays out all the container " +
                                                "nodes in a tree. Node " +
                                                "containing images are "+
                                                "collapsed, so images are " +
                                                "not showing.";

    private static final int    HSPACE = 10;
    
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
        Dimension d;
        Object[] children = LayoutFactory.orderedChildrenbyWidth(node);
        ImageDisplay child;
        int y = 0, x = HSPACE;
        int h;
        if (node.getParentDisplay() == null) x = 0;
        for (int i = 0; i < children.length; i++) {
            child = (ImageDisplay) children[i];
            child.setVisible(true);
            child.setCollapsed(true);
            d = child.getPreferredSize();
            h = (int) child.getSize().getHeight();
            child.setBounds(x, y, d.width, h);
            y += h; 
        }
        Rectangle bounds = node.getContentsBounds();
        d = bounds.getSize();
        node.getInternalDesktop().setPreferredSize(d);
        node.setVisible(true);
        if (node.getParentDisplay() != null) node.setCollapsed(true);
    }
    
    /**
     * Lays out the current container display.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            node.getInternalDesktop().setPreferredSize(
                    node.getTitleBar().getMinimumSize());
            node.setVisible(true);
            return;
        }
        if (node.containsImages()) LayoutFactory.visitNodeWithLeaves(node);
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
