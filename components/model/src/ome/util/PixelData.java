/*
 * ome.util.PixelData
 *
 *   Copyright 2007-2014 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

import loci.formats.FormatTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a block of pixel data.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:chris@glencoesoftware.com">chris@glencoesoftware.com</a>
 * @author m.t.b.carroll@dundee.ac.uk
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class PixelData
{
    public static final String CONFIG_KEY = "omero.pixeldata.dispose";

    private static final Logger LOG = LoggerFactory.getLogger(PixelData.class);

    /* set to sun.nio.ch.DirectBuffer.cleaner() only if it is both available and to be invoked, otherwise null */
    private static final Method DIRECT_BUFFER_CLEANER;

    /* the clean() method of DIRECT_BUFFER_CLEANER.cleaner() */
    private static final Method DIRECT_BUFFER_CLEANER_CLEAN;

    static {
        final String configValue = System.getProperties().getProperty(CONFIG_KEY);
        final Boolean dispose;

        /* parse config value and log accordingly */
        if ("true".equalsIgnoreCase(configValue)) {
            dispose = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(configValue)) {
            dispose = Boolean.FALSE;
        } else {
            dispose = null;
        }
        if (dispose == null) {
            LOG.warn("{} cannot be set to invalid value {}, must be true or false", CONFIG_KEY, configValue);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} set to {}", CONFIG_KEY, dispose.toString());
            }
        }

        if (Boolean.TRUE.equals(dispose)) {
            Method cleanerGetter = null;
            Method cleaner = null;
            try {
                /* find cleaner() on DirectBuffer, if the class can be loaded at all */
                final Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                FIND_CLEANER_CLEAN:
                for (final Method directBufferMethod : directBufferClass.getMethods()) {
                    if (!directBufferMethod.isBridge() && !directBufferMethod.isSynthetic() &&
                            "cleaner".equals(directBufferMethod.getName()) &&
                            directBufferMethod.getParameterTypes().length == 0) {
                        /* find void clean() on cleaner() */
                        final Class<?> cleanerClass = directBufferMethod.getReturnType();
                        for (final Method cleanerMethod : cleanerClass.getMethods()) {
                            if (!cleanerMethod.isBridge() && !cleanerMethod.isSynthetic() &&
                                    "clean".equals(cleanerMethod.getName()) &&
                                    cleanerMethod.getReturnType() == void.class &&
                                    cleanerMethod.getParameterTypes().length == 0) {
                                cleanerGetter = directBufferMethod;
                                cleaner = cleanerMethod;
                                break FIND_CLEANER_CLEAN;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                /* logged below */
            }
            if (cleanerGetter == null) {
                LOG.warn("{} set to true, but cannot be actioned in the JVM", CONFIG_KEY);
                DIRECT_BUFFER_CLEANER = null;
                DIRECT_BUFFER_CLEANER_CLEAN = null;
            } else {
                DIRECT_BUFFER_CLEANER = cleanerGetter;
                DIRECT_BUFFER_CLEANER_CLEAN = cleaner;
            }
        } else {
            DIRECT_BUFFER_CLEANER = null;
            DIRECT_BUFFER_CLEANER_CLEAN = null;
        }

    }
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

    /** Identifies the type used to store pixel values. */
    public static final int BIT = 6;

    /** Type of the pixel data. */
    protected String pixelsType;

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
     * @param pixelsType The OME pixels type.
     * @param data The raw pixel data.
     */
    public PixelData(String pixelsType, ByteBuffer data)
    {
        this.data = data;
        this.pixelsType = pixelsType;
        bytesPerPixel = bytesPerPixel();
        int type = FormatTools.pixelTypeFromString(pixelsType);
        long[] values = FormatTools.defaultMinMax(type);
        isSigned = FormatTools.isSigned(type);
        isFloat = FormatTools.isFloatingPoint(type);
        minimum = values[0];
        maximum = values[1];
        switch(type) {
            case FormatTools.INT8:
            case FormatTools.UINT8:
                javaType = BYTE;
                break;
            case FormatTools.INT16:
            case FormatTools.UINT16:
                javaType = SHORT;
                break;
            case FormatTools.INT32:
            case FormatTools.UINT32:
                javaType = INT;
                break;
            case FormatTools.FLOAT:
                javaType = FLOAT;
                break;
            case FormatTools.DOUBLE:
                javaType = DOUBLE;
                break;
            case FormatTools.BIT:
                javaType = BIT;
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
        for (int i = 0; i < strings.length; i++) {
            if (pixelsType.equals(strings[i])) {
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
        int type = FormatTools.pixelTypeFromString(pixelsType);
        return FormatTools.getBytesPerPixel(type);
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
     * Returns the Java type that has the same byte width of the pixel data.
     *
     * @return See above.
     */
    public int javaType()
    {
        return javaType;
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
            case BIT:
                int byteOffset = offset / 8;
                byte x = data.get(byteOffset);
                if (value == 0)
                {
                    data.put(byteOffset, (byte) (x & ~(1 << (7 - (offset % 8)))));
                    break;
                }
                data.put(byteOffset, (byte) (x | 1 << (7 - (offset % 8))));
                break;
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
                case BIT:
                    return data.get(offset / 8) >> (7 - (offset % 8)) & 1;
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

    /**
     * Returns the pixel count of this block of pixel data.
     *
     * @return See above.
     */
    public int size()
    {
        return data.capacity() / bytesPerPixel;
    }

    /**
     * Retrieves the bit width of a particular <code>PixelsType</code>.
     *
     * @param type
     *            a pixel type.
     * @return width of a single pixel value in bits.
     */
    public static int getBitDepth(String type) {
        int value = FormatTools.pixelTypeFromString(type);
        switch(value) {
            case FormatTools.INT8:
            case FormatTools.UINT8:
                return 8;
            case FormatTools.INT16:
            case FormatTools.UINT16:
                return 16;
            case FormatTools.INT32:
            case FormatTools.UINT32:
            case FormatTools.FLOAT:
                return 32;
            case FormatTools.DOUBLE:
                return 64;
            case FormatTools.BIT:
                return 1;
        }
        throw new RuntimeException("Pixels type '" + type
                + "' unsupported by nio.");
    }

    /**
     * Attempt to free up any native memory resources associated with the data buffer.
     * This is a temporary workaround hoped to ameliorate trac ticket #11250.
     * This {@link PixelData} instance <em>must not</em> be accessed by any thread after this method is called.
     * If not called, the resources should eventually be freed anyway by garbage collection and finalization.
     */
    public void dispose() {
        if (DIRECT_BUFFER_CLEANER != null && this.data != null &&
                DIRECT_BUFFER_CLEANER.getDeclaringClass().isAssignableFrom(this.data.getClass())) {
            try {
                final Object cleaner = DIRECT_BUFFER_CLEANER.invoke(this.data);
                if (cleaner != null) {
                    DIRECT_BUFFER_CLEANER_CLEAN.invoke(cleaner);
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("DirectBuffer disposal failed", e);
                }
            }
            this.data = null;
        }
    }
}
