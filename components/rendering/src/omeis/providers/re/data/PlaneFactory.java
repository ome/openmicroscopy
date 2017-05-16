/*
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.data;

import java.io.IOException;

import loci.formats.FormatTools;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

/**
 * 
 * Encapsulates access to the image raw data. Contains the logic to interpret a
 * linear byte array as a 5D array. Knows how to extract a 2D-plane from the 5D
 * array, but delegates to the specified 2D-Plane the retrieval of pixel values.
 * 
 * @author Chris Allan <callan@blackcat.ca>
 * 
 */
public class PlaneFactory {

    /** 
     * Identifies the <i>Bit</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     BIT = ome.model.enums.PixelsType.VALUE_BIT;

    /** 
     * Identifies the <i>INT8</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     INT8 = ome.model.enums.PixelsType.VALUE_INT8;

    /** 
     * Identifies the <i>INT16</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     INT16 = ome.model.enums.PixelsType.VALUE_INT16;

    /** 
     * Identifies the <i>INT32</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     INT32 = ome.model.enums.PixelsType.VALUE_INT32;

    /** 
     * Identifies the <i>UINT8</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     UINT8 = ome.model.enums.PixelsType.VALUE_UINT8;

    /** 
     * Identifies the <i>UINT16</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     UINT16 = ome.model.enums.PixelsType.VALUE_UINT16;

    /** 
     * Identifies the <i>UINT32</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     UINT32 = ome.model.enums.PixelsType.VALUE_UINT32;

    /** 
     * Identifies the <i>FLOAT</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     FLOAT_TYPE = ome.model.enums.PixelsType.VALUE_FLOAT;

    /** 
     * Identifies the <i>DOUBLE</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    @Deprecated
    public static final String     DOUBLE_TYPE = ome.model.enums.PixelsType.VALUE_DOUBLE;

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

    /**
     * Returns the pixels type as a int.
     *
     * @param type The type.
     * @return See above.
     */
    private static int getPixelType(PixelsType type)
    {
        if (type == null) return -1;
        return FormatTools.pixelTypeFromString(type.getValue());
    }

    /**
     * A static helper method to check if a type is one of the elements in an
     * array.
     *
     * @param type
     *            A pixels type enumeration.
     * @param strings
     *            The strings for which you want to check against.
     * @return True on successful match and false on failure to match.
     */
    public static boolean in(PixelsType type, String[] strings) {
        String typeAsString = type.getValue();
        for (int i = 0; i < strings.length; i++) {
            if (typeAsString.equals(strings[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * A static helper method to retrieve pixel byte widths.
     *
     * @param type
     *            The pixels type for which you want to know the byte width.
     * @return The number of bytes per pixel value.
     */
    static int bytesPerPixel(PixelsType type) {
        return FormatTools.getBytesPerPixel(getPixelType(type));
    }

    /**
     * A static helper method to retrieve pixel byte signage.
     *
     * @param type
     *            The pixels type for which you want to know the byte width.
     * @return The number of bytes per pixel value.
     */
    public static boolean isTypeSigned(PixelsType type) {
        return FormatTools.isSigned(getPixelType(type));
    }

    /**
     * Factory method to fetch plane data and create an object to access it.
     *
     * @param planeDef
     *            Defines the plane to be retrieved. Must not be null.
     * @param channel
     *            The wavelength at which data is to be fetched.
     * @param pixels
     *            The pixels from which the data is to be fetched.
     * @param buffer
     *            The pixels buffer from which the data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     */
    public static Plane2D createPlane(PlaneDef planeDef, int channel,
            Pixels pixels, PixelBuffer buffer) {
        if (planeDef == null) {
            throw new NullPointerException("Expecting not null planeDef");
        } else if (pixels == null) {
            throw new NullPointerException("Expecting not null pixels");
        } else if (buffer == null) {
            throw new NullPointerException("Expecting not null buffer");
        }

        Integer z = Integer.valueOf(planeDef.getZ());
        Integer c = Integer.valueOf(channel);
        Integer t = Integer.valueOf(planeDef.getT());
        Integer stride = planeDef.getStride();
        try {
            RegionDef region = planeDef.getRegion();
            if (region != null) {
                switch (planeDef.getSlice()) {
                    case PlaneDef.XY:
                        return new Plane2D(planeDef, pixels, buffer.getTile(
                                z, c, t, region.getX(), region.getY(),
                                region.getWidth(), region.getHeight()));
                    case PlaneDef.XZ: //TODO
                        return new Plane2D(planeDef, pixels,
                                buffer.getStack(c, t));
                    case PlaneDef.ZY: //TODO
                        return new Plane2D(planeDef, pixels,
                                buffer.getStack(c, t));
                }
            } else {
                switch (planeDef.getSlice()) {
                    case PlaneDef.XY:
                        if (stride == null || stride <= 0)
                            return new Plane2D(planeDef, pixels,
                                    buffer.getPlane(z, c, t));
                        return new Plane2D(planeDef, pixels,
                                buffer.getPlaneRegion(0, 0,
                                        pixels.getSizeX(),
                                        pixels.getSizeY(), z, c, t,
                                        stride));
                    case PlaneDef.XZ:
                        return new Plane2D(planeDef, pixels,
                                buffer.getStack(c, t));
                    case PlaneDef.ZY:
                        return new Plane2D(planeDef, pixels,
                                buffer.getStack(c, t));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
