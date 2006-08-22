/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.CollapseVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Collapses the nodes corresponding to a container e.g. a <code>Dataset</code>.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class CollapseVisitor
	implements TreeViewNodeVisitor
{

    /** A reference to the Model. */
    private TreeView model;
    
    /**
     * Creates a new instance.
     * 
     * @param model A reference to the Model. Mustn't be <code>null</code>.
     */
    CollapseVisitor(TreeView model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /**
     * Required by the {@link TreeViewNodeVisitor} I/F but no-op implementation
     * in our case.
     * @see TreeViewNodeVisitor#visit(TreeViewImageNode)
     */
    public void visit(TreeViewImageNode node) {}

    /**
     * Collapses the node if expanded.
     * @see TreeViewNodeVisitor#visit(TreeViewImageSet)
     */
    public void visit(TreeViewImageSet node) { model.collapseNode(node); }

}
