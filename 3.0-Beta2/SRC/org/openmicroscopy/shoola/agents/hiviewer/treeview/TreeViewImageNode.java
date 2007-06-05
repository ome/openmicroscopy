/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeViewImageNode
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a leaf in the composite structure used to visualize an
 * image hierarchy.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class TreeViewImageNode
	extends TreeViewNode
{
    
    /**
     * Implemented as specified by superclass.
     * @see TreeViewNode#doAccept(TreeViewNodeVisitor)
     */
    protected void doAccept(TreeViewNodeVisitor visitor)
    {
        visitor.visit(this);
    }

    /**
     * Creates a new leaf node.
     * 
     * @param userObject 	The original object in the image hierarchy which
     * 						is visualized by this node. It has to be an image
     *                      object in this case. 
     *                      Never pass <code>null</code>.
     */
    protected TreeViewImageNode(Object userObject)
    {
        super(userObject);
    }
    
    /**
     * Spits out a runtime exception because it's not possible to add a
     * child to a leaf node.
     * @see TreeViewNode#addChildNode(TreeViewNode)
     */
    public void addChildNode(TreeViewNode child)
    {
        throw new IllegalArgumentException(
                "Can't add a child to a TreeImageNode.");
    }
    
    /**
     * Implemented as specified by superclass.
     * @see TreeViewNode#containsImages()
     */
    public boolean containsImages() { return false; }
    
}
