/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;

import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

/** 
 * Canvas to display the selected buffered 2D-image.
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
public class ImageCanvas
	extends JPanel
{

    /** 
     * Width and height of the canvas. 
     * The <code>width</code> (resp. the <code>height</code>) is equal to 
     * <code>width</code> (resp. <code>height</code>) of the buffered image 
     * multiplied by the <code>magFactor</code>.
     */
    private int                     w, h;
    
    /** Zoom level. */
    private double                  magFactor;
    
    /** Affine transformation. */
    private BufferedImageOp         biop;
    
    /** Buffered image to filter. */
    private BufferedImage           bimg;
    
    private BufferedImage           zoomImage;
    
	/** The original bufferedImage to display. */	
	private BufferedImage          image;
    
	/** Coordinates of the top-left corner of the canvas. */
	private int                    x, y;
    
	/** Reference to the {@link Viewer view}. */
	private ViewerUIF 			  view;
	
	public ImageCanvas(ViewerUIF view)
	{
		this.view = view;
        magFactor = 1.0;
		setBackground(Viewer.BACKGROUND_COLOR); 
		setDoubleBuffered(true);
	}
 
    public void resetMagFactor()
    { 
        magFactor = 1.0;
        image = null;
        zoomImage = null;
    }
    
	/** 
	 * Paint the image. 
	 * 
	 * @param image		buffered image to display.
	 * 	
	 */	
	public void paintImage(BufferedImage image)
	{
		this.image = image;
        if (image != null) {
            paintImage(magFactor, 
                    (int) (image.getWidth()*magFactor)+2*ViewerUIF.START, 
                    (int) (image.getHeight()*magFactor)+2*ViewerUIF.START);
        }
	} 
	
    /** 
     * Create a bufferedImage with dataBufferByte as dataBuffer, 
     * b/c of the implementation of the TIFFEncoder.
     */
    public BufferedImage getZoomImage()
    { 
        //Now we only need to tell Java2D how to handle the RGB buffer. 
        int sizeX = zoomImage.getWidth();
        int sizeY = zoomImage.getHeight();
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        DataBuffer dataBuf = zoomImage.getRaster().getDataBuffer();
        ColorModel cm = zoomImage.getColorModel();
        int v;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                v = dataBuf.getElem(0, sizeX*y+x);
                buffer.setElem(0, sizeX*y+x, cm.getRed(v));
                buffer.setElem(1, sizeX*y+x, cm.getGreen(v));
                buffer.setElem(2, sizeX*y+x, cm.getBlue(v));
            } 
        }  
        ComponentColorModel ccm = new ComponentColorModel(
                                    ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                                    null, false, false, Transparency.OPAQUE, 
                                    DataBuffer.TYPE_BYTE);
        BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
                                    sizeX, sizeY, 3);
        return new BufferedImage(ccm, 
                Raster.createWritableRaster(csm, buffer, null), false, null);
    }
     
    /** 
     * Paint the zoomed image. 
     * 
     * @param level     zoom level, value between 
     *                  {@link ImageInspectorManager#MIN_ZOOM_LEVEL} and
     *                  {@link ImageInspectorManager#MAX_ZOOM_LEVEL}.
     * @param w         width of the zoomed image.
     * @param h         height of the zoomed image.
     *  
     */ 
    public void paintImage(double level, int w, int h)
    {
        magFactor = level;
        this.w = w;
        this.h = h;
        AffineTransform at = new AffineTransform();
        at.scale(magFactor, magFactor);
        bimg = new BufferedImage(image.getWidth(), image.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
        RescaleOp rop = new RescaleOp((float) magFactor, 0.0f, null);
        rop.filter(image, bimg);
        biop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        zoomImage =  new BufferedImage((int) (image.getWidth()*magFactor), 
                                    (int) (image.getHeight()*magFactor),
                            BufferedImage.TYPE_INT_RGB);
        Graphics2D big = zoomImage.createGraphics();
        big.drawImage(bimg, biop, 0, 0);
        repaint();
    } 
    
	/** Overrides the paintComponent. */
	public void paint(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		setLocation();
		paintXYFrame(g2D);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);

		g2D.setColor(Color.black);
        if (zoomImage != null)
            g2D.drawImage(zoomImage, null, ViewerUIF.START, ViewerUIF.START);
	}

	/** Paint the XY-frame. */
	private void paintXYFrame(Graphics2D g2D)
	{
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight()/4;
		//x-axis
		int x1 = ViewerUIF.START-ViewerUIF.ORIGIN;
		int y1 = ViewerUIF.START-ViewerUIF.ORIGIN;
		g2D.drawLine(x1, y1, x1+ViewerUIF.LENGTH, y1);
		g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1-ViewerUIF.ARROW, 
					x1+ViewerUIF.LENGTH, y1);
		g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1+ViewerUIF.ARROW, 
					x1+ViewerUIF.LENGTH, y1);
		//y-axis
		g2D.drawLine(x1, y1, x1, ViewerUIF.LENGTH+y1);
		g2D.drawLine(x1-ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
					x1, ViewerUIF.LENGTH+y1);
		g2D.drawLine(x1+ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
					x1, ViewerUIF.LENGTH+y1);	
		//name
		g2D.drawString("o", x1-hFont, y1-hFont);
		g2D.drawString("x", x1+ViewerUIF.LENGTH/2, y1-hFont);
		g2D.drawString("y", x1-2*hFont, y1+ViewerUIF.LENGTH-hFont);	
	}
	
	/** Set the coordinates of the top-left corner of the image. */
	private void setLocation()
	{
		Rectangle r = view.getScrollPane().getViewportBorderBounds();
		x = ((r.width-w)/2);
		y = ((r.height-h)/2);
		if (x < 0) x = 0;
		if (y < 0) y = 0;
        setBounds(x, y, w, h);
	}
	
}
