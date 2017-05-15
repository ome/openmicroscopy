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

import pytest

from omero.plugins.render import RenderControl
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest
from omero.gateway import BlitzGateway


# TODO: rdefid, tbid
SUPPORTED = {
    "idonly": "-1",
    "imageid": "Image:-1",
    "plateid": "Plate:-1",
    "screenid": "Screen:-1",
}


class TestRender(CLITest):

    def setup_method(self, method):
        super(TestRender, self).setup_method(method)
        self.cli.register("render", RenderControl, "TEST")
        self.args += ["render"]

    def create_image(self):
        self.gw = BlitzGateway(client_obj=self.client)
        self.plates = []
        for plate in self.importPlates(fields=2, sizeC=4):
            self.plates.append(self.gw.getObject("Plate", plate.id.val))
        # Now pick the first Image
        self.imgobj = list(self.plates[0].listChildren())[0].getImage(index=0)
        self.idonly = "%s" % self.imgobj.id
        self.imageid = "Image:%s" % self.imgobj.id
        self.plateid = "Plate:%s" % self.plates[0].id
        self.screenid = "Screen:%s" % self.plates[0].getParent().id
        # And another one as the source for copies
        self.source = list(self.plates[0].listChildren())[0].getImage(index=1)
        self.source = "Image:%s" % self.source.id
        # And for all the images, pre-load a thumbnail
        for p in self.plates:
            for w in p.listChildren():
                for i in range(w.countWellSample()):
                    w.getImage(index=i).getThumbnail(
                        size=(96,), direct=False)

    # rendering tests
    # ========================================================================

    @pytest.mark.parametrize('targetName', sorted(SUPPORTED.keys()))
    def testNonExistingImage(self, targetName, tmpdir):
        target = SUPPORTED[targetName]
        self.args += ["info", target]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('targetName', sorted(SUPPORTED.keys()))
    def testInfo(self, targetName, tmpdir):
        self.create_image()
        target = getattr(self, targetName)
        self.args += ["info", target]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('targetName', sorted(SUPPORTED.keys()))
    def testCopy(self, targetName, tmpdir):
        self.create_image()
        target = getattr(self, targetName)
        self.args += ["copy", self.source, target]
        self.cli.invoke(self.args, strict=True)
