/*
 * org.openmicroscopy.shoola.util.ui.tpane.HiBgPainter
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

package org.openmicroscopy.shoola.util.ui.tpane;


//Java imports
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;

//Third-party libraries

//Application-internal dependencies

/** 
 * Paints an highlighted background for the <code>TitleBar</code> of 
 * {@link TinyPane}. This is a gradient paint. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4694 $ $Date: 2006-12-15 17:02:59 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
public class HiBgPainter
    extends Painter
{

    /** The color to use for highlighting the title bar. */
    private Color   highlightColor;
    
    
    /**
     * Creates a new instance.
     * 
     * @param highlightColor The color to use for highlighting the title bar.
     *                          Mustn't be <code>null</code>.
     */
    public HiBgPainter(Color highlightColor)
    {
        if (highlightColor == null)
            throw new NullPointerException("No highlight color.");
        this.highlightColor = highlightColor;
    }
    
    /**
     * Paints an highlighted background.
     * @see Painter#doPaint(Graphics2D, int, int)
     */
    protected void doPaint(Graphics2D g2D, int width, int height)
    {
        //Top half.
        Paint p = new GradientPaint(0, ((float) height)/2, Color.WHITE, 0, 0,
                					highlightColor);
        g2D.setPaint(p);
        g2D.fillRect(0, 0, width, height/2);
        
        //Bottom half.
        p = new GradientPaint(0, ((float) height)/2, Color.WHITE, 0, height,
                				highlightColor);
        g2D.setPaint(p);
        g2D.fillRect(0, height/2, width, height/2);
    }

}
