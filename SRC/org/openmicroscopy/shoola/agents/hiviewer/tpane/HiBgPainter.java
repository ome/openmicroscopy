/*
 * org.openmicroscopy.shoola.agents.hiviewer.tutil.HiBgPainter
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

package org.openmicroscopy.shoola.agents.hiviewer.tpane;


//Java imports
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;

//Third-party libraries

//Application-internal dependencies

/** 
 * Paints an highlighted background for the <code>TitleBar</code> of 
 * {@link org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrame}
 * or 
 * {@link org.openmicroscopy.shoola.agents.hiviewer.tpane.TinyPane}.
 * 
 * This is a gradient paint. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
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
     * @see Painter#doPaint(java.awt.Graphics2D, int, int)
     */
    protected void doPaint(Graphics2D g2D, int width, int height)
    {
        //Top half.
        Paint p = new GradientPaint(0, height/2, Color.WHITE, 
                                    0, 0, highlightColor);
        g2D.setPaint(p);
        g2D.fillRect(0, 0, width, height/2);
        
        //Bottom half.
        p = new GradientPaint(0, height/2, Color.WHITE, 
                              0, height, highlightColor);
        g2D.setPaint(p);
        g2D.fillRect(0, height/2, width, height/2);
    }

}
