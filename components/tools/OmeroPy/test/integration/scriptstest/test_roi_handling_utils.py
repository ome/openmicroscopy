#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
   Integration tests for the various methods from roi_handling_utils

"""

from omero.testlib import ITest
from omero.util.roi_handling_utils import get_line_data
from omero.gateway import BlitzGateway


class TestRoiHandlingUtils(ITest):

    def test_get_line_data(self):
        client = self.new_client()
        image = self.create_test_image(100, 100, 1, 1, 1, client.getSession())
        id = image.id.val
        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image", id)
        pixels = image.getPrimaryPixels()
        line_width = 10
        # vertical line
        x = 10
        line = get_line_data(pixels, x, 0, x, 20, line_width)
        assert line.shape == (line_width, 20)
        # horizontal line
        y = 5
        line = get_line_data(pixels, 10, y, 50, y, line_width)
        assert line.shape == (line_width, 40)
        # diagonal line
        line = get_line_data(pixels, 0, 0, 50, 40, line_width)
        assert line.shape[0] == line_width
