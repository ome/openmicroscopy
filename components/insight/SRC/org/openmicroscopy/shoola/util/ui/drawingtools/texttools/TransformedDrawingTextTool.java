/*
* org.openmicroscopy.shoola.util.ui.drawingtools.texttools.TransformedDrawingTextTool
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.texttools;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jhotdraw.draw.TextHolderFigure;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TransformedDrawingTextTool
	extends DrawingTextTool
{
	/** The transformed shape to place the textOverlay in correct place. */
	Shape transformedShape;
	
	/**
	 * Constructor for creating drawing tool.
	 * @param prototype figure drawing on
	 * @param transformedShape transformed area of shape.
	 */
	public TransformedDrawingTextTool(TextHolderFigure prototype, Shape transformedShape)
	{
		super(prototype);
		this.transformedShape = transformedShape;
	}

    
    /**
     * Returns the bounds of the figure.
     * 
     * @param figure The figure to handle.
     * @return See above.
     */
    protected Rectangle getFieldBounds(TextHolderFigure figure)
    {
    	Rectangle2D r = transformedShape.getBounds2D();
    	Rectangle2D.Double doubleRect = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        Rectangle textBox = getView().drawingToView(doubleRect);
    
        int h = (int) Math.min(24, textBox.getHeight());
        int y = (int) textBox.getY()+(int)(textBox.getHeight()/2)-h/2;
        
        Rectangle box = new Rectangle((int) textBox.getX(), y, 
        							(int) textBox.getWidth(), h);
        Insets insets = textField.getInsets();
        return new Rectangle(
                box.x - insets.left, 
                box.y - insets.top, 
                box.width + insets.left + insets.right, 
                box.height + insets.top + insets.bottom
                );
    }
    
	
	
	
}


