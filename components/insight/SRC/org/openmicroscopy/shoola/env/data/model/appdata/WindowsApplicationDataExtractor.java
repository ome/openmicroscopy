/*
 * org.openmicroscopy.shoola.env.data.model.appdata.WindowsApplicationDataExtractor 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Provides the Windows implementation to extract application properties using
 * the {@link LANGANDCODEPAGE} class with the <a
 * href="https://github.com/twall/jna">JNA</a> library.
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class WindowsApplicationDataExtractor implements
		ApplicationDataExtractor {

	/**
	 * Windows specific keys for accessing application properties @see
	 * http://msdn
	 * .microsoft.com/en-us/library/windows/desktop/ms647464(v=vs.85).aspx
	 */
	private static final String APPLICATION_PROPERTY_KEY_COMMENTS = "Comments";
	private static final String APPLICATION_PROPERTY_KEY_COMPANY_NAME = "CompanyName";
	private static final String APPLICATION_PROPERTY_KEY_FILE_DESCRIPTION = "FileDescription";
	private static final String APPLICATION_PROPERTY_KEY_FILE_VERSION = "FileVersion";
	private static final String APPLICATION_PROPERTY_KEY_INTERNAL_NAME = "InternalName";
	private static final String APPLICATION_PROPERTY_KEY_LEGAL_COPYRIGHT = "LegalCopyright";
	private static final String APPLICATION_PROPERTY_KEY_LEGAL_TRADEMARK = "LegalTrademark";
	private static final String APPLICATION_PROPERTY_KEY_ORIGINAL_FILE_NAME = "OriginalFileName";
	private static final String APPLICATION_PROPERTY_KEY_PRODUCT_NAME = "ProductName";
	private static final String APPLICATION_PROPERTY_KEY_PRODUCT_VERSION = "ProductVersion";
	private static final String APPLICATION_PROPERTY_KEY_PRIVATE_BUILD = "PrivateBuild";
	private static final String APPLICATION_PROPERTY_KEY_SPECIAL_BUILD = "SpecialBuild";

	private static final String WINDOWS_QUERYPATH_TRANSLATION = "\\VarFileInfo\\Translation";

	/** The default application location on <code>Windows</code> platform. */
	private static final String LOCATION_WINDOWS = "C:\\Program Files\\";

	/**
	 * Gets the system icon for the application
	 * 
	 * @param file
	 *            the file location of the application to retrieve the icon for
	 * @return the icon associated with this application
	 */
	private Icon getSystemIconFor(File file) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);

		return icon;
	}

	/**
	 * Allocates a portion of memory for use by JNA
	 * 
	 * @param size
	 *            the size of the memory to allocate
	 * @return a pointer to the memory location
	 */
	private Pointer allocateBuffer(int size) {
		byte[] bufferarray = new byte[size];
		Pointer buffer = new Memory(bufferarray.length);

		return buffer;
	}

	/**
	 * Returns the language code page string for the application on the current
	 * windows platform
	 * 
	 * @param applicationPath
	 * @param fileVersionInfoSize
	 * @return See above.
	 * @throws Exception
	 */
	private String getTranslation(String applicationPath,
			int fileVersionInfoSize) throws Exception {
		Pointer lpData = allocateBuffer(fileVersionInfoSize);

		boolean fileVersionInfoSuccess = Version.INSTANCE.GetFileVersionInfo(
				applicationPath, 0, fileVersionInfoSize, lpData);

		if (!fileVersionInfoSuccess)
			throw new Exception("Unable to load application information");

		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();

		boolean verQueryValSuccess = ExecuteQuery(lpData,
				WINDOWS_QUERYPATH_TRANSLATION, lplpBuffer, puLen);

		if (!verQueryValSuccess)
			throw new Exception("Unable to load application information");

		LANGANDCODEPAGE lplpBufStructure = new LANGANDCODEPAGE(
				lplpBuffer.getValue());
		lplpBufStructure.read();

		StringBuilder hexBuilder = new StringBuilder();

		String languageAsHex = String
				.format("%04x", lplpBufStructure.wLanguage);
		String codePageAsHex = String
				.format("%04x", lplpBufStructure.wCodePage);

		hexBuilder.append(languageAsHex);
		hexBuilder.append(codePageAsHex);

		return hexBuilder.toString();
	}

	/**
	 * Performs the windows specific query related to application properties
	 * 
	 * @param lpData
	 * @param lpSubBlock
	 * @param lplpBuffer
	 * @param puLen
	 * @return
	 */
	private boolean ExecuteQuery(Pointer lpData, String lpSubBlock,
			PointerByReference lplpBuffer, IntByReference puLen) {
		return Version.INSTANCE.VerQueryValue(lpData, lpSubBlock, lplpBuffer,
				puLen);
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
	private String getFilePropertyValue(String applicationPath,
			int fileVersionInfoSize, String translation, String propertyKey)
			throws Exception {

		Pointer lpData = allocateBuffer(fileVersionInfoSize);

		boolean fileInfoStatusSuccess = Version.INSTANCE.GetFileVersionInfo(
				applicationPath, 0, fileVersionInfoSize, lpData);
		if (!fileInfoStatusSuccess)
			throw new Exception("Unable to load application information");

		String queryPath = "\\StringFileInfo\\" + translation + "\\"
				+ propertyKey;

		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();

		boolean verQuerySuccess = ExecuteQuery(lpData, queryPath, lplpBuffer,
				puLen);
		if (!verQuerySuccess)
			throw new Exception("Unable to load application information");

		int descLength = puLen.getValue();

		Pointer pointerToPropertyStringValue = lplpBuffer.getValue();
		char[] charBuffer = pointerToPropertyStringValue.getCharArray(0,
				descLength);

		String propertyValue = new String(charBuffer);

		return propertyValue.trim();
	}

	/**
	 * @return the Windows specific directory where applications are located
	 */
	public String getDefaultAppDirectory() {
		return LOCATION_WINDOWS;
	}

	/**
	 * Extracts the application data for the application on a windows platform
	 * 
	 * @param file
	 *            the file pointing to the application's location on disk
	 * @return the {@link ApplicationData} object representing this applications
	 *         system properties
	 * @throws FileNotFoundException
	 *             if the file specified is null or does not exist on disk
	 */
	public ApplicationData extractAppData(File file) throws Exception {

		String absPath = file.getAbsolutePath();

		if (file == null || !file.exists())
			throw new FileNotFoundException(absPath);

		Icon icon = getSystemIconFor(file);

		IntByReference dwDummy = new IntByReference(0);
		int fileVersionInfoSize = com.sun.jna.platform.win32.Version.INSTANCE
				.GetFileVersionInfoSize(absPath, dwDummy);

		String applicationName = FilenameUtils.getBaseName(absPath);

		if (fileVersionInfoSize > 0) {
			String translation = getTranslation(absPath, fileVersionInfoSize);

			String fileDescription = getFilePropertyValue(absPath,
					fileVersionInfoSize, translation,
					APPLICATION_PROPERTY_KEY_FILE_DESCRIPTION);
			String productName = getFilePropertyValue(absPath,
					fileVersionInfoSize, translation,
					APPLICATION_PROPERTY_KEY_PRODUCT_NAME);

			if (fileDescription != "")
				applicationName = fileDescription;
			else if (productName != "")
				applicationName = productName;
		}

		return new ApplicationData(icon, applicationName, absPath);
	}

	/**
	 * Returns the command string to launch the default application for the
     *          file specified by {@code location}.
	 * @param location
	 *            the location pointing to the object to be opened
	 * @return See above.
	 */
	public String[] getDefaultOpenCommandFor(URL location) {
		return new String[] { "cmd", "/c", "start", location.toString() };
	}
}