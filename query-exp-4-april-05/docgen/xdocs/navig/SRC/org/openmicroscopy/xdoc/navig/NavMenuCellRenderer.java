/*
 * org.openmicroscopy.xdoc.navig.NavMenuCellRenderer
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

package org.openmicroscopy.xdoc.navig;



//Java imports
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

/** 
 * Custom renderer for the navigation tree in the {@link NavMenuUI}.
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
class NavMenuCellRenderer
    extends DefaultTreeCellRenderer
{
    
    /**
     * Overrides parent to configures the renderer based on the passed in 
     * components.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        switch (node.getLevel()) {  //Returns the distance from the root.
            case 0:  //Root node, that is the toc node.
                setFont(getFont().deriveFont(Font.BOLD));
                if (expanded) 
                    setIcon(IconFactory.getIcon(IconFactory.TOC_OPEN));
                break;
            case 1:  //A root child, that is a section node.
                setFont(getFont().deriveFont(Font.PLAIN));
                if (node.isLeaf())
                    setIcon(IconFactory.getIcon(IconFactory.SUB_SECTION));
                else {
                    if (expanded)
                        setIcon(IconFactory.getIcon(IconFactory.SECTION_OPEN));
                    else 
                        setIcon(IconFactory.getIcon(
                                    IconFactory.SECTION_CLOSED));
                }
                break;
            case 2:  //A root grandchild, that is a sub-section node.
                setFont(getFont().deriveFont(Font.ITALIC));
                setIcon(IconFactory.getIcon(IconFactory.SUB_SECTION));
                break;
            default:
                setIcon(null);
        } 
        return this;
    }

}
