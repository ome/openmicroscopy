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
 * 
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
    
    /** ID used to select the appropriated icon. */
    private static final int    TOC_OPEN = 0;
    private static final int    TOC_CLOSED = 1;
    private static final int    SECTION_OPEN = 2;
    private static final int    SECTION_CLOSED = 3;
    private static final int    SUB_SECTION = 4;
    private static final int    NO_ICON = 5; 
    
    private static final int    LEVEL_ROOT = 0;
    private static final int    LEVEL_SECTION = 1;
    private static final int    LEVEL_SUB_SECTION = 2;
    
    private Font                font;
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        if (font == null) font = getFont();
        int index = getIconID(value, expanded);
        try {
            switch (index) {
                case TOC_OPEN:
                    setIcon(IconFactory.getIcon(IconFactory.TOC_OPEN)); break;
                case TOC_CLOSED:
                    setIcon(IconFactory.getIcon(IconFactory.TOC_CLOSED)); break;
                case SECTION_OPEN:
                    setIcon(IconFactory.getIcon(IconFactory.SECTION_OPEN)); 
                    break;
                case SECTION_CLOSED:
                    setIcon(IconFactory.getIcon(IconFactory.SECTION_CLOSED));
                    break;
                case SUB_SECTION:
                    setIcon(IconFactory.getIcon(IconFactory.SUB_SECTION));
                    break;
                case NO_ICON:
                    setIcon(null);
            }                                   
        } catch(NumberFormatException nfe) {   
            throw new Error("Invalid Action ID "+index, nfe);
        } 
        return this;
    }
    
   
    private int getIconID(Object value, boolean expanded)
    {
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        int id = TOC_CLOSED;
        switch (node.getLevel()) {
            case LEVEL_ROOT:
                setFont(font.deriveFont(Font.BOLD));
                if (expanded) id = TOC_OPEN;
                break;
            case LEVEL_SECTION:
                setFont(font);
                if (expanded) id = SECTION_OPEN;
                else id = SECTION_CLOSED;
                break;
            case LEVEL_SUB_SECTION:
                setFont(font.deriveFont(Font.ITALIC));
                id = SUB_SECTION;
                break;
        }
        return id;
    }
}
