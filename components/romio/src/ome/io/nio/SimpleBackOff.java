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
import loci.formats.codec.JPEG2000CodecOptions;
import loci.formats.services.JAIIIOService;
import ome.conditions.MissingPyramidException;
import ome.model.core.Pixels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

/**
 * Basic {@link BackOff} implementation which attempts several writes of the
 * default block size on startup, and uses that as a scaling factor for all
 * subsequent calculations.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.1
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/5910">ticket 5910</a>
 */
public class SimpleBackOff implements BackOff {

    private static final int[] CODE_BLOCK = new int[] { 4, 4 };

    private static final int IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;

    private final static Logger log = LoggerFactory.getLogger(SimpleBackOff.class);

    private final JAIIIOService service;

    protected final double scalingFactor;

    protected final double warmUpFactor;

    protected final int count;

    protected final TileSizes sizes;

    public SimpleBackOff() {
        this(new ConfiguredTileSizes());
    }

    public SimpleBackOff(TileSizes sizes) {
        this.sizes = sizes;
        this.count = 10;
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
           pixels.getSizeC(), pixels.getSizeT(),
           sizes.getTileWidth(), sizes.getTileHeight());
        return count[0];
    }

    protected double calculate() throws Exception {
        final String key = String.format("%s.%sX%s", getClass().getName(),
                sizes.getTileWidth(), sizes.getTileHeight());

        StopWatch sw;
        BufferedImage image;
        ByteArrayOutputStream stream;
        long elapsed = 0;

        for (int i = 0; i < count; i++) {
            sw = new Slf4JStopWatch(key);
            JPEG2000CodecOptions options = JPEG2000CodecOptions.getDefaultOptions();
            options.lossless = false;
            options.codeBlockSize = CODE_BLOCK;
            options.quality = 1.0f;
            image = new BufferedImage(sizes.getTileWidth(), sizes.getTileHeight(), IMAGE_TYPE);
            stream = new ByteArrayOutputStream();
            service.writeImage(stream, image, options);
            sw.stop();
            elapsed += sw.getElapsedTime();
        }

        return ((double) elapsed) / count;
    }

    @Override
    public String toString() {
        return String.format("%s(factor=%s)",
                getClass().getName(), scalingFactor);
    }
}
