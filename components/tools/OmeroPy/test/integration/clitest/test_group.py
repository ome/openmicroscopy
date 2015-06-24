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

from omero.plugins.group import GroupControl, defaultperms
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest, RootCLITest
from test.integration.clitest.cli import get_user_ids, get_group_ids
from test.integration.clitest.cli import GroupIdNameFixtures
from test.integration.clitest.cli import GroupFixtures
from test.integration.clitest.cli import UserFixtures
import pytest

GroupNames = [str(x) for x in GroupFixtures]
UserNames = [str(x) for x in UserFixtures]
GroupIdNameNames = [str(x) for x in GroupIdNameFixtures]
perms_pairs = [('--perms', v) for v in defaultperms.values()]
perms_pairs.extend([('--type', v) for v in defaultperms.keys()])


class TestGroup(CLITest):

    @classmethod
    def setup_class(self):
        super(TestGroup, self).setup_class()
        self.cli.register("group", GroupControl, "TEST")
        self.groups = self.sf.getAdminService().lookupGroups()

    def setup_method(self, method):
        super(TestGroup, self).setup_method(method)
        self.args += ["group"]

    # List subcommand
    # ========================================================================
    @pytest.mark.parametrize("sort_key", [None, "id", "name"])
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
        ids = get_group_ids(out, sort_key=sort_key)

        # Check all groups are listed
        if sort_key == 'name':
            self.groups.sort(key=lambda x: x.name.val)
        else:
            self.groups.sort(key=lambda x: x.id.val)
        assert ids == [group.id.val for group in self.groups]

    def testAddAdminOnly(self, capsys):
        group_name = self.uuid()
        self.args += ["add", group_name]

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")

    # Info subcommand
    # ========================================================================
    def testInfoNoArgument(self, capsys):
        self.args += ["info"]
        self.cli.invoke(self.args, strict=True)

        # Read from the stdout
        out, err = capsys.readouterr()
        ids = get_group_ids(out)
        assert ids == [self.group.id.val]

    @pytest.mark.parametrize("groupfixture", GroupFixtures, ids=GroupNames)
    def testInfoArgument(self, capsys, groupfixture):
        self.args += ["info"]
        self.args += groupfixture.get_arguments(self.group)
        self.cli.invoke(self.args, strict=True)

        # Read from the stdout
        out, err = capsys.readouterr()
        ids = get_group_ids(out)
        assert ids == [self.group.id.val]

    def testInfoInvalidGroup(self, capsys):
        self.args += ["info"]
        self.args += ["-1"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    # Listgroups subcomand
    # ========================================================================
    def testListUsersNoArgument(self, capsys):
        self.args += ["listusers"]
        self.cli.invoke(self.args, strict=True)

        out, err = capsys.readouterr()
        ids = get_user_ids(out)
        assert ids == [self.user.id.val]

    @pytest.mark.parametrize("groupfixture", GroupFixtures, ids=GroupNames)
    def testListUsersArgument(self, capsys, groupfixture):
        self.args += ["listusers"]
        self.args += groupfixture.get_arguments(self.group)
        self.cli.invoke(self.args, strict=True)

        out, err = capsys.readouterr()
        ids = get_user_ids(out)
        assert ids == [self.user.id.val]

    def testListUsersInvalidArgument(self, capsys):
        self.args += ["listgroups"]
        self.args += ["-1"]

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)


