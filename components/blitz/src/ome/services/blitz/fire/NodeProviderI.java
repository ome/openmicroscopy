/*
 * Copyright (C) 2017 Glencoe Software, Inc.
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

package ome.services.blitz.fire;

import java.util.Set;

import ome.model.meta.Node;
import ome.system.Principal;

/**
 * Provider for {@link Node} objects which is responsible for persisting and
 * populating such entities.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see Ring
 * @since 5.3.0
 */
public interface NodeProviderI {

    Set<String> getManagerList(final boolean onlyActive);

    /**
     * Assumes that the given manager is no longer available and so will not
     * attempt to call cache.removeSession() since that requires the session to
     * be in memory. Instead directly modifies the database to set the session
     * to closed.
     * 
     * @param managerUuid
     * @return
     */
    int closeSessionsForManager(final String managerUuid);

    void setManagerDown(final String managerUuid);

    Node addManager(String managerUuid, String proxyString);

    Principal principal();
}
