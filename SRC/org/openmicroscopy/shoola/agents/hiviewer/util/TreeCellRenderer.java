/*
 * org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer
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

package org.openmicroscopy.shoola.agents.hiviewer.util;


//Java imports
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Determines and sets the icon associated to a data object.
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
public class TreeCellRenderer
    extends DefaultTreeCellRenderer
{
    
    /** Identifies the <i>ROOT</i> icon. */
    private static final int    ROOT = 0;  
    
    /** Identifies the <i>PROJECT</i> icon. */
    private static final int    PROJECT = 1;
    
    /** Identifies the <i>DATASET</i> icon. */
    private static final int    DATASET = 2;
    
    /** Identifies the <i>CATEGORY_GROUP</i> icon. */
    private static final int    CATEGORY_GROUP = 3;
    
    /** Identifies the <i>CATEGORY</i> icon. */
    private static final int    CATEGORY = 4;
    
    /** Identifies the <i>IMAGE</i> icon. */
    private static final int    IMAGE = 5;
    
    /** Identifies the <i>NULL</i> icon. */
    private static final int    NO_ICON = 6; 
    
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;

    
    /**
     * Retrieves the icon's ID according to the type of the DataObject.
     * @param value The data object.
     * @return The ID of the corresponding icon.
     */
    private int getIconID(Object value)
    {
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        Object usrObject = node.getUserObject();
        int id = ROOT;
        if (node.getLevel() != 0) {
            if (usrObject instanceof ProjectData)  {
                setText(((ProjectData) usrObject).getName());
                id = PROJECT;
            } else if (usrObject instanceof DatasetData) {
                setText(((DatasetData) usrObject).getName());
                id = DATASET;
            } else if (usrObject instanceof ImageData) {
                setText(((ImageData) usrObject).getName());
                id = IMAGE;
            } else if (usrObject instanceof CategoryGroupData) {
                setText(((CategoryGroupData) usrObject).getName());
                id = CATEGORY_GROUP;
            } else if (usrObject instanceof CategoryData) {
                setText(((CategoryData) usrObject).getName());
                id = CATEGORY;
            } else if (usrObject instanceof String) id = NO_ICON;
        }
        return id;
    }

    /** Creates a new instance. */
    public TreeCellRenderer()
    {
        icons = IconManager.getInstance();
    }
    
    /**
     * Sets the icon.
     */
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
