/*
 * org.openmicroscopy.shoola.agents.metadata.FlretAnalyser
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata;





//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;
import pojos.ImageData;

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FretAnalyser 
	extends MetadataLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle			handle;
    
    /** The control image. */
    private ImageData			control;
    
    /** The annotation hosting the function. */
    private FileAnnotationData	irf;
    
    /** The id of the image to analyze. */
    private long				toAnalyzeID;
    
	/**
     * Creates a new instance.
     * 
     * @param viewer 	The view this loader is for.
     *               	Mustn't be <code>null</code>.
     * @param control  	The control image.
     * @param toAnalyzeID The id of the image to analyze.
     * @param irf		The function related to the control.
     */
	public FretAnalyser(MetadataViewer viewer, ImageData control,
			long toAnalyzeID, FileAnnotationData irf)
	{
		super(viewer);
		if (control == null)
			throw new IllegalArgumentException("No control specified.");
		if (toAnalyzeID <= 0)
			throw new IllegalArgumentException("No image to analyze.");
		this.control = control;
		this.toAnalyzeID = toAnalyzeID;
		this.irf = irf;
	}
	
	/**
     * Analyzes the images.
     * @see MetadataLoader#load()
     */
    public void load()
    {
    	long id = -1;
    	if (irf != null) id = irf.getFileID();
        handle = ivView.analyseFretFit(control.getId(), toAnalyzeID, id, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see MetadataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
    	//viewer.uploadMovie(null, null);
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    }
}
