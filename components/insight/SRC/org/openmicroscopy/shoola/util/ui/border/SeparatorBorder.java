/*
 * org.openmicroscopy.shoola.env.ui.LineBorder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.border;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/** 
 * Bottom line of a Line border.
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
public class SeparatorBorder
extends AbstractBorder
{

    /** Default color for the separator. */
    private static Color    DEFAULT_COLOR = Color.LIGHT_GRAY;

    /** The insets of the component. */
    private Insets	insets;

    /** The color of the line. */
    private Color	lineColor;

    /**
     * Creates a border with the specified color.
     */
    public SeparatorBorder()
    {
        this(DEFAULT_COLOR);
    }

    /**
     * Creates a line border with the specified color and no margin.
     * 
     * @param lineColor The color of the border.
     */
    public SeparatorBorder(Color lineColor)
    {
        insets = new Insets(2, 2, 2, 2);
        if (lineColor == null) lineColor = DEFAULT_COLOR;
        this.lineColor = lineColor;
    }

    /**
     * Returns the insets of the border.
     * 
     * @param c the component for which this border insets value applies.
     * @return See above.
     */
    public Insets getBorderInsets(Component c) { return insets; }

    /**
     * Returns whether or not the border is opaque.
     * @see AbstractBorder#isBorderOpaque()
     */
    public boolean isBorderOpaque() { return false; }

    /**
     * Implemented to paint the border for the specified component with the 
     * specified position and size.
     * 
     * @param c The component for which this border is being painted.
     * @param g The paint graphics.
     * @param x The x position of the painted border.
     * @param y The y position of the painted border.
     * @param width The width of the painted border.
     * @param height The height of the painted border.
     */
    public void paintBorder(Component c, Graphics g, 
            int x, int y, int width, int height)
    {
        //Remember current attributes.
        Color originalColor = g.getColor();

        //Paint the margin in the background color.  We paint a line at a
        //time b/c we can't use fillRectangle -- it would erase the component.
        g.setColor(lineColor);

        //Now paint the line border.
        g.drawLine(x, y+height-1, x+width-1, y+height-1);
        //Finally, restore attributes.
        g.setColor(originalColor);
    }

}
