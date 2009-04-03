
/*
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

package omeroCal.util;

import java.util.prefs.Preferences;


public class PreferencesManager {
	
	/**
	 * This attribute stores the date-time (in UTC milliseconds) that the application shut
	 * down, so that when it restarts, the intervening time can be checked for alarms.
	 */
	public static final String SHUTDOWN_TIME_UTC = "shutdownTimeUTC";
	
	/**
	 * Sets a preference.
	 * 
	 * @param preferenceName 	The name of the preference.
	 * @param preferenceValue	The value to be saved.
	 */
	public static void setPreference(String preferenceName, String preferenceValue)
	{
		if (preferenceValue == null) return;
		Preferences prefs = Preferences.userNodeForPackage(PreferencesManager.class);
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
		Preferences prefs = Preferences.userNodeForPackage(PreferencesManager.class);
		return prefs.get(preferenceName, null);	// null is default value
	}
	
	/**
	 * Gets a preference.
	 * 
	 * @param preferenceName 	The name of the preference.
	 * @return 		The value of the preference
	 */
	public static boolean isPreferenceTrue(String preferenceName)
	{
		if (getPreference(preferenceName) == null) {
			return false;
		} else {
			return (getPreference(preferenceName).equals("true"));
		}
	}
	
	
}
