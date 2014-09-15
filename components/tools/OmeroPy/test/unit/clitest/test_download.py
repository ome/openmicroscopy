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
from omero.plugins.download import DownloadControl
from omero.cli import CLI, NonZeroReturnCode


class TestDownload(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("download", DownloadControl, "TEST")
        self.args = ["download"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize(
        'bad_input',
        ['-1', 'OriginalFile:-1', 'FileAnnotation:-1', 'Image:-1'])
    def testInvalidInput(self, bad_input):
        self.args += [bad_input, '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
