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

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;


/**
 * Saves the imageJ results.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class SaveResultsActivity
extends ActivityComponent
{

    /** The description of the activity. */
    private static final String DESCRIPTION_CREATION = "Saving";

    /** The description of the activity when finished. */
    private static final String DESCRIPTION_DONE = "Results saved";

    /** The description of the activity when cancelled. */
    private static final String DESCRIPTION_CANCEL = "Saving cancelled";

    /** The results to save. */
    private ResultsObject results;
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param results The results to save.
     */
    public SaveResultsActivity(UserNotifier viewer, Registry registry,
            SecurityContext ctx, ResultsObject results)
    {
        super(viewer, registry, ctx);
        if (results == null)
            throw new IllegalArgumentException("Parameters not valid.");
        this.results = results;
        initialize(DESCRIPTION_CREATION, null);
    }
    /**
     * Modifies the text of the component. 
     * @see ActivityComponent#notifyActivityCancelled()
     */
    protected void notifyActivityCancelled() {
        type.setText(DESCRIPTION_CANCEL);
    }

    /**
     * Modifies the text of the component. 
     * @see ActivityComponent#notifyActivityEnd()
     */
    protected void notifyActivityEnd()
    {
        if (results.isROI()) {
            if (results.isTable()) {
                type.setText("ROIs and Results Saved");
            } else {
                type.setText("ROIs Saved");
            }
        }
        if (results.isTable()) {
            type.setText("Results Saved");
        }
    }

    /** 
     * No-operation in this case.
     * @see ActivityComponent#notifyActivityError()
     */
    protected void notifyActivityError() {}

    /**
     * Creates a concrete loader.
     * @see ActivityComponent#createLoader()
     */
    protected UserNotifierLoader createLoader()
    {
        return new SaveResultsLoader(viewer, registry, ctx, results, this);
    }

}
