#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
import omero

from test.integration.clitest.cli import CLITest
from omero.plugins.tx import TxControl
from omero.rtypes import rstring, rlong
from omero.util.temp_files import create_path


class TestTx(CLITest):

    def setup_method(self, method):
        super(TestTx, self).setup_method(method)
        self.cli.register("tx", TxControl, "TEST")
        self.args += ["tx"]
        self.setup_mock()

    def teardown_method(self, method):
        self.teardown_mock()
        super(TestTx, self).teardown_method(method)

    def create_script(self):
        path = create_path()
        for x in ("Screen", "Plate", "Project", "Dataset"):
            path.write_text("new %s name=test" % x)
            path.write_text("new %s name=test description=foo" % x)
        return path

    def test_create_from_file(self):
        path = self.create_script()
        self.args.append("--file=%s" % path)
        self.cli.invoke(self.args, strict=True)
        rv = self.cli.get("tx.out")
        assert 8 == len(rv)
