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
package org.openmicroscopy.shoola.env.ui;

import ij.IJ;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;


/**
 * Saves the imageJ results back to OMERO.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class SaveResultsLoader
    extends UserNotifierLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The results to save. */
    private ResultsObject results;

    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
        activity.notifyError("Unable to save the imageJ results", message, ex);
    }

    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the model. Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param results The parameters used to save the images.
     * @param activity The activity associated to this loader.
     */
    public SaveResultsLoader(UserNotifier viewer, Registry registry,
            SecurityContext ctx, ResultsObject results,
            ActivityComponent activity)
    {
        super(viewer, registry, ctx, activity);
        if (results == null)
            throw new IllegalArgumentException("Parameters cannot be null.");
        this.results = results;
    }

    /**
     * Saves the imageJ results.
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = ivView.saveResults(ctx, results, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    {
        handle.cancel();
    }

    /** 
     * Feeds the results back.
     * @see UserNotifierLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        int percDone = fe.getPercentDone();
        if (percDone == 100) {
            activity.endActivity(Boolean.TRUE);
        }
    }

}
