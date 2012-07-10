/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Central configuration for OMERO properties from (in order):
 * <ul>
 * <li>Any injected {@link Properties} instances</li>
 * <li>Java {@link System#getProperties()}</li>
 * <li>Any configured property files</li>
 * </ul>
 * 
 * As of OMERO 4.2, server configurations are not stored in Java's
 * Preferences API but in an IceGrid xml file under etc/grid of the server
 * installation. The properties are set in the config file on node startup, for
 * example in var/master/servers/Blitz-0/config/config. When the Java process
 * starts, {@link ome.services.blitz.Entry} places the values in
 * {#link {@link System#getProperties()}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see <a href="https://trac.openmicroscopy.org.uk/ome/ticket/800">#800</a>
 */
public class PreferenceContext extends PropertyPlaceholderConfigurer {

    private final static Log log = LogFactory.getLog(PreferenceContext.class);

    final private Map<String, Preference> preferences = new ConcurrentHashMap<String, Preference>();

    private PropertyPlaceholderHelper helper;
    
    private String path;

    /**
     * By default, configures this instance for
     * {@link PropertyPlaceholderConfigurer#SYSTEM_PROPERTIES_MODE_OVERRIDE} as
     * well as ignoring unfound resources.
     */
    public PreferenceContext() {
        setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_OVERRIDE);
        setIgnoreResourceNotFound(true);
        helper = new PropertyPlaceholderHelper(
                PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX,
                PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX,
                PropertyPlaceholderConfigurer.DEFAULT_VALUE_SEPARATOR,
                false); // Note, we want the IllegalArgumentThrown for catching.
    }

    /**
     * Lookup method for getting access to the {@link #mergeProperties() merged
     * properties} for this instance.
     */
    public String getProperty(String key) {
        try {
            //return parseStringValue("${" + key + "}", mergeProperties(),
            //                            new java.util.HashSet<String>());
            key = "${" + key + "}";
            return helper.replacePlaceholders(key,
                    new PropertyPlaceholderConfigurerResolver(mergeProperties()));
        } catch (IllegalArgumentException iae) {
            return null; // From change of helper in Spring 3.0
        } catch (BeanDefinitionStoreException bdse) {
            return null; // Unknown property. Ok
        } catch (IOException e) {
            log.error("Error on mergeProperties()", e);
            return null;
        } catch (Exception exc) {
            log.error("Other exception on getProperty", exc);
            return null;
        }
    }

    /**
     * With ticket:2214, preferences are no longer mutable. For that, we will
     * need a python server which can update the XML file.
     */
    public void setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }

    public void setPreferences(List<Preference> preferences) {
        for (Preference pref : preferences) {
            this.preferences.put(pref.getName(), pref);
        }

    }

    // Defined Preferences
    // =========================================================================

    public String resolveAlias(String key) {

        if (preferences.containsKey(key)) {
            return key;
        }

        for (String current : preferences.keySet()) {
            Preference preference = preferences.get(current);
            if (preference.hasAlias(key)) {
                return current;
            }
        }

        return key;
    }

    public boolean checkDatabase(String key) {
        Preference preference = getPreferenceOrDefault(key);

        return preference.isDb();
    }

    public boolean canRead(EventContext ec, String key) {
        Preference preference = getPreferenceOrDefault(key);

        switch (preference.getVisibility()) {
        case all:
            return true;
        case admin:
            return ec.isCurrentUserAdmin();
        case hidden:
        default:
            return false;
        }
    }

    // Helpers
    // =========================================================================

    private Preference getPreferenceOrDefault(String key) {
        Preference preference = preferences.get(key);

        if (preference == null) {
            preference = new Preference();
        }
        return preference;
    }
    
    // Copied from PropertyPlaceholderConfigurer
    private class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

        private final Properties props;

        private PropertyPlaceholderConfigurerResolver(Properties props) {
            this.props = props;
        }

        public String resolvePlaceholder(String placeholderName) {
            return PreferenceContext.this.resolvePlaceholder(placeholderName,
                    props, PreferenceContext.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        }
    }

}
