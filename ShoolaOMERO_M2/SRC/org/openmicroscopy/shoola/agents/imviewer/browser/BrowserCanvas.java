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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
    implements MouseListener, MouseMotionListener
{

    /** String to representing the nanometer symbol. */
    private static final String         NANOMETER = " \u00B5m";
    
    /** Stroke of the scale bar. */
    private static final BasicStroke    UNIT_BAR_STROKE = new BasicStroke(2.0f);
    
    /** Color of the scale bar. */
    private static final Color          UNIT_BAR_COLOR = Color.GRAY;
    
    /** Color of the axis. */
    private static final Color          AXIS_COLOR = Color.BLACK;
    
    /** Location to the origin of the frame. */
    private static final int            ORIGIN_FRAME = 
                                            BrowserUI.TOP_LEFT_IMAGE-5;
    
    /** Length of the frame axis. */
    private static final int            LENGTH = 20;
    
    /** Length of the arrow. */
    private static final int            ARROW = 3;
    
    
    /** The unit bar value. */
    private double          unitBarValue;
    
    /** Width of a character w.r.t. the font metrics. */
    private int             charWidth;
    
    /** Flag to determine if a dragging event is ongoing. */
    private boolean         dragging;
    
    /** The anchor point. */
    private Point           anchor;
    
    /** Reference to the Model. */
    private BrowserModel    model;
    
    /** Reference to the component hosting the canvas. */
    private BrowserUI       view;
    
    /**
     * Paints the XY-frame.
     * 
     * @param g2D The graphics context.
     */
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
        g2D.drawString(s, x+l/2-size/2, y-hFont);
        g2D.setStroke(UNIT_BAR_STROKE);
        g2D.drawLine(x, y, x+l, y);
    }
    
    /**
     * Sets the location of the canvas.
     * 
     * @param w The width of the canvas.
     * @param h The height of the canvas.
     */
    private void setCanvasLocation(int w, int h)
    {
        Rectangle r = view.getViewportBorderBounds();
        int x = ((r.width-w)/2);
        int y = ((r.height-h)/2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        view.setComponentsSize(w, h);
        setBounds(x, y, w, h);
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
        setDoubleBuffered(true);
        charWidth = getFontMetrics(getFont()).charWidth('m');
        dragging = false;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Determines the location of the mouse and paints a lens image if the
     * <code>Magnifier</code> is on screen.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        if (e.getClickCount() != 1) return;
        Point p = new Point(e.getPoint());
        if (!dragging) {
            dragging = true;
        }
    }

    /**
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
        dragging = false;
    }
    
    /**
     * Paints the lens image if the <code>Magnifier</code> is on screen.
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Overriden to paint the image.
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        System.out.print("painting \n");
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        BufferedImage image = model.getDisplayedImage();
        if (image == null) return;
        paintXYFrame(g2D);
        setCanvasLocation(image.getWidth()+2*BrowserUI.TOP_LEFT_IMAGE,
                            image.getHeight()+2*BrowserUI.TOP_LEFT_IMAGE);
        double v = 1;//model.getPixelsSizeX()/model.getZoomFactor();
        if (v > 0) {
            int h = image.getHeight()+3*BrowserUI.TOP_LEFT_IMAGE/2;
            paintScaleBar(g2D, BrowserUI.TOP_LEFT_IMAGE, h, 2*LENGTH, 
                      ""+(int) v+NANOMETER);
        }
        g2D.drawImage(image, null, BrowserUI.TOP_LEFT_IMAGE,
                BrowserUI.TOP_LEFT_IMAGE);  
    }

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our 
     * case, no op implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */ 
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our 
     * case, no op implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */ 
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our 
     * case, no op implementation.
     * @see MouseListener#mouseExited(MouseEvent)
     */ 
    public void mouseExited(MouseEvent e) {}

    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */ 
    public void mouseMoved(MouseEvent e) {}
    
}
