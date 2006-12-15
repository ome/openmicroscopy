/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.data;

import java.io.IOException;
import ome.api.IPixels;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;

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
        if (in(type, new String[] { "int8", "uint8" })) {
            return 1;
        } else if (in(type, new String[] { "int16", "uint16" })) {
            return 2;
        } else if (in(type, new String[] { "int32", "uint32", "float" })) {
            return 4;
        } else if (type.getValue().equals("double")) {
            return 8;
        } else {
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
        }
    }

    /**
     * A static helper method to retrieve Java type mappings.
     * 
     * @param type
     *            The pixels type for which you wish to know the mapped Java
     *            type.
     * @return The Java type as an enumerated integer.
     */
    static int javaType(PixelsType type) {
        if (in(type, new String[] { "int8", "uint8" })) {
            return BYTE;
        } else if (in(type, new String[] { "int16", "uint16" })) {
            return SHORT;
        } else if (in(type, new String[] { "int32", "uint32" })) {
            return INT;
        } else if (type.getValue().equals("float")) {
            return FLOAT;
        } else if (type.getValue().equals("double")) {
            return DOUBLE;
        } else {
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
        }
    }

    /**
     * A static helper method to retrieve pixel byte signage.
     * 
     * @param type
     *            The pixels type for which you want to know the byte width.
     * @return The number of bytes per pixel value.
     */
    static boolean isTypeSigned(PixelsType type) {
        if (in(type, new String[] { "uint8", "uint16", "uint32" })) {
            return false;
        } else if (in(type, new String[] { "int8", "int16", "int32", "float",
                "double" })) {
            return true;
        } else {
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
        }
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

        try {
            switch (planeDef.getSlice()) {
                case PlaneDef.XY:
                    return new XYPlane(planeDef, pixels, buffer.getPlane(z, c,
                            t));
                case PlaneDef.XZ:
                    return new XZPlane(planeDef, pixels, buffer.getStack(c, t));
                case PlaneDef.ZY:
                    return new ZYPlane(planeDef, pixels, buffer.getStack(c, t));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Helper method to retrieve a RenderingModel enumeration from the database.
     * 
     * @param value
     *            The enumeration value.
     * @return A rendering model enumeration object.
     */
    public static RenderingModel getRenderingModel(IPixels iPixels, String value) {
        return iPixels.getEnumeration(RenderingModel.class,
                value);
    }
}
