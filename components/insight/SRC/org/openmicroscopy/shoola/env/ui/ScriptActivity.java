/*
 * org.openmicroscopy.shoola.env.ui.ScriptActivity
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


import javax.swing.Icon;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ScriptActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;

/** 
 * Activity to run the specified scripts.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ScriptActivity
    extends ActivityComponent
{

    /** Indicates to run the script. */
    public static final int RUN = ScriptActivityParam.RUN;

    /** Indicates to upload the script. */
    public static final int UPLOAD = ScriptActivityParam.UPLOAD;

    /** Indicates to download the script. */
    public static final int DOWNLOAD = ScriptActivityParam.DOWNLOAD;

    /** Indicates to view the script. */
    public static final int VIEW = ScriptActivityParam.VIEW;

    /** The description of the activity. */
    private static final String DESCRIPTION_RUN_CREATION = "Running ";

    /** The description of the activity when it is finished. */
    private static final String DESCRIPTION_RUN_CREATED = ": finished";

    /** The description of the activity. */
    private static final String DESCRIPTION_UPLOAD_CREATION = "Uploading ";

    /** The description of the activity when finished. */
    private static final String DESCRIPTION_UPLOAD_CREATED = ": uploaded";

    /** The description of the activity when cancelled. */
    private static final String DESCRIPTION_UPLOAD_CANCEL = "Upload cancelled";

    /** The description of the activity when cancelled. */
    private static final String DESCRIPTION_RUN_CANCEL = "Run cancelled";

    /** The script to run. */
    private ScriptObject script;

    /** One of the constants defined by this class. */
    private int index;

    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param script The script to run.
     * @param index The activity associated to this loader.
     */
    public ScriptActivity(UserNotifier viewer, Registry registry,
            SecurityContext ctx, ScriptObject script, int index)
    {
        super(viewer, registry, ctx);
        if (script == null)
            throw new IllegalArgumentException("Parameters not valid.");
        if (script.isParametersLoaded() && !script.allRequiredValuesPopulated())
            throw new IllegalArgumentException("Not all required fields " +
                    "have been filled.");
        initialize(DESCRIPTION_RUN_CREATION+script.getDisplayedName(),
                script.getIcon());
        switch (index) {
        case UPLOAD:
            type.setText(DESCRIPTION_UPLOAD_CREATION+script.getName());
            break;
        }

        this.script = script;
        this.index = index;
        Icon icon = script.getIcon();
        if (icon != null) iconLabel.setIcon(icon);
    }

    /**
     * Creates a concrete loader.
     * @see ActivityComponent#createLoader()
     */
    protected UserNotifierLoader createLoader()
    {
        switch (index) {
        case UPLOAD:
            loader = new ScriptUploader(viewer, registry, ctx, script, this);
            break;
        case RUN:
            loader = new ScriptRunner(viewer, registry, ctx, script, this);
        }
        return loader;
    }

    /**
     * Modifies the text of the component.
     * @see ActivityComponent#notifyActivityEnd()
     */
    protected void notifyActivityEnd()
    {
        switch (index) {
        case UPLOAD:
            type.setText(script.getName()+DESCRIPTION_UPLOAD_CREATED);
            break;
        case RUN:
            type.setText(script.getDisplayedName()+DESCRIPTION_RUN_CREATED);
        }
    }

    /**
     * Modifies the text of the component.
     * @see ActivityComponent#notifyActivityCancelled()
     */
    protected void notifyActivityCancelled()
    {
        switch (index) {
        case UPLOAD:
            type.setText(DESCRIPTION_UPLOAD_CANCEL);
            break;
        case RUN:
            type.setText(DESCRIPTION_RUN_CANCEL);
        }
    }

    /** 
     * No-operation in this case.
     * @see ActivityComponent#notifyActivityError()
     */
    protected void notifyActivityError() {}
}
