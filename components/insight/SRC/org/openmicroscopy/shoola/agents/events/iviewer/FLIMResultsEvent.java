/*
 * org.openmicroscopy.shoola.agents.events.iviewer.FLIMResultsEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.events.iviewer;


//Java imports
import java.io.File;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * Event posted to display the results of a FLIM script.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FLIMResultsEvent
	extends RequestEvent
{

	/** The image the results are for. */
	public ImageData image;
	
	/** The results to display. */
	public Map<FileAnnotationData, File> results;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param image The image the results are for.
	 * @param results The results to display.
	 */
	public FLIMResultsEvent(ImageData image, 
			Map<FileAnnotationData, File> results)
	{
		this.image = image;
		this.results = results;
	}
	
	/**
	 * Returns the image.
	 * 
	 * @return See above.
	 */
	public ImageData getImage() { return image; }
	
	/**
	 * Returns the results to display.
	 * 
	 * @return See above.
	 */
	public Map<FileAnnotationData, File> getResults() { return results; }
	
}
