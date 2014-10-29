#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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
Basic tests for additions/changes to the 5.1 model.
"""

import test.integration.library as lib
import omero

from omero.rtypes import unwrap


class TestModel51(lib.ITest):

    def testExposureTime(self):
        img = self.importMIF(name="testExposureTime", exposureTime=1.2)[0]
        plane_info = self.query.findByQuery((
            "select pi from PlaneInfo pi "
            "join fetch pi.exposureTime "
            "join fetch pi.exposureTime.unit "
            "join pi.pixels as pix join pix.image as img "
            "where img.id = :id"), omero.sys.ParametersI().addId(img.id.val))
        exposure = plane_info.getExposureTime()
        unit = exposure.getUnit()
        assert "SI.SECOND" == unwrap(unit.getMeasurementSystem())
        assert "s" == unwrap(unit.getValue())

        micros = self.query.findByQuery((
            "select ut from UnitsTime ut "
            "where ut.value = 'ms'"), None)

        exposure.setUnit(micros)
        plane_info = self.update.saveAndReturnObject(plane_info)
        exposure = plane_info.getExposureTime()
        unit = exposure.getUnit()
        assert "ms" == unwrap(unit.getValue())
