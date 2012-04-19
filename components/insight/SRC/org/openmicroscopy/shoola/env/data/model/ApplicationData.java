/*
 * org.openmicroscopy.shoola.env.data.model.ApplicationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.io.File;
import java.util.List;

import javax.swing.Icon;

import org.openmicroscopy.shoola.env.data.model.appdata.ApplicationDataExtractor;
import org.openmicroscopy.shoola.env.data.model.appdata.MacApplicationDataExtractor;
import org.openmicroscopy.shoola.env.data.model.appdata.WindowsApplicationDataExtractor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Provides information about an external application.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @author Scott Littlewood&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ApplicationData {

	private static ApplicationDataExtractor extractor;

	/** The default location <code>Linux</code> platform. */
	public static final String LOCATION_LINUX = "/Applications";

	/** The path to the application. */
	private File file;

	/** The name of the application. */
	private String applicationName;

	/** The icon associated. */
	private Icon applicationIcon;

	/** The path to the executable. */
	private String executable;

	/** The commands to add. */
	private List<String> commands;

	/**
	 * Static constructor that creates the platform specific app data extractor
	 */
	static {
		if (UIUtilities.isWindowsOS())
			extractor = new WindowsApplicationDataExtractor();
		if (UIUtilities.isMacOS())
			extractor = new MacApplicationDataExtractor();
	}

	/**
	 * Returns the default location depending on the OS.
	 * 
	 * @return See above.
	 */
	public static String getDefaultLocation() {
		return extractor.getDefaultAppDirectory();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            the application.
	 * @throws Exception
	 */
	public ApplicationData(File file) throws Exception {
		this.file = file;

		if (!file.exists())
			throw new Exception("Application does nto exists @ "
					+ file.getAbsolutePath());

		try {
			ApplicationData data = extractor.extractAppData(file);

			this.applicationName = data.applicationName;
			this.executable = data.executable;
			this.applicationIcon = data.applicationIcon;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ApplicationData(Icon icon, String applicationName,
			String executablePath) {
		this.applicationIcon = icon;
		this.applicationName = applicationName;
		this.executable = executablePath;
	}

	/**
	 * Returns the name of the application.
	 * 
	 * @return See above.
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Returns the icon associated to the application.
	 * 
	 * @return See above.
	 */
	public Icon getApplicationIcon() {
		return applicationIcon;
	}

	/**
	 * Returns the application's path.
	 * 
	 * @return See above.
	 */
	public String getApplicationPath() {
		return file.getAbsolutePath();
	}

	/**
	 * Returns the command line arguments for the application.
	 * 
	 * @return See above.
	 */
	public Iterable<String> getCommandLineArguments() {
		return commands;
	}

	/**
	 * Sets the command line arguments for the application.
	 * 
	 * @param commands
	 *            The commands to set.
	 */
	public void setCommandLineArguments(List<String> commands) {
		this.commands = commands;
	}

	/**
	 * Overridden to return the name of the application.
	 * 
	 * @see ApplicationData#toString()
	 */
	public String toString() {
		return applicationName;
	}

	public static String buildCommand(ApplicationData data, File file) {
		StringBuilder commandBuilder = new StringBuilder();

		if (data == null) {
			if (UIUtilities.isMacOS()) {
				commandBuilder.append("open ");
			}
		} else if (data != null) {
			commandBuilder.append(data.executable);
			commandBuilder.append(" ");

			Iterable<String> arguments = data.getCommandLineArguments();

			if (arguments != null) {
				for (String argument : arguments) {
					commandBuilder.append(argument);
					commandBuilder.append(" ");
				}
			}
		}

		commandBuilder.append(file.getAbsolutePath());

		return commandBuilder.toString();
	}
}
