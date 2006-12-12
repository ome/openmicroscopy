/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.PartialNameVisitor 
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Visits the image's nodes.
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
implements TreeImageDisplayVisitor
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
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node)
	{
		if (node.isPartialName() == partialName) return;
		node.setPartialName(partialName);
		
	}

	/** 
	 * Required by the {@link TreeImageDisplayVisitor} I/F but no-op
	 * implementation in our case.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node) {
		// TODO Auto-generated method stub
		
	}

}
