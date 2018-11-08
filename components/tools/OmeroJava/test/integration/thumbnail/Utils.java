/*
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2018 University of Dundee. All rights reserved.
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
package integration.thumbnail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import omero.ServerError;
import omero.api.ThumbnailStorePrx;

import org.testng.Assert;


/**
 * Collections of tests for the <code>ThumbnailStore</code> service.
 *
 * @author Riad Gozim &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:r.gozim@dundee.ac.uk">r.gozim@dundee.ac.uk</a>
 * @since 5.4.10
 */
class Utils {

    static final int DEFAULT_SIZE_X = 96;

    static final int DEFAULT_SIZE_Y = 96;

    public static byte[] getThumbnail(ThumbnailStorePrx svc) throws ServerError {

        // Get thumbnail
        byte[] values = svc.getThumbnail(
                omero.rtypes.rint(DEFAULT_SIZE_X),
                omero.rtypes.rint(DEFAULT_SIZE_Y));
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);

        // Return the bytes
        return values;
    }

    public static byte[] getThumbnailWithoutDefault(ThumbnailStorePrx svc) throws ServerError {

        // Get thumbnail
        byte[] values = svc.getThumbnailWithoutDefault(
                omero.rtypes.rint(DEFAULT_SIZE_X),
                omero.rtypes.rint(DEFAULT_SIZE_Y));
        Assert.assertNotNull(values);

        // Return the bytes
        return values;
    }

    public static void setThumbnailStoreToPixels(ThumbnailStorePrx svc, long pixelsId) throws ServerError {
        if (!svc.setPixelsId(pixelsId)) {
            svc.resetDefaults();
            svc.setPixelsId(pixelsId);
        }
    }

    public static void checkSize(byte[] values, int sizeX, int sizeY) {
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);
        // Check width and height
        try(InputStream in = new ByteArrayInputStream(values)) {
            BufferedImage buf = ImageIO.read(in);
            Assert.assertEquals(sizeX, buf.getWidth());
            Assert.assertEquals(sizeX, buf.getHeight());
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert byte array", e);
        }
    }
}
