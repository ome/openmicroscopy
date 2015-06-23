/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import org.openmicroscopy.shoola.agents.dataBrowser.view.AdvancedResultSearchModel;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

import pojos.DataObject;
import pojos.ImageData;

/**
 * Loads all thumbnails for the specified images. This class calls the
 * {@link HierarchyBrowsingView#loadThumbnails} method.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
public class SearchThumbnailLoader extends DataBrowserLoader {
    /**
     * The <code>ImageData</code> objects for the images whose thumbnails have
     * to be fetched.
     */
    private Collection<DataObject> objects;

    /** Handle to AdvancedResultSearchModel to pass the thumbs on */
    private AdvancedResultSearchModel model;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer
     *            The viewer this data loader is for. Mustn't be
     *            <code>null</code>.
     * @param ctx
     *            The security context.
     * @param objects
     *            The <code>DataObject</code>s associated to the images to
     *            fetch. Mustn't be <code>null</code>.
     * @param model
     *            Reference to the {@link AdvancedResultSearchModel} to forward
     *            the thumbnails to
     */
    public SearchThumbnailLoader(DataBrowser viewer, SecurityContext ctx,
            Collection<ImageData> imgs, AdvancedResultSearchModel model) {
        super(viewer, ctx);
        if (imgs == null)
            throw new NullPointerException(
                    "The provided collection of images shouldn't be null.");

        this.model = model;

        // transform the List of ImageData into a List of DataObjects
        // (the underlying call to HierarchyBrowsingView.loadThumbnails
        // just takes a List of DataObjects)
        this.objects = new ArrayList<DataObject>(imgs.size());
        for (ImageData img : imgs)
            objects.add(img);

    }

    /**
     * Retrieves the thumbnails.
     * 
     * @see DataBrowserLoader#load()
     */
    public void load() {
        handle = hiBrwView.loadThumbnails(ctx, objects,
                ThumbnailProvider.THUMB_MAX_WIDTH,
                ThumbnailProvider.THUMB_MAX_HEIGHT, -1,
                HierarchyBrowsingView.IMAGE, this);
    }

    /**
     * Cancels the data loading.
     * 
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() {
        handle.cancel();
    }

    /**
     * Feeds the thumbnails back to the model, as they arrive.
     * 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) {
        if (viewer.getState() == DataBrowser.DISCARDED)
            return; // Async cancel.
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();

        if (status == null)
            status = (percDone == 100) ? "Done" : "";

        ThumbnailData td;
        long imgId;

        td = (ThumbnailData) fe.getPartialResult();
        if (td != null) {
            imgId = td.getImageID();
            BufferedImage thumb = td.getThumbnail();
            model.setThumbnail(imgId, thumb);
        }
    }

    /**
     * Does nothing as the asynchronous call returns <code>null</code>. The
     * actual pay-load (thumbnails) is delivered progressively during the
     * updates.
     * 
     * @see DataBrowserLoader#handleNullResult()
     */
    public void handleNullResult() {
    }

    public void onEnd() {
        model.notifyThumbsLoaded();
    }

    /**
     * Notifies the user that an error has occurred.
     * 
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) {
        String s = "Thumbnail Retrieval Failure: ";
        registry.getLogger().error(this, s + exc);
        registry.getUserNotifier().notifyError("Thumbnail Retrieval Failure",
                s, exc);
    }

}
