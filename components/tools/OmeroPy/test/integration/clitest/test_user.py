#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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
from test.integration.clitest.cli import CLITest, RootCLITest
import getpass
import pytest

user_pairs = [('--id', 'id'), ('--name', 'omeName')]
group_pairs = [(None, 'id'), (None, 'name'), ('--group-id', 'id'),
               ('--group-name', 'name')]
sort_keys = [None, "id", "login", "first-name", "last-name", "email"]
columns = {'login': 1, 'first-name': 2, 'last-name': 3, 'email': 4}
middlename_prefixes = [None, '-m', '--middlename']
email_prefixes = [None, '-e', '--email']
institution_prefixes = [None, '-i', '--institution']
admin_prefixes = [None, '-a', '--admin']
password_prefixes = [None, '--no-password', '-P', '--userpassword']


class TestUser(CLITest):

    def setup_method(self, method):
        super(TestUser, self).setup_method(method)
        self.cli.register("user", UserControl, "TEST")
        self.args += ["user"]

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
        elif sort_key == 'email':
            users.sort(key=lambda x: (x.email and x.email.val or ""))
        else:
            users.sort(key=lambda x: x.id.val)
        assert ids == [user.id.val for user in users]

    @pytest.mark.parametrize("style", [None, "sql", "csv", "plain"])
    def testListWithStyles(self, capsys, style):
        self.args += ["list"]
        if style:
            self.args += ["--style=%s" % style]
        self.cli.invoke(self.args, strict=True)

    # Email subcommand
    # ========================================================================
    @pytest.mark.parametrize("oneperline_arg", [None, "-1", "--one"])
    def testEmail(self, capsys, oneperline_arg):
        self.args += ["email", "-i"]
        if oneperline_arg:
            self.args += [oneperline_arg]
        self.cli.invoke(self.args, strict=True)

        # Read from the stdout
        out, err = capsys.readouterr()

        # Check all users are listed
        users = self.sf.getAdminService().lookupExperimenters()
        emails = [x.email.val for x in users if x.email and x.email.val]
        if oneperline_arg:
            assert out.strip() == "\n".join(emails)
        else:
            assert out.strip() == ", ".join(emails)

    # Password subcommand
    # ========================================================================
    def testPassword(self):
        self.args += ["password"]
        password = self.uuid()
        login = self.sf.getAdminService().getEventContext().userName

        self.setup_mock()
        self.mox.StubOutWithMock(getpass, 'getpass')
        i1 = 'Please enter password for your user (%s): ' % login
        i2 = 'Please enter password to be set: '
        i3 = 'Please re-enter password to be set: '
        getpass.getpass(i1).AndReturn('')
        getpass.getpass(i2).AndReturn(password)
        getpass.getpass(i3).AndReturn(password)
        self.mox.ReplayAll()

        self.cli.invoke(self.args, strict=True)
        self.teardown_mock()

        # Check session creation using new password
        self.new_client(user=login, password=password)


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
    @pytest.mark.parametrize("admin_prefix", admin_prefixes)
    def testAdd(self, middlename_prefix, email_prefix, institution_prefix,
                admin_prefix):
        group = self.new_group()
        login = self.uuid()
        firstname = self.uuid()
        lastname = self.uuid()

        self.args += ["add", login, firstname, lastname]
        self.args += ["%s" % group.id.val]
        if middlename_prefix:
            middlename = self.uuid()
            self.args += [middlename_prefix, middlename]
        if email_prefix:
            email = "%s.%s@%s.org" % (firstname[:6], lastname[:6],
                                      self.uuid()[:6])
            self.args += [email_prefix, email]
        if institution_prefix:
            institution = self.uuid()
            self.args += [institution_prefix, institution]
        if admin_prefix:
            self.args += [admin_prefix]
        self.args += ['--no-password']
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        user = self.sf.getAdminService().lookupExperimenter(login)
        assert user.omeName.val == login
        assert user.firstName.val == firstname
        assert user.lastName.val == lastname
        assert user.id.val in self.getuserids(group.id.val)
        if middlename_prefix:
            assert user.middleName.val == middlename
        if email_prefix:
            assert user.email.val == email
        if institution_prefix:
            assert user.institution.val == institution
        if admin_prefix:
            roles = self.sf.getAdminService().getSecurityRoles()
            assert user.id.val in self.getuserids(roles.systemGroupId)

    @pytest.mark.parametrize("group_prefix,group_attr", group_pairs)
    def testAddGroup(self, group_prefix, group_attr):
        group = self.new_group()
        login = self.uuid()
        firstname = self.uuid()
        lastname = self.uuid()

        self.args += ["add", login, firstname, lastname]
        if group_prefix:
            self.args += [group_prefix]
        self.args += ["%s" % getattr(group, group_attr).val]
        self.args += ['--no-password']
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        user = self.sf.getAdminService().lookupExperimenter(login)
        assert user.omeName.val == login
        assert user.firstName.val == firstname
        assert user.lastName.val == lastname
        assert user.id.val in self.getuserids(group.id.val)

    @pytest.mark.parametrize("password_prefix", password_prefixes)
    def testAddPassword(self, password_prefix):
        group = self.new_group()
        login = self.uuid()
        firstname = self.uuid()
        lastname = self.uuid()

        self.args += ["add", login, firstname, lastname]
        self.args += ["%s" % group.id.val]
        if password_prefix:
            self.args += [password_prefix]
            if password_prefix != '--no-password':
                password = self.uuid()
                self.args += ["%s" % password]
            else:
                password = None
        else:
            password = self.uuid()
            self.setup_mock()
            self.mox.StubOutWithMock(getpass, 'getpass')
            i1 = 'Please enter password for your new user (%s): ' % login
            i2 = 'Please re-enter password for your new user (%s): ' % login
            getpass.getpass(i1).AndReturn(password)
            getpass.getpass(i2).AndReturn(password)
            self.mox.ReplayAll()

        self.cli.invoke(self.args, strict=True)
        if not password_prefix:
            self.teardown_mock()

        # Check user has been added to the list of member/owners
        user = self.sf.getAdminService().lookupExperimenter(login)
        assert user.omeName.val == login
        assert user.firstName.val == firstname
        assert user.lastName.val == lastname
        assert user.id.val in self.getuserids(group.id.val)

        # Check session creation using password
        self.new_client(user=login, password=password)

    # Password subcommand
    # ========================================================================
    def testPassword(self):
        user = self.new_user()
        login = user.omeName.val
        self.args += ["password", "%s" % login]
        root_password = self.root.getProperty("omero.rootpass")
        password = self.uuid()

        self.setup_mock()
        self.mox.StubOutWithMock(getpass, 'getpass')
        i1 = 'Please enter password for your user (root): '
        i2 = 'Please enter password to be set: '
        i3 = 'Please re-enter password to be set: '
        getpass.getpass(i1).AndReturn(root_password)
        getpass.getpass(i2).AndReturn(password)
        getpass.getpass(i3).AndReturn(password)
        self.mox.ReplayAll()

        self.cli.invoke(self.args, strict=True)
        self.teardown_mock()

        # Check session creation using new password
        self.new_client(user=login, password=password)
