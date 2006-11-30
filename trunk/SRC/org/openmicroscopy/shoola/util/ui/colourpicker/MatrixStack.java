/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.MatrixStack
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

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Stack;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a matrix stack, it will save the current affline transformation of 
 * the graohic device when pushed onto the stack and return that context when 
 * popped. 
 * <p>
 * I did think of saving the graphics context inside the matrix stack and using
 * simply push and pop to set the states of the graphics context itself. I 
 * decided that this had less utility (also a danger of dangling references) 
 * than passing the affinetransform.
 * </p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class MatrixStack
{

	/** Stack containing affinetransform. */
	private Stack stack;
	
	/** Creates a new instance and constructs the matrix stack.
	 */
	MatrixStack()
	{
		stack = new Stack();
	}
	
	/**
	 * Push affinetransform saveMatrix onto the stack.
     * 
	 * @param saveMatrix The transformation to push
	 */
	void push(AffineTransform saveMatrix)
	{
		stack.push(saveMatrix);
	}

	/**
 	 * Push the current affinestransform of the Graphics context onto the stack.
     * 
 	 * @param saveMatrix The transformation to push.
	 */
	void push(Graphics2D saveMatrix)
	{
		stack.push(saveMatrix.getTransform());
	}
	
	/**
	 * Retrieves top transform from stack and returns it.
     * 
	 * @return See above. 
	 */
	AffineTransform pop()
	{
		return (AffineTransform) stack.pop();
	}
	
	/**
	 * Retrieves the top transform from stack and put it onto the current 
	 * graphics context.
     * 
	 * @param g The graphic context.
	 */
	void pop(Graphics2D g)
	{
		g.setTransform((AffineTransform) stack.pop());
	}
	
	/** Removes all elements from the stack. */
	void clear() { stack.clear(); }
	
}
