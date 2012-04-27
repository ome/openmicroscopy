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
package org.openmicroscopy.shoola.env.data.model.appdata;

import java.io.File;
import java.net.URL;

import org.openmicroscopy.shoola.env.data.model.ApplicationData;

/**
 * Interface representing platform specific code required for extracting an
 * applications properties
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public interface ApplicationDataExtractor {

	/**
	 * @return the platform specific directory where applications are located
	 */
	String getDefaultAppDirectory();

	/**
	 * Extracts the application data for the application on a windows platform
	 * 
	 * @param file
	 *            the file pointing to the application's location on disk
	 * @return the {@link ApplicationData} object representing this applications
	 *         system properties
	 */
	ApplicationData extractAppData(File file) throws Exception;

	/**
	 * @param location
	 *            the file location to be opened
	 * @return the platform specific default command used to open documents
	 */
	String[] getDefaultOpenCommandFor(URL location);
}
