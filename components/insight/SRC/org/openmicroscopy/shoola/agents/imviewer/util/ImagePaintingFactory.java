/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

//Third-party libraries

//Application-internal dependencies

/** 
 * Collection of helper methods used to paint images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImagePaintingFactory
{
    
	/** The default color of the background. */
	public static final Color			DEFAULT_BACKGROUND = 
											new Color(200, 200, 200);
	
	/** Name of the default default color of the background. */
	public static final String			DEFAULT_BACKGROUND_NAME = 
													"Default background";
	
	/** Color of the scale bar. */
    public static final Color          	UNIT_BAR_COLOR = Color.GRAY;
    
	/** Name of the default color of the scale bar. */
    public static final String			UNIT_BAR_COLOR_NAME = "Gray";
    
    /** Stroke of the scale bar. */
    private static final BasicStroke    UNIT_BAR_STROKE = new BasicStroke(2.0f);
   
    /**
     * Paints the XY-frame.
     * 
     * @param g2D The graphics context.
     */
    /*
    private void paintXYFrame(Graphics2D g2D)
    {
        g2D.setColor(AXIS_COLOR);
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight()/4;
        int x1 = ORIGIN_FRAME;
        int y1 = ORIGIN_FRAME;
        g2D.drawLine(x1, y1, x1+LENGTH, y1);
        g2D.drawLine(x1+LENGTH-ARROW, y1-ARROW, x1+LENGTH, y1);
        g2D.drawLine(x1+LENGTH-ARROW, y1+ARROW, x1+LENGTH, y1);
        //y-axis
        g2D.drawLine(x1, y1, x1, y1+LENGTH);
        g2D.drawLine(x1-ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);
        g2D.drawLine(x1+ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);   
        //name
        g2D.drawString("o", x1-hFont, y1-hFont);
        g2D.drawString("x", x1+LENGTH/2, y1-hFont);
        g2D.drawString("y", x1-2*hFont, y1+LENGTH-hFont); 
    }
    */
    
    /**
     * Sets the value of the for the rendering algorithms (interpolation 
     * enabled by default)
     * 
     * @param g2D The graphics context.
     */
    public static void setGraphicRenderingSettings(Graphics2D g2D)
    {
        setGraphicRenderingSettings(g2D, true);
    }
    
    /**
     * Sets the value of the for the rendering algorithms.
     * 
     * @param g2D The graphics context.
     * @param interpolate Pass <code>false</code> to disable interpolation
     */
    public static void setGraphicRenderingSettings(Graphics2D g2D, boolean interpolate)
    {
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        if (interpolate) {
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }
    
    /**
     * Paints a scale bar.
     * 
     * @param g2D   The graphics context.
     * @param x     The x-coordinate of the bar.
     * @param y     The y-coordinate of the bar.
     * @param l     The length of the bar.
     * @param s     The text displayed on of the bar.
     */
    public static void paintScaleBar(Graphics2D g2D, int x, int y, int l, 
                                String s)
    {
        paintScaleBar(g2D, x, y, l, s, UNIT_BAR_COLOR);
    }
    
    /**
     * Paints a scale bar.
     * 
     * @param g2D   The graphics context.
     * @param x     The x-coordinate of the bar.
     * @param y     The y-coordinate of the bar.
     * @param l     The length of the bar.
     * @param s     The text displayed on of the bar.
     * @param c		The color of the scale bar. If <code>null</code> the default
     * 				color is set.
     */
    public static void paintScaleBar(Graphics2D g2D, int x, int y, int l, 
                                String s, Color c)
    {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight()/3;
        if (c == null) c = UNIT_BAR_COLOR;
        g2D.setColor(c);
        g2D.drawString(s, x+(l-fontMetrics.stringWidth(s))/2+1, y-hFont);
        g2D.setStroke(UNIT_BAR_STROKE);
        g2D.drawLine(x, y, x+l, y);
    }
    
}
