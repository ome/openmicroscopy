/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import ome.api.IConfig;

/**
 * Propagate the server's read-only status into corresponding keys in the configuration service.
 * This makes the server's runtime state available for query by clients.
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class ReadOnlyConfigInit {

    private static final String KEY_PREFIX = "omero.cluster.read_only.runtime.";

    /**
     * Set read-only status in the current configuration.
     * @param iConfig the configuration service
     * @param readOnly the read-only status
     */
    public ReadOnlyConfigInit(IConfig iConfig, ReadOnlyStatus readOnly) {
        iConfig.setConfigValue(KEY_PREFIX + "db",   Boolean.toString(readOnly.isReadOnlyDb()));
        iConfig.setConfigValue(KEY_PREFIX + "repo", Boolean.toString(readOnly.isReadOnlyRepo()));
    }
}
