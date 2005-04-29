/*
 * org.openmicroscopy.shoola.agents.hiviewer.search.TreeCellRenderer
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

package org.openmicroscopy.shoola.agents.hiviewer.search;







//Java imports
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
class TreeCellRenderer
    extends DefaultTreeCellRenderer
{
    
    /** ID used to select the appropriated icon. */
    private static final int    ROOT = 0;   
    private static final int    PROJECT = 1;
    private static final int    DATASET = 2;
    private static final int    CATEGORY_GROUP = 3;
    private static final int    CATEGORY = 4;
    private static final int    IMAGE = 5;
    private static final int    NO_ICON = 6; 
    
    
    private IconManager         icons;
    
    TreeCellRenderer()
    {
        icons = IconManager.getInstance();
    }
    
    /** Retrieves the icon's ID according to the type of the DataObject. */
    private int getIconID(Object value)
    {
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        Object usrObject = node.getUserObject();
        int id = ROOT;
        if (node.getLevel() != 0) {
            if (usrObject instanceof ProjectSummary)  id = PROJECT;
            else if (usrObject instanceof DatasetSummary) id = DATASET;
            else if (usrObject instanceof ImageSummary) id = IMAGE;
            else if (usrObject instanceof CategoryGroupData) 
                id = CATEGORY_GROUP;
            else if (usrObject instanceof CategoryData) id = CATEGORY;
            else if (usrObject instanceof String) id = NO_ICON;
        }
        return id;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        int index = getIconID(value);
        try {
            switch (index) {
                case ROOT:
                    setIcon(icons.getIcon(IconManager.ROOT)); break;
                case PROJECT:
                    setIcon(icons.getIcon(IconManager.PROJECT)); break;
                case DATASET:
                    setIcon(icons.getIcon(IconManager.DATASET)); break;
                case CATEGORY:
                    setIcon(icons.getIcon(IconManager.CATEGORY)); break;
                case CATEGORY_GROUP:
                    setIcon(icons.getIcon(IconManager.CATEGORY_GROUP)); break;
                case IMAGE:
                    setIcon(icons.getIcon(IconManager.IMAGE)); break;
                case NO_ICON:
                    setIcon(null);
            }                                   
        } catch(NumberFormatException nfe) {   
            throw new Error("Invalid Action ID "+index, nfe);
        } 
        return this;
    }
    
}
