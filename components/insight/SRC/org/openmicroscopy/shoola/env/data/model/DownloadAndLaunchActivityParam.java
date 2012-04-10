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
package org.openmicroscopy.shoola.env.data.model;

import java.io.File;

import javax.swing.Icon;

import omero.model.OriginalFile;

/**
 * 
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DownloadAndLaunchActivityParam extends DownloadActivityParam {

	/** The third party application. */
	private ApplicationData data;

	public DownloadAndLaunchActivityParam(long id, int index, File folder,
			Icon icon) {
		super(id, index, folder, icon);
	}

	public DownloadAndLaunchActivityParam(OriginalFile originalFile, File file, Icon icon) {
		super(originalFile,file,icon);
	}

	/**
	 * Sets the application data.
	 * 
	 * @param data The third party application or <code>null</code>.
	 */
	public void setApplicationData(ApplicationData data) { this.data = data; }

	/**
	 * Returns the application data.
	 * 
	 * @return See above.
	 */
	public ApplicationData getApplicationData() { return data; }

}
