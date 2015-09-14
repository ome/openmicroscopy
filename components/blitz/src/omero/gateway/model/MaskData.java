/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.awt.image.BufferedImage;
import java.awt.Color;

import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.Shape;


/**
 * Represents an Mask in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class MaskData
    extends ShapeData
{

    /**
     * Creates a new instance.
     *
     * @param shape The shape this object represents.
     */
    public MaskData(Shape shape)
    {
        super(shape);
    }

    /** Creates a new instance of MaskData. */
    public MaskData()
    {
        this(0.0, 0.0, 0.0, 0.0, null);
    }

    /**
     * Creates a new instance of the MaskData.
     *
     * @param x The x-coordinate of the top-left corner of the image.
     * @param y The y-coordinate of the top-left corner of the image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param mask The mask image.
     */
    public MaskData(double x, double y, double width, double height,
            byte[] mask)
    {
        super(new MaskI(), true);
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
        this.setMask(mask);
    }

    /**
     * Returns the text of the shape.
     *
     * @return See above.
     */
    public String getText()
    {
        Mask shape = (Mask) asIObject();
        RString value = shape.getTextValue();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Sets the text of the shape.
     *
     * @param text See above.
     */
    public void setText(String text)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Mask shape = (Mask) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setTextValue(rtypes.rstring(text));
    }

    /**
     * Returns the x-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    public double getX()
    {
        Mask shape = (Mask) asIObject();
        RDouble value = shape.getX();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the x-coordinate top-left corner of an untransformed mask.
     *
     * @param x The value to set.
     */
    public void setX(double x)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Mask shape = (Mask) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setX(rtypes.rdouble(x));
    }

    /**
     * Returns the y-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    public double getY()
    {
        Mask shape = (Mask) asIObject();
        RDouble value = shape.getY();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the y-coordinate top-left corner of an untransformed mask.
     *
     * @param y See above.
     */
    public void setY(double y)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Mask shape = (Mask) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setY(rtypes.rdouble(y));
    }

    /**
     * Returns the width of the mask.
     *
     * @return See above.
     */
    public double getWidth()
    {
        Mask shape = (Mask) asIObject();
        RDouble value = shape.getWidth();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the width of an untransformed mask.
     *
     * @param width See above.
     */
    public void setWidth(double width)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Mask shape = (Mask) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setWidth(rtypes.rdouble(width));
    }

    /**
     * Returns the height of the mask.
     *
     * @return See above.
     */
    public double getHeight()
    {
        Mask shape = (Mask) asIObject();
        RDouble value = shape.getHeight();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the height of an untransformed mask.
     *
     * @param height See above.
     */
    public void setHeight(double height)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Mask shape = (Mask) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setHeight(rtypes.rdouble(height));
    }

    /**
     * Sets the mask image.
     *
     * @param mask See above.
     */
    public void setMask(byte[] mask)
    {
        Mask shape = (Mask) asIObject();
        shape.setBytes(mask);	
    }

    /**
     * Sets the mask from the bufferedImage.
     *
     * @param image See above.
     */
    public void setMask(BufferedImage image)
    {
        byte[] data = new byte[(int) (getWidth()*getHeight())];
        boolean colourSet = false;
        ShapeSettingsData settings = getShapeSettings();
        for (int y = 0 ; y < getHeight(); y++)
            for (int x = 0; x < getWidth(); x++)
                if (image.getRGB(x,y) == 0)
                    setBit(data, (int) (y*getWidth()+x), 0);
                else
                {
                    if (!colourSet)
                    {
                        settings.setFill(new Color(image.getRGB(x,y), true));
                        colourSet = true;
                    }
                    setBit(data, (int)(y*getWidth()+x), 1);
                }
    }

    /**
     * Returns the mask image.
     *
     * @return See above.
     */
    public BufferedImage getMaskAsBufferedImage()
    {
        Mask shape = (Mask) asIObject();
        byte[] data = shape.getBytes();
        if (data == null) return null;
        double width = getWidth();
        if (width == 0) return null;
        double height = getHeight();
        if (height == 0) return null;
        BufferedImage bufferedImage = new BufferedImage((int) width,
                (int) height, BufferedImage.TYPE_INT_ARGB);
        int offset = 0;
        int colourValue = getShapeSettings().getFill().getRGB();
        for (int y = 0 ; y < (int)height ; y++)
        {
            for (int x = 0 ; x < (int)width ; x++)
            {
                int bit = getBit(data, offset);
                if (bit == 1)
                    bufferedImage.setRGB(x, y, colourValue);
                else
                    bufferedImage.setRGB(x, y, 0);
                offset++;
            }
        }
        return bufferedImage;
    }

    /**
     * Returns the mask image.
     *
     * @return See above.
     */
    public int[][] getMaskAsBinaryArray()
    {
        Mask shape = (Mask) asIObject();
        byte[] data = shape.getBytes();
        if (data == null) return null;
        double width = getWidth();
        if (width == 0) return null;
        double height = getHeight();
        if (height == 0) return null;
        int[][] returnArray = new int[(int)width][(int)height];
        int offset = 0;
        for (int y = (int) height-1 ; y > 0 ; y--)
        {
            for (int x = 0 ; x < (int)width ; x++)
            {
                returnArray[x][y] = getBit(data, offset);
                offset++;
            }
        }
        return returnArray;
    }

    /**
     * Returns the mask as a byte array.
     * @return See above.
     */
    public byte[] getMask()
    {
        Mask shape = (Mask) asIObject();
        byte[] data = shape.getBytes();
        return data;
    }

    /** 
     * Sets the bit value in a byte array at position bit to be the value
     * value.
     *
     * @param data See above.
     * @param bit See above.
     * @param val See above.
     */
    public void setBit(byte[] data, int bit, int val) 
    {
        int bytePosition = bit/8;
        int bitPosition = 7-bit%8;
        data[bytePosition] = (byte) ((byte)(data[bytePosition]&
                (~(byte)(0x1<<bitPosition)))|
                (byte)(val<<bitPosition));
    }

    /** 
     * Sets the bit value in a byte array at position bit to be the value
     * value.
     *
     * @param data See above.
     * @param bit See above.
     */
    public byte getBit(byte[] data, int bit)
    {
        int bytePosition = bit/8;
        int bitPosition = 7-bit%8;
        return (byte) ((byte)(data[bytePosition] & (0x1<<bitPosition)) != 0 ? 
                (byte)1 : (byte)0);
    }


}
