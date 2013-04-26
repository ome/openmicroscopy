/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.DecoratorVisitor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;

/**
 * Sets the node decorations.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class DecoratorVisitor 
	implements ImageDisplayVisitor
{

	/** The id of the user currently logged in.*/
	private long currentUserID;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param currentUserID The identifier of the current user.
	 */
	public DecoratorVisitor(long currentUserID)
	{
		this.currentUserID = currentUserID;
	}
	
	/**
	 * Sets the icons of the nodes.
	 * @see ImageDisplayVisitor#visit(ImageNode)
	 */
	public void visit(ImageNode node)
	{
		node.setNodeDecoration(currentUserID);
	}

	/**
	 * Required by {@link ImageDisplayVisitor} I/F no-operation in our case
	 * @see ImageDisplayVisitor#visit(ImageSet)
	 */
	public void visit(ImageSet node)
	{
		node.setNodeDecoration(currentUserID);
	}
}
