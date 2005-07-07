/*
 * org.openmicroscopy.shoola.agents.viewer.ImageFactory
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

package org.openmicroscopy.shoola.agents.viewer;


//Java imports
import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

/** 
 * Factory class to create buffered Image.
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
public class ImageFactory
{

    private static final int    TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_LEFT = 2,
                                BOTTOM_RIGHT = 3;
    
    /** Default color components. */
    private static final int    RED = 255, GREEN = 0, BLUE = 0; 
    
    /** Main image and the lens image in the top-left corner. */
    public static BufferedImage getImagePinTopLeft(BufferedImage img, 
            BufferedImage lens, PlaneArea pa, boolean painting, Color c)
    {
        return getImagePinOnSide(img, lens, pa, painting, c, TOP_LEFT);
    }

    /** Main image and the lens image in the bottom-left corner. */
    public static BufferedImage getImagePinBottomLeft(BufferedImage img, 
            BufferedImage lens, PlaneArea pa, boolean painting, Color c)
    {
        return getImagePinOnSide(img, lens, pa, painting, c, BOTTOM_LEFT);
    }
    
    /** Main image and the lens image in the top-right corner. */
    public static BufferedImage getImagePinTopRight(BufferedImage img, 
            BufferedImage lens, PlaneArea pa, boolean painting, Color c)
    {
        return getImagePinOnSide(img, lens, pa, painting, c, TOP_RIGHT);
    }
    
    /** Main image and the lens image in the bottom-right corner. */
    public static BufferedImage getImagePinBottomRight(BufferedImage img, 
            BufferedImage lens, PlaneArea pa, boolean painting, Color c)
    {
        return getImagePinOnSide(img, lens, pa, painting, c, BOTTOM_RIGHT);
    }

    /** 
     * Compose two bufferedImages: the main one and the pin one.
     * The pin one is positionned on top of the main image.
     * 
     * @param img   main displayed image.
     * @param lens  pin image
     * @param xLoc  x-coordinate of the pin image ref frame: main one.
     * @param yLoc  y-coordinate of the pin image ref frame: main one.
     * @return      A bufferedImage with dataBufferByte as dataBuffer, 
     *              b/c of the implementation of the TIFFEncoder.
     */ 
    public static BufferedImage getImagePinOn(BufferedImage img, 
                      BufferedImage lens, int xLoc, int yLoc)
    {
        int sizeX = img.getWidth();
        int sizeY = img.getHeight();
        int sizeX1 = lens.getWidth();
        int wPinEnd = lens.getWidth()+xLoc;
        int hPinEnd = lens.getHeight()+yLoc;
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        //DataBuffer and ColorModel of the main image
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        
        //DataBuffer of the pin image
        DataBuffer dataBufLens = lens.getRaster().getDataBuffer();
        //Read pixels from buffer.
        int v, pos;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                pos = sizeX*y+x;
                //Display the pin Image on the original one.s
                if (x >= xLoc && y >= yLoc && x < wPinEnd && y < hPinEnd) 
                    v = dataBufLens.getElem(0, sizeX1*(y-yLoc)+x-xLoc);
                else 
                    v = dataBuf.getElem(0, pos);
                buffer.setElem(0, pos, cm.getRed(v));
                buffer.setElem(1, pos, cm.getGreen(v));
                buffer.setElem(2, pos, cm.getBlue(v));
            } 
        } 
        return createImage(sizeX, sizeY, buffer); 
    }

    /**
     * 
     * Create a sub-BufferedImage from an original bufferedImage.
     * 
     * @param img       original buffered image.
     * @param x1        x-coordinate of the subImage, default <code>0</code>.
     * @param y1        y-coordinate of the subImage, default <code>0</code>.
     * @param sizeX     width of the subImage, default 
     *                  <code>img.getWidth()</code>.
     * @param sizeY     height of the subImage, 
     *                  default <code>img.getHeight()</code>.
     * @param painting  If <code>true</code>, paint the clip area.
     * @param c         Color of the clip area.
     * 
     * @return      A bufferedImage with dataBufferByte as dataBuffer, 
     *              b/c of the implementation of the TIFFEncoder.
     */
    public static BufferedImage getImage(BufferedImage img, int x1, int y1, 
                                int sizeX, int sizeY, boolean painting, Color c)
    {
        if (painting && c == null) c = Color.RED;
        int sizeX1 = img.getWidth();
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        int v, pos;
        int red, green, blue;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                pos = sizeX*y+x;
                if (painting && (x == 0 || y == 0 || x == sizeX-1 || 
                        y == sizeY-1)) {
                    red = c.getRed();
                    green = c.getGreen();
                    blue = c.getBlue();
                } else {
                    v = dataBuf.getElem(0, sizeX1*(y+y1)+x+x1);
                    red = cm.getRed(v);
                    green = cm.getGreen(v);
                    blue = cm.getBlue(v);
                }
                buffer.setElem(0, pos, red);
                buffer.setElem(1, pos, green);
                buffer.setElem(2, pos, blue);
            } 
        }  
        return createImage(sizeX, sizeY, buffer); 
    }
    
    public static BufferedImage getImage(BufferedImage img)
    {
        return getImage(img, 0, 0, img.getWidth(), img.getHeight(), false, 
                        null);
    }
    
    /**
     * Create a buffered image with the pin area on top.
     * 
     * @param img       original buffered image
     * @param pa        rectangle representing the lens.
     * @param painting  <code>true<code> if we pain the original lens area, 
     * @param c         Color of the pin area.
     * 
     * @return      A bufferedImage with dataBufferByte as dataBuffer, 
     *              b/c of the implementation of the TIFFEncoder.
     */
    public static BufferedImage getImageWithPinArea(BufferedImage img, 
                                        PlaneArea pa, boolean painting, Color c)
    {
        if (painting && c == null) c = new Color(RED, GREEN, BLUE);
        int sizeX = img.getWidth();
        int sizeY = img.getHeight();
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        int v, pos;
        int red, green, blue;      
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                pos = sizeX*y+x;
                if (painting && pa.onBoundaries(x, y)) {
                    red = c.getRed();
                    green = c.getGreen();
                    blue = c.getBlue();
                } else {
                    v = dataBuf.getElem(0, pos);
                    red = cm.getRed(v);
                    green = cm.getGreen(v);
                    blue = cm.getBlue(v);
                }
                buffer.setElem(0, pos, red);
                buffer.setElem(1, pos, green);
                buffer.setElem(2, pos, blue);
            } 
        }  
        return createImage(sizeX, sizeY, buffer); 
    }

    /** 
     * Compose two bufferedImages: the main image displayed and the pin one
     * on side.
     * 
     * @param img       main image displayed.
     * @param lens      pin image.
     * @param pa        rectangle representing the lens positionned.
     * @param x1        x-coordinate of the lens image ref frame: magnified 
     *                  image.
     * @param y1        y-coordinate of the lens image ref frame: magnified 
     *                  image.
     * @param wRealPin  width of the lens w.r.t. to the main image.
     * @param hRealPin  height of the lens w.r.t. to the main image.
     * @param painting  <code>true<code> if we pain the original lens area,
     * @param c         Color of the pin area. Default Color: Color.RED.
     * 
     * @return          A bufferedImage with dataBufferByte as dataBuffer, 
     *                  b/c of the implementation of the TIFFEncoder.
     */
    private static BufferedImage getImagePinOnSide(BufferedImage img, 
                BufferedImage lens, PlaneArea pa, boolean painting, Color c, 
            int index)
    {
        if (painting && c == null) c = new Color(RED, GREEN, BLUE);
        int sizeX = img.getWidth();
        int sizeY = img.getHeight();
        int sizeX1 = lens.getWidth();
        int sizeY1 = lens.getHeight();
        int diffY = 0, extraXPin = 0, extraXMain = 0;
        switch (index) {
            case TOP_LEFT:
                diffY = 0;
                extraXMain = 0;
                extraXPin = sizeX1;
                break;
            case TOP_RIGHT:
                diffY = 0;
                extraXMain = sizeX;
                extraXPin = 0;
                break;
            case BOTTOM_RIGHT:
                extraXMain = sizeX;
                extraXPin = 0;
                diffY = sizeY-sizeY1;
                break;
            case BOTTOM_LEFT:
                extraXMain = 0;
                extraXPin = sizeX1;
                diffY = sizeY-sizeY1;
                break;
        }
        int sizeXTra = sizeX+sizeX1;
        DataBufferByte buffer = new DataBufferByte(sizeXTra*sizeY, 3);
        //DataBuffer and ColorModel of the main image
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        
        //DataBuffer of the pin image
        DataBuffer dataBufLens = lens.getRaster().getDataBuffer();
        //Read pixels from buffer.
        int v, pos;
        //Store the lens pixels in the Big buffer
        for (int y = 0; y < sizeY1; ++y) {
            for (int x = 0; x < sizeX1; ++x) {
                v = dataBufLens.getElem(0, sizeX1*y+x);
                pos = sizeXTra*(y+diffY)+x+extraXMain;
                buffer.setElem(0, pos, cm.getRed(v));
                buffer.setElem(1, pos, cm.getGreen(v));
                buffer.setElem(2, pos, cm.getBlue(v));
            }
        } 
        int red = 0, green = 0, blue = 0;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                //draw the border of the lens on the original image.
                if (painting && pa.onBoundaries(x, y)) {
                    red = c.getRed();
                    green = c.getGreen();
                    blue = c.getBlue();       
                } else {
                    v = dataBuf.getElem(0, sizeX*y+x);
                    red = cm.getRed(v);
                    green = cm.getGreen(v);
                    blue = cm.getBlue(v);
                }
                pos = sizeXTra*y+x+extraXPin;
                buffer.setElem(0, pos, red);
                buffer.setElem(1, pos, green);
                buffer.setElem(2, pos, blue);
            } 
        } 
        
        return createImage(sizeXTra, sizeY, buffer); 
    }
    
    /** Create the Bufferedimage. */
    private static BufferedImage createImage(int sizeX, int sizeY, 
                                DataBufferByte buffer)
    {
        ComponentColorModel ccm = new ComponentColorModel(
                                ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                                null, false, false, Transparency.OPAQUE, 
                                DataBuffer.TYPE_BYTE);
        BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
                                sizeX, sizeY, 3);
        return new BufferedImage(ccm, 
                Raster.createWritableRaster(csm, buffer, null), false, null);
    }
    
}

