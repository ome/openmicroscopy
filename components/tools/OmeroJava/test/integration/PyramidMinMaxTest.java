/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import omero.model.Image;
import omero.model.Pixels;
import omero.model.StatsInfo;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Collection of tests to import "big" images and check min/max values.
 *
 * @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
 *
 *         These tests use a simple single plane image.
 *
 *         These tests depend on PNG being imported using fs-lite and having a
 *         pyramid file created. If that changes then it will be necessary to
 *         change the import.
 */
public class PyramidMinMaxTest extends AbstractServerTest {

    /** The format tested here. */
    private static final String FORMAT = "png";

    /* Total wait time will be WAITS * INTERVAL milliseconds */
    /** Maximum number of intervals to wait for pyramid **/
    private static final int WAITS = 100;

    /** Wait time in milliseconds **/
    private static final long INTERVAL = 100L;

    /** The collection of files that have to be deleted. */
    private List<File> files;

    /**
     * Overridden to initialize the list.
     *
     * @see AbstractServerTest#setUp()
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        files = new ArrayList<File>();
    }

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
        Iterator<File> i = files.iterator();
        while (i.hasNext()) {
            i.next().delete();
        }
        files.clear();
    }

    /**
     * Import a <code>PNG</code> which generates a pyramid files with all
     * zeroes.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testForMinMaxAllZero() throws Exception {
        // Create a file, the default is all zeroes
        BufferedImage bi = new BufferedImage(ModelMockFactory.WIDTH,
                ModelMockFactory.HEIGHT, BufferedImage.TYPE_INT_RGB);
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        Pixels p = importAndWaitForPyramid(f, FORMAT);
        assertMinMaxOnAllChannels(p, 0.0, 0.0);
    }

    /**
     * Import a <code>PNG</code> which generates a pyramid files with all
     * FFFFFF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testForMinMaxAll255() throws Exception {
        // Create a png file, with all RGB values FFFFFF
        BufferedImage bi = new BufferedImage(ModelMockFactory.WIDTH,
                ModelMockFactory.HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ModelMockFactory.WIDTH; x++) {
            for (int y = 0; y < ModelMockFactory.HEIGHT; y++) {
                bi.setRGB(x, y, Integer.valueOf("FFFFFF", 16));
            }
        }
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        Pixels p = importAndWaitForPyramid(f, FORMAT);
        assertMinMaxOnAllChannels(p, 255.0, 255.0);
    }

    /**
     * Import a <code>PNG</code> which generates a pyramid files with some 0 and
     * some FFFFFF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testForMinMaxHalfZeroAndHalf255() throws Exception {
        // Create a png file, with half RGB values FFFFFF
        BufferedImage bi = new BufferedImage(ModelMockFactory.WIDTH,
                ModelMockFactory.HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ModelMockFactory.WIDTH; x += 2) {
            for (int y = 0; y < ModelMockFactory.HEIGHT; y++) {
                bi.setRGB(x, y, Integer.valueOf("FFFFFF", 16));
            }
        }
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        Pixels p = importAndWaitForPyramid(f, FORMAT);
        assertMinMaxOnAllChannels(p, 0.0, 255.0);
    }

    /**
     * Import a <code>PNG</code> which generates a pyramid files with most 0 and
     * one FFFFFF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testForMinMaxAllZeroWithFirst255() throws Exception {
        // Create a png file, with first RGB value FFFFFF
        BufferedImage bi = new BufferedImage(ModelMockFactory.WIDTH,
                ModelMockFactory.HEIGHT, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, Integer.valueOf("FFFFFF", 16));
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        Pixels p = importAndWaitForPyramid(f, FORMAT);
        assertMinMaxOnAllChannels(p, 0.0, 255.0);
    }

    /**
     * Import a <code>PNG</code> which generates a pyramid files with most 0 and
     * one FFFFFF.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testForMinMaxAllZeroWithLast255() throws Exception {
        // Create a png file, with last RGB value FFFFFF
        BufferedImage bi = new BufferedImage(ModelMockFactory.WIDTH,
                ModelMockFactory.HEIGHT, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(ModelMockFactory.WIDTH - 1, ModelMockFactory.HEIGHT - 1,
                Integer.valueOf("FFFFFF", 16));
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        Pixels p = importAndWaitForPyramid(f, FORMAT);
        assertMinMaxOnAllChannels(p, 0.0, 255.0);
    }

    /**
     * Check the min and max on all three channels
     */
    private void assertMinMaxOnAllChannels(Pixels p, double min, double max) {
        for (int c = 0; c < 3; c++) {
            assert (p.getChannel(c).getStatsInfo().getGlobalMin().getValue() == min);
            assert (p.getChannel(c).getStatsInfo().getGlobalMax().getValue() == max);
        }
    }

    /**
     * Create an image file from a BufferedImage of the given format.
     */
    private File createImageFileWithBufferedImage(BufferedImage bi,
            String format) throws Exception {
        File f = File.createTempFile("testImage", "." + format);
        Iterator writers = ImageIO.getImageWritersByFormatName(format);
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        writer.write(bi);
        ios.close();
        return f;
    }

    /**
     * Import an image file of the given format then wait for a pyramid file to
     * be generated by checking if stats exists.
     */
    private Pixels importAndWaitForPyramid(File f, String format)
            throws Exception {
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, FORMAT);
        } catch (Throwable e) {
            Assert.fail("Cannot import image file: " + f.getAbsolutePath()
                    + " Reason: " + e.toString());
        }
        // Wait for a pyramid to be built (stats will be not null)
        Pixels p = factory.getPixelsService().retrievePixDescription(
                pixels.get(0).getId().getValue());
        StatsInfo stats = p.getChannel(0).getStatsInfo();
        int waits = 0;
        while (stats == null && waits < WAITS) {
            Thread.sleep(INTERVAL);
            waits++;
            p = factory.getPixelsService().retrievePixDescription(
                    pixels.get(0).getId().getValue());
            stats = p.getChannel(0).getStatsInfo();
        }
        if (stats == null) {
            Assert.fail("No pyramid after " + WAITS * INTERVAL / 1000.0 + " seconds");
        }
        return p;
    }

    /**
     * Test the creation of tiles using RPSTileLoop.
     * @throws Exception
     */
    @Test
    public void testRPSTileloop() throws Exception {
        int sizeX = 256;
        int sizeY = 256;
        int sizeZ = 2;
        int sizeT = 3;
        int sizeC = 4;
        Image image = mmFactory.createImage(sizeX, sizeY, sizeZ, sizeT,
                sizeC);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
     // first write to the image
        omero.util.RPSTileLoop loop = new omero.util.RPSTileLoop(
                client.getSession(), pixels);
        loop.forEachTile(sizeX, sizeY, new omero.util.TileLoopIteration() {
            public void run(omero.util.TileData data, int z, int c, int t,
                    int x, int y, int tileWidth, int tileHeight, int tileCount) {
                data.setTile(new byte[tileWidth * tileHeight * 8], z, c, t, x,
                        y, tileWidth, tileHeight);
            }
        });
        // This block will change the updateEvent on the pixels
        // therefore we're going to reload the pixels.

        image.setPixels(0, loop.getPixels());
    }
}
