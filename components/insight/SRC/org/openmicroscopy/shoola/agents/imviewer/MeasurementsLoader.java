/*
 * org.openmicroscopy.shoola.agents.imviewer.MeasurementsLoader
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.ImageData;
import pojos.PlateData;

/**
 * Loads the measurements associated to the specified object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MeasurementsLoader 
	extends DataLoader
{

	/** The object to handle. */
	private DataObject object;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /**
     * Checks the passed type if supported.
     * 
     * @param value The value to handle.
     */
    private void checkType(DataObject value)
    {
    	if (value instanceof PlateData) return;
    	if (value instanceof ImageData) return;
    	throw new IllegalArgumentException("Type not supported.");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model.
     * @param ctx The security context.
     * @param object The object.
     */
	public MeasurementsLoader(ImViewer model, SecurityContext ctx,
			DataObject object)
	{
		super(model, ctx);
		checkType(object);
		this.object = object;
	}
	
	/**
     * Loads the measurements related to the passed object.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = mhView.loadROIMeasurement(ctx, object, -1, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        viewer.setMeasurements((Collection) result);
    }
    
}
