/*
 * org.openmicroscopy.shoola.agents.browser.images.DrawStyle
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.images;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Encapsulates the style (outline color, fill color, stroke type) of
 * a shape.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class DrawStyle
{
    private Color outlineColor;
    private Paint fillPaint;
    private Stroke stroke;
    
    /**
     * Default outline color (black).
     */
    public static final Color DEFAULT_OUTLINE_COLOR = Color.black;
    
    /**
     * Default fill color (white).
     */
    public static final Paint DEFAULT_FILL_PAINT = Color.white;
    
    /**
     * Default stroke specification (width 1, no decoration or dashes)
     */
    public static final Stroke DEFAULT_STROKE = new BasicStroke(1);
    
    /**
     * Constructs a shape style using the default outline color, fill pattern,
     * and stroke specification.
     */
    public DrawStyle()
    {
        outlineColor = DEFAULT_OUTLINE_COLOR;
        fillPaint = DEFAULT_FILL_PAINT;
        stroke = DEFAULT_STROKE;
    }
    
    /**
     * Constructs a shape style using custom values.  If any of the parameters
     * are null, the new shape style will revert to the default values (just
     * like {@link java.awt.Font}.)
     * @return A shape style with the specified values, default if the parameter
     *         is null.
     */
    public DrawStyle(Color outlineColor,
                      Paint fillPattern,
                      Stroke stroke)
    {
        if(outlineColor == null)
        {
            this.outlineColor = DEFAULT_OUTLINE_COLOR;
        }
        else
        {
            this.outlineColor = outlineColor;
        }
        
        if(fillPattern == null)
        {
            this.fillPaint = DEFAULT_FILL_PAINT;
        }
        else
        {
            this.fillPaint = fillPattern;
        }
        
        if(stroke == null)
        {
            this.stroke = DEFAULT_STROKE;
        }
        else
        {
            this.stroke = stroke;
        }
    }
    
    /**
     * Returns the outline color of this style.
     * @return See above.
     */
    public Color getOutlineColor()
    {
        return outlineColor;
    }
    
    /**
     * Returns the fill pattern of this style.
     * @return See above.
     */
    public Paint getFillPaint()
    {
        return fillPaint;
    }
    
    /**
     * Returns the stroke of this style.
     * @return See above.
     */
    public Stroke getStroke()
    {
        return stroke;
    }
    
    /**
     * Sets the outline color to the specified value.
     * @param color The color to set (no effect if null)
     */
    public void setOutlineColor(Color color)
    {
        if(color != null)
        {
            outlineColor = color;
        }
    }
        
    /**
     * Sets the fill pattern to the specified value.
     * @param paint The paint to set (no effect if null)
     */
    public void setFillPaint(Paint paint)
    {
        if(paint != null)
        {
            fillPaint = paint;
        } 
    }

    /**
     * Sets the stroke pattern to the specified value.
     * @param stroke The stroke to set (no effect if null)
     */
    public void setStroke(Stroke stroke)
    {
        if(stroke != null)
        {
            this.stroke = stroke;
        } 
    }
    
    /**
     * Applies this style to the specified graphics, returning the old style.
     * If <code>graphics</code> is null, this method will return null.
     * 
     * @param graphics The graphics to apply the style to.
     * @return The old style specification (so you can reapply it)
     */
    public DrawStyle applyStyle(Graphics2D graphics)
    {
        if(graphics == null)
        {
            return null;
        }
        
        DrawStyle oldStyle = new DrawStyle(graphics.getColor(),
                                           graphics.getPaint(),
                                           graphics.getStroke());
                                           
        graphics.setColor(outlineColor);
        graphics.setPaint(fillPaint);
        graphics.setStroke(stroke);
        
        return oldStyle;
    }
}
