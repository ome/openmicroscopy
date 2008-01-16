/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;

/**
 * Provides static access for the creation of singleton and non-singleton
 * application contexts. Also provides context names as constant fields which
 * can be used for the lookup of particular contexts, through either
 * {@link #getInstance(String)} or
 * {@link ome.system.ServiceFactory#ServiceFactory(String)}.
 * 
 * By passing a {@link java.util.Properties} instance into the
 * {@link #getClientContext(Properties)} method, a non-static version is
 * created. Currently this is only supported for the client context.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @DEV.TODO Code duplication with prefs.java
 */
public class PreferenceContext extends PreferencesPlaceholderConfigurer {

    private final static Log log = LogFactory.getLog(PreferenceContext.class);

    public final static String DEFAULT = "default";

    public final static String ROOT = "omero.prefs";

    public final static String ENV = "OMERO";

    /**
     * Automatically sets the {@link #setUserTreePath(String)} value, overriding
     * any value provided in configuration.
     */
    @Override
    public void afterPropertiesSet() {
        String OMERO = System.getenv(ENV);
        if (OMERO == null) {
            OMERO = Preferences.userRoot().node(ROOT).get(DEFAULT, null);
        }
        if (OMERO == null) {
            OMERO = Preferences.systemRoot().node(ROOT).get(DEFAULT, null);
        }

        // Ok, then if we've found something use it.
        if (OMERO != null) {
            setUserTreePath(ROOT + "." + OMERO);
        }

        super.afterPropertiesSet();
    }

    public String getProperty(String key) {
        try {
            return resolvePlaceholder(key, this.mergeProperties());
        } catch (IOException e) {
            log.error("Error trying to retrieve property:" + key, e);
            return null;
        }
    }
}
