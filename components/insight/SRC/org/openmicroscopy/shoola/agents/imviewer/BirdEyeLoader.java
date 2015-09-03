/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
 * Loads the image for the bird eye view.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class BirdEyeLoader
    extends DataLoader
{

    /** The minimum ration value.*/
    public static final double MIN_RATIO = 0.1;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The object the image is for. */
    private ImageData image;

    /** The ratio by which to scale the image down.*/
    private double ratio = -1;

    /** The image size.*/
    private int imageSize = -1;
    
    /** Flag indicating that this loader has been cancelled.*/
    private boolean cancelled;

    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param image The image to handle.
     * @param ratio The ratio by with to scale the image.
     */
    public BirdEyeLoader(ImViewer viewer, SecurityContext ctx, ImageData image,
            double ratio)
    {
        super(viewer, ctx);
        if (image == null)
            throw new IllegalArgumentException("No image to load.");
        this.image = image;
        this.ratio = ratio;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param image The image to handle.
     * @param imageSize The requested image size
     */
    public BirdEyeLoader(ImViewer viewer, SecurityContext ctx, ImageData image,
            int imageSize)
    {
        super(viewer, ctx);
        if (image == null)
            throw new IllegalArgumentException("No image to load.");
        this.image = image;
        this.imageSize = imageSize;
    }

    /**
     * Loads the image.
     * 
     * @see DataLoader#load()
     */
    public void load() {
        // Load the thumbnail
        List<DataObject> objects = new ArrayList<DataObject>();
        objects.add(image);
        if (ratio > 0) {
            // load image with default thumbnail size (fast)
            handle = hiBrwView.loadThumbnails(ctx, objects,
                    Factory.THUMB_DEFAULT_WIDTH, Factory.THUMB_DEFAULT_HEIGHT,
                    -1, HierarchyBrowsingView.IMAGE, this);
        } else if (imageSize > 0) {
            // load image with a custom size (might be slow)
            handle = hiBrwView.loadThumbnails(ctx, objects, imageSize,
                    imageSize, -1, HierarchyBrowsingView.IMAGE, this);
        }
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel()
    {
        cancelled = true;
        handle.cancel();
    }

    /**
     * Notifies the user that the data retrieval has been cancelled.
     */
    public void handleCancellation() {}

    /**
     * Notifies the user that an error has occurred.
     * @see DataLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc)
    {
        String s = "Bird Eye Retrieval Failure: ";
        if (viewer.getState() == ImViewer.DISCARDED) return;
        registry.getLogger().error(this, s+exc);
        if (viewer.getState() == ImViewer.CANCELLED)
            if (cancelled) viewer.discard();
            else registry.getUserNotifier().notifyError(s, s, exc);
    }

    /** 
     * Feeds the image back to the bird eye viewer, as they arrive.
     * @see DataLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        ThumbnailData data = (ThumbnailData) fe.getPartialResult();
        if (data != null) {
            BufferedImage image = (BufferedImage) data.getThumbnail();
            boolean scaled = false;
            if (image != null && ratio > 0 && ratio != 1) {
                image = Factory.magnifyImage(ratio, image);
                scaled = true;
            }
            viewer.setBirdEyeView(image, scaled);
        }
    }

}
