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

"""
Simple tests of various ROI utilities
"""

from omero.util.ROI_utils import pointsStringToXYlist, xyListToBbox
from omero.util.roi_handling_utils import points_string_to_xy_list


class TestRoiUtils(object):

    def test_old_format(self):
        xy_list = pointsStringToXYlist((
            "points[1,2, 3,4, 5,6] "
        ))
        assert xy_list == [(1, 2), (3, 4), (5, 6)]

    def test_new_format(self):
        xy_list = pointsStringToXYlist((
            "1,2 3,4 5,6"
        ))
        assert xy_list == [(1, 2), (3, 4), (5, 6)]

    def test_bbox(self):
        xy_list = self.test_old_format()
        bbox = xyListToBbox(xy_list)
        assert bbox == (1, 2, 4, 4)

    def test_old_format_new_method(self):
        xy_list = points_string_to_xy_list((
            "points[1,2, 3,4, 5,6] "
        ))
        assert xy_list == [(1, 2), (3, 4), (5, 6)]

    def test_new_format_new_method(self):
        xy_list = points_string_to_xy_list((
            "1,2 3,4 5,6"
        ))
        assert xy_list == [(1, 2), (3, 4), (5, 6)]
