/*
 * util.ui.DatasetViewRenderer 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------
 */
package util.ui;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import omero.model.Category;
import omero.model.CategoryGroup;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.Image;
import omero.model.Project;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DatasetViewRenderer
extends DefaultTreeCellRenderer
{
    
    /** Flag to determine if the background color is modified. */
    private boolean             visibleColor;
    
    /** 
     * Value set to <code>true</code> to display a thumbnail of the image,
     * <code>false</code> otherwise.
     */
    private boolean				thumbnail;
    
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;
    
    /**
     * Sets the color of the selected cell depending on the darkness 
     * of the specified color.
     * 
     * @param c The color of reference.
     */
    private void setTextColor(Color c)
    {
    	if (c == null) return;
    	// check if the passed color is dark if yes, modify the text color.
    	if (UIUtilities.isDarkColor(c))
    		setForeground(UIUtilities.DEFAULT_TEXT);
    }
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * If an icon is passed, the passed icon is set
     * 
     * @param node The node to handle.
     */
    private void setIcon(DefaultMutableTreeNode node)
    {
    	Object usrObject = node.getUserObject();
        Icon icon = icons.getIcon(IconManager.OWNER);
        if (usrObject instanceof Project)
            icon = icons.getIcon(IconManager.PROJECT);
        else if (usrObject instanceof Dataset) {
            int i = ((Dataset) usrObject).annotationLinks.size();
            if (i == 0)
                icon = icons.getIcon(IconManager.DATASET);
            else icon = icons.getIcon(IconManager.ANNOTATED_DATASET);
        } else if (usrObject instanceof Image) {
            Image img = (Image) usrObject;
            icon = icons.getIcon(IconManager.IMAGE);
        } else if (usrObject instanceof CategoryGroup)
            icon = icons.getIcon(IconManager.CATEGORY_GROUP);
        else if (usrObject instanceof Category)
            icon = icons.getIcon(IconManager.CATEGORY);
        else if (usrObject instanceof String)
        	icon = icons.getIcon(IconManager.ROOT);
        setIcon(icon);
    }

    /**
     * Set the text of the node object
     * @param node
     */
    private void setNodeText(DefaultMutableTreeNode node)
    {
    	Object usrObject = node.getUserObject();
        if (usrObject instanceof Project)
        {
        	Project project = (Project)usrObject;
            setText(project.name.val);
        }
        else if (usrObject instanceof Dataset) 
        {
        	Dataset dataset = (Dataset)usrObject;
        	setText(dataset.name.val);
        } 
        else if (usrObject instanceof Image) 
        {
            Image img = (Image) usrObject;
            setText(img.name.val);
        } 
        else if (usrObject instanceof CategoryGroup)
        {
        	CategoryGroup group = (CategoryGroup)usrObject;
            setText(group.name.val);
        }
        else if (usrObject instanceof Category)
        {
        	Category category = (Category)usrObject;
            setText(category.name.val);
        }
        else if (usrObject instanceof String)
        {
        	String str = (String)usrObject;
        	setText(str);
        }
    }
    
    /** Creates a new instance. */
    public DatasetViewRenderer()
    {
        this(false, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param visibleColor  Pass <code>true</code> to modify the backgroundColor
     *                      according to the highlight color of the node.,
     *                      <code>false</code> otherwise.
     * @param thumbnail		Pass <code>true</code> to display a thumbnail of the 
     * 						image, <code>false</code> otherwise.                     
     */
    public DatasetViewRenderer(boolean visibleColor, boolean thumbnail)
    {
        this.visibleColor = visibleColor;
        this.thumbnail = thumbnail;
        icons = IconManager.getInstance();
    }
  
    /**
     * Overridden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     * 								boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        Color c = null;
        int w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        if (node.getLevel() == 0) {
        	if (node.getUserObject() instanceof Experimenter)
        		setIcon(icons.getIcon(IconManager.OWNER));
        	else setIcon(icons.getIcon(IconManager.ROOT));
            if (getIcon() != null) w += getIcon().getIconWidth();
            w += getIconTextGap();
            w += fm.stringWidth(getText());
            setPreferredSize(new Dimension(w, fm.getHeight()));
            if (sel) setTextColor(getBackgroundSelectionColor());
            return this;
        }
        
        setIcon(node);
        setNodeText(node); 
        c = tree.getForeground();
        setForeground(c);
        if (!sel) setBorderSelectionColor(getBackground());
        else setTextColor(getBackgroundSelectionColor());
       
        if (getIcon() != null) w += getIcon().getIconWidth();
        w += getIconTextGap();
        return this;
    }
    
}
