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


from omero.cli import CLI
from omero.plugins.sessions import SessionsControl

from library import ITest
from omero_ext.mox import Mox


class AbstractCLITest(ITest):

    @classmethod
    def setup_class(cls):
        super(AbstractCLITest, cls).setup_class()
        cls.cli = CLI()
        cls.cli.register("sessions", SessionsControl, "TEST")

    def setup_mock(self):
        self.mox = Mox()

    def teardown_mock(self):
        self.mox.UnsetStubs()
        self.mox.VerifyAll()


class CLITest(AbstractCLITest):

    def setup_method(self, method):
        self.args = self.login_args()


class RootCLITest(AbstractCLITest):

    def setup_method(self, method):
        self.args = self.root_login_args()


class ArgumentFixture(object):

    """
    Used to test the user/group argument
    """

    def __init__(self, prefix, attr):
        self.prefix = prefix
        self.attr = attr

    def get_arguments(self, obj):
        args = []
        if self.prefix:
            args += [self.prefix]
        if self.attr:
            args += ["%s" % getattr(obj, self.attr).val]
        return args

    def __repr__(self):
        if self.prefix:
            return "%s" % self.prefix
        else:
            return "%s" % self.attr


UserIdNameFixtures = (
    ArgumentFixture('--id', 'id'),
    ArgumentFixture('--name', 'omeName'),
    )

UserFixtures = (
    ArgumentFixture(None, 'id'),
    ArgumentFixture(None, 'omeName'),
    ArgumentFixture('--user-id', 'id'),
    ArgumentFixture('--user-name', 'omeName'),
    )

GroupIdNameFixtures = (
    ArgumentFixture('--id', 'id'),
    ArgumentFixture('--name', 'name'),
    )

GroupFixtures = (
    ArgumentFixture(None, 'id'),
    ArgumentFixture(None, 'name'),
    ArgumentFixture('--group-id', 'id'),
    ArgumentFixture('--group-name', 'name'),
    )


def get_user_ids(out, sort_key=None):
    columns = {'login': 1, 'first-name': 2, 'last-name': 3, 'email': 4}
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
    return ids


def get_group_ids(out, sort_key=None):
    lines = out.split('\n')
    ids = []
    last_value = None
    for line in lines[2:]:
        elements = line.split('|')
        if len(elements) < 4:
            continue

        ids.append(int(elements[0].strip()))
        if sort_key:
            if sort_key == 'id':
                new_value = ids[-1]
            else:
                new_value = elements[1].strip()
            assert new_value >= last_value
            last_value = new_value
    return ids
