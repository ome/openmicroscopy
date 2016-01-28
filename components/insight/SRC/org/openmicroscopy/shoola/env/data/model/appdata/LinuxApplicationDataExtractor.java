/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;

/**
 * Provides the Linux specific implementation to retrieve information about an
 * application, attempts to read the .desktop information and extracts the
 * property values
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class LinuxApplicationDataExtractor implements ApplicationDataExtractor
{

	/**
	 * The default location on <code>Linux</code> platforms that applications
	 * reside in.
	 */
	private static final String LOCATION_LINUX = "/usr/bin";
	
	/**
	 * Returns the directory where applications are generally located.
	 * @return the Linux specific directory where applications are generally
	 *         located
	 */
	public String getDefaultAppDirectory() {
		return LOCATION_LINUX;
	}

	/**
	 * Reads the application data for the application on a linux platform
	 * 
	 * @param file
	 *            the file pointing to the application's location on disk
	 * @return the {@link ApplicationData} object representing this applications
	 *         system properties
	 * @throws FileNotFoundException
	 *             if the file specified is null or does not exist on disk
	 */
	public ApplicationData extractAppData(File file) throws Exception {
		String applicationPath = file.getAbsolutePath();

		String applicationName = FilenameUtils.getBaseName(applicationPath);

		// TODO: read the location of this from .desktop file
		Icon applicationIcon = new ImageIcon();

		return new ApplicationData(applicationIcon, applicationName,
				applicationPath);
	}

    /**
     * Returns the command string to launch the default application for the
     *          file specified by {@code location} with xdg-open which should
     *          be available on all freedesktop.org compliant linux desktops.
     * @param location
     *            the location pointing to the file to be opened
     * @return See above.
     */
    public String[] getDefaultOpenCommandFor(URL location) {
        String[] defaultCommands = new String[] { "xdg-open",
                location.toString() };
        return defaultCommands;
    }

}
