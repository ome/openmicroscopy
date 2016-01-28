/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;

/**
 * Definition of a server configuration variable ("preference") along with its
 * mutability, visibility, aliases and other important information. These
 * {@link Preference preferences} are defined in ome/config.xml along with the
 * {@link PreferenceContext}, and the default values are defined in the
 * etc/*.properties files which get stored in the final jars.
 * 
 * For any configuration which does not have an explicit mapping, the default
 * will be as if "new Preference()" is called. See the individual fields below
 * for more information.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/800">#800</a>
 */
public class Preference implements BeanNameAware {

    public enum Visibility {
        hidden, all, admin, user;
    }

    /**
     * Whether or not an administrator can change this property remotely. By
     * default this value is true, since otherwise creating new configuration
     * values would be impossible. Therefore, it is necessary to explicitly
     * specify all configuration keys which should be immutable.
     */
    private boolean mutable = true;

    /**
     * Whether or not a configuration value can be found in the database. Some
     * values inherently make no sense to store in the db, like the db
     * connection information. All other properties should be storable there,
     * and so {@link #db} is true by default.
     */
    private boolean db = true;

    /**
     * Whether or not a configuration value can be found in the system
     * preferences. True by default.
     */
    private boolean prefs = true;

    /**
     * For whom this preference is visible. By default, admin.
     */
    private Visibility visibility = Visibility.admin;

    /**
     * For backwards compatibility, the key strings which were use in
     * OMERO-Beta3 have been aliased to the new key strings. These may
     * eventually be removed.
     */
    private String[] aliases = new String[0];

    /**
     * To simplify configuration, the Spring bean id/name becomes the key string
     * for this {@link Preference}.
     */
    private String beanName = "unknown";

    /**
     * By default, configures this instance for
     * {@link PropertyPlaceholderConfigurer#SYSTEM_PROPERTIES_MODE_OVERRIDE} as
     * well as ignoring unfound resources. The
     * {@link PreferencesPlaceholderConfigurer#setUserTreePath(String)}
     * user-tree is set according to a similar logic as in the {@link prefs}
     * command-line tool, using first from the environment if
     * present, otherwise the value of "omero.prefs.default".
     */
    public Preference() {

    }

    public Preference(String beanName, boolean mutable, Visibility visibility,
            String[] aliases) {
        setBeanName(beanName);
        setMutable(mutable);
        setVisibility(visibility);
        setAliases(aliases);
    }

    public String getName() {
        return this.beanName;
    }

    /**
     * Setter injector
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Setter injector
     */
    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    /**
     * Setter injector
     */
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public boolean hasAlias(String key) {
        if (aliases != null && key != null) {
            for (int i = 0; i < aliases.length; i++) {
                if (key.equals(aliases[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Setter injector
     */
    public void setAliases(String[] aliases) {
        if (aliases == null) {
            this.aliases = null;
        } else {

            this.aliases = new String[aliases.length];
            System.arraycopy(aliases, 0, this.aliases, 0, aliases.length);
        }
    }

    public boolean isDb() {
        return this.db;
    }

    /**
     * Setter injector
     */
    public void setDb(boolean db) {
        this.db = db;
    }
    
    public boolean isPrefs() {
        return this.prefs;
    }

    public void setPrefs(boolean prefs) {
        this.prefs = prefs;
    }
}
