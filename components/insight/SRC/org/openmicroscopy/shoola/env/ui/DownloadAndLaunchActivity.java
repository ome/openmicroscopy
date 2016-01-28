/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import omero.gateway.SecurityContext;

/**
 * Activity to describe downloading a file and launching a specific application
 * to open the downloaded file
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DownloadAndLaunchActivity extends DownloadActivity {

	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	private String getFileName() {
		return parameters.getOriginalFileName();
	}
	
	/**
	 * Create an instance of this class
	 * 
	 * @param viewer the viewer used for performing this activity
	 * @param registry the system registry for service lookup
	 * @param ctx the {@link SecurityContext} in use for this operation
	 * @param parameters parameters passed in to the activity
	 */
	DownloadAndLaunchActivity(UserNotifier viewer, Registry registry,
			SecurityContext ctx, DownloadAndLaunchActivityParam parameters) {
		super(viewer, registry, ctx, parameters);

		fileName = getFileName();
		messageLabel.setText("Opening: " + fileName);
	}

	/**
	 * Modifies the text of the component.
	 * 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd() {
		type.setText("Opening File");
		DownloadAndLaunchActivityParam param = (DownloadAndLaunchActivityParam) parameters;
		viewer.openApplication((ApplicationData) param.getApplicationData(),
				file.getAbsolutePath());
		if (parameters.getSource() != null)
			parameters.getSource().setEnabled(true);
	}

	/**
	 * Callback if the operation is cancelled.
	 */
	@Override
	protected void notifyActivityCancelled() {
		type.setText("Opening Cancelled");
	}

	/**
	 * No-operation in this case.
	 * 
	 * @see ActivityComponent#notifyActivityError()
	 */
	@Override
	protected void notifyActivityError() {
	}

	/**
	 * calls the super class to create the loader and forces deleteOnExit() for
	 * the temporary file
	 */
	@Override
	protected UserNotifierLoader createLoader() {
		UserNotifierLoader loader = super.createLoader();
		file.deleteOnExit();
		return loader;
	}
}
