/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapFilter
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.browser.datamodel.DataElementType;

/**
 * Trims the nodes in a SemanticTypeTree to deal only with classifications
 * and with scalar values.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapFilter
{
    /**
     * Pares all the non-scalar elements from the tree.
     * @param tree The tree to prune.
     */
    public static void filter(SemanticTypeTree tree)
    {
        if(tree == null)
        {
            return;
        }
        
        SemanticTypeTree.TreeNode root = tree.getRootNode();
        List dfsQueue = new ArrayList();
        dfsQueue.add(root);
        
        while(dfsQueue.size() > 0)
        {
            SemanticTypeTree.TreeNode node =
                (SemanticTypeTree.TreeNode)dfsQueue.get(0);
            dfsQueue.remove(node);
            
            List children = new ArrayList(node.getChildren());
            for(Iterator iter = children.iterator(); iter.hasNext();)
            {
                Object o = iter.next();
                if(o instanceof SemanticTypeTree.ElementNode)
                {
                    SemanticTypeTree.ElementNode eNode =
                        (SemanticTypeTree.ElementNode)o;
                    DataElementType det = eNode.getType();
                    if(det != DataElementType.BOOLEAN &&
                       det != DataElementType.DOUBLE &&
                       det != DataElementType.FLOAT &&
                       det != DataElementType.INT &&
                       det != DataElementType.LONG)
                    {
                        // pruning right here
                        node.removeChild(eNode);
                    }
                }
                else if(o instanceof SemanticTypeTree.TypeNode)
                {
                    dfsQueue.add(o);
                }
            }
        }
    }
}
