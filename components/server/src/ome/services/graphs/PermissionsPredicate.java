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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.services.graphs.GraphPolicy.Details;

/**
 * A predicate that allows {@code perms=rwr---} and similar to be used in graph policy rule matches.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.3
 */
public class PermissionsPredicate implements GraphPolicyRulePredicate {

    private Map<Long, String> groupPermissions = new HashMap<Long, String>();

    @Override
    public String getName() {
        return "perms";
    }

    @Override
    public void noteDetails(Session session, IObject object, String realClass, long id) {
        if (!(object instanceof ExperimenterGroup)) {
            /* This is a small object and should typically hit the ORM cache but if still too slow then we could exploit that
             * GraphTraversal.planning.detailsNoted already has the details object cached. */
            final String hql = "SELECT details FROM " + realClass + " WHERE id = " + id;
            final ome.model.internal.Details details = (ome.model.internal.Details) session.createQuery(hql).uniqueResult();
            if (details == null) {
                return;
            }
            object = details.getGroup();
            if (object == null) {
                return;
            }
        }
        final Long groupId = object.getId();
        if (!groupPermissions.containsKey(groupId)) {
            final ExperimenterGroup group = (ExperimenterGroup) session.get(ExperimenterGroup.class, groupId);
            final Permissions permissions = group.getDetails().getPermissions();
            groupPermissions.put(groupId, permissions.toString());
        }
    }

    @Override
    public boolean isMatch(Details object, String parameter) throws GraphException {
        if (object.groupId == null) {
            throw new GraphException("no group for " + object);
        }
        final String permissions = groupPermissions.get(object.groupId);
        if (permissions == null) {
            throw new GraphException("no group permissions for " + object);
        }
        if (parameter.length() != permissions.length()) {
            throw new GraphException(
                    "parameter " + parameter + " has different length from permissions " + permissions + " on " + object);
        }
        int index = permissions.length();
        while (--index >= 0) {
            final char parameterChar = parameter.charAt(index);
            if (parameterChar != '?' && parameterChar != permissions.charAt(index)) {
                return false;
            }
        }
        return true;
    }
}
