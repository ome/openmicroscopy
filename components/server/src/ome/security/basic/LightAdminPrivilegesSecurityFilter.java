/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

package ome.security.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ome.model.enums.AdminPrivilege;
import ome.model.internal.Details;
import ome.system.EventContext;
import ome.system.Roles;

import org.hibernate.Filter;
import org.hibernate.Session;

import com.google.common.collect.ImmutableMap;

/**
 * Filter database queries to respect light administrator privileges.
 * Specifically, provide means to be sudo-aware in filtering according
 * to the current {@link ome.model.meta.Session}'s ownership and the
 * light administrator's privileges.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class LightAdminPrivilegesSecurityFilter extends AbstractSecurityFilter {

    private static final ImmutableMap<String, String> PARAMETER_TYPES =
            ImmutableMap.of("real_owner", "long",
                            "privileges", "string");

    private final LightAdminPrivileges adminPrivileges;

    /**
     * Construct a new light administrator filter.
     * @param roles the users and groups that are special to OMERO
     * @param adminPrivileges the light administrator privileges helper
     */
    public LightAdminPrivilegesSecurityFilter(Roles roles, LightAdminPrivileges adminPrivileges) {
        super(roles);
        this.adminPrivileges = adminPrivileges;
    }

    @Override
    public Map<String, String> getParameterTypes() {
        return PARAMETER_TYPES;
    }

    @Override
    public String getDefaultCondition() {
        /* provided instead by annotations */
        return null;
    }

    @Override
    public boolean passesFilter(Session session, Details details, EventContext ec) {
        /* this method will not be called with system types */
        return false;
    }

    @Override
    public void enable(Session session, EventContext ec) {
        final ome.model.meta.Session omeroSession = (ome.model.meta.Session)
                session.get(ome.model.meta.Session.class, ec.getCurrentSessionId());
        final List<String> privilegeValues = new ArrayList<String>();
        for (final AdminPrivilege adminPrivilege : adminPrivileges.getSessionPrivileges(omeroSession)) {
            privilegeValues.add(adminPrivilege.getValue());
        }
        final Long realOwner;
        if (omeroSession.getSudoer() != null) {
            realOwner = omeroSession.getSudoer().getId();
        } else {
            realOwner = omeroSession.getOwner().getId();
        }

        final int isAdmin01 = ec.isCurrentUserAdmin() ? 1 : 0;

        final Filter filter = session.enableFilter(getName());
        filter.setParameter("real_owner", realOwner);
        if (privilegeValues.isEmpty()) {
            filter.setParameterList("privileges", Collections.singletonList("none"));
        } else {
            filter.setParameterList("privileges", privilegeValues);
        }
        enableBaseFilters(session, isAdmin01, ec.getCurrentUserId());
    }
}
