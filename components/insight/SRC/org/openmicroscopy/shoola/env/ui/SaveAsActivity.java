/*
 * org.openmicroscopy.shoola.env.ui.SaveAsActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
import java.io.File;

//Third-party libraries

import omero.model.OriginalFile;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

import pojos.FileAnnotationData;

/** 
 * The activity associated to the Save as action i.e. save a collection of
 * images as JPEG.
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
public class SaveAsActivity
	extends ActivityComponent
{

	/** The description of the activity. */
	private static final String		DESCRIPTION_CREATION = "Saving Images as ";
	
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_CREATED = "Images saved in";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = 
		"Images saving cancelled";
	
	/** The parameters hosting information about the images to save. */
    private SaveAsParam	parameters;

    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param ctx The security context.
     * @param parameters	The parameters used to save the collection of images.
     */
	public SaveAsActivity(UserNotifier viewer,  Registry registry,
			SecurityContext ctx, SaveAsParam parameters)
	{
		super(viewer, registry, ctx);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		initialize(DESCRIPTION_CREATION+parameters.getIndexAsString(), 
				parameters.getIcon());
		File folder = parameters.getFolder();
		messageLabel.setText("in "+folder.getName());
		messageLabel.setToolTipText(folder.getAbsolutePath());
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new SaveAsLoader(viewer, registry, ctx, parameters, this);
		return loader;
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		//Download the file.
		if (result instanceof FileAnnotationData) {
			FileAnnotationData data = (FileAnnotationData) result;
			String name = "";
			if (data.isLoaded()) name = data.getFileName();
			else name = "Annotation_"+data.getId();
			download("", result, new File(parameters.getFolder(), name));
		}
		
		type.setText(DESCRIPTION_CREATED+" "+parameters.getFolder().getName());
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
	 * No-operation in this case.
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}
	
}
