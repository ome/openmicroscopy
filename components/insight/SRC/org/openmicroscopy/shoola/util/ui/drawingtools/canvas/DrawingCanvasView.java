/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.canvas;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

//Third-party libraries
import org.jhotdraw.draw.DefaultDrawingView;

//Application-internal dependencies

/** 
 * Basic class suited for viewing drawings with a small number
 * of Figures.
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
public class DrawingCanvasView
	extends DefaultDrawingView
{

	/** The flag indicating to duplicate or not. */
	private boolean 	duplicateAction = false;
	
	/** The default background. */
	private static final Color			BACKGROUND = new Color(0xf0f0f0);
	
	/** 
	 * Default point used to override the {@link #drawBackground(Graphics2D)}
	 * method.
	 */
	private static final Point2D.Double	ORIGIN = new Point2D.Double(0, 0);
	
	/**
	 * Sets the size of the component. This method takes in the original size
	 * of the image and the scale factor and sets the size of the component 
	 * then calls {@link DefaultDrawingView#setScaleFactor(double)}.
	 * 
	 * @param f The new scale factor. 
	 * @param originalSize The original size of the image. 
	 */
	public void setScaleFactor(double f, Dimension originalSize)
	{
		if (originalSize != null)
			setSize((int) (originalSize.getWidth()*f), 
					(int) (originalSize.getHeight()*f));
		super.setScaleFactor(f);
	}
	
	/**
	 * Overridden to set the background color only and not fill a rectangle.
	 * @see DefaultDrawingView#drawBackground(Graphics2D)
	 */
	protected void drawBackground(Graphics2D g)
	{
		// No method to access the translate point
		// the only way to have -translate.x *scaleFactor
        // Position of the zero coordinate point on the view
		Point p = drawingToView(ORIGIN);
        
        g.setColor(getBackground());
    
        if (p.y > 0) g.setColor(BACKGROUND);
        if (p.x > 0) g.setColor(BACKGROUND);
    }
	
	/**
	 * Overridden to set the drawing action.
	 * @see DefaultDrawingView#duplicate()
	 */
	public void duplicate()
	{
		duplicateAction = true;
       super.duplicate();
    }
	
	/** Un-sets the duplicate action. */
	public void unsetDuplicate() { duplicateAction = false; }
	
	/**
	 * Returns <code>true</code> if the action is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDuplicate() { return duplicateAction; }
	
}
