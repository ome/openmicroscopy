/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Central configuration for OMERO properties from (in order):
 * <ul>
 * <li>Any injected {@link Properties} instances</li>
 * <li>Java {@link Preferences}</li>
 * <li>Java {@link System#getProperties()}</li>
 * <li>Any configured property files</li>
 * </ul>
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/800">#800</a>
 * @DEV.TODO Code duplication with prefs.java
 */
public class PreferenceContext extends PreferencesPlaceholderConfigurer {
// FIXME    implements ApplicationContextAware {

    private final static Log log = LogFactory.getLog(PreferenceContext.class);

    public final static String DEFAULT = "default";

    public final static String ROOT = "/omero/prefs";

    public final static String ENV = "OMERO_CONFIG";

    final private Map<String, Preference> preferences = new ConcurrentHashMap<String, Preference>();

    private OmeroContext ctx;

    /**
     * By default, configures this instance for
     * {@link PropertyPlaceholderConfigurer#SYSTEM_PROPERTIES_MODE_OVERRIDE} as
     * well as ignoring unfound resources. The {@link #setUserTreePath(String)}
     * user-tree is set according to a similar logic as in the {@link prefs}
     * command-line tool, using first {@link #ENV} from the environment if
     * present, otherwise the value of "omero.prefs.default".
     */
    public PreferenceContext() {
        setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_OVERRIDE);
        setIgnoreResourceNotFound(true);

        String OMERO = System.getenv(ENV);
        if (OMERO == null) {
            OMERO = Preferences.userRoot().node(ROOT).get(DEFAULT, null);
        }
        if (OMERO == null) {
            OMERO = Preferences.systemRoot().node(ROOT).get(DEFAULT, null);
        }

        // Ok, then if we've found something use it.
        if (OMERO != null) {
            setUserTreePath(ROOT + "/" + OMERO);
        }

    }

    /**
     * Lookup method for getting access to the {@link #mergeProperties() merged
     * properties} for this instance.
     */
    public String getProperty(String key) {
        try {
            return resolvePlaceholder(key, this.mergeProperties());
        } catch (IOException e) {
            log.error("Error trying to retrieve property:" + key, e);
            return null;
        }
    }
 
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }
    
    @Override
    protected void processProperties(ConfigurableListableBeanFactory arg0,
            Properties arg1) throws BeansException {
        super.processProperties(arg0, arg1);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        /*
        Map<String, Object> ps = this.ctx.getBeansOfType(Preference.class);
        for (String key : ps.keySet()) {
            this.preferences.put(key, (Preference) ps.get(key));
        }
        */
        
    }
    
    // Defined Preferences
    // =========================================================================

    public boolean checkDatabase(String key) {
        return false;
    }

    public boolean canRead(EventContext ec, String key) {
        Preference preference = preferences.get(key);
        
        if (preference == null || preference.visibility == null) {
            return false;
        }
        
        switch (preference.visibility) {
        case all:
            return true;
        case admin:
            return ec.isCurrentUserAdmin();
        case hidden:
        default:
            return false;
        }
    }

}