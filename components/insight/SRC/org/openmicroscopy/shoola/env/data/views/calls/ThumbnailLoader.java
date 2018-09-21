/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2018 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views.calls;

import ome.conditions.ResourceError;
import omero.MissingPyramidException;
import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.api.ThumbnailStorePrx;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.log.LogMessage;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.EncoderException;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import org.openmicroscopy.shoola.util.ui.IconManager;

import java.awt.Dimension;
import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Command to load a given set of thumbnails.
 * <p>As thumbnails are retrieved from <i>OMERO</i>, they're posted back to the
 * caller through <code>DSCallFeedbackEvent</code>s. Each thumbnail will be
 * posted in a single event; the caller can then invoke the <code>
 * getPartialResult</code> method to retrieve a <code>ThumbnailData</code>
 * object for that thumbnail. The final <code>DSCallOutcomeEvent</code> will
 * have no result.</p>
 * <p>Thumbnails are generated respecting the <code>X/Y</code> ratio of the
 * original image and so that their area doesn't exceed <code>maxWidth*
 * maxHeight</code>, which is specified to the constructor.</p>
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">
 * a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailLoader extends BatchCallTree {

    /**
     * The images for which we need thumbnails.
     */
    private Collection<DataObject> images;

    /**
     * The maximum acceptable width of the thumbnails.
     */
    private int maxWidth;

    /**
     * The maximum acceptable height of the thumbnails.
     */
    private int maxHeight;

    /**
     * The lastly retrieved thumbnail.
     */
    private Object currentThumbnail;

    /**
     * Collection of user IDs.
     */
    private Collection<Long> userIDs;

    /**
     * Helper reference to the image service.
     */
    private OmeroImageService service;

    /**
     * The security context.
     */
    private SecurityContext ctx;

    /**
     * Load the thumbnail as an full size image.
     */
    private boolean asImage = false;

    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     *
     * @param ctx       The security context.
     * @param imgs      Contains {@link DataObject}s, one
     *                  for each thumbnail to retrieve.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userIDs   The users the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, Collection<DataObject> imgs,
                           int maxWidth, int maxHeight, Collection<Long> userIDs) {
        if (imgs == null) {
            throw new NullPointerException("No images.");
        }

        if (maxWidth <= 0) {
            throw new IllegalArgumentException(
                    "Non-positive width: " + maxWidth + ".");
        }

        if (maxHeight <= 0) {
            throw new IllegalArgumentException(
                    "Non-positive height: " + maxHeight + ".");
        }

        this.images = imgs;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.userIDs = userIDs;
        this.ctx = ctx;
        this.service = context.getImageService();
    }

    public ThumbnailLoader(SecurityContext ctx, Collection<DataObject> imgs, long userID) {
        this(ctx, imgs, 0, 0, Collections.singleton(userID));
        this.asImage = true;
    }

    public ThumbnailLoader(SecurityContext ctx, Collection<DataObject> imgs, int maxWidth, int maxHeight, long userID) {
        this(ctx, imgs, maxWidth, maxHeight, Collections.singleton(userID));
    }

    public ThumbnailLoader(SecurityContext ctx, ImageData image, int maxWidth, int maxHeight, long userID) {
        this(ctx, new HashSet<DataObject>(), maxWidth, maxHeight, Collections.singleton(userID));
        images.add(image);
    }

    public ThumbnailLoader(SecurityContext ctx, ImageData image, int maxWidth, int maxHeight, Collection<Long> userIDs) {
        this(ctx, new HashSet<DataObject>(), maxWidth, maxHeight, userIDs);
        images.add(image);
    }

    /**
     * Returns the last loaded thumbnail (important for the BirdsEyeLoader to
     * work correctly). But in fact, thumbnails are progressively delivered with
     * feedback events.
     *
     * @see BatchCallTree#getResult()
     */
    @Override
    protected Object getResult() {
        return currentThumbnail;
    }

    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     *
     * @return A {@link ThumbnailData} containing the thumbnail pixels.
     */
    @Override
    protected Object getPartialResult() {
        return currentThumbnail;
    }

    /**
     * Adds a {@link BatchCall} to the tree for each thumbnail to retrieve.
     *
     * @see BatchCallTree#buildTree()
     */
    @Override
    protected void buildTree() {
        final int lastIndex = images.size() - 1;
        for (final long userId : userIDs) {
            int k = 0;
            for (DataObject image : images) {
                // Cast our image to pixels object
                final PixelsData pxd = dataObjectToPixelsData(image);

                // Flag to check if we've iterated to the last image
                final boolean last = lastIndex == k++;

                // Add a new load thumbnail task to tree
                BatchCall call = new BatchCall("Loading thumbnails") {
                    @Override
                    public void doCall() throws Exception {
                        ThumbnailStorePrx store = null;
                        try {
                            store = getThumbnailStore(pxd);
                            handleBatchCall(store, pxd, userId);
                        } catch (DSAccessException | ServerError e) {
                            currentThumbnail = new ThumbnailData(pxd.getImage().getId(),
                                    getErrorIcon(), userId, false);

                            LogMessage msg = new LogMessage(
                                    "Couldn't initialize the ThumbnailStore for pixels id "
                                            + pxd.getId(), e);

                            context.getLogger().warn(this, msg);
                        } finally {
                            if (last && store != null) {
                                context.getDataService().closeService(ctx, store);
                            }
                        }
                    }
                };

                add(call);
            }
        }
    }

    private void handleBatchCall(ThumbnailStorePrx store, PixelsData pxd, long userId) throws DSOutOfServiceException,
            DSAccessException {
        Image thumbnail = null;
        try {
            byte[] thumbnailData = loadThumbnail(store, pxd, userId);
            if (thumbnailData == null || thumbnailData.length == 0) {
                // Find out why the thumbnail is not ready on the server
                if (requiresPixelsPyramid(pxd)) {
                    thumbnail = determineThumbnailState(pxd);
                } else {
                    thumbnail = getLoadingIcon();
                }
            } else {
                thumbnail = WriterImage.bytesToImage(thumbnailData);
            }
        } catch (ServerError e) {
            context.getLogger().error(this,
                    new LogMessage("API error", e));
        } catch (EncoderException e) {
            // Thrown if conversion of bytes to Image fails
            context.getLogger().error(this,
                    new LogMessage("Failed to convert thumbnail byte array to BufferedImage", e));
        }

        if (thumbnail == null) {
            thumbnail = getErrorIcon();
        }

        // Convert thumbnail to whatever
        currentThumbnail = new ThumbnailData(pxd.getImage().getId(),
                thumbnail, userId, true);
    }

    private PixelsData dataObjectToPixelsData(DataObject image) {
        return image instanceof ImageData ?
                ((ImageData) image).getDefaultPixels() :
                (PixelsData) image;
    }

    private Image determineThumbnailState(PixelsData pxd)
            throws DSOutOfServiceException, ServerError {
        RawPixelsStorePrx rawPixelStore = context.getGateway()
                .getPixelsStore(ctx);
        try {
            // This method will throw if there is an issue with the pyramid
            // generation (i.e. it's not finished, corrupt)
            rawPixelStore.setPixelsId(pxd.getId(), false);
        } catch (MissingPyramidException e) {
            // Thrown if pyramid file is missing, then we know the thumbnail still has
            // to be generated in a short time
            return getLoadingIcon();
        } catch (ResourceError e) {
            context.getLogger().error(this, new LogMessage("Error getting pyramid from server," +
                    " it might be corrupt", e));
        }
        return getErrorIcon();
    }

    private ThumbnailStorePrx getThumbnailStore(PixelsData pxd) throws DSAccessException,
            DSOutOfServiceException, ServerError {
        ThumbnailStorePrx store = service.createThumbnailStore(ctx);
        if (!store.setPixelsId(pxd.getId())) {
            store.resetDefaults();
            store.setPixelsId(pxd.getId());
        }
        return store;
    }

    /**
     * Loads the thumbnail for {@link #images}<code>[index]</code>.
     *
     * @param pxd    The image the thumbnail for.
     * @param userId The id of the user the thumbnail is for.
     * @param store  The thumbnail store to use.
     */
    private byte[] loadThumbnail(ThumbnailStorePrx store, PixelsData pxd, long userId)
            throws ServerError, DSAccessException, DSOutOfServiceException {
        int sizeX = maxWidth, sizeY = maxHeight;
        if (asImage) {
            sizeX = pxd.getSizeX();
            sizeY = pxd.getSizeY();
        } else {
            Dimension d = Factory.computeThumbnailSize(sizeX, sizeY,
                    pxd.getSizeX(), pxd.getSizeY());
            sizeX = d.width;
            sizeY = d.height;
        }

        if (userId >= 0) {
            long rndDefId = service.getRenderingDef(ctx,
                    pxd.getId(), userId);
            // the user might not have own rendering settings
            // for this image
            if (rndDefId >= 0)
                store.setRenderingDefId(rndDefId);
        }

        if ((boolean) context.lookup(LookupNames.SERVER_5_4_8_OR_LATER)) {
            // If the client is connecting to a server with version 5.4.8 or greater, use the thumbnail
            // loading function that doesn't return a clock.
            // TODO: Can be removed for >= 5.5.0 release
            return store.getThumbnailWithoutDefault(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
        } else {
            return store.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
        }
    }

    /**
     * Returns whether a pyramid should be used for the given {@link PixelsData}.
     * This usually implies that this is a "Big image" and therefore will need
     * tiling.
     *
     * @param pxd
     * @return See above.
     */
    private boolean requiresPixelsPyramid(PixelsData pxd) {
        int maxWidth = (Integer) context.lookup(LookupNames.MAX_PLANE_WIDTH);
        int maxHeight = (Integer) context.lookup(LookupNames.MAX_PLANE_HEIGHT);
        return pxd.getSizeX() * pxd.getSizeY() > maxWidth * maxHeight;
    }

    private Image getLoadingIcon() {
        return IconManager.getInstance()
                .getImageIcon(IconManager.THUMBNAIL_LOADING_TIMER_BLACK)
                .getImage();
    }

    private Image getErrorIcon() {
        return IconManager.getInstance()
                .getImageIcon(IconManager.THUMBNAIL_ERROR_BLACK)
                .getImage();
    }

}
