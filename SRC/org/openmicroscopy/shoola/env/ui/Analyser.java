/*
 * org.openmicroscopy.shoola.env.ui.Analyser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class Analyser 
	extends UserNotifierLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The parameters to use.*/
    private Object 					param;
    
    /** The select objects. */
    private List<Long>				ids;
    
    /** The type of object to handle. */
    private Class					type;
    
    /** The result. */
    private FileAnnotationData		data;
    
    /** The type of analysis to perform. */
    private int						index;
    
    /** Reference to the activity. */
    private ActivityComponent 		activity;
    
    /** Notifies the user that an error occurred. */
    protected void onException() { handleNullResult(); }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param param  	The parameters used to create the movie.
     * @param ids		The selected objects.
     * @param type		The type of objects.
     * @param index		The type of analysis to perform.
     * @param activity 	The activity associated to this loader.
     */
	public Analyser(UserNotifier viewer,  Registry registry,
			Object param, List<Long> ids, Class type, int index, 
			ActivityComponent activity)
	{
		super(viewer, registry);
		if (activity == null)
			throw new IllegalArgumentException("Activity valid.");
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("Objects not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.ids = ids;
		this.type = type;
		this.activity = activity;
		this.index = index;
	}
	
	/**
     * Creates a figure of the selected images.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case AnalysisParam.FLIM:
				break;
			case AnalysisParam.FRAP:
				 handle = ivView.analyseFRAP(ids, type, param, this);
				break;
		}
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it wasn't possible to create the figure.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    { 
    	activity.notifyError("Unable to analyse data");
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result) { activity.endActivity(result); }
	
}
