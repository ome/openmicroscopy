#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012-2014 Glencoe Software, Inc. All Rights Reserved.
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
Integration test focused on the omero.api.IMetadata interface.

"""

import library as lib
import pytest
import omero


NAMES = ("TagAnnotation",
         "TagAnnotationI",
         "omero.model.TagAnnotation",
         "omero.model.TagAnnotationI",
         "ome.model.annotations.TagAnnotation")


class TestIMetadata(lib.ITest):

    def setup_method(self, method):
        self.md = self.client.sf.getMetadataService()

    def testLoadAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadAnnotations('Project', [0], ['X'], None, None)
        for name in NAMES:
            self.md.loadAnnotations('Project', [0], [name], None, None)
        self.md.loadAnnotations('Project', [0], NAMES, None, None)

    def testLoadAnnotationsUsedNotOwned3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadAnnotationsUsedNotOwned('X', 0, None)
        for name in NAMES:
            self.md.loadAnnotationsUsedNotOwned(name, 0, None)

    def testCountAnnotationsUsedNotOwned3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.countAnnotationsUsedNotOwned('X', 0, None)
        for name in NAMES:
            self.md.countAnnotationsUsedNotOwned(name, 0, None)

    def testCountSpecifiedAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.countSpecifiedAnnotations('X', [], [], None)
        for name in NAMES:
            self.md.countSpecifiedAnnotations(name, [], [], None)

    def testLoadSpecifiedAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadSpecifiedAnnotations('X', [], [], None)
        for name in NAMES:
            self.md.loadSpecifiedAnnotations(name, [], [], None)
