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

    
    /** Default color components. */
    private static final int RED = 255, GREEN = 0, BLUE = 0; 
    
    /** 
     * Compose two bufferedImages: the main image displayed and the pin one.
     * In this case, the pin one is located on the right side of the displayed 
     * one. 
     * @param img       main image displayed.
     * @param lens      pin image.
     * @param xLoc      x-coordinate of the pin image ref frame: main one.
     * @param yLoc      y-coordinate of the pin image ref frame: main one.
     * @param wLoc      width of the pin image.
     * @param hLoc      height of the pin image.
     * @param x1        x-coordinate of the lens image ref frame: magnified 
     *                  image.
     * @param y1        y-coordinate of the lens image ref frame: magnified 
     *                  image.
     * @param wRealPin  width of the lens w.r.t. to the main image.
     * @param hRealPin  height of the lens w.r.t. to the main image.
     * @param painting  <code>true<code> if we pain the original lens area,
     * @param c         Color of the pin area. Default Color: Color.RED
     * @return          A bufferedImage with dataBufferByte as dataBuffer, 
     *                  b/c of the implementation of the TIFFEncoder.
     */
    public static BufferedImage getImagePinOnSide(BufferedImage img, 
            BufferedImage lens, int xLoc, int yLoc, int wRealPin, int hRealPin,
                boolean painting, Color c)
    {
        if (painting && c == null) c = new Color(RED, GREEN, BLUE);
        int sizeX = img.getWidth();
        int sizeY = img.getHeight();
        int sizeX1 = lens.getWidth();
        int sizeY1 = lens.getHeight();
        int wPinEnd = wRealPin+xLoc;
        int hPinEnd = hRealPin+yLoc;
        int sizeXTra = sizeX+sizeX1;
        DataBufferByte buffer = new DataBufferByte(sizeXTra*sizeY, 3);
        //DataBuffer and ColorModel of the main image
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        
        //DataBuffer of the pin image
        DataBuffer dataBufLens = lens.getRaster().getDataBuffer();
        //Read pixels from buffer.
        int v, pos;
        int diff = sizeY-sizeY1;
        //Store the lens pixels in the Big buffer
        for (int y = 0; y < sizeY1; ++y) {
            for (int x = 0; x < sizeX1; ++x) {
                v = dataBufLens.getElem(0, sizeX1*y+x);
                pos = sizeXTra*(y+diff)+x+sizeX;
                buffer.setElem(0, pos, cm.getRed(v));
                buffer.setElem(1, pos, cm.getGreen(v));
                buffer.setElem(2, pos, cm.getBlue(v));
            }
        } 
        int red = 0, green = 0, blue = 0;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                //draw the border of the lens on the original image.
                if (painting && ((x == xLoc  && y >= yLoc && y <= hPinEnd) ||
                    (y == yLoc && x >= xLoc && x <= wPinEnd) ||
                    (x == wPinEnd  && y >= yLoc && y <= hPinEnd) ||
                    (y == hPinEnd && x >= xLoc && x <= wPinEnd))) {
                    red = c.getRed();
                    green = c.getGreen();
                    blue = c.getBlue();       
                } else {
                    v = dataBuf.getElem(0, sizeX*y+x);
                    red = cm.getRed(v);
                    green = cm.getGreen(v);
                    blue = cm.getBlue(v);
                }
                pos = sizeXTra*y+x;
                buffer.setElem(0, pos, red);
                buffer.setElem(1, pos, green);
                buffer.setElem(2, pos, blue);
            } 
        } 
        
        //Create the image.
        ComponentColorModel ccm = new ComponentColorModel(
                                  ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                                  null, false, false, Transparency.OPAQUE, 
                                  DataBuffer.TYPE_BYTE);
        BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
                                  sizeXTra, sizeY, 3);
        return new BufferedImage(ccm, 
              Raster.createWritableRaster(csm, buffer, null), false, null);
    }
    
    /** 
     * Compose two bufferedImages: the main one, and the pin one.
     * The pin one is located on top of the main image.
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
        
        //Create the image.
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
     * 
     * Create a sub-BufferedImage from an original bufferedImage.
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
        ComponentColorModel ccm = new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                        null, false, false, Transparency.OPAQUE, 
                        DataBuffer.TYPE_BYTE);
        BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
                        sizeX, sizeY, 3);
        return new BufferedImage(ccm, 
        Raster.createWritableRaster(csm, buffer, null), false, null);
    }
    
    public static BufferedImage getImage(BufferedImage img)
    {
        return getImage(img, 0, 0, img.getWidth(), img.getHeight(), false, 
                        null);
    }
    
    /**
     * Create a buffered image with the pin area drawn on top.
     * 
     * @param img       original buffered image
     * @param xLoc      x-coordinate of the pin image, ref. frame: main image.
     * @param yLoc      y-coordinate of the pin image, ref. frame: main image.
     * @param wPin      width of the pin image, ref. frame: main image.    
     * @param hPin      height of the pin image, ref. frame: main image.
     * @param painting  <code>true<code> if we pain the original lens area, 
     * @param c         Color of the pin area.
     * 
     * @return      A bufferedImage with dataBufferByte as dataBuffer, 
     *              b/c of the implementation of the TIFFEncoder.
     */
    public static BufferedImage getImageWithPinArea(BufferedImage img, 
                                        int xLoc, int yLoc, int wPin, int hPin, 
                                        boolean painting, Color c)
    {
        if (painting && c == null) c = new Color(RED, GREEN, BLUE);
        int sizeX = img.getWidth();
        int sizeY = img.getHeight();
        int wPinEnd = wPin+xLoc;
        int hPinEnd = hPin+yLoc;
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        int v, pos;
        int red, green, blue;      
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                pos = sizeX*y+x;
                if (painting && ((x == xLoc && y <= hPinEnd && y >= yLoc) ||
                    (y == yLoc && x >= xLoc && x <= wPinEnd) ||
                    (x == wPinEnd && y <= hPinEnd && y >= yLoc) ||
                    (y == hPinEnd && x >= xLoc && x <= wPinEnd)))
                {
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

