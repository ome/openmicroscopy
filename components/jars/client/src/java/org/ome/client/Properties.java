/*
 * Created on Feb 12, 2005
*/
package org.ome.omero.client;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author josh
*/
public class Properties {
	private static final String BUNDLE_NAME = "org.ome.omero.client.client";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Properties() {
	}

	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}