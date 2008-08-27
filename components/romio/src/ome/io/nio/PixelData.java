/*
 * ome.io.nio.DeltaVision
 *
 *   Copyright 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;

import ome.model.enums.PixelsType;

/**
 * Represents a block of pixel data.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:chris@glencoesoftware.com">chris@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class PixelData
{
    /** Identifies the type used to store pixel values. */
    public static final int BYTE = 0;

    /** Identifies the type used to store pixel values. */
    public static final int SHORT = 1;

    /** Identifies the type used to store pixel values. */
    public static final int INT = 2;

    /** Identifies the type used to store pixel values. */
    public static final int LONG = 3;

    /** Identifies the type used to store pixel values. */
    public static final int FLOAT = 4;

    /** Identifies the type used to store pixel values. */
    public static final int DOUBLE = 5;
    
    /** Type of the pixel data. */
    protected PixelsType pixelsType;
    
    /** The pixels data backing buffer. */
    protected ByteBuffer data;
    
    /** If the data is signed. */
    protected boolean isSigned;
    
    /** If the data is floating point. */
    protected boolean isFloat;
    
    /** The pixels type as it would be represented in Java. */
    protected int javaType;
    
    /** The number of bytes per pixel. */
    protected int bytesPerPixel;
    
    /** The minimum pixel value for the pixels type of the pixel data. */
    protected double minimum;
    
    /** The maximum pixel value for the pixels type of the pixel data. */
    protected double maximum;
    
	/**
	 * Default constructor.
	 * 
	 * @param pixelsType The pixels type.
	 * @param data The raw pixel data.
	 */
	public PixelData(PixelsType pixelsType, ByteBuffer data)
	{
		this.data = data;
		this.pixelsType = pixelsType;
		bytesPerPixel = bytesPerPixel();
		String value = pixelsType.getValue();
		if (value.equals("int8")) {
		    isSigned = true;
		    isFloat = false;
		    javaType = BYTE;
		    minimum = Integer.MIN_VALUE;
		    maximum = Integer.MAX_VALUE;
		} else if (value.equals("uint8")) {
		    isSigned = false;
		    isFloat = false;
		    javaType = BYTE;
		    minimum = 0;
		    maximum = 255;
		} else if (value.equals("int16")) {
		    isSigned = true;
		    isFloat = false;
		    javaType = SHORT;
		    minimum = Short.MIN_VALUE;
		    maximum = Short.MAX_VALUE;
		} else if (value.equals("uint16")) {
		    isSigned = false;
		    isFloat = false;
		    javaType = SHORT;
		    minimum = 0;
		    maximum = 65535;
		} else if (value.equals("int32")) {
		    isSigned = true;
		    isFloat = false;
		    javaType = INT;
		    minimum = Integer.MIN_VALUE;
		    maximum = Integer.MAX_VALUE;
		} else if (value.equals("uint32")) {
		    isSigned = false;
		    isFloat = false;
		    javaType = INT;
		    minimum = 0;
		    maximum = 4294967295L;
		} else if (value.equals("float")) {
		    isSigned = true;
		    isFloat = true;
		    javaType = FLOAT;
		    minimum = Float.MIN_VALUE;
		    maximum = Float.MAX_VALUE;
		} else if (value.equals("double")) {
		    isSigned = true;
		    isFloat = true;
		    javaType = DOUBLE;
		    minimum = Double.MIN_VALUE;
		    maximum = Double.MAX_VALUE;
		} else {
	          throw new IllegalArgumentException(
	                    "Unknown pixel type: " + pixelsType.getValue());
		}
	}

    /**
     * Returns whether or not the pixel data type is is one of the elements in 
     * an array.
     * 
     * @param strings The strings for which you want to check against.
     * @return See above.
     */
    public boolean in(String[] strings) {
        String typeAsString = pixelsType.getValue();
        for (int i = 0; i < strings.length; i++) {
            if (typeAsString.equals(strings[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of byte per pixel for the pixel data.
     * 
     * @return See above.
     */
    public int bytesPerPixel()
    {
    	return PixelsService.getBitDepth(pixelsType) / 8;
    }

    /**
     * Returns the Java type that has the same byte width of the pixel data.
     * 
     * @return See above.
     */
    public int javaType()
    {
        return javaType;
    }

    /**
     * Returns whether or not the data is signed.
     * 
     * @return See above.
     */
    public boolean isSigned()
    {
        return isSigned;
    }
    
    /**
     * Returns whether or not the data is floating point.
     * 
     * @return See above.
     */
    public boolean isFloat()
    {
        return isFloat;
    }
    
    
    /**
     * Returns the minimum pixel value this pixel data supports.
     * 
     * @return See above.
     */
    public double getMinimum()
    {
        return minimum;
    }
    
    /**
     * Returns the minimum pixel value this pixel data supports.
     * 
     * @return See above.
     */
    public double getMaximum()
    {
        return maximum;
    }
    
    /**
     * Sets the pixel intensity value of the pixel at a given offset within
     * the backing buffer. This method takes into account bytes per pixel. 
     * 
     * @param offset The relative offset (taking into account the number of 
     * bytes per pixel) within the backing buffer.
     * @param value Pixel value to set.
     */
    public void setPixelValue(int offset, double value)
    {
        setPixelValueDirect(offset * bytesPerPixel, value);
    }
    
    /**
     * Sets the pixel intensity value of the pixel at a given offset within
     * the backing buffer. This method does not take into account bytes per
     * pixel.
     * 
     * @param offset The absolute offset within the backing buffer.
     * @param value Pixel value to set.
     */
    public void setPixelValueDirect(int offset, double value)
    {
        switch (javaType)
        {
            case BYTE:
                data.put(offset, (byte) value);
                break;
            case SHORT:
                data.putShort(offset, (short) value);
                break;
            case INT:
                data.putInt(offset, (int) value);
                break;
            case FLOAT:
                data.putFloat(offset, (float) value);
                break;
            case DOUBLE:
                data.putDouble(offset, value);
                break;
        }
    }
    
    /**
     * Returns the pixel intensity value of the pixel at a given offset within
     * the backing buffer. This method takes into account bytes per pixel. 
     * 
     * @param offset The relative offset (taking into account the number of 
     * bytes per pixel) within the backing buffer.
     * @return The intensity value.
     */
    public double getPixelValue(int offset)
    {
    	return getPixelValueDirect(offset * bytesPerPixel);
    }
    
    /**
     * Returns the pixel intensity value of the pixel at a given offset within
     * the backing buffer. This method does not take into account bytes per
     * pixel.
     * 
     * @param offset The absolute offset within the backing buffer.
     * @return The intensity value.
     */
    public double getPixelValueDirect(int offset)
    {
    	if (isSigned()) {
    		switch (javaType)
        	{
	        	case BYTE:
	        		return data.get(offset);
	        	case SHORT:
	        		return data.getShort(offset);
	        	case INT:
	        		return data.getInt(offset);
	        	case FLOAT:
	        		return data.getFloat(offset);
	        	case DOUBLE:
	        		return data.getDouble(offset);
        	}
    	} else {
    		switch (javaType)
        	{
	        	case BYTE:
	        		return (short) (data.get(offset) & 0xFF);
	        	case SHORT:
	        		return data.getShort(offset) & 0xFFFF;
	        	case INT:
	        		return data.getInt(offset) & 0xFFFFFFFFL;
	        	case FLOAT:
	        		return data.getFloat(offset);
	        	case DOUBLE:
	        		return data.getDouble(offset);
        	}
    	}
    	throw new RuntimeException("Unknown pixel type.");
    }
    
    /**
     * Returns the backing buffer for the pixel data.
     * 
     * @return See above.
     */
    public ByteBuffer getData()
    {
    	return data;
    }
    
    /**
     * Returns the byte order of the backing buffer.
     * 
     * @return See above.
     */
    public ByteOrder getOrder()
    {
    	return data.order();
    }
    
    /**
     * Set the byte order of the backing buffer.
     * 
     * @param order The byte order.
     */
    public void setOrder(ByteOrder order)
    {
    	data.order(order);
    }
}
