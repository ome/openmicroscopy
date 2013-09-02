/*
 * org.openmicroscopy.shoola.agents.hiviewer.tutil.FrameBorder
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




//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * A line border with an optional margin.
 * This class paints a 1-pixel line around a component and optionally an outer
 * line of a specified thickness. This outer line usually serves as a margin
 * around the component and the 1-pixel line.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4717 $ $Date: 2007-01-11 09:59:10 +0000 (Thu, 11 Jan 2007) $)
 * </small>
 * @since OME2.2
 */
public class FrameBorder
    extends AbstractBorder
{

    /** Default color for the margin. */
    public static Color DEFAULT_COLOR = Color.WHITE;
    
    /** The margin thickness in pixels. */
    private int     margin;
    
    /** The color of the line. */
    private Color   lineColor;
    
    /** The color of the background, which is used for the margin. */
    private Color   backgroundColor;
    
    /** The insets of the component. */
    private Insets	insets;
    
    /**
     * Creates a line border with the specified color and no margin.
     * 
     * @param lineColor The color of the border.
     */
    public FrameBorder(Color lineColor)
    {
        this(lineColor, DEFAULT_COLOR, 0);
    }
    
    /**
     * Creates a line border with the specified color and the given margin.
     * 
     * @param lineColor The color of the border.
     * @param margin    The margin thickness in pixels.
     */
    public FrameBorder(Color lineColor, int margin)
    {
        this(lineColor, DEFAULT_COLOR, margin);
    }
    
    /**
     * Creates a line border with the specified color and the given margin.
     * 
     * @param lineColor         The color of the border.
     * @param backgroundColor   The color for the margin.
     * @param margin            The margin thickness in pixels.
     */
    public FrameBorder(Color lineColor, Color backgroundColor, int margin)
    {
        if (lineColor == null) lineColor = DEFAULT_COLOR;
        if (backgroundColor == null) backgroundColor = DEFAULT_COLOR;
        if (margin < 0) margin = 0;
        this.lineColor = lineColor;
        this.backgroundColor = backgroundColor;
        this.margin = margin;
        insets = new Insets(margin+1, margin+1, margin+1, margin+1);
    }

    /**
     * Returns the insets of the border.
     * 
     * @param c the component for which this border insets value applies.
     * @return See above.
     */
    public Insets getBorderInsets(Component c) { return insets; }

    /** 
     * Returns the color of the border.
     * 
     * @return See above. 
     */
    public Color getLineColor() { return lineColor; }
    
    /** 
     * Returns the background color in which the margin (if any) is painted.
     * 
     * @return See above. 
     */
    public Color getBackgroundColor() { return backgroundColor; }
    
    /** 
     * Returns the margin thickness in pixels.
     * A return value of <code>0</code> means that no margin is painted.
     * 
     * @return See above.
     */
    public int getMargin() { return margin; }
    
    /**
     * Sets the color of the background. if <code>null</code>, the default color
     * is then set.
     * 
     * @param c The color of the background.
     */
    public void setBackgroundColor(Color c)
    {
        if (c == null) backgroundColor = DEFAULT_COLOR;
        else backgroundColor = c;
    }
    
    
    /**
     * Implemented to paint the border for the specified component with the 
     * specified position and size.
     * 
     * @param c         The component for which this border is being painted.
     * @param g         The paint graphics.
     * @param x         The x position of the painted border.
     * @param y         The y position of the painted border.
     * @param width     The width of the painted border.
     * @param height    The height of the painted border.
     */
    public void paintBorder(Component c, Graphics g, 
                            int x, int y, int width, int height)
    {
        //Remember current attributes.
        Color originalColor = g.getColor();
        
        //Paint the margin in the background color.  We paint a line at a
        //time b/c we can't use fillRectangle -- it would erase the component.
        g.setColor(backgroundColor);
        for (int i = 0; i < margin; i++)
            g.drawRect(x+i, y+i, width-2*i-1, height-2*i-1);
        
        //Now paint the line border.
        g.setColor(lineColor);
        g.drawRect(x+margin, y+margin, width-2*margin-1, height-2*margin-1);
        
        //Finally, restore attributes.
        g.setColor(originalColor);
    }

}
