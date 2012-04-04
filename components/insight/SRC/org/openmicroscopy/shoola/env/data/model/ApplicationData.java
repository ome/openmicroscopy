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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.openmicroscopy.shoola.env.data.util.Parser;
import org.openmicroscopy.shoola.util.image.io.IconReader;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Hosts information about an external application.
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

	/** The default location on <code>MAC</code> platform. */
	public static final String LOCATION_MAC = "/Applications";

	/** The default location on <code>Windows</code> platform. */
	public static final String LOCATION_WINDOWS = "C:\\Program Files";

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
	 * Converts the <code>.icns</code> to an icon.
	 * 
	 * @param path
	 *            The path to the file to convert.
	 * @return See above.
	 */
	private static Icon convert(String path) {
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

	/** Parses the file. */
	private void parseMac() {
		try {
			Map<String, Object> m = Parser.parseInfoPList(getApplicationPath());
			executable = (String) m.get(Parser.EXECUTABLE_PATH);
			applicationIcon = convert((String) m.get(Parser.EXECUTABLE_ICON));
			applicationName = (String) m.get(Parser.EXECUTABLE_NAME);
		} catch (Exception e) {
			applicationName = UIUtilities.removeFileExtension(file.getAbsolutePath());
			applicationIcon = null;
			executable = getApplicationPath();
		}
		if (applicationName == null || applicationName.length() == 0)
			applicationName = UIUtilities.removeFileExtension(file.getName());
		if (executable == null || executable.length() == 0)
			executable = getApplicationPath();
		if (executable.contains("Microsoft"))
			executable = null;
	}

	/**
	 * Returns the default location depending on the OS.
	 * 
	 * @return See above.
	 */
	public static String getDefaultLocation() {
		if (UIUtilities.isMacOS())
			return LOCATION_MAC;
		if (UIUtilities.isWindowsOS())
			return LOCATION_WINDOWS;
		return LOCATION_LINUX;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            the application.
	 */
	public ApplicationData(File file) {
		this.file = file;
		String name = file.getName();
		if (name == null || name.length() == 0) {
			applicationName = "";
			applicationIcon = null;
			executable = null;
		} else {
			if (UIUtilities.isMacOS())
				parseMac();
			if (UIUtilities.isWindowsOS())
				parseWindows();
		}
	}

	private Icon getSystemIconFor(File file) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);

		return icon;
	}

	private void parseWindows() {
		applicationIcon = getSystemIconFor(file);

		try {
			applicationName = getFilePropertyValue(file.getAbsolutePath(), "FileDescription");
			executable = file.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Pointer allocateBuffer(int size) {
		byte[] bufferarray = new byte[size];
		Pointer buffer = new Memory(bufferarray.length);

		return buffer;
	}

	private String getTranslation(String applicationPath, int fileVersionInfoSize) throws Exception {
		Pointer lpData = allocateBuffer(fileVersionInfoSize);

		boolean fileVersionInfoSuccess = com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfo(applicationPath, 0, fileVersionInfoSize, lpData);

		if (!fileVersionInfoSuccess)
			throw new Exception("Unable to load application information");

		String queryPath = "\\VarFileInfo\\Translation";

		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();

		boolean verQueryValSuccess = ExecuteQuery(lpData, queryPath, lplpBuffer, puLen);

		if (!verQueryValSuccess)
			throw new Exception("Unable to load application information");

		LANGANDCODEPAGE lplpBufStructure = new LANGANDCODEPAGE(lplpBuffer.getValue());
		lplpBufStructure.read();

		StringBuilder hexBuilder = new StringBuilder();

		String languageAsHex = String.format("%04x", lplpBufStructure.wLanguage);
		String codePageAsHex = String.format("%04x", lplpBufStructure.wCodePage);

		hexBuilder.append(languageAsHex);
		hexBuilder.append(codePageAsHex);

		return hexBuilder.toString();
	}

	public boolean ExecuteQuery(Pointer lpData, String lpSubBlock, PointerByReference lplpBuffer, IntByReference puLen) {
		return com.sun.jna.platform.win32.Version.INSTANCE.VerQueryValue(lpData, lpSubBlock, lplpBuffer, puLen);
	}

	/**
	 * Extracts the information item with key @propertyKey from the application
	 * properties found in @applicationPath
	 * 
	 * @param applicationPath
	 * @param fileVersionInfoSize
	 * @param translation
	 * @param propertyKey
	 * @return
	 * @throws Exception
	 */
	private String getFilePropertyValue(String applicationPath, String propertyKey) throws Exception {

		IntByReference dwDummy = new IntByReference(0);

		int fileVersionInfoSize = com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfoSize(applicationPath, dwDummy);

		String translation = getTranslation(applicationPath, fileVersionInfoSize);

		Pointer lpData = allocateBuffer(fileVersionInfoSize);

		String queryPath = "\\StringFileInfo\\" + translation + "\\" + propertyKey;

		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();

		boolean verQuerySuccess = ExecuteQuery(lpData, queryPath, lplpBuffer, puLen);
		if(!verQuerySuccess)
			throw new Exception("Unable to load application information");
		
		int descLength = puLen.getValue();

		Pointer pointerToPropertyStringValue = lplpBuffer.getValue();
		char[] charBuffer = pointerToPropertyStringValue.getCharArray(0, descLength);

		String propertyValue = new String(charBuffer);

		return propertyValue;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param path
	 *            The path to the application.
	 */
	public ApplicationData(String path) {
		this(new File(path));
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
	 * Returns the arguments.
	 * 
	 * @return See above.
	 */
	public List<String> getArguments() {
		List<String> list = new ArrayList<String>();
		if (UIUtilities.isMacOS()) {
			if (executable != null && executable.length() > 0)
				list.add(executable);
			else
				list.add("open");
			if (commands != null && commands.size() > 0)
				list.addAll(commands);
		} else if (UIUtilities.isWindowsOS()) {

		}
		return list;
	}

	/**
	 * Sets the commands.
	 * 
	 * @param commands
	 *            The commands to set.
	 */
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	/**
	 * Overridden to return the name of the application.
	 * 
	 * @see ApplicationData#toString()
	 */
	public String toString() {
		if (applicationName == null)
			return "";
		return applicationName;
	}

}
