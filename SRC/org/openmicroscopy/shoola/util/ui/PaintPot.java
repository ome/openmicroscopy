/*
 * org.openmicroscopy.shoola.util.ui.PaintPot 
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
package org.openmicroscopy.shoola.util.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
 * @since OME3.0
 */
public class PaintPot
	extends JPanel
{
	
	/** Colour of the paint pot. */
	protected Color					colour;
   	 
	/** Width  of paint pot. */
	protected int 					w;
    
	/** Height of paint pot. */
	protected int 					h;
    
	/** Border of paintpot. */
	protected Rectangle2D				strokeRect;
    
	/** Area covered by paintpot. */
	protected Rectangle2D				whiteRect;
    
	/** Represents the colour picked without Alpha. */
	protected Polygon					topPoly;
    
	/** Bottom represents the colour picked with alpha channel added. */
	protected Polygon					bottomPoly;
    
	/** X coordinates of the polygons to represent the colour pots. */
	protected int[]					topXPoints; 
    
	/** X coordinates of the polygons to represent the colour pots.  */
	protected int[]					bottomXPoints; 
    
	/** Y coordinates of the polygons to represent the colour pots.  */
	protected int[]					topYPoints; 
    
	/** Y coordinates of the polygons to represent the colour pots. */
	protected int[]					bottomYPoints; 
	
    /**
     * Creates the UI elements of the paint pot, the two colour parts of the 
     * white rect, the top half representing the colour without the alpha 
     * component and the bottom half with the alpha.
     */
    protected void createUI()
    {
        w = this.getWidth();
        h = this.getHeight();
        topXPoints = new int[3];
        topYPoints = new int[3];
        bottomXPoints = new int[3];
        bottomYPoints = new int[3];
        strokeRect = new Rectangle2D.Double(0, 0, w-1, h-1);
        whiteRect = new Rectangle2D.Double(1, 1, w-2, h-2);
        
        topXPoints[0] = (int) whiteRect.getX();
        topXPoints[1] = (int) (whiteRect.getX()+whiteRect.getWidth());
        topXPoints[2] = (int) (whiteRect.getX());
        topYPoints[0] = (int) whiteRect.getY();
        topYPoints[1] = (int) (whiteRect.getY());
        topYPoints[2] = (int) (whiteRect.getY()+whiteRect.getHeight());
        topPoly = new Polygon(topXPoints, topYPoints,3);
        bottomXPoints[0] = (int) strokeRect.getX();
        bottomXPoints[1] = (int) (strokeRect.getX()+strokeRect.getWidth());
        bottomXPoints[2] = (int) (strokeRect.getX()+strokeRect.getWidth());
        bottomYPoints[0] = (int) (strokeRect.getY()+strokeRect.getHeight());
        bottomYPoints[1] = (int) (strokeRect.getY());
        bottomYPoints[2] = (int) (strokeRect.getY()+strokeRect.getHeight());
        bottomPoly = new Polygon(bottomXPoints, bottomYPoints,3);   
    }
    
    /**
     * Renders is called from {@link #paintComponent(Graphics)} to render all 
     * the graphic elements of the component. 
     * 
     * @param og The graphic context.
     */
    protected void render(Graphics og)
    {
        Graphics2D g = (Graphics2D)og;
        createUI();
       Color c = new Color(colour.getRed(), colour.getGreen(), 
        					colour.getBlue());
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(topPoly);
        g.setColor(colour);
        g.fill(bottomPoly);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_OFF);          
        g.setColor(Color.black);
        g.draw(strokeRect);
        
    }
    
	/**
	 * Creates the UI and attach the component to the control.
	 * 
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	public PaintPot(Color col)
	{
     	colour = col;
		createUI();
	}
	
	/** Set the colour of the component. 
	 * @param col the colour of the component.
	 */
	public void setColour(Color col)
	{
		colour = col;
	}
	
    /**
     * Overridden to render the paint pot. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        render(g);
    }

    
}

