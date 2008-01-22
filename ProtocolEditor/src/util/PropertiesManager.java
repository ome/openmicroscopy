package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
	
	public static final String CURRENT_FILES_FOLDER = "currentFilesFolder";
	public static final String CURRENT_EXPORT_FOLDER = "currentExportFolder";
	
	public static final String PROPERTIES_FILE_NAME = "properties";
	
	PropertiesManager uniqueInstance = new PropertiesManager();
	
	private PropertiesManager() {
		
		Properties properties = new Properties();

		try {
			FileInputStream in;
			in = new FileInputStream(PROPERTIES_FILE_NAME);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * These properties set by the methods below are lost when this application closes.
	 * To maintain properties between sessions, need to save them to a particular location ??
	 * using methods described here	
	 * http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
	 */
	
	public static String getProperty(String key) {
		
		System.out.println("PropertiesManager getProperty(" + key + ") = " + System.getProperty(key));
		
		return System.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		
		System.out.println("PropertiesManager setProperty() key = " + key + ", value = " + value);
		
		System.setProperty(key, value);
	}

}
