/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.security.auth;

import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.tools.hibernate.SessionFactory;
import ome.util.SqlAction;

/**
 * Extends {@link SimpleRoleProvider} by setting the "ldap" flag
 * to "true" on all {@link Experimenter} and {@link ExperimenterGroup}
 * instances that are created.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4
 */

public class LdapRoleProvider extends SimpleRoleProvider {

    final protected SqlAction sql;

    public LdapRoleProvider(SecuritySystem sec, SessionFactory sf, SqlAction sql) {
        super(sec, sf);
        this.sql = sql;
    }

    public long createGroup(String name, Permissions perms, boolean strict) {
        long id = super.createGroup(name, perms, strict);
        sql.setGroupLdapFlag(id, true);
        return id;
    }

    public long createGroup(ExperimenterGroup group) {
        long id = super.createGroup(group);
        sql.setGroupLdapFlag(id, true);
        return id;
    }

    public long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {
        long id = super.createExperimenter(experimenter, defaultGroup, otherGroups);
        sql.setUserLdapFlag(id, true);
        return id;
    }
}
