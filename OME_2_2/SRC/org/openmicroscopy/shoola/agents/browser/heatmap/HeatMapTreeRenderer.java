/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapTreeRenderer
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;
import org.openmicroscopy.shoola.agents.browser.datamodel.DataElementType;

/**
 * The renderer for the heat map tree.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapTreeRenderer extends DefaultTreeCellRenderer
{
    private Icon typeIcon;
    private Icon stringIcon;
    private Icon numericIcon;
    private Icon booleanIcon;
    
    protected static final int ROOT_NODE = 0;
    protected static final int ATTRIBUTE_NODE = 1;
    protected static final int NUMERIC_NODE = 2;
    protected static final int STRING_NODE = 3;
    protected static final int BOOLEAN_NODE = 4;
    protected static final int UNKNOWN_NODE = 5;
    
    /**
     * Creates a new instance of the heat map tree renderer, which probes
     * the browser context for references to the icons it uses to display
     * nodes.
     */
    public HeatMapTreeRenderer()
    {
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        IconManager iconManager = env.getIconManager();
        
        typeIcon = iconManager.getSmallIcon(IconManager.ST_TREE_TYPE);
        stringIcon = iconManager.getSmallIcon(IconManager.ST_TREE_STRING);
        numericIcon = iconManager.getSmallIcon(IconManager.ST_TREE_NUMBER);
        booleanIcon = iconManager.getSmallIcon(IconManager.ST_TREE_BOOLEAN);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree,value,sel,expanded,
                                          leaf,row,hasFocus);
        int nodeType = getNodeType(value);
        if(nodeType == STRING_NODE)
        {
            setIcon(stringIcon);
        }
        else if(nodeType == NUMERIC_NODE)
        {
            setIcon(numericIcon);
        }
        else if(nodeType == ATTRIBUTE_NODE)
        {
            setIcon(typeIcon);
        }
        else if(nodeType == BOOLEAN_NODE)
        {
            setIcon(booleanIcon);
        }
        return this;
    }
    
    protected int getNodeType(Object value)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        
        if(!(node.getUserObject() instanceof SemanticTypeTree.TreeNode))
        {
            return UNKNOWN_NODE;
        }
        
        SemanticTypeTree.TreeNode stNode =
            (SemanticTypeTree.TreeNode)node.getUserObject();
        
        if(stNode instanceof SemanticTypeTree.TypeNode)
        {
            return ATTRIBUTE_NODE;
        }
        else if(stNode instanceof SemanticTypeTree.ElementNode)
        {
            SemanticTypeTree.ElementNode elNode =
                (SemanticTypeTree.ElementNode)stNode;
            DataElementType type = elNode.getType();
            if(type == DataElementType.DOUBLE ||
               type == DataElementType.INT ||
               type == DataElementType.FLOAT ||
               type == DataElementType.LONG)
            {
                return NUMERIC_NODE;
            }
            else if(type == DataElementType.STRING)
            {
                return STRING_NODE;
            }
            else if(type == DataElementType.BOOLEAN)
            {
                return BOOLEAN_NODE;
            }
            else return UNKNOWN_NODE;
        }
        else return ROOT_NODE; // should be the only TreeNode used
    }
    
}

