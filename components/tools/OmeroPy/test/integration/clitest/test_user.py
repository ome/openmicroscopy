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
sort_keys = [None, "id", "login", "first-name", "last-name", "email"]
columns = {'login': 1, 'first-name': 2, 'last-name': 3, 'email': 4}
middlename_prefixes = [None, '-m', '--middlename']
email_prefixes = [None, '-e', '--email']
institution_prefixes = [None, '-i', '--institution']
admin_prefixes = [None, '-a', '--admin']
admin_prefixes = ['--no-password', '-P', '--password']


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

    # List subcommand
    # ========================================================================
    @pytest.mark.parametrize("sort_key", sort_keys)
    @pytest.mark.parametrize("group_format", [None, "count", "long"])
    def testList(self, capsys, sort_key, group_format):
        self.args += ["list"]
        if sort_key:
            self.args += ["--sort-by-%s" % sort_key]
        if group_format:
            self.args += ["--%s" % group_format]
        self.cli.invoke(self.args, strict=True)

        # Read from the stdout
        out, err = capsys.readouterr()
        lines = out.split('\n')
        ids = []
        last_value = None
        for line in lines[2:]:
            elements = line.split('|')
            if len(elements) < 8:
                continue

            ids.append(int(elements[0].strip()))
            if sort_key:
                if sort_key == 'id':
                    new_value = ids[-1]
                else:
                    new_value = elements[columns[sort_key]].strip()
                assert new_value >= last_value
                last_value = new_value

        # Check all users are listed
        users = self.sf.getAdminService().lookupExperimenters()
        if sort_key == 'login':
            users.sort(key=lambda x: x.omeName.val)
        elif sort_key == 'first-name':
            users.sort(key=lambda x: x.firstName.val)
        elif sort_key == 'last-name':
            users.sort(key=lambda x: x.lastName.val)
        elif sort_key == 'login':
            users.sort(key=lambda x: (x.email and x.email.val or ""))
        else:
            users.sort(key=lambda x: x.id.val)
        assert ids == [user.id.val for user in users]


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

    # User add subcommand
    # ========================================================================
    @pytest.mark.parametrize("middlename_prefix", middlename_prefixes)
    @pytest.mark.parametrize("email_prefix", email_prefixes)
    @pytest.mark.parametrize("institution_prefix", institution_prefixes)

    def testAdd(self, middlename_prefix, email_prefix, institution_prefix):
        group = self.new_group()
        username = self.uuid()
        firstname = self.uuid()
        lastname = self.uuid()

        self.args += ["add", username, firstname, lastname]
        if middlename_prefix:
            middlename = self.uuid()
            self.args += [middlename_prefix, middlename]
        if email_prefix:
            email = "%s.%s@%s.org" % (firstname[:6], lastname[:6],
                                      self.uuid()[:6])
            self.args += [email_prefix, email]
        self.args += ['--no-password']
        self.args += ['--group-id', group.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        user = self.sf.adminService.lookupExperimenter(username)
        assert user.firstName.val == firstname
        assert user.lastName.val == lastname
