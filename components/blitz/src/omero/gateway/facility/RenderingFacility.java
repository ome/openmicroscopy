/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import omero.ServerError;
import omero.api.IPixelsPrx;
import omero.api.ThumbnailStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.RenderingDef;
import pojos.PixelsData;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class RenderingFacility extends Facility {

    /** The default width of a thumbnail. */
    private static final int THUMB_DEFAULT_WIDTH = 96;

    /** The default width of a thumbnail. */
    private static final int THUMB_DEFAULT_HEIGHT = 96;

    RenderingFacility(Gateway gateway) {
        super(gateway);
    }

    public BufferedImage getThumbnail(SecurityContext ctx, PixelsData pix)
            throws DSOutOfServiceException, DSAccessException {
        return getThumbnail(ctx, pix, -1, -1, -1, true);
    }

    public BufferedImage getThumbnail(SecurityContext ctx, PixelsData pix,
            boolean close) throws DSOutOfServiceException, DSAccessException {
        return getThumbnail(ctx, pix, -1, -1, -1, close);
    }

    public BufferedImage getThumbnail(SecurityContext ctx, PixelsData pix,
            int sizeX, int sizeY, long userID, boolean close)
            throws DSOutOfServiceException, DSAccessException {
        if (sizeX <= 0)
            sizeX = THUMB_DEFAULT_WIDTH;
        if (sizeY <= 0)
            sizeY = THUMB_DEFAULT_HEIGHT;

        BufferedImage result = null;
        ThumbnailStorePrx store = gateway.getThumbnailService(ctx);
        
        try {
            if (!store.setPixelsId(pix.getId())) {
                store.resetDefaults();
                store.setPixelsId(pix.getId());
            }

            if (userID >= 0) {
                store.setRenderingDefId(getRenderingDef(ctx, pix.getId(),
                        userID).getId().getValue());
            }
            result = createImage(store.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY)));
        } catch (ServerError e) {
            handleException(this, e, "Could not create thumbnail.");
        }
        finally {
            if (close) {
                gateway.closeService(ctx, store);
            }
        }

        return result;
    }

    /**
     * Retrieves the rendering settings for the specified pixels set.
     *
     * @param ctx
     *            The security context.
     * @param pixelsID
     *            The pixels ID.
     * @param userID
     *            The id of the user who set the rendering settings.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public RenderingDef getRenderingDef(SecurityContext ctx, long pixelsID,
            long userID) throws DSOutOfServiceException, DSAccessException {

        try {
            IPixelsPrx service = gateway.getPixelsService(ctx);
            return service.retrieveRndSettingsFor(pixelsID, userID);
        } catch (Exception e) {
            handleException(this, e, "Cannot retrieve the rendering settings");
        }
        return null;
    }

    /**
     * Creates a BufferedImage from the passed array of bytes.
     * 
     * @param values
     *            The array of bytes.
     * @return See above.
     */
    private BufferedImage createImage(byte[] values) {
        if (values == null)
            throw new IllegalArgumentException("No array specified.");
        ByteArrayInputStream stream = null;
        try {
            stream = new ByteArrayInputStream(values);
            BufferedImage image = ImageIO.read(stream);
            if (image != null)
                image.setAccelerationPriority(1f);
            return image;
        } catch (Exception e) {
            logWarn(this, "Could not create BufferedImage", e);
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {
                }
            }
        }
    }

}
