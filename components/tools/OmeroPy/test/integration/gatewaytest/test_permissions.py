#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""Test Permissions functionality of the BlitzGateway wrapper."""


from omero.testlib import ITest
from omero.gateway import BlitzGateway


class TestPrivileges(ITest):
    """Test getting and updating Privileges."""

    def test_update_admin_privileges(self):
        """Test getting and updating Privileges."""
        conn = BlitzGateway(client_obj=self.root)
        privileges = ['Chown', 'ModifyGroup', 'ModifyUser',
                      'ModifyGroupMembership']
        exp = self.new_user(privileges=privileges)
        # Check before update
        assert set(privileges) == set(conn.getAdminPrivileges(exp.id.val))

        # Update and check again...
        conn.updateAdminPrivileges(exp.id.val, add=['Sudo', 'ModifyUser'],
                                   remove=['Chown', 'ModifyGroupMembership'])

        expected = ['Sudo', 'ModifyGroup', 'ModifyUser']
        assert set(expected) == set(conn.getAdminPrivileges(exp.id.val))

    def test_full_admin_privileges(self):
        """Test full admin privileges check."""
        conn = BlitzGateway(client_obj=self.root, try_super=True)
        allPrivs = []
        for p in conn.getEnumerationEntries('AdminPrivilege'):
            allPrivs.append(p.getValue())
        conn.updateAdminPrivileges(self.user.id.val, add=allPrivs)
        test = conn.getCurrentAdminPrivileges()
        assert set(allPrivs) == set(test)
        assert conn.isFullAdmin()

        conn.updateAdminPrivileges(self.user.id.val, remove=['Chown'])

        privs = set(conn.getCurrentAdminPrivileges())
        print "privs:"
        print privs

        print "all:"
        print allPrivs

        assert not conn.isFullAdmin()
