/*
 * org.openmicroscopy.shoola.util.ui.PaintPot 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Component displaying a color and its alpha variation.
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

	/** The background image */
	protected BufferedImage         image;
	
	/** Width  of the paint pot. */
	protected int 					w;

	/** Height of the paint pot. */
	protected int 					h;

	/** Border of the paint pot. */
	protected Rectangle2D			strokeRect;

	/** Area covered by the paint pot. */
	protected Rectangle2D			whiteRect;

	/** Represents the colour picked without Alpha. */
	protected Polygon				topPoly;

	/** Bottom represents the colour picked with alpha channel added. */
	protected Polygon				bottomPoly;

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
        
        double x = whiteRect.getX();
        double y = whiteRect.getY();
        topXPoints[0] = (int) x;
        topXPoints[1] = (int) (x+whiteRect.getWidth());
        topXPoints[2] = (int) x;
        topYPoints[0] = (int) y;
        topYPoints[1] = (int) y;
        topYPoints[2] = (int) (y+whiteRect.getHeight());
        topPoly = new Polygon(topXPoints, topYPoints,3);
        x = strokeRect.getX();
        y = strokeRect.getY();
        bottomXPoints[0] = (int) x;
        bottomXPoints[1] = (int) (x+strokeRect.getWidth());
        bottomXPoints[2] = (int) (x+strokeRect.getWidth());
        bottomYPoints[0] = (int) (y+strokeRect.getHeight());
        bottomYPoints[1] = (int) y;
        bottomYPoints[2] = (int) (y+strokeRect.getHeight());
        bottomPoly = new Polygon(bottomXPoints, bottomYPoints, 3);   
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
        
        if (image != null) {
            g.drawImage(image, 0, 0, w, h, null);
        } else {
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
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param c The color to display. Mustn't be <code>null</code>.
	 */
	public PaintPot(Color c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No color specified.");
     	colour = c;
		createUI();
	}
	
	/** 
	 * Sets the colour of the component. 
	 * 
	 * @param col the colour of the component.
	 */
	public void setColour(Color col)
	{
		if (col == null) 
			throw new IllegalArgumentException("No color specified.");
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

