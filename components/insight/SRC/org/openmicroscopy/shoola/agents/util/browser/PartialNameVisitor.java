/*
 * org.openmicroscopy.shoola.agents.util.browser.PartialNameVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.browser;

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
public class PartialNameVisitor 
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
	public PartialNameVisitor(boolean v)
	{
		partialName = v;
	}
	
	/**
	 * Sets the value.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node)
	{
		node.setPartialName(partialName);
	}

	/** 
	 * Required by the {@link TreeImageDisplayVisitor} I/F but no-op
	 * implementation in our case.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node)
	{
	    node.setPartialName(partialName);
	}

}
