/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import loci.common.services.ServiceFactory;
import loci.formats.services.JAIIIOService;
import ome.conditions.MissingPyramidException;
import ome.model.core.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Basic {@link BackOff} implementation which attempts several writes of the
 * default block size on startup, and uses that as a scaling factor for all
 * subsequent calculations.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.1
 * @see ticket:5910
 */
public class SimpleBackOff implements BackOff {

    private static final int[] CODE_BLOCK = new int[] { 4, 4 };

    private static final int IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;

    private final static Log log = LogFactory.getLog(SimpleBackOff.class);

    private final JAIIIOService service;

    protected final double scalingFactor;

    protected final double warmUpFactor;

    protected final int sizeX, sizeY;

    protected final int count;

    public SimpleBackOff() {
        sizeX = 256;
        sizeY = 256;
        count = 10;
        try {
            ServiceFactory sf = new ServiceFactory();
            service = sf.getInstance(JAIIIOService.class);
            warmUpFactor = calculate(); // WARM-UP
            scalingFactor = calculate();
        } catch (Exception e) {
            log.error("Failed to create simpleBackOff", e);
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return count;
    }

    public double getScalingFactor() {
        return scalingFactor;
    }

    public double getWarmUpFactor() {
        return warmUpFactor;
    }

    public void throwMissingPyramidException(String msg, Pixels pixels) {
        throw new MissingPyramidException(msg, calculate(pixels),
                pixels.getId());
    }

    protected long calculate(Pixels pixels) {
        return (long) (scalingFactor * countTiles(pixels));
    }

    protected int countTiles(Pixels pixels) {
        final int[] count = new int[] { 0 };
        ome.io.nio.Utils.forEachTile(new TileLoopIteration() {

            public void run(int z, int c, int t, int x, int y, int tileWidth,
                    int tileHeight, int tileCount) {
                count[0]++;
            }
        }, pixels.getSizeX(), pixels.getSizeY(), pixels.getSizeZ(),
           pixels.getSizeC(), pixels.getSizeT(), sizeX, sizeY);
        return count[0];
    }

    protected double calculate() throws Exception {
        final String key = String.format("%s.%sX%s", getClass().getName(),
                sizeX, sizeY);

        StopWatch sw;
        BufferedImage image;
        ByteArrayOutputStream stream;
        long elapsed = 0;

        for (int i = 0; i < count; i++) {
            sw = new CommonsLogStopWatch(key);
            image = new BufferedImage(sizeX, sizeY, IMAGE_TYPE);
            stream = new ByteArrayOutputStream();
            service.writeImage(stream, image, false, CODE_BLOCK, 1.0);
            sw.stop();
            elapsed += sw.getElapsedTime();
        }

        return ((double) elapsed) / count;
    }

}
