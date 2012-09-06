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

package ome.services.ldap;

import java.io.File;

import org.jmock.Mock;

import ome.logic.LdapImpl;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.system.Roles;
import ome.util.SqlAction;

public class UnitFixture
    extends Fixture
{

    private Mock role;

    private Mock sql;

    public UnitFixture(File ctxFile)
    {
        super(ctxFile);
        role = mock(RoleProvider.class);
        RoleProvider provider = (RoleProvider) role.proxy();
        sql = mock(SqlAction.class);
        SqlAction sql = (SqlAction) this.sql.proxy();
        ldap = new LdapImpl(data, new Roles(), config, provider, sql);
        this.provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap);
    }

    public void createUserWithGroup(final String dn, String group)
    {
        role.expects(atLeastOnce()).method("createGroup")
            .with(eq(group), NULL, eq(false)).will(returnValue(101L));
        role.expects(once()).method("createExperimenter")
            .will(returnValue(101L));
        // sql.expects(once()).method("update") // FIXME
    }
}
