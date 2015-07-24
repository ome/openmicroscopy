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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.Channel;
import omero.model.Pixels;
import omero.sys.ParametersI;
import pojos.ChannelData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class MetadataFacility extends Facility {

    private BrowseFacility browse;

    MetadataFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Loads the {@link ImageAcquisitionData} for a specific image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId
     * @return See above
     */
    public ImageAcquisitionData getImageAcquisitionData(SecurityContext ctx,
            long imageId) {
        ParametersI params = new ParametersI();
        params.acquisitionData();
        ImageData img = browse.getImage(ctx, imageId, params);
        return new ImageAcquisitionData(img.asImage());
    }

    /**
     * Get the {@link ChannelData} for a specific image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId to get the ChannelData for
     * @return List of ChannelData
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<ChannelData> getChannelData(SecurityContext ctx, long imageId)
            throws DSOutOfServiceException, DSAccessException {
        List<ChannelData> result = new ArrayList<ChannelData>();

        try {
            ImageData img = browse.getImage(ctx, imageId);

            long pixelsId = img.getDefaultPixels().getId();
            Pixels pixels = gateway.getPixelsService(ctx)
                    .retrievePixDescription(pixelsId);
            List<Channel> l = pixels.copyChannels();
            for (int i = 0; i < l.size(); i++)
                result.add(new ChannelData(i, l.get(i)));

        } catch (Throwable t) {
            handleException(this, t, "Cannot load channel data.");
        }

        return result;
    }
}
