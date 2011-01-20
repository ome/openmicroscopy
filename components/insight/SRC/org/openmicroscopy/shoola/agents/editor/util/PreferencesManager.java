/*
 * org.openmicroscopy.shoola.agents.editor.util.PreferencesManager
 * 
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.util;

// Java imports
import java.util.prefs.Preferences;

//Third-party libraries

//Application-internal dependencies


/** 
 * A class for saving local preferences.
 * Uses the {@link java.util.prefs.Preferences} class to set and get 
 * preferences.
 * 
 * A number of static preference names are also defined.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PreferencesManager
{
	
	/**
	 * The key for a the <code>Current Files Folder</code> preference.
	 * This folder path is the last place that a user went to open a file. 
	 */
	public static final String CURRENT_FILES_FOLDER = 	"currentFilesFolder";
	
	/**
	 * The key for a the <code>Current Images Folder</code> preference.
	 * This folder path is the last place that a user went to open an image. 
	 */
	public static final String CURRENT_IMAGES_FOLDER = 	"currentImagesFolder";
	
	/**
	 * The key for a the <code>Current Export Folder</code> preference.
	 * This folder path is the last place that a user went to export a file. 
	 */
	public static final String CURRENT_EXPORT_FOLDER = 	"currentExportFolder";
	
	/**
	 * The key for a the <code>Root Files Folder</code> preference.
	 * This folder path is the last place that a user chose as the root to 
	 * their Editor directories. E.g. The root to index all
	 * editor files for searching. 
	 */
	public static final String ROOT_FILES_FOLDER = 		"rootFilesFolder";
	
	/**
	 * Sets a preference.
	 * 
	 * @param preferenceName 	The name of the preference.
	 * @param preferenceValue	The value to be saved.
	 */
	public static void setPreference(String preferenceName, 
			String preferenceValue)
	{
		if (preferenceValue == null) return;
		
		Preferences prefs = Preferences.userNodeForPackage
				(PreferencesManager.class);
		prefs.put(preferenceName, preferenceValue);
	}

	/**
	 * Gets a preference.
	 * 
	 * @param preferenceName 	The name of the preference.
	 * @return 		The value of the preference
	 */
	public static String getPreference(String preferenceName)
	{
		Preferences prefs = Preferences.userNodeForPackage
				(PreferencesManager.class);
		return prefs.get(preferenceName, null);	// null is default value
	}
	
	/**
	 * Gets a preference, and returns True if the value of the 
	 * preference is <code>true</code>.
	 * 
	 * @param preferenceName 	The name of the preference.
	 * @return 		The value of the preference
	 */
	public static boolean isPreferenceTrue(String preferenceName)
	{
		if (getPreference(preferenceName) == null) return false;
		
		return (getPreference(preferenceName).equals("true"));
	}
	
}
