/*
 * org.openmicroscopy.shoola.env.data.model.ApplicationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.env.data.model.appdata.ApplicationDataExtractor;
import org.openmicroscopy.shoola.env.data.model.appdata.LinuxApplicationDataExtractor;
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
 * @since 3.0-Beta4
 */
public class ApplicationData {

	/**
	 * The platform specific extractor used to obtain application information
	 */
	private static ApplicationDataExtractor extractor;

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

	/** The script to use.*/
	private String script;

	/** Flag indicating if the application is a known application.*/
	private boolean registered;

	/**
	 * Static constructor that creates the platform specific application
	 * data extractor.
	 */
	static {
		if (UIUtilities.isWindowsOS())
			extractor = new WindowsApplicationDataExtractor();
		else if (UIUtilities.isMacOS())
			extractor = new MacApplicationDataExtractor();
		else if (UIUtilities.isLinuxOS())
			extractor = new LinuxApplicationDataExtractor();
	}

	/**
	 * Returns the default location depending on the OS.
	 * 
	 * @return See above.
	 */
	public static String getDefaultLocation() {
		return extractor.getDefaultAppDirectory();
	}

	/** Creates a new instance. */
    public ApplicationData() {}

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            the {@link File} pointing to the location of the application.
	 * @throws Exception
	 *             thrown if the File points to a location that is incorrect
	 */
	public ApplicationData(File file) throws Exception {
		this.file = file;
		this.commands = new ArrayList<String>();

		if (!file.exists())
			throw new Exception("Application does not exist @ "
					+ file.getAbsolutePath());
		ApplicationData data = extractor.extractAppData(file);

        this.applicationName = data.applicationName;
        this.executable = data.executable;
        this.applicationIcon = data.applicationIcon;
        register();
	}

	/**
	 * Creates a new instance of @link {@link ApplicationData}
	 * 
	 * @param icon
	 *            the icon of the application
	 * @param applicationName
	 *            the name of the application
	 * @param executablePath
	 *            the system path to the application executable
	 */
	public ApplicationData(Icon icon, String applicationName,
			String executablePath) {
		this.applicationIcon = icon;
		this.applicationName = applicationName;
		this.executable = executablePath;
		this.commands = new ArrayList<String>();
		register();
	}

	/**
	 * Returns the name of the application.
	 * 
	 * @return See above.
	 */
	public String getApplicationName() {
	    if (StringUtils.isEmpty(applicationName)) return executable;
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

	/**
	 * Builds the command used to open the file,
	 * if the {@link ApplicationData} is <code>null</code>
	 * then the platform specific default command is built.
	 * 
	 * @param file the {@link File} representing the location of the file to
	 * open.
	 * @return the command string that when executed should open the file.
	 * @throws MalformedURLException
	 *             when the file referenced is unable to be converted to a URL
	 *             in the format file://...
	 */
	public String[] buildCommand(File file)
			throws MalformedURLException {

		if (this.executable == null) //default installation.
			return extractor.getDefaultOpenCommandFor(file.toURI().toURL());

		List<String> commandLine = new ArrayList<String>();
		commandLine.add(executable);

		for (String commandArg : getCommandLineArguments()) {
			commandLine.add(commandArg);
		}

		if (file != null) commandLine.add(file.getAbsolutePath());

		return commandLine.toArray(new String[0]);
	}

	/**
	 * Returns the script to use if any.
	 *
	 * @return See above.
	 */
	public String getScript() { return script; }

	/**
	 * Sets the script.
	 *
	 * @param script The value to set.
	 */
	public void setScript(String script) { this.script = script; }

	/**
	 * Returns <code>true</code> if the application is a known application
	 * e.g. FLIMfit, <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
    public boolean isRegistered() { return registered; }

    /** Registers the application as a known application.*/
    public void register() { registered = true; }

}
