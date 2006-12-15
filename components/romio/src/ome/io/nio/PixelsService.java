/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ome.model.core.Pixels;

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

    public PixelBuffer createPixelBuffer(Pixels pixels) throws IOException {
        PixelBuffer pixbuf = new PixelBuffer(getPixelsPath(pixels.getId()),
                pixels);
        initPixelBuffer(pixbuf);

        return pixbuf;
    }

    public PixelBuffer getPixelBuffer(Pixels pixels) {
        return new PixelBuffer(getPixelsPath(pixels.getId()), pixels);
    }

    private void initPixelBuffer(PixelBuffer pixbuf) throws IOException {
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
}
