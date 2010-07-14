/*
 * org.openmicroscopy.shoola.env.data.views.ScriptBatchCall
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2010 Glencoe Software, Inc. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.grid.ProcessCallbackI;

import org.openmicroscopy.shoola.env.data.ScriptCallback;
import org.openmicroscopy.shoola.util.concur.tasks.MultiStepTask;

/**
 * Subclass of {@link BatchCall} which handles periodically polling for
 * Script completion.
 *
 * @version 4.2.0
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME Beta4.2.0
 */
public abstract class ScriptBatchCall
    extends BatchCall
{

    public ScriptBatchCall(String name) {
        super(name);
    }

    /**
     * Whether or not the first call has been made.
     */
    private boolean initialized;

    /**
     * The script callback which is needed to poll the server for completion.
     */
    private ScriptCallback cb;

    /**
     * Method call to initialize this BatchCall.
     */
    protected abstract ScriptCallback initialize() throws Exception;

    /**
     * Disabling this method since we want to enforce the use of
     * doStep.
     */
    public final void doCall() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Forwards the call to the {@link #doCall() doCall} method.
     * @see MultiStepTask#doStep()
     */
    public Object doStep()
        throws Exception
    {
        //
        // 1. If we have not yet been called, then the first thing we will do
        // is call the script and get the ScriptCallback instance. We return
        // immediately if null is returned.
        //
        if (!initialized) {
            try {
                cb = initialize();
                if (cb == null) {
                    done = true;
                    return null; // EARLY EXIT!
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Sorry, not sure what to do here.
            } finally {
                initialized = true;
            }
        }

        //
        // 2. Once we have a ScriptCallback, we will continue to call block on
        // it per step until it returns non-null value, at which point we set
        // done to true and close the callback.
        //
        if (!done) {
            try {
                ProcessCallbackI.Action action = cb.block(500); // Do a unit of work
                if (action != null) {
                    done = true;
                }
            } catch (InterruptedException e) {
                // ignore.
            }
        } else {
            if (cb != null) {
                try {
                    cb.close();
                } finally {
                    cb = null;
                }
            }
        }

        return null;

    }

    /**
     * Tells whether or not the {@link #doStep() doStep} method has been
     * invoked.
     * @see MultiStepTask#isDone()
     */
    public boolean isDone() { return done; }

}
