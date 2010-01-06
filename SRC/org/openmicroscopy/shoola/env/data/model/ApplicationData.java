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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Third-party libraries
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Hosts information about an external application.
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
public class ApplicationData
{

	/** The default location on <code>MAC</code> platform. */
	public static final String LOCATION_MAC = "/Applications";
	
	/** The default location on <code>Windows</code> platform. */
	public static final String LOCATION_WINDOWS = "/Applications";
	
	/** The default location <code>Linux</code> platform. */
	public static final String LOCATION_LINUX = "/Applications";
	
	/** Path to the resources of the application on MAC. */
	private static final String RESOURCES_MAC = "/Contents/Resources/";
	
	/** Path to the resources of the application on MAC. */
	private static final String EXECUTABLE_MAC = "/Contents/MacOS/";

	/** The file to look to retrieve the information. */
	private static final String INFO_FILE_MAC = "/Contents/Info.plist";
	
	/** Tag identifying the executable. */
	private static final String EXECUTABLE_TAG_MAC = "CFBundleExecutable";
	
	/** Tag identifying the icon associated to the application. */
	private static final String ICON_TAG_MAC = "CFBundleIconFile";
	
	/** Tag identifying the name of the application. */
	private static final String NAME_TAG_MAC = "CFBundleName";

	/** The path to the application. */
	private File file;
	
	/** The name of the application. */
	private String applicationName;
	
	/** The icon associated. */
	private Icon applicationIcon;
	
	private String executable;
	
	/** 
	 * Converts the <code>.icns</code> to an icon.
	 * 
	 * @param path The path to the file to convert.
	 * @return See above.
	 */
	private Icon convert(String path)
	{
		if (path == null) return null;
		return new ImageIcon(path);
	}
	
	
	/** Parses the file. */
	private void parseMac()
	{
		try {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = f.newDocumentBuilder();
			Document doc = builder.parse(
					new File(file.getAbsolutePath()+INFO_FILE_MAC));
			//Extract the info
			NodeList list = doc.getElementsByTagName("dict");
			Node node, child, s;
			NodeList nodes;
			String value, name;
			for (int i = 0; i < list.getLength() ; i++){
				node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					nodes = node.getChildNodes();
					for (int j = 0; j < nodes.getLength(); j++) {
						child = nodes.item(j);
						if (child.getNodeType() == Node.ELEMENT_NODE &&
								child.getNodeName().equals("key")) {
							value = child.getTextContent().trim();
							if (EXECUTABLE_TAG_MAC.equals(value)) {
								if (child.getNextSibling() != null) {
									s = child.getNextSibling().getNextSibling();
									executable = getApplicationPath()
										+EXECUTABLE_MAC
										+s.getTextContent().trim();
								}
							} else if (ICON_TAG_MAC.equals(value)) {
								if (child.getNextSibling() != null) {
									s = child.getNextSibling().getNextSibling();
									name = 
										getApplicationPath()+RESOURCES_MAC+
										s.getTextContent().trim();
									applicationIcon = convert(name);
								}
							} else if (NAME_TAG_MAC.equals(value)) {
								if (child.getNextSibling() != null) {
									s = child.getNextSibling().getNextSibling();
									applicationName = s.getTextContent().trim();
								}
							}
						}
					}
				}//end of if clause
			}
		} catch (Exception e) {
			applicationName = UIUtilities.removeFileExtension(
					file.getAbsolutePath());
			applicationIcon = null;
			executable = getApplicationPath();
		}
		if (applicationName == null || applicationName.length() == 0)
			applicationName = UIUtilities.removeFileExtension(file.getName());
		if (executable == null || executable.length() == 0)
			executable = getApplicationPath();
	}
	
	/**
	 * Returns the default location depending on the OS.
	 * 
	 * @return See above.
	 */
	public static String getDefaultLocation()
	{
		if (UIUtilities.isMacOS()) return LOCATION_MAC;
		if (UIUtilities.isWindowsOS()) return LOCATION_WINDOWS;
		return LOCATION_LINUX;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file the application.
	 */
	public ApplicationData(File file)
	{
		this.file = file;
		if (UIUtilities.isMacOS()) parseMac();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param path The path to the application.
	 */
	public ApplicationData(String path)
	{
		this(new File(path));
	}
	
	/**
	 * Returns the name of the application.
	 * 
	 * @return See above.
	 */
	public String getApplicationName() { return applicationName; }
	
	/**
	 * Returns the icon associated to the application.
	 * 
	 * @return See above.
	 */
	public Icon getApplicationIcon()
	{
		return applicationIcon;
	}
	
	/**
	 * Returns the application's path.
	 * 
	 * @return See above.
	 */
	public String getApplicationPath()
	{
		return file.getAbsolutePath();
	}
	
	/**
	 * Returns the arguments.
	 * 
	 * @return See above.
	 */
	public List<String> getArguments()
	{
		List<String> list = new ArrayList<String>(); 
		if (executable != null && executable.length() > 0)
			list.add(executable);
		return list;
	}
	
}
