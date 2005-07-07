/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.PreviewCanvas
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

package org.openmicroscopy.shoola.agents.viewer.canvas;

//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ImageFactory;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.util.ImageSaver;

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
public class PreviewCanvas
    extends JPanel
{
    
    private static final int    BORDER = 2;
    
    private static final Color  DEFAULT_COLOR = Color.RED;
    
    /**  Width and height of the canvas. */
    private int                 w, h;
    
    /** Coordinates of the top-left corner of the canvas. */
    private int                 x, y;
    
    /** The original bufferedImage to display. */   
    private BufferedImage       image, sideImage, originalImage;
    
    private JScrollPane         scrollPane;
    
    private int                 txtWidth;
    
    public PreviewCanvas()
    {
        txtWidth = getFontMetrics(getFont()).charWidth('m');
        setBackground(ViewerUIF.BACKGROUND_COLOR); 
        setDoubleBuffered(true);
    }
    
    public void setContainer(JScrollPane c) { scrollPane = c; }
    
    public BufferedImage getDisplayImage()
    {
        return ImageFactory.getImage(image);
    }
    
    /** Write some text on the image. */
    public void paintTextOnImage(String txt, int location, Color c)
    {
        if (c == null) c = DEFAULT_COLOR;
        image = paintNewImage(originalImage, txt, location, c);
        repaint();
    }
    
    /** 
     * Paint the specified image.
     * 
     * @param img   {@link BufferedImage img} to pain.
     */
    public void paintImage(BufferedImage image)
    {
        this.image = image;
        originalImage = image;
        if (image != null) {
            w = image.getWidth()+2*ViewerUIF.START;
            h = image.getHeight()+2*ViewerUIF.START;
            repaint();
        }
    }
    
    /** 
     * 
     * Paint the specified images.
     * @param image         main image.
     * @param sideImage     lens image.
     */
    public void paintImages(BufferedImage image, BufferedImage sideImage)
    {
        this.image = image;
        this.sideImage = sideImage;
        if (image != null && sideImage != null) {
            w = image.getWidth()+sideImage.getWidth()+3*ViewerUIF.START;
            h = image.getHeight()+sideImage.getHeight()+2*ViewerUIF.START;
            repaint();
        }      
    }
    
    /** Overrides the {@link #paint(Graphics)} method. */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        setLocation();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (image != null)
            g2D.drawImage(image, null, ViewerUIF.START, ViewerUIF.START);
        if (sideImage != null && image != null) 
            g2D.drawImage(sideImage, null, image.getWidth()+2*ViewerUIF.START, 
                    ViewerUIF.START);
    }
    
    /** Create a new BufferedImage. */
    private BufferedImage paintNewImage(BufferedImage img, String txt, int
            index, Color c)
    {
        int width = img.getWidth(), height =  img.getHeight();
        BufferedImage newImage = 
            (BufferedImage) createImage(width, height);
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, null, 0, 0); 
        
        //Paint the text
        FontMetrics fontMetrics = g2.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        int length = txt.length()*txtWidth;
        int xTxt = 0, yTxt = 0;
        switch (index) {
            case ImageSaver.PREVIEW_TOP_LEFT:
                xTxt = BORDER;
                yTxt = BORDER+hFont;
                break;
            case ImageSaver.PREVIEW_TOP_RIGHT:
                xTxt = width-BORDER-length;
                yTxt = BORDER+hFont;
                break;
            case ImageSaver.PREVIEW_BOTTOM_LEFT:
                xTxt = BORDER;
                yTxt = height-BORDER-hFont;
                break;
            case ImageSaver.PREVIEW_BOTTOM_RIGHT:
                xTxt = width-BORDER-length;
                yTxt = height-BORDER-hFont;
        }
        g2.setColor(c);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD));
        g2.drawString(txt, xTxt, yTxt);
        return newImage;
    }
    
    /** Set the coordinates of the top-left corner of the image. */
    private void setLocation()
    {
        Rectangle r = scrollPane.getViewportBorderBounds();
        x = ((r.width-w)/2);
        y = ((r.height-h)/2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        setBounds(x, y, w, h);
    }
    
}
