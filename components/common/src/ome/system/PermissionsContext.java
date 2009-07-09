/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import ome.model.internal.Permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration of the default permissions. This class is created by the
 * ome/config.xml Spring configuration file and resets the
 * {@link Permissions#DEFAULT} value.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.1
 */
public class PermissionsContext {

    public final static String KEY = "omero.security.default_permissions";

    private final static Log log = LogFactory.getLog(PermissionsContext.class);

    public PermissionsContext(PreferenceContext prefs) {
        String perms = prefs.getProperty(KEY);
        log.info("Setting " + KEY + " to " + perms);
        Permissions.setDefaultPermissions(perms);
    }

}