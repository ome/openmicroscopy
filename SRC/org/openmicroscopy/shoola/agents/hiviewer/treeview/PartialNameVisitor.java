/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.PartialNameVisitor 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.hiviewer.treeview;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Visits the image's nodes to display the full name or a truncated name
 * depending on the value.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class PartialNameVisitor 
	implements TreeViewNodeVisitor
{

	/** Indicates if the partial name is displayed. */
	private boolean partialName;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param v Pass <code>true</code> to display a partial name,
	 * 			<code>false</code> otherwise.
	 */
	PartialNameVisitor(boolean v)
	{
		partialName = v;
	}
	
	/**
	 * Sets the value.
	 * @see TreeViewNodeVisitor#visit(TreeViewImageNode)
	 */
	public void visit(TreeViewImageNode node)
	{
		if (node.isPartialName() == partialName) return;
		node.setPartialName(partialName);
	}

	/**
	 * Required by the {@link TreeViewNodeVisitor} I/F but no-op
	 * implementation in our case.
	 * @see TreeViewNodeVisitor#visit(TreeViewImageSet)
	 */
	public void visit(TreeViewImageSet node) {}

}