class TestGroupRoot(RootCLITest):

    def setup_method(self, method):
        super(TestGroupRoot, self).setup_method(method)
        self.cli.register("group", GroupControl, "TEST")
        self.args += ["group"]

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

    # Group addition subcommand
    # ========================================================================
    def testAddDefaults(self):
        group_name = self.uuid()
        self.args += ["add", group_name]
        self.cli.invoke(self.args, strict=True)

        # Check group is created with private permissions
        group = self.sf.getAdminService().lookupGroup(group_name)
        assert str(group.details.permissions) == 'rw----'

    @pytest.mark.parametrize("perms_prefix,perms", perms_pairs)
    def testAddPerms(self, perms_prefix, perms):
        group_name = self.uuid()
        self.args += ["add", group_name, perms_prefix, perms]
        self.cli.invoke(self.args, strict=True)

        # Check group is created with the right permissions
        group = self.sf.getAdminService().lookupGroup(group_name)
        if perms_prefix == "--perms":
            assert str(group.details.permissions) == perms
        else:
            assert str(group.details.permissions) == defaultperms[perms]

    def testAddSameNamefails(self):
        group_name = self.uuid()
        self.args += ["add", group_name]
        self.cli.invoke(self.args, strict=True)
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testAddIgnoreExisting(self):
        group_name = self.uuid()
        self.args += ["add", group_name]
        self.cli.invoke(self.args, strict=True)
        self.args += ["--ignore-existing"]
        self.cli.invoke(self.args, strict=True)

        # Check group is created
        group = self.sf.getAdminService().lookupGroup(group_name)
        assert group.name.val == group_name

    # Group permissions subcommand
    # ========================================================================
    @pytest.mark.parametrize(
        "idnamefixture", GroupIdNameFixtures, ids=GroupIdNameNames)
    @pytest.mark.parametrize("from_perms", defaultperms.values())
    @pytest.mark.parametrize("perms_prefix,to_perms", perms_pairs)
    def testPerms(self, idnamefixture, from_perms, perms_prefix, to_perms):
        group = self.new_group([], from_perms)
        group = self.sf.getAdminService().getGroup(group.id.val)
        assert str(group.details.permissions) == from_perms

        self.args += ["perms"]
        self.args += idnamefixture.get_arguments(group)
        self.args += [perms_prefix, to_perms]
        self.cli.invoke(self.args, strict=True)

        # Check group is created with the right permissions
        group = self.sf.getAdminService().getGroup(group.id.val)
        if perms_prefix == "--perms":
            assert str(group.details.permissions) == to_perms
        else:
            assert str(group.details.permissions) == defaultperms[to_perms]

    # Group adduser subcommand
    # ========================================================================
    @pytest.mark.parametrize(
        "idnamefixture", GroupIdNameFixtures, ids=GroupIdNameNames)
    @pytest.mark.parametrize("userfixture", UserFixtures, ids=UserNames)
    @pytest.mark.parametrize("owner_arg", [None, '--as-owner'])
    def testAddUser(self, idnamefixture, userfixture, owner_arg):
        group = self.new_group()
        user = self.new_user()
        assert user.id.val not in self.getuserids(group.id.val)

        self.args += ["adduser"]
        self.args += idnamefixture.get_arguments(group)
        self.args += userfixture.get_arguments(user)
        if owner_arg:
            self.args += [owner_arg]
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        if owner_arg:
            assert user.id.val in self.getownerids(group.id.val)
        else:
            assert user.id.val in self.getmemberids(group.id.val)

    # Group removeuser subcommand
    # ========================================================================
    @pytest.mark.parametrize(
        "idnamefixture", GroupIdNameFixtures, ids=GroupIdNameNames)
    @pytest.mark.parametrize("userfixture", UserFixtures, ids=UserNames)
    @pytest.mark.parametrize("is_owner", [True, False])
    @pytest.mark.parametrize("owner_arg", [None, '--as-owner'])
    def testRemoveUser(self, idnamefixture, userfixture, is_owner, owner_arg):
        user = self.new_user()
        group = self.new_group([user])
        if is_owner:
            self.root.sf.getAdminService().setGroupOwner(group, user)
            assert user.id.val in self.getownerids(group.id.val)
        else:
            assert user.id.val in self.getmemberids(group.id.val)

        self.args += ["removeuser"]
        self.args += idnamefixture.get_arguments(group)
        self.args += userfixture.get_arguments(user)
        if owner_arg:
            self.args += [owner_arg]
        self.cli.invoke(self.args, strict=True)

        # Check user has been added to the list of member/owners
        if owner_arg:
            assert user.id.val not in self.getownerids(group.id.val)
        else:
            assert user.id.val not in self.getuserids(group.id.val)

    # Group copyusers subcommand
    # ========================================================================
    @pytest.mark.parametrize("from_group", ['id', 'name'])
    @pytest.mark.parametrize("to_group", ['id', 'name'])
    @pytest.mark.parametrize("owner_only", [None, '--as-owner'])
    def testCopyUsers(self, from_group, to_group, owner_only):
        users = [self.new_user(), self.new_user()]
        owners = [self.new_user(), self.new_user()]
        users.extend(owners)
        group1 = self.new_group(users)
        for owner in owners:
            self.root.sf.getAdminService().setGroupOwner(group1, owner)
        group2 = self.new_group([])

        self.args += ["copyusers", "%s" % getattr(group1, from_group).val,
                      "%s" % getattr(group2, to_group).val]
        if owner_only:
            self.args += [owner_only]
        self.cli.invoke(self.args, strict=True)

        # Check all owners have been copied
        if owner_only:
            for owner in owners:
                assert owner.id.val in self.getownerids(group2.id.val)
        else:
            for user in users:
                assert user.id.val in self.getmemberids(group2.id.val)
