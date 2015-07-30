#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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

from omero.plugins.metadata import MetadataControl
from test.integration.clitest.cli import CLITest


class TestMetadata(CLITest):

    def setup_method(self, method):
        super(TestMetadata, self).setup_method(method)
        self.cli.register("metadata", MetadataControl, "TEST")
        self.args += ["metadata"]

    def testOriginal(self, capfd):
        uuid = self.uuid()
        img = self.importSingleImage(GlobalMetadata={uuid: uuid})
        prx = "Image:%s" % img.id.val
        self.args += ["original", prx]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        assert uuid in o
