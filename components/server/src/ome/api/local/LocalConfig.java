/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;


/**
 * Provides local (internal) extensions for configuration. Primarily this is
 * important because some configuration values are "hidden" for the public API
 * (passwords, etc.), but must be available for internal use.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public interface LocalConfig extends ome.api.IConfig {

    /**
     * Provides a configuration value, ignoring the visibility of the given
     * value.
     */
    public String getInternalValue(String key);
    
     /**
     * Retrieves the newest database patch. Also functions a simple DB ping.
     */
    public String getDatabaseVersion();

}
