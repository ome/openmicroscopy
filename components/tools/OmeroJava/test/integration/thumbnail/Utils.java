/*
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
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

import omero.ServerError;
import omero.api.ThumbnailStorePrx;

import org.testng.Assert;


/**
 * Collections of tests for the <code>ThumbnailStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:donald@lifesci.dundee.ac.uk"
 * >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
class Utils {

    public static byte[] getThumbnail(ThumbnailStorePrx svc) throws ServerError {
        final int sizeX = 96;
        final int sizeY = 96;

        // Get thumbnail
        byte[] values = svc.getThumbnail(
                omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY));
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);

        // Return the bytes
        return values;
    }

    public static byte[] getThumbnailWithoutDefault(ThumbnailStorePrx svc) throws ServerError {
        final int sizeX = 96;
        final int sizeY = 96;

        // Get thumbnail
        byte[] values = svc.getThumbnailWithoutDefault(
                omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY));
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
}
