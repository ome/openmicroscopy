/*
 * org.openmicroscopy.shoola.env.ui.DownloadArchivedActivity 
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
package org.openmicroscopy.shoola.env.ui;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam;

/** 
 * Downloads the archived image.
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
public class DownloadArchivedActivity 
	extends ActivityComponent
{


	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_CREATED = "Archived Image " +
			"downloaded";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Download Archived " +
			"Image cancelled";
	
	/** The parameters to download. */
	private DownloadArchivedActivityParam parameters;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
	 * @param parameters    The object hosting information about the 
	 * 						archived image.
	 */
	DownloadArchivedActivity(UserNotifier viewer, Registry registry,
			DownloadArchivedActivityParam parameters) 
	{
		super(viewer, registry, "Downloaded Archived Image", 
				parameters.getIcon());
		messageLabel.setText("in "+parameters.getLocation());
		this.parameters = parameters;
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new ArchivedLoader(viewer,  registry, parameters.getImage(), 
				 parameters.getLocation(), this);
		return loader;
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled()
	{
		type.setText(DESCRIPTION_CANCEL);
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		//review
		type.setText(DESCRIPTION_CREATED);
		int v = (Integer) result;
		String value = null;
		if (v > 1)
			value ="All "+v+" files downloaded in "+parameters.getLocation();
		if (value != null)
			messageLabel.setText(value);
	}
    
	/** 
	 * No-operation in this case.
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}
}
