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

from test.integration.clitest.cli import CLITest
import omero.plugins.admin
from omero.cli import NonZeroReturnCode
from path import path
from omero.util.upgrade_check import UpgradeCheck


def createUpgradeCheckClass(version):
    class MockUpgradeCheck(UpgradeCheck):
        def __init__(self, agent, url=None):
            if url:
                super(MockUpgradeCheck, self).__init__(
                    agent, url, version=version)
            else:
                super(MockUpgradeCheck, self).__init__(agent, version=version)

    return MockUpgradeCheck


class TestAdmin(CLITest):

    def setup_method(self, method):
        super(TestAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        # omero needs the etc/grid directory
        self.cli.dir = (path(__file__).dirname()
                        / ".." / ".." / ".." / ".." / ".." / ".." / "dist")
        self.args += ["admin"]

    def go(self):
        self.cli.invoke(self.args, strict=True)
        return self.cli.get("tx.state")

    def test_checkupgrade0(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.admin, "UpgradeCheck",
                            createUpgradeCheckClass("999999999.0.0"))
        self.args.append("checkupgrade")
        self.go()

    def test_checkupgrade1(self, monkeypatch):
        monkeypatch.setattr(omero.plugins.admin, "UpgradeCheck",
                            createUpgradeCheckClass("0.0.0"))
        self.args.append("checkupgrade")
        with pytest.raises(NonZeroReturnCode) as exc:
            self.go()
        assert exc.value.rv == 1
