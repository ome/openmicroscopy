/*
 * Created on Feb 20, 2005
 */
package org.ome.srv.db.jena;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author josh
 */
public class JenaProperties {
    private static final String BUNDLE_NAME = "jena";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private JenaProperties() {
    }

    public static String getString(String key) {
        try {
         return RESOURCE_BUNDLE.getString(key);
         } catch (MissingResourceException e) {
         return '!' + key + '!';
         } 
    }
}