#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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


import pytest
import os

from omero.testlib.cli import CLITest
from omero.testlib.cli import RootCLITest
import omero.plugins.admin
from omero.cli import NonZeroReturnCode
from omero_ext.path import path
from omero.util.upgrade_check import UpgradeCheck

OMERODIR = os.getenv('OMERODIR', None)


def createUpgradeCheckClass(version):
    class MockUpgradeCheck(UpgradeCheck):
        def __init__(self, agent, url=None):
            if url:
                super(MockUpgradeCheck, self).__init__(
                    agent, url, version=version)
            else:
                super(MockUpgradeCheck, self).__init__(agent, version=version)

    return MockUpgradeCheck


class TestAdmin(RootCLITest):

    def setup_method(self, method):
        super(TestAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        # omero needs the etc/grid directory
        self.cli.dir = (
            path(__file__).dirname() /
            ".." / ".." / ".." / ".." / ".." / ".." / "dist")
        self.args += ["admin"]

    def go(self):
        self.cli.invoke(self.args, strict=True)

    def test_checkupgrade0(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.prefs, "UpgradeCheck",
                            createUpgradeCheckClass("999999999.0.0"))
        self.args.append("checkupgrade")
        self.go()

    def test_checkupgrade1(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.prefs, "UpgradeCheck",
                            createUpgradeCheckClass("0.0.0"))
        self.args.append("checkupgrade")
        with pytest.raises(NonZeroReturnCode) as exc:
            self.go()
        assert exc.value.rv == 1

    def test_log(self):
        import uuid
        test = str(uuid.uuid4())
        self.args += ["log"]
        self.args += ["ScriptRepo"]
        self.args += [test]
        self.cli.invoke(self.args, strict=True)

        log_file = OMERODIR + "/var/log/Blitz-0.log"
        import fileinput
        found = False
        for line in fileinput.input(log_file):
            if line.__contains__(test):
                found = True
                break
        fileinput.close()
        assert found


class TestAdminRestrictedAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Restricted
    # Admin with no privileges
    DEFAULT_PRIVILEGES = ()

    def setup_method(self, method):
        super(TestAdminRestrictedAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        # omero needs the etc/grid directory
        self.cli.dir = (
            path(__file__).dirname() /
            ".." / ".." / ".." / ".." / ".." / ".." / "dist")
        self.args += ["admin"]

    def test_log(self):
        import uuid
        test = str(uuid.uuid4())
        self.args += ["log"]
        self.args += ["ScriptRepo"]
        self.args += [test]
        self.cli.invoke(self.args, strict=True)

        log_file = OMERODIR + "/var/log/Blitz-0.log"
        import fileinput
        found = False
        for line in fileinput.input(log_file):
            if line.__contains__(test):
                found = True
                break
        fileinput.close()
        assert found

    def test_checkupgrade0(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.prefs, "UpgradeCheck",
                            createUpgradeCheckClass("999999999.0.0"))
        self.args.append("checkupgrade")
        self.cli.invoke(self.args, strict=True)

    def test_checkupgrade1(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.prefs, "UpgradeCheck",
                            createUpgradeCheckClass("0.0.0"))
        self.args.append("checkupgrade")
        with pytest.raises(NonZeroReturnCode) as exc:
            self.cli.invoke(self.args, strict=True)
        assert exc.value.rv == 1
