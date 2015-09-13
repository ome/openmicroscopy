/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.keywords;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.NoSuchElementException;

import javax.swing.JPanel;

import abbot.finder.BasicFinder;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Robot Framework SwingLibrary keyword library offering methods for checking thumbnails.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class ThumbnailCheckLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /**
     * An iterator over the integer pixel values of a rendered image,
     * first increasing <em>x</em>, then <em>y</em> when <em>x</em> wraps back to 0.
     * This is written so as to be scalable over arbitrary image sizes
     * and to not cause heap allocations during the iteration.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 4.4.9
     */
    private static class IteratorIntPixel {
        final Raster raster;
        final int width;
        final int height;
        final int[] pixel = new int[1];
        int x = 0;
        int y = 0;

        /**
         * Create a new pixel iterator for the given image.
         * The image is assumed to be of a type that packs data for each pixel into an <code>int</code>.
         * @param image the image over whose pixels to iterate
         */
        IteratorIntPixel(RenderedImage image) {
            this.raster = image.getData();
            this.width = image.getWidth();
            this.height = image.getHeight();
        }

        /**
         * @return if any pixels remain to be read with {@link #next()}
         */
        boolean hasNext() {
            return y < height;
        }

        /**
         * @return the next pixel
         * @throws NoSuchElementException if no more pixels remain
         */
        int next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            raster.getDataElements(x, y, pixel);
            if (++x == width) {
                x = 0;
                ++y;
            }
            return pixel[0];
        }
    }

    /**
     * Find the thumbnail <code>Component</code> in the AWT hierarchy.
     * @param panelType if the thumbnail should be the whole <code>"image node"</code> or just its <code>"thumbnail"</code> canvas
     * @param imageFilename the name of the image whose thumbnail is to be rasterized
     * @return the AWT <code>Component</code> for the thumbnail
     * @throws MultipleComponentsFoundException if multiple thumbnails are for the given image name
     * @throws ComponentNotFoundException if no thumbnails are for the given image name
     */
    private static Component componentFinder(final String panelType, final String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        return new BasicFinder().find(new Matcher() {
            private final String soughtName = panelType + " for " + imageFilename;
            public boolean matches(Component component) {
                return component instanceof JPanel && this.soughtName.equals(component.getName());
            }});
    }

    /**
     * Convert the thumbnail for the image of the given filename into rasterized pixel data.
     * Each pixel is represented by an <code>int</code>.
     * @param panelType if the thumbnail should be the whole <code>"image node"</code> or just its <code>"thumbnail"</code> canvas
     * @param imageFilename the name of the image whose thumbnail is to be rasterized
     * @return the image on the thumbnail
     * @throws MultipleComponentsFoundException if multiple thumbnails are for the given image name
     * @throws ComponentNotFoundException if no thumbnails are for the given image name
     */
    private static RenderedImage captureImage(final String panelType, final String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final JPanel thumbnail = (JPanel) componentFinder(panelType, imageFilename);
        final int width = thumbnail.getWidth();
        final int height = thumbnail.getHeight();
        final BufferedImage image = new BufferedImage(width, height, StaticFieldLibrary.IMAGE_TYPE);
        final Graphics2D graphics = image.createGraphics();
        if (graphics == null) {
            throw new RuntimeException("thumbnail is not displayable");
        }
        thumbnail.paint(graphics);
        graphics.dispose();
        return image;
    }

    /**
     * <table>
     *   <td>Get Thumbnail Border Color</td>
     *   <td>name of image whose thumbnail is queried</td>
     * </table>
     * @param imageFilename the name of the image
     * @return the color of the thumbnail's corner pixel
     * @throws MultipleComponentsFoundException if multiple thumbnails exist for the given name
     * @throws ComponentNotFoundException if no thumbnails exist for the given name
     */
    public String getThumbnailBorderColor(String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final RenderedImage image = captureImage("image node", imageFilename);
        final IteratorIntPixel pixels = new IteratorIntPixel(image);
        if (!pixels.hasNext()) {
            throw new RuntimeException("image node has no pixels");
        }
        return Integer.toHexString(pixels.next());
    }

    /**
     * <table>
     *   <td>Is Thumbnail Monochromatic</td>
     *   <td>name of image whose thumbnail is queried</td>
     * </table>
     * @param imageFilename the name of the image
     * @return if the image's thumbnail canvas is solidly one color
     * @throws MultipleComponentsFoundException if multiple thumbnails exist for the given name
     * @throws ComponentNotFoundException if no thumbnails exist for the given name
     */
    public boolean isThumbnailMonochromatic(String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final RenderedImage image = captureImage("thumbnail", imageFilename);
        final IteratorIntPixel pixels = new IteratorIntPixel(image);
        if (!pixels.hasNext()) {
            throw new RuntimeException("thumbnail image has no pixels");
        }
        final int oneColor = pixels.next();
        while (pixels.hasNext()) {
            if (pixels.next() != oneColor) {
                return false;
            }
        }
        return true;
    }

    /**
     * <table>
     *   <td>Get Thumbnail Hash</td>
     *   <td>name of image whose thumbnail is queried</td>
     * </table>
     * @param imageFilename the name of the image
     * @return the hash of the thumbnail canvas image
     * @throws MultipleComponentsFoundException if multiple thumbnails exist for the given name
     * @throws ComponentNotFoundException if no thumbnails exist for the given name
     */
    public String getThumbnailHash(String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final RenderedImage image = captureImage("thumbnail", imageFilename);
        final IteratorIntPixel pixels = new IteratorIntPixel(image);
        final Hasher hasher = Hashing.goodFastHash(128).newHasher();
        while (pixels.hasNext()) {
            hasher.putInt(pixels.next());
        }
        return hasher.hash().toString();
    }

    /**
     * <table>
     *   <td>Get Name Of Thumbnail For Image</td>
     *   <td>name of image whose thumbnail is queried</td>
     * </table>
     * @param imageFilename the name of the image
     * @return the return value of the corresponding <code>ThumbnailCanvas.getName()</code>
     * @throws MultipleComponentsFoundException if multiple thumbnails exist for the given name
     * @throws ComponentNotFoundException if no thumbnails exist for the given name
     */
    public String getNameOfThumbnailForImage(final String imageFilename)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        return componentFinder("thumbnail", imageFilename).getName();
    }
}
