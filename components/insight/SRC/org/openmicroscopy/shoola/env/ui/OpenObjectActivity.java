/*
 * org.openmicroscopy.shoola.env.ui.OpenObjectActivity 
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
import java.io.File;

//Third-party libraries


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.OpenActivityParam;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

/** 
 * Activity to open an image or a file.
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
public class OpenObjectActivity 
	extends ActivityComponent
{

	/** The description of the activity when starting. */
	private static final String		DESCRIPTION_STARTING = "Opening in ";
	
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_CREATED = "Object ready " +
	"to be opened";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Opening cancelled";
	
    /** The parameters hosting information about the object to open. */
    private OpenActivityParam parameters;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param ctx The security context.
     * @param parameters  	The parameters used to export the image.
     */
	public OpenObjectActivity(UserNotifier viewer, Registry registry,
			SecurityContext ctx, OpenActivityParam parameters)
	{
		super(viewer, registry, ctx);
		if (parameters == null)
			throw new IllegalArgumentException("No parameters");
		this.parameters = parameters;
		initialize(DESCRIPTION_STARTING+parameters.getLabel(),
				parameters.getIcon());
	}
	
	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new OpenObjectLoader(viewer,  registry, ctx, 
				parameters.getObject(), 
				parameters.getFolderPath(), this);
		return loader;
	}

	/**
	 * Modifies the text of the component and opens the application. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		File f = (File) result;
		String path = f.getAbsolutePath();
		//Special handling for fiji or imagej
		ApplicationData data = parameters.getApplication();
		if (data != null) {
		    String name = data.getApplicationName();
		    if (name != null) {
		        name = name.toLowerCase();
		        //Always use BF to open the file if Fiji or ImageJ
		        if (name.contains("fiji") || name.contains("imagej")) {
		            path = null;
		            List<String> args = new ArrayList<String>();
		            args.add("-eval");
		            args.add("run('Bio-Formats Importer',"
		                    + "'open=["+f.getAbsolutePath()+"]')");
		            data.setCommandLineArguments(args);
		        }
		    }
		}
		viewer.openApplication(parameters.getApplication(), path);
		type.setText(DESCRIPTION_CREATED);
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
