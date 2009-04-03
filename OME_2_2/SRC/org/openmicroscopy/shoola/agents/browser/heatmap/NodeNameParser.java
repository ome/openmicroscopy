/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.DatabaseNameParser
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

import org.openmicroscopy.ds.dto.SemanticType;

/**
 * Parses the name of an element object into an attribute and element.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class NodeNameParser
{
    /**
     * Returns the attribute name of the specified node.
     * @param node The node to parse.
     * @return The attribute name of the node.  Returns null if the node is null.
     */
    public static String getAttributeName(SemanticTypeTree.TreeNode node)
    {
        if(node == null)
        {
            return null;
        }
        SemanticTypeTree.TreeNode pathNode = node;
        SemanticTypeTree.TypeNode parentNode = null;
        while(pathNode.getParent() instanceof SemanticTypeTree.TypeNode &&
              pathNode.getParent() != null)
        {
            parentNode = (SemanticTypeTree.TypeNode)pathNode.getParent();
            pathNode = parentNode;
        }
        SemanticType type = 
            ((SemanticTypeTree.TypeNode)pathNode).getType();
        String attributeName = type.getName();
        return attributeName;
    }
    
    /**
     * Returns the fully-qualified element name of the specified node.
     *
     * @param node The node to parse.
     * @return The element name of the node.  Returns null if the node is null.
     */
    public static String getElementName(SemanticTypeTree.TreeNode node)
    {
        if(node == null) return null;
        return node.getFQName();
    }
}
