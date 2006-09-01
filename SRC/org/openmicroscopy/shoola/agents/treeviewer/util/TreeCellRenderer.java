/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer
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

package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Determines and sets the icon corresponding to a data object.
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
    
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;
    
    /** Flag to indicate if the number of children is visible. */
    private boolean             numberChildrenVisible;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * If an icon is passed, the passed icon is set
     * 
     * @param usrObject The user's object.
     */
    private void setIcon(Object usrObject)
    {
        Icon icon = null;
        if (usrObject instanceof ProjectData)
            icon = icons.getIcon(IconManager.PROJECT);
        else if (usrObject instanceof DatasetData) {
            Long i = ((DatasetData) usrObject).getAnnotationCount();
            if (i == null || i.longValue() == 0)
                icon = icons.getIcon(IconManager.DATASET);
            else icon = icons.getIcon(IconManager.ANNOTATED_DATASET);
        } else if (usrObject instanceof ImageData) {
            ImageData img = (ImageData) usrObject;
            Long a = img.getAnnotationCount();
            Long c = img.getClassificationCount();
            long n = 0, m = 0;
            if (a != null) n = a.longValue();
            if (c != null) m = c.longValue();
            if (n == 0 && m == 0) icon = icons.getIcon(IconManager.IMAGE);
            else if (n == 0 && m != 0)
                icon = icons.getIcon(IconManager.CLASSIFIED_IMAGE);
            else if (n != 0 && m == 0)
                icon = icons.getIcon(IconManager.ANNOTATED_IMAGE);
            else if (n != 0 && m != 0)
                icon = icons.getIcon(IconManager.ANNOTATED_CLASSIFIED_IMAGE);
        } else if (usrObject instanceof CategoryGroupData)
            icon = icons.getIcon(IconManager.CATEGORY_GROUP);
        else if (usrObject instanceof CategoryData)
            icon = icons.getIcon(IconManager.CATEGORY);
        setIcon(icon);
    }

    /**
     * Creates a new instance.
     * 
     * @param b Passed <code>true</code> to show the number of children,
     *          <code>false</code> otherwise.
     */ 
    public TreeCellRenderer(boolean b)
    {
        numberChildrenVisible = b;
        icons = IconManager.getInstance();
    }
    
    /** Creates a new instance. */
    public TreeCellRenderer() { this(true); }
    
    /**
     * Overriden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     * 								boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        if (!(value instanceof TreeImageDisplay)) return this;
        TreeImageDisplay  node = (TreeImageDisplay) value;
        
        if (node.getLevel() == 0) {
            setIcon(icons.getIcon(IconManager.ROOT));
            return this;
        }
        if (numberChildrenVisible) setText(node.getNodeText());
        else setText(node.getNodeName());
        setToolTipText(node.getToolTip());
        setIcon(node.getUserObject());
        Color c = node.getHighLight();
        if (c == null) c = tree.getForeground();
        setForeground(c);
        return this;
    }
    
}
