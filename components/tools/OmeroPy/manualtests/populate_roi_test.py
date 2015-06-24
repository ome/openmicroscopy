#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
...
"""

#
#  Copyright (C) 2009 University of Dundee. All rights reserved.
#
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#


import unittest
import os

from omero.util.populate_roi import AbstractPlateAnalysisCtx
from omero.util.populate_roi import MIASPlateAnalysisCtx
from omero.util.populate_roi import FlexPlateAnalysisCtx
from omero.util.populate_roi import InCellPlateAnalysisCtx

from omero.rtypes import rstring, rint
from omero.model import OriginalFileI, ImageI, WellI, WellSampleI


class TestingServiceFactory(object):
    """
    Testing service factory implementation.
    """
    def getUpdateService(self):
        return None

    def getQueryService(self):
        return None


class FromFileOriginalFileProvider(object):
    """
    Provides a testing original file provider which provides file data
    directly from disk.
    """
    def __init__(self, service_factory):
        pass

    def get_original_file_data(self, original_file):
        """Returns a file handle to the path of the original file."""
        return open(original_file.path.val)


class MIASParseRoiTest(unittest.TestCase):

    LOG_FILE = "NEOlog2008-09-18-14h37m07s.txt"

    RESULT_FILE = "Well0001_mode1_z000_t000_detail_2008-09-18-10h48m54s.txt"

    ROOT = "/Users/callan/testimages/siRNA_PRIM1_03102008/"\
        "001-365700055641/results/"

    def setUp(self):
        AbstractPlateAnalysisCtx.DEFAULT_ORIGINAL_FILE_PROVIDER = \
            FromFileOriginalFileProvider
        original_files = list()
        # Create our container images and an original file image map
        images = list()
        n_images = 0
        for row in range(16):
            for column in range(24):
                well = WellI(n_images, True)
                well.column = rint(column)
                well.row = rint(row)
                well_sample = WellSampleI(n_images, True)
                well_sample.well = well
                image = ImageI(n_images, True)
                image.addWellSample(well_sample)
                images.append(image)
        original_file_image_map = dict()
        # Our required original file format
        format = rstring('Companion/MIAS')
        # Create original file representing the log file
        o = OriginalFileI(1L, True)
        o.name = rstring(self.LOG_FILE)
        o.path = rstring(os.path.join(self.ROOT, self.LOG_FILE))
        o.mimetype = format
        original_files.append(o)  # [1L] = o
        original_file_image_map[1L] = images[0]
        # Create original file representing the result file
        o = OriginalFileI(2L, True)
        o.name = rstring(self.RESULT_FILE)
        o.path = rstring(os.path.join(self.ROOT, self.RESULT_FILE))
        o.mimetype = format
        original_files.append(o)  # [2L] = o
        original_file_image_map[2L] = images[0]
        sf = TestingServiceFactory()
        self.analysis_ctx = MIASPlateAnalysisCtx(
            images, original_files, original_file_image_map, 1L, sf)

    def test_get_measurement_ctx(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        self.assertNotEqual(None, ctx)

    def test_get_columns(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        columns = ctx.parse()
        self.assertNotEqual(None, columns)
        self.assertEqual(9, len(columns))
        self.assertEqual('Image', columns[0].name)
        self.assertEqual('ROI', columns[1].name)
        self.assertEqual('Label', columns[2].name)
        self.assertEqual('Row', columns[3].name)
        self.assertEqual('Col', columns[4].name)
        self.assertEqual('Nucleus Area', columns[5].name)
        self.assertEqual('Cell Diam.', columns[6].name)
        self.assertEqual('Cell Type', columns[7].name)
        self.assertEqual('Mean Nucleus Intens.', columns[8].name)
        for column in columns:
            if column.name == "ROI":
                continue
            self.assertEqual(173, len(column.values))


class FlexParseRoiTest(unittest.TestCase):

    ROOT = "/Users/callan/testimages/"

    RESULT_FILE = "An_02_Me01_12132846(2009-06-17_11-56-17).res"

    def setUp(self):
        AbstractPlateAnalysisCtx.DEFAULT_ORIGINAL_FILE_PROVIDER = \
            FromFileOriginalFileProvider
        original_files = list()
        # Create our container images and an original file image map
        images = list()
        n_images = 0
        for row in range(16):
            for column in range(24):
                well = WellI(n_images, True)
                well.column = rint(column)
                well.row = rint(row)
                well_sample = WellSampleI(n_images, True)
                well_sample.well = well
                image = ImageI(n_images, True)
                image.addWellSample(well_sample)
                images.append(image)
        original_file_image_map = dict()
        # Our required original file format
        format = rstring('Companion/Flex')
        # Create original file representing the result file
        o = OriginalFileI(1L, True)
        o.name = rstring(self.RESULT_FILE)
        o.path = rstring(os.path.join(self.ROOT, self.RESULT_FILE))
        o.mimetype = format
        original_files.append(o)  # [1L] = o
        original_file_image_map[1L] = images[0]
        sf = TestingServiceFactory()
        self.analysis_ctx = FlexPlateAnalysisCtx(
            images, original_files, original_file_image_map, 1L, sf)

    def test_get_measurement_ctx(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        self.assertNotEqual(None, ctx)

    def test_get_columns(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        columns = ctx.parse()
        self.assertNotEqual(None, columns)
        self.assertEqual(50, len(columns))
        for column in columns:
            self.assertEqual(384, len(column.values))


class InCellParseRoiTest(unittest.TestCase):

    ROOT = "/Users/callan/testimages"

    RESULT_FILE = "Mara_488 and hoechst_P-HisH3.xml"

    def setUp(self):
        AbstractPlateAnalysisCtx.DEFAULT_ORIGINAL_FILE_PROVIDER = \
            FromFileOriginalFileProvider
        original_files = list()
        # Create our container images and an original file image map
        images = list()
        n_images = 0
        for row in range(16):
            for column in range(24):
                well = WellI(n_images, True)
                well.column = rint(column)
                well.row = rint(row)
                well_sample = WellSampleI(n_images, True)
                well_sample.well = well
                image = ImageI(n_images, True)
                image.addWellSample(well_sample)
                images.append(image)
        original_file_image_map = dict()
        # Our required original file format
        format = rstring('Companion/InCell')
        # Create original file representing the result file
        o = OriginalFileI(1L, True)
        o.name = rstring(self.RESULT_FILE)
        o.path = rstring(os.path.join(self.ROOT, self.RESULT_FILE))
        o.mimetype = format
        original_files.append(o)  # [1L] = o
        original_file_image_map[1L] = image
        sf = TestingServiceFactory()
        self.analysis_ctx = InCellPlateAnalysisCtx(
            images, original_files, original_file_image_map, 1L, sf)

    def test_get_measurement_ctx(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        self.assertNotEqual(None, ctx)

    def test_get_columns(self):
        ctx = self.analysis_ctx.get_measurement_ctx(0)
        columns = ctx.parse()
        self.assertNotEqual(None, columns)
        for column in columns:
            print 'Column: %s' % column.name
        self.assertEqual(33, len(columns))
        for column in columns:
            self.assertEqual(114149, len(column.values))

if __name__ == '__main__':
    unittest.main()
