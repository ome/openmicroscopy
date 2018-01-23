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

package ome.security;

import java.util.Set;

import ome.model.meta.Node;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

/**
 * Provider for {@link Node} objects which is responsible for persisting and
 * populating such entities.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see ome.services.blitz.fire.Ring
 * @since 5.3.0
 */
public interface NodeProvider {

    Set<String> getManagerList(final boolean onlyActive);

    /**
     * Retrieves a given manager node ID.
     * @param managerUuid manager node UUID to retrieve
     * @param sql active SQL context which can be used to make queries
     * @return See above.
     */
    long getManagerIdByUuid(String managerUuid, SqlAction sql);

    /**
     * Retrieves a given manager node.
     * @param managerUuid manager node UUID to retrieve
     * @param sf current session's service factory
     * @return See above.
     */
    Node getManagerByUuid(String managerUuid, ServiceFactory sf);

    /**
     * Closes all sessions for a given manager node.
     *
     * @param managerUuid manager node UUID to close sessions for
     * @return number of sessions affected by the closure
     */
    int closeSessionsForManager(final String managerUuid);

    /**
     * Sets a given manager node as down.
     *
     * @param managerUuid manager node UUID to set as down
     */
    void setManagerDown(final String managerUuid);

    /**
     * Adds a manager node.
     *
     * @param managerUuid manager node UUID to add
     * @param proxyString manager node proxy connection string
     * @return populated node entity.
     */
    Node addManager(String managerUuid, String proxyString);

    /**
     * Retrieves the current active principal.
     *
     * @return See above.
     */
    Principal principal();
}
