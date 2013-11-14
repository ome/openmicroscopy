#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

from omero.plugins.user import UserControl
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest, RootCLITest
import pytest

subcommands = ['add', 'list', 'password', 'email', 'joingroup', 'leavegroup']
user_pairs = [('--id', 'id'), ('--name', 'omeName')]
group_pairs = [(None, 'id'), (None, 'name'), ('--group-id', 'id'),
               ('--group-name', 'name')]


class TestUser(CLITest):

    def setup_method(self, method):
        super(TestUser, self).setup_method(method)
        self.cli.register("user", UserControl, "TEST")
        self.args += ["user"]

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("subcommand", subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)


class TestUserRoot(RootCLITest):

    def setup_method(self, method):
        super(TestUserRoot, self).setup_method(method)
        self.cli.register("user", UserControl, "TEST")
        self.args += ["user"]

    def getuserids(self, gid):
        group = self.sf.getAdminService().getGroup(gid)
        return [x.child.id.val for x in group.copyGroupExperimenterMap()]

    def getmemberids(self, gid):
        group = self.sf.getAdminService().getGroup(gid)
        return [x.child.id.val for x in group.copyGroupExperimenterMap()
                if not x.owner.val]

    def getownerids(self, gid):
        group = self.sf.getAdminService().getGroup(gid)
        return [x.child.id.val for x in group.copyGroupExperimenterMap()
                if x.owner.val]

    # User joingroup subcommand
    # ========================================================================
    @pytest.mark.parametrize("user_prefix,user_attr", user_pairs)
    @pytest.mark.parametrize("group_prefix,group_attr", group_pairs)
    @pytest.mark.parametrize("owner_arg", [None, '--as-owner'])
    def testJoinGroup(self, user_prefix, user_attr, group_prefix, group_attr,
                      owner_arg):
        user = self.new_user()
        group = self.new_group()
        assert user.id.val not in self.getuserids(group.id.val)

        self.args += ["joingroup", user_prefix,
                      "%s" % getattr(user, user_attr).val]
        if group_prefix:
            self.args += [group_prefix]
        self.args += ["%s" % getattr(group, group_attr).val]
        if owner_arg:
            self.args += [owner_arg]
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        if owner_arg:
            assert user.id.val in self.getownerids(group.id.val)
        else:
            assert user.id.val in self.getmemberids(group.id.val)

    # User leavegroup subcommand
    # ========================================================================
    @pytest.mark.parametrize("user_prefix,user_attr", user_pairs)
    @pytest.mark.parametrize("group_prefix,group_attr", group_pairs)
    @pytest.mark.parametrize("is_owner", [True, False])
    @pytest.mark.parametrize("owner_arg", [None, '--as-owner'])
    def testLeaveGroup(self, user_prefix, user_attr, group_prefix, group_attr,
                       is_owner, owner_arg):
        user = self.new_user()
        group = self.new_group([user])
        if is_owner:
            self.root.sf.getAdminService().setGroupOwner(group, user)
            assert user.id.val in self.getownerids(group.id.val)
        else:
            assert user.id.val in self.getmemberids(group.id.val)

        self.args += ["leavegroup", user_prefix,
                      "%s" % getattr(user, user_attr).val]
        if group_prefix:
            self.args += [group_prefix]
        self.args += ["%s" % getattr(group, group_attr).val]
        if owner_arg:
            self.args += [owner_arg]
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        if owner_arg:
            assert user.id.val not in self.getownerids(group.id.val)
        else:
            assert user.id.val not in self.getuserids(group.id.val)
