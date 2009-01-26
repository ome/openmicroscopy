/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Definition of a server configuration variable ("preference") along with its
 * mutability, visibility, aliases and other important information. These
 * {@link Preference preferences} are defined in ome/config.xml along with the
 * {@link PreferenceContext}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/800">#800</a>
 */
public class Preference implements BeanNameAware {

    public enum Visibility {
        hidden, all, admin, user;
    }

    boolean mutable;

    boolean db;
    
    boolean prefs;

    Visibility visibility;

    String[] aliases;

    String beanName;
    
    /**
     * By default, configures this instance for
     * {@link PropertyPlaceholderConfigurer#SYSTEM_PROPERTIES_MODE_OVERRIDE} as
     * well as ignoring unfound resources. The {@link #setUserTreePath(String)}
     * user-tree is set according to a similar logic as in the {@link prefs}
     * command-line tool, using first {@link #ENV} from the environment if
     * present, otherwise the value of "omero.prefs.default".
     */
    public Preference() {
        this("unknown", false, Visibility.admin.toString(), new String[0]);
    }

    public Preference(String beanName, boolean mutable, String visibility,
            String[] aliases) {
        setBeanName(beanName);
        setMutable(mutable);
        setVisibility(visibility);
        setAliases(aliases);
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

    /**
     * Setter injector
     */
    public void setVisibility(String visibility) {
        this.visibility = Preference.Visibility.valueOf(visibility);
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

    /**
     * Setter injector
     */
    public void setDb(boolean db) {
        this.db = db;
    }

}
