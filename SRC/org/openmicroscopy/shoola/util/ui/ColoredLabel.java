/*
 * org.openmicroscopy.shoola.util.ui.table.ColorLabel
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class ColoredLabel
    extends JLabel
{
    
    private Color   DEFAULT_FONT_COLOR = Color.BLACK;
    
    private int     textWidth;
    
    private String  text;
    
    private Color   fontColor; 
    
    public ColoredLabel(String text)
    {
        this.text = text;
        initTextWidth();
    }
    
    public ColoredLabel()
    {
        initTextWidth();
    }
    
    /** Overrides the setText method. */
    public void setText(String text) { this.text = text; }
    
    public void setFontColor(Color color) { fontColor = color; }
    
    /** Overrides the paintComponent method. */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(getBackground());
        Dimension d = getSize();
        g2D.fillRect(0, 0, d.width, d.height); 
        if (text != null) paintText(g2D, d.width, d.height);
    }
    
    private void paintText(Graphics2D g2D, int width, int height)
    {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        int wText = text.length()*textWidth;
        int x = 0, y;
        if ((width-wText) > 0) x = (width-wText)/2;
        y = hFont+(height-hFont)/2;
        if (fontColor == null) g2D.setColor(DEFAULT_FONT_COLOR);
        else g2D.setColor(fontColor);
        
        g2D.drawString(text, x, y);
    }
    
    /** Initializes the width of the text. */
    private void initTextWidth()
    {
        FontMetrics metrics = getFontMetrics(getFont());
        textWidth = metrics.charWidth('m');
    }
    
}
