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
   Tests for the stateful Exporter service.

"""

import omero
import library as lib
import pytest


class TestExporter(lib.ITest):

    def bigimage(self):
        pix = self.pix(x=4000, y=4000, z=1, t=1, c=1)
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
            return pix
        finally:
            rps.close()

    def testBasic(self):
        """
        Runs a simple export through to completion
        as a smoke test.
        """
        pix_ids = self.import_image()
        image_id = self.client.sf.getQueryService().projection(
            "select i.id from Image i join i.pixels p where p.id = :id",
            omero.sys.ParametersI().addId(pix_ids[0]))[0][0].val
        exporter = self.client.sf.createExporter()
        exporter.addImage(image_id)
        length = exporter.generateTiff()
        offset = 0
        while True:
            rv = exporter.read(offset, 1000 * 1000)
            if not rv:
                break
            rv = rv[:min(1000 * 1000, length - offset)]
            offset += len(rv)

    def test6713(self):
        """
        Tests that a big image will not be exportable.
        """
        pix = self.bigimage()
        exporter = self.client.sf.createExporter()
        exporter.addImage(pix.getImage().id.val)
        with pytest.raises(omero.ApiUsageException):
            exporter.generateTiff()
