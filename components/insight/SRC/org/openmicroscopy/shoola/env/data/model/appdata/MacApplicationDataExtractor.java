/*
 * org.openmicroscopy.shoola.env.data.model.appdata.MacApplicationDataExtractor 
 *
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.Parser;
import org.openmicroscopy.shoola.util.image.io.IconReader;

/**
 * Provides the Mac specific implementation to retrieve information about an
 * application, reads the plist information to extract the property values
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MacApplicationDataExtractor implements ApplicationDataExtractor {

	/** The default location on <code>MAC</code> platform. */
	private static final String LOCATION_MAC = "/Applications";

	/**
	 * Converts the <code>.icns</code> to an icon.
	 * 
	 * @param path
	 *            The path to the file to convert.
	 * @return See above.
	 */
	private Icon convert(String path) {
		if (path == null)
			return null;
		if (!path.endsWith("icns"))
			path += ".icns";
		IconReader reader = new IconReader(path);
		BufferedImage img = null;
		try {
			img = reader.decode(IconReader.ICON_16);
		} catch (Exception e) {
		}
		if (img == null)
			return null;
		return new ImageIcon(img);
	}

	/**
	 * Returns the Mac specific directory where applications are located.
	 * 
	 * @return See above.
	 */
	public String getDefaultAppDirectory() {
		return LOCATION_MAC;
	}

	/**
	 * Extracts the application data for the application on a mac platform
	 * 
	 * @param file
	 *            the file pointing to the application's location on disk
	 * @return the {@link ApplicationData} object representing this applications
	 *         system properties
	 * @throws FileNotFoundException
	 *             if the file specified is null or does not exist on disk
	 */
	public ApplicationData extractAppData(File file) throws Exception {
		if (file == null || !file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());

		Map<String, Object> m = Parser.parseInfoPList(file.getAbsolutePath());

		String executablePath = (String) m.get(Parser.EXECUTABLE_PATH);
		Icon icon = convert((String) m.get(Parser.EXECUTABLE_ICON));
		String applicationName = (String) m.get(Parser.EXECUTABLE_NAME);

		ApplicationData data = new ApplicationData(icon, applicationName,
				executablePath);

		return data;
	}

	/**
	 * Returns the command string to launch the default application for the
     *          file specified by {@code location}.
	 * @param location
	 *            the location pointing to the file to be opened
	 * @return See above.
	 */
	public String[] getDefaultOpenCommandFor(URL location) {
		return new String[] { "open", location.toString() };
	}
}
