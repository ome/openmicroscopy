/*
 * org.openmicroscopy.shoola.util.ui.tpane.BgPainter
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
 *------------------------------------------------------------------------------s
 */

package org.openmicroscopy.shoola.util.ui.tpane;


//Java imports
import java.awt.Color;
import java.awt.Graphics2D;

//Third-party libraries

//Application-internal dependencies

/** 
 * Paints the normal background of the <code>TitleBar</code> of 
 * {@link TinyPane}.
 * This is a three-line repeated pattern.
 * This class is stateless and can be shared among all instances of the frame.
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
public class BgPainter
    extends Painter
{

    /** Color to draw the line 1 in the pattern. */
    private static final Color    ROW_1 = new Color (227, 245, 238);
    
    /** Color to draw the line 2 in the pattern. */
    private static final Color    ROW_2 = new Color(242, 252, 248);
    
    /** Color to draw the line 3 in the pattern. */
    private static final Color    ROW_3 = new Color(253, 251, 251);
    

    /** 
     * Draws a three-line repeated pattern on the specified <code>area</code>.
     * 
     * @see Painter#doPaint(Graphics2D, int, int)
     */
    protected void doPaint(Graphics2D g2D, int width, int height)
    {
        //Draw the pattern.
        for(int y = 0; y < height; y += 3) {
            //Row 1.
            g2D.setColor(ROW_1);
            g2D.drawLine(0, y, width, y);
            //Row 2.
            g2D.setColor(ROW_2);
            g2D.drawLine(0, y+1, width, y+1);
            //Row 3.
            g2D.setColor(ROW_3);
            g2D.drawLine(0, y+2, width, y+2);
        }
        //NOTE: If h%3 != 0, the above will draw more lines than h.
        //Don't care: the graphics context has already been clipped 
        //to Rect[0, 0, width, height].
    }

}
