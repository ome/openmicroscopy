#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
   Integration test which checks the various methods from script_utils

"""

import library as lib
import omero.util.script_utils as scriptUtil
from omero.gateway import BlitzGateway
import tempfile
import shutil
from os import listdir
from os.path import isfile, join
from numpy import int32, uint8


class TestScriptUtils(lib.ITest):

    def testSplitImage(self):
        imported_pix = ",".join(self.import_image())
        dir = tempfile.mkdtemp()
        query_string = "select p from Pixels p where p.id='%s'" % imported_pix
        pixels = self.query.findByQuery(query_string, None)
        sizeZ = pixels.getSizeZ().getValue()
        sizeC = pixels.getSizeC().getValue()
        sizeT = pixels.getSizeT().getValue()
        # split the image into file
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id in (%s)" % imported_pix, None)
        scriptUtil.split_image(self.client, imported_img.id.getValue(), dir,
                               unformattedImageName="a_T%05d_C%s_Z%d_S1.tiff")
        files = [f for f in listdir(dir) if isfile(join(dir, f))]
        shutil.rmtree(dir)
        assert sizeZ*sizeC*sizeT == len(files)

    def testNumpyToImage(self):
        imported_pix = ",".join(self.import_image())
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id in (%s)" % imported_pix, None)
        conn = BlitzGateway(client_obj=self.client)
        image = conn.getObject("Image", imported_img.id.getValue())
        pixels = image.getPrimaryPixels()
        channelMinMax = []
        for c in image.getChannels():
            minC = c.getWindowMin()
            maxC = c.getWindowMax()
            channelMinMax.append((minC, maxC))
        theZ = image.getSizeZ() / 2
        theT = 0
        cIndex = 0
        for minMax in channelMinMax:
            plane = pixels.getPlane(theZ, cIndex, theT)
            i = scriptUtil.numpyToImage(plane, minMax, int32)
            assert i is not None
            try:
                # check if the image can be handled.
                i.load()
                assert True
            except IOError:
                assert False
            else:
                i.close()
            cIndex += 1

    def testConvertNumpyArray(self):
        imported_pix = ",".join(self.import_image())
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id in (%s)" % imported_pix, None)
        conn = BlitzGateway(client_obj=self.client)
        image = conn.getObject("Image", imported_img.id.getValue())
        pixels = image.getPrimaryPixels()
        channelMinMax = []
        for c in image.getChannels():
            minC = c.getWindowMin()
            maxC = c.getWindowMax()
            channelMinMax.append((minC, maxC))
        theZ = image.getSizeZ() / 2
        theT = 0
        cIndex = 0
        for minMax in channelMinMax:
            plane = pixels.getPlane(theZ, cIndex, theT)
            i = scriptUtil.convertNumpyArray(plane, minMax, uint8)
            assert i is not None
            cIndex += 1

    def testNumpySaveAsImage(self):
        imported_pix = ",".join(self.import_image())
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id in (%s)" % imported_pix, None)
        conn = BlitzGateway(client_obj=self.client)
        image = conn.getObject("Image", imported_img.id.getValue())
        pixels = image.getPrimaryPixels()
        channelMinMax = []
        for c in image.getChannels():
            minC = c.getWindowMin()
            maxC = c.getWindowMax()
            channelMinMax.append((minC, maxC))
        theZ = image.getSizeZ() / 2
        theT = 0
        cIndex = 0
        for minMax in channelMinMax:
            plane = pixels.getPlane(theZ, cIndex, theT)
            name = "test%s.tiff" % cIndex
            scriptUtil.numpySaveAsImage(plane, minMax, int32, name)
            cIndex += 1
