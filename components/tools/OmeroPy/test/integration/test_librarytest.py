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

"""
   Tests for the integration library.
"""

from omero.testlib import ITest


class TestLibrary(ITest):

    def test9188(self):
        self.create_test_image(10, 10, 1, 1, 1)
        self.create_test_image(10, 10, 10, 1, 1)
        self.create_test_image(10, 10, 1, 10, 1)
        self.create_test_image(10, 10, 1, 1, 10)
