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

from omero.plugins.group import GroupControl, defaultperms
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest, RootCLITest
import pytest

subcommands = ['add', 'perms', 'list', 'copyusers', 'adduser', 'removeuser']
group_pairs = [('--id', 'id'), ('--name', 'name')]
perms_prefixes = ['--perms', '--type']
perms_pairs = [('--perms', v) for v in defaultperms.values()]
perms_pairs.extend([('--type', v) for v in defaultperms.keys()])


class TestGroup(CLITest):

    def setup_method(self, method):
        super(TestGroup, self).setup_method(method)
        self.cli.register("group", GroupControl, "TEST")
        self.args += ["group"]

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("subcommand", subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)


class TestGroupRoot(RootCLITest):

    def setup_method(self, method):
        super(TestGroupRoot, self).setup_method(method)
        self.cli.register("group", GroupControl, "TEST")
        self.args += ["group"]

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
    @pytest.mark.parametrize("group_prefix,group_attr", group_pairs)
    @pytest.mark.parametrize("from_perms", defaultperms.values())
    @pytest.mark.parametrize("perms_prefix,to_perms", perms_pairs)
    def testPerms(self, group_prefix, group_attr, from_perms, perms_prefix,
                  to_perms):
        group = self.new_group([], from_perms)
        group = self.sf.getAdminService().getGroup(group.id.val)
        assert str(group.details.permissions) == from_perms

        self.args += ["perms", group_prefix,
                      "%s" % getattr(group, group_attr).val]
        self.args += [perms_prefix, to_perms]
        self.cli.invoke(self.args, strict=True)

        # Check group is created with the right permissions
        group = self.sf.getAdminService().getGroup(group.id.val)
        if perms_prefix == "--perms":
            assert str(group.details.permissions) == to_perms
        else:
            assert str(group.details.permissions) == defaultperms[to_perms]
