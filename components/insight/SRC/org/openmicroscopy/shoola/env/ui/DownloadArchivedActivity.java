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
import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.util.file.IOUtil;

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
	private static final String DESCRIPTION_CREATED = "Archived Image " +
			"downloaded";
	
	/** The description of the activity when cancelled. */
	private static final String DESCRIPTION_CANCEL = "Download Archived " +
			"Image cancelled";
	
	/** The description of the activity when no archived files found. */
	private static final String DESCRIPTION_NO_ARCHIVED = "No Archived " +
			"Image available";
	
	/** The description of the activity when no archived files found. */
	private static final String OPTION_NO_ARCHIVED = "You can download the " +
			"Image as OME-TIFF";
	
	/** The parameters to download. */
	private DownloadArchivedActivityParam parameters;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param ctx The security context.
	 * @param parameters    The object hosting information about the 
	 * 						archived image.
	 */
	DownloadArchivedActivity(UserNotifier viewer, Registry registry,
			SecurityContext ctx, DownloadArchivedActivityParam parameters) 
	{
		super(viewer, registry, ctx);
		if (parameters == null)
			throw new IllegalArgumentException("No parameters");
		this.parameters = parameters;
		initialize("Downloaded Archived Image", parameters.getIcon());
		messageLabel.setText("in "+parameters.getLocation());
		this.parameters = parameters;
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new ArchivedLoader(viewer, registry, ctx, 
				parameters.getImage(), parameters.getLocation(), this);
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
		List<File> files = (List<File>) result;
		//Handle no file returned.
		if (files.size() == 0) {
			type.setText(DESCRIPTION_NO_ARCHIVED);
			messageLabel.setText(OPTION_NO_ARCHIVED);
			return;
		}
		type.setText(DESCRIPTION_CREATED);
		StringBuffer buffer = new StringBuffer();
		buffer.append("as ");
		if (files.size() > 1) {//zip the result
			try {
				//Create a folder
				
				File zipFolder = new File(parameters.getLocation(),
				FilenameUtils.removeExtension(parameters.getImage().getName()));
				zipFolder.mkdir();
				//copy file into the directory
				Iterator<File> j = files.iterator();
				File child;
				while (j.hasNext()) {
					child = j.next();
					FileUtils.copyFileToDirectory(child, zipFolder, true);
					child.delete();
				}
				
				//rename 
				IOUtil.zipDirectory(zipFolder);
				messageLabel.setText(zipFolder.getAbsolutePath());
				//empty folder.
				File[] entries = zipFolder.listFiles();
				for (int i = 0; i < entries.length; i++)
					entries[i].delete();
				
				zipFolder.delete();
				buffer.append(zipFolder.getAbsolutePath());
				buffer.append(IOUtil.ZIP_EXTENSION);
				messageLabel.setText(buffer.toString());
			} catch (Exception e) {
				registry.getLogger().debug(this, "Cannot create a zip");
			}
		} else {
			buffer.append(files.get(0).getAbsolutePath());
			messageLabel.setText(buffer.toString());
		}
	}
    
	/** 
	 * No-operation in this case.
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}
}
