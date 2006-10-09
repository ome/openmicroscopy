/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * UI component where the renderered image is painted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class BrowserCanvas
    extends JPanel
{
    
    /** Stroke of the scale bar. */
    private static final BasicStroke    UNIT_BAR_STROKE = new BasicStroke(2.0f);
    
    /** Color of the scale bar. */
    private static final Color          UNIT_BAR_COLOR = Color.GRAY;
    
    /** Width of a character w.r.t. the font metrics. */
    private int             charWidth;
    
    /** Reference to the Model. */
    private BrowserModel    model;
    
    /** Reference to the component hosting the canvas. */
    private BrowserUI       view;
    
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
     * Paints the scale bar.
     * 
     * @param g2D   The graphics context.
     * @param x     The x-coordinate of the bar.
     * @param y     The y-coordinate of the bar.
     * @param l     The length of the bar.
     * @param s     The text displayed on of the bar.
     */
    private void paintScaleBar(Graphics2D g2D, int x, int y, int l, 
                                String s)
    {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight()/3;
        int size = s.length()*charWidth;
        g2D.setColor(UNIT_BAR_COLOR);
        g2D.drawString(s, x+l/2-size/2+1, y-hFont);
        g2D.setStroke(UNIT_BAR_STROKE);
        g2D.drawLine(x, y, x+l, y);
    }
    
    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    BrowserCanvas(BrowserModel model, BrowserUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        charWidth = getFontMetrics(getFont()).charWidth('m');
        setDoubleBuffered(true);
    }

    /**
     * Overriden to paint the image.
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        BufferedImage img = model.getDisplayedImage();
        if (img == null) return;
        //paintXYFrame(g2D);

        g2D.drawImage(img, null, 0, 0);  
        double v = model.getPixelsSizeX()/model.getZoomFactor();
        v *= model.getUnitBarSize();
        int size = (int) model.getUnitBarSize();
        if (v > 0 && model.isUnitBar()) {
            paintScaleBar(g2D, img.getWidth()-size-5, 
                        img.getHeight()-10, size, ""+(int) v);
        }
    }
    
}
