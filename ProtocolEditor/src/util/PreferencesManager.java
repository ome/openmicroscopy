package util;

import java.util.prefs.Preferences;


public class PreferencesManager {
	
	
	public static final String CURRENT_FILES_FOLDER = "currentFilesFolder";
	
	public static final String CURRENT_EXPORT_FOLDER = "currentExportFolder";
	
	public static final String ROOT_FILES_FOLDER = "rootFilesFolder";
	
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
}
