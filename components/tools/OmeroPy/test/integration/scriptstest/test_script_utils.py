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

import pytest
from omero.testlib import ITest
import omero
import omero.util.script_utils as scriptUtil
from omero.gateway import BlitzGateway
import tempfile
import shutil
from os import listdir, remove
from os.path import isfile, join, exists
from numpy import int32, uint8

try:
    from PIL import Image  # see ticket:2597
except:  # pragma: nocover
    import Image  # see ticket:2597


class TestScriptUtils(ITest):

    def testSplitImage(self):
        imported_pix = ",".join(self.import_image())
        dir = tempfile.mkdtemp()
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = omero.rtypes.rlong(imported_pix)

        query_string = "select p from Pixels p where p.id=:id"
        pixels = self.query.findByQuery(query_string, params)
        sizeZ = pixels.getSizeZ().getValue()
        sizeC = pixels.getSizeC().getValue()
        sizeT = pixels.getSizeT().getValue()
        # split the image into file
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id=:id", params)
        scriptUtil.split_image(self.client, imported_img.id.getValue(), dir,
                               unformattedImageName="a_T%05d_C%s_Z%d_S1.tiff")
        files = [f for f in listdir(dir) if isfile(join(dir, f))]
        shutil.rmtree(dir)
        assert sizeZ*sizeC*sizeT == len(files)

    def testNumpyToImage(self):
        imported_pix = ",".join(self.import_image())
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = omero.rtypes.rlong(imported_pix)
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id=:id", params)
        # session is closed during teardown
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
            cIndex += 1

    def testConvertNumpyArray(self):
        imported_pix = ",".join(self.import_image())
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = omero.rtypes.rlong(imported_pix)

        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id=:id", params)
        # session is closed during teardown
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

    @pytest.mark.parametrize('format', ['tiff', 'foo'])
    @pytest.mark.parametrize('is_file', [True, False])
    def testNumpySaveAsImage(self, format, is_file):
        imported_pix = ",".join(self.import_image())
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = omero.rtypes.rlong(imported_pix)

        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id=:id", params)
        # session is closed during teardown
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

            suffix = ".%s" % format
            name = None
            if is_file is True:
                # create a temporary file
                tf = tempfile.NamedTemporaryFile(mode='r+b', suffix=suffix)
                name = tf.name
            else:
                name = "test%s.%s" % (cIndex, format)
            scriptUtil.numpySaveAsImage(plane, minMax, int32, name)
            # try to open the image
            try:
                Image.open(name)
                assert format == "tiff"
                # delete it since to handle case where it is not a tmp file
                remove(name)
            except IOError:
                assert format != "tiff"
                # file should have been deleted
                assert exists(name) is False
            cIndex += 1
