/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.services.graphs;

import ome.model.IObject;
import ome.security.SecuritySystem;
import ome.services.graphs.GraphPolicy.Details;
import ome.system.Roles;

import org.hibernate.Session;

import com.google.common.collect.ImmutableMap;

/**
 * A predicate that allows {@code group=system} and similar to be used in graph policy rule matches.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.3
 */
public class GroupPredicate implements GraphPolicyRulePredicate {

    private static enum GroupMarker { SYSTEM, USER, GUEST };

    private static ImmutableMap<String, GroupMarker> groupsByName = ImmutableMap.of(
            "system", GroupMarker.SYSTEM,
            "user",   GroupMarker.USER,
            "guest",  GroupMarker.GUEST);

    private static ImmutableMap<Long, GroupMarker> groupsById;

    /**
     * Set the security system with whose roles this predicate is to work.
     * @param securitySystem the security system
     */
    public static void setSecuritySystem(SecuritySystem securitySystem) {
        final Roles roles = securitySystem.getSecurityRoles();
        groupsById = ImmutableMap.of(
                roles.getSystemGroupId(), GroupMarker.SYSTEM,
                roles.getUserGroupId(),   GroupMarker.USER,
                roles.getGuestGroupId(),  GroupMarker.GUEST);
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public void noteDetails(Session session, IObject object, String realClass, long id) {
        /* nothing to do */
    }

    @Override
    public boolean isMatch(Details object, String parameter) throws GraphException {
        if (object.groupId == null) {
            throw new GraphException("no group for " + object);
        }
        final boolean isInvert;
        if (parameter.startsWith("!")) {
            parameter = parameter.substring(1);
            isInvert = true;
        } else {
            isInvert = false;
        }
        final GroupMarker sought = groupsByName.get(parameter);
        if (sought == null) {
            throw new GraphException("unknown group: " + parameter);
        }
        final GroupMarker actual = groupsById.get(object.groupId);
        return isInvert != (sought == actual);
    }
}
