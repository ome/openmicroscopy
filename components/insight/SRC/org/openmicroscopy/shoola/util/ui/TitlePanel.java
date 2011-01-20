/*
 * org.openmicroscopy.shoola.util.ui.TitlePanel
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import javax.swing.Icon;

//Third-party libraries
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.RectanglePainter;

//Application-internal dependencies

/** 
 * A general purpose title panel having a title, sub-title, explanatory text,
 * and graphics.
 * The title, sub-title, and explanatory text are aligned to the left in three
 * horizontal rows and take up as much width as is available.  The grahics is
 * aligned to the right and spawns all three rows.  The title is displayed in
 * a bold font, the sub-title in a normal font, and the explanatory text in an
 * italic font.  The title and sub-title are displayed in a single line label,
 * as the explanatory text is embedded in a multi-line label.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TitlePanel
    extends JXHeader
{
    
    /** Default background color. */
    private static final Color  BG_COLOR = 
    	UIUtilities.SELECTED_BACKGROUND_COLOUR.darker();//new Color(0x417DDD);  
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param text The explanatory text.
     * @param icon An icon to use as the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, String text, Icon icon)
    {
    	super.setTitle(title);
    	String s = subTitle;
    	if (text != null) s += "\n"+text;
    	setDescription(s);
    	if (icon != null) setIcon(icon);
    	Color translucent = new Color(BG_COLOR.getRed(), BG_COLOR.getGreen(), 
    			BG_COLOR.getBlue(), 0);
    	setForeground(Color.LIGHT_GRAY);
    	GradientPaint bgToTranslucent = new GradientPaint(
    			new Point2D.Double(.4, 0), BG_COLOR,
    			new Point2D.Double(1, 0), translucent);
    	MattePainter veil = new MattePainter(bgToTranslucent);
    	veil.setPaintStretched(true);
    	Painter backgroundPainter = new RectanglePainter(Color.white, null);
    	Painter p = new CompoundPainter(backgroundPainter, veil, 
    			new GlossPainter());
    	setBackgroundPainter(p);
    }
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param icon An icon to use as the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, Icon icon)
    {
        this(title, subTitle, null, icon);
    }
    
    /**
     * Sets the title displayed in the header.
     * 
     * @param text The text to set.
     */
    public void setTitle(String text)
    {
    	//title.setText(text);
    	super.setTitle(text);
    	repaint();
    }
    
    /**
     * Sets the text displayed in the header.
     * 
     * @param text The text to set.
     */
    public void setTextHeader(String text)
    {
    	super.setDescription(text);
       // this.text.setText(text);
        repaint();
    }
    
    /**
     * Sets the note displayed in the header.
     * 
     * @param text The text to set.
     */
    public void setSubtitle(String text)
    {
    	String s = super.getDescription();
    	if (text != null)
    		s += "\n"+text;
    	setTextHeader(s);
    }
   
}
