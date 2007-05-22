/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.FileOutputStream;
import java.io.IOException;

import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

/**
 * @author callan
 * 
 */
public class PixelsService extends AbstractFileSystemService {

    public PixelsService(String path) {
        super(path);
    }

    public static final int NULL_PLANE_SIZE = 64;

    public static final byte[] nullPlane = new byte[] { -128, 127, -128, 127,
            -128, 127, -128, 127, -128, 127, // 10
            -128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 20
            -128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 30
            -128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 40
            -128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 50
            -128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 60
            -128, 127, -128, 127 }; // 64

    public PixelBuffer createPixelBuffer(Pixels pixels) throws IOException
    {
        RomioPixelBuffer pixbuf =
        	new RomioPixelBuffer(getPixelsPath(pixels.getId()), pixels);
        initPixelBuffer(pixbuf);
        return pixbuf;
    }

    public PixelBuffer getPixelBuffer(Pixels pixels) {
        String path = getPixelsPath(pixels.getId());
        createSubpath(path);
        return new RomioPixelBuffer(path, pixels);
    }

    private void initPixelBuffer(RomioPixelBuffer pixbuf) throws IOException
    {
        String path = getPixelsPath(pixbuf.getId());
        createSubpath(path);
        byte[] padding = new byte[pixbuf.getPlaneSize() - NULL_PLANE_SIZE];
        FileOutputStream stream = new FileOutputStream(path);

        for (int z = 0; z < pixbuf.getSizeZ(); z++) {
            for (int c = 0; c < pixbuf.getSizeC(); c++) {
                for (int t = 0; t < pixbuf.getSizeT(); t++) {
                    stream.write(nullPlane);
                    stream.write(padding);
                }
            }
        }
    }
    
    /**
     * Retrieves the bit width of a particular <code>PixelsType</code>.
     * @param type a pixel type.
     * @return width of a single pixel value in bits.
     */
    public static int getBitDepth(PixelsType type) {
        if (type.getValue().equals("int8") || type.getValue().equals("uint8")) {
            return 8;
        } else if (type.getValue().equals("int16")
                || type.getValue().equals("uint16")) {
            return 16;
        } else if (type.getValue().equals("int32")
                || type.getValue().equals("uint32")
                || type.getValue().equals("float")) {
            return 32;
        } else if (type.getValue().equals("double")) {
            return 64;
        }

        throw new RuntimeException("Pixels type '" + type.getValue()
                + "' unsupported by nio.");
    }
}
