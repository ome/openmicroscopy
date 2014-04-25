#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
Integration test focused on the omero.api.IUpdate interface.
"""

import test.integration.library as lib
import omero


class TestIUpdate(lib.ITest):
    """
    Basic test of all IUpdate functionality
    """

    def tags(self, count=3):
        return [omero.model.TagAnnotationI() for x in range(count)]

    def testSaveArray(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveArray(tags)

    def testSaveCollection(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveCollection(tags)
