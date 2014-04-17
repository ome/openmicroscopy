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

import omero
from test.integration.clitest.cli import CLITest
import pytest


class TestImport(CLITest):

    def get_plate_id(self, image_id):
        params = omero.sys.ParametersI()
        params.addIds([image_id])
        query = "select well from Well as well "
        query += "left outer join well.wellSamples as ws "
        query += "left outer join ws.image as img "
        query += "where ws.image.id in (:ids)"
        wells = self.query.findAllByQuery(query, params)
        return wells[0].plate.id.val

    @pytest.mark.parametrize("obj_type", ["image", "plate"])
    @pytest.mark.parametrize("name", [None, '-n', '--name', '--plate_name'])
    @pytest.mark.parametrize(
        "description", [None, '-x', '--description', '--plate_description'])
    def testNamingArguments(self, obj_type, name, description, tmpdir):

        if obj_type == 'image':
            fakefile = tmpdir.join("test.fake")
        else:
            fakefile = tmpdir.join("SPW&plates=1&plateRows=1&plateCols=1&"
                                   "fields=1&plateAcqs=1.fake")
        fakefile.write('')

        extra_args = []
        if name:
            extra_args += [name, 'name']
        if description:
            extra_args += [description, 'description']

        pixIds = self.import_image(str(fakefile), extra_args=extra_args)
        pixels = self.query.get("Pixels", long(pixIds[0]))
        if obj_type == 'image':
            obj = self.query.get("Image", pixels.getImage().id.val)
        else:
            plateid = self.get_plate_id(pixels.getImage().id.val)
            obj = self.query.get("Plate", plateid)

        if name:
            assert obj.getName().val == 'name'
        if description:
            assert obj.getDescription().val == 'description'
