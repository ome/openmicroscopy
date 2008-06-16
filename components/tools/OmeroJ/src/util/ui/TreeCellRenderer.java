/*
 * org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer
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

//Third-party libraries

//Application-internal dependencies


/** 
 * Determines and sets the icon and text associated to a data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4929 $ $Date: 2007-06-19 08:22:30 +0000 (Tue, 19 Jun 2007) $)
 * </small>
 * @since OME2.2
 */
public class TreeCellRenderer
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
    
 
    /** Creates a new instance. */
    public TreeCellRenderer()
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
    public TreeCellRenderer(boolean visibleColor, boolean thumbnail)
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
        
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        if (node.getLevel() == 0) {
            setIcon(icons.getIcon(IconManager.ROOT));
            if (sel) setTextColor(getBackgroundSelectionColor());
            return this;
        }
        return this;
    }
    
}
