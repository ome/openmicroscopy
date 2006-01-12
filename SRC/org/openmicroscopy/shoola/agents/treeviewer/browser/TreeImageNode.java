/*
 * org.openmicroscopy.shoola.agents.treemng.browser.TreeImageNode
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;



//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeImageNode
    extends TreeImageDisplay
{

    /**
     * Implemented as specified by superclass.
     * @see TreeImageDisplay#doAccept(TreeImageDisplayVisitor)
     */
    protected void doAccept(TreeImageDisplayVisitor visitor)
    {
        visitor.visit(this);
    }
    
    /**
     * Creates a new leaf node.
     * 
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node. It has to be an image
     *                        object in this case. 
     *                        Never pass <code>null</code>.
     */
    public TreeImageNode(Object hierarchyObject)
    {
        super(hierarchyObject);
    }
    
    /**
     * Spits out a runtime exception because it's not possible to add a
     * child to a leaf node.
     * @see TreeImageDisplay#addChildDisplay(TreeImageDisplay)
     */
    public void addChildDisplay(TreeImageDisplay child)
    {
        throw new IllegalArgumentException(
                "Can't add a child to a TreeImageNode.");
    }
    
    /**
     * Always returns <code>false</code> as this is not a container node.
     * @see TreeImageDisplay#containsImages()
     */
    public boolean containsImages() { return false; }

}
