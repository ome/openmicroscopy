#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved. Use is subject to license terms supplied in LICENSE.txt
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
   Integration test for analysis scripts.
"""

import omero
import omero.scripts
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import runScript, pointsToString
from test.integration.scriptstest.script import checkFileAnnotation

kymograph_path = "scripts/omero/analysis_scripts/Kymograph.py"
plot_profile_path = "scripts/omero/analysis_scripts/Plot_Profile.py"


class TestAnalysisScripts(ScriptTest):

    def testKymograph(self):
        scriptId = super(TestAnalysisScripts, self).upload(kymograph_path)

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # create a test image
        sizeT = 3
        sizeX = 100
        sizeY = 100
        image = self.createTestImage(sizeX, sizeY, 1, 2, sizeT)    # x,y,z,c,t
        image_id = image.getId().getValue()
        roi = createROI(image_id, 0, sizeX / 2, 0, sizeY / 2, sizeT)
        session.getUpdateService().saveAndReturnObject(roi)
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image_id))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds),
            "Line_Width": omero.rtypes.rint(5)
        }

        kymograph_img = runScript(client, scriptId, argMap, "New_Image")

        # check the result
        assert kymograph_img is not None
        assert kymograph_img.val.id.val > 0

    def testPlotProfile(self):
        scriptId = super(TestAnalysisScripts, self).upload(plot_profile_path)
        # root session is root.sf
        session = self.root.sf
        client = self.root

        # create a test image
        sizeT = 3
        sizeX = 100
        sizeY = 100
        image = self.createTestImage(sizeX, sizeY, 1, 2, sizeT)    # x,y,z,c,t
        image_id = image.getId().getValue()
        roi = createROI(image_id, 0, sizeX / 2, 0, sizeY / 2, sizeT)
        session.getUpdateService().saveAndReturnObject(roi)
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image_id))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds),
            "Line_Width": omero.rtypes.rint(2),
            "Sum_or_Average": omero.rtypes.rstring("Average")
        }
        ann = runScript(client, scriptId, argMap, "Line_Data")
        checkFileAnnotation(client, ann, True)


def createROI(imageId, x1, x2, y1, y2, sizeT):
    """
    Create an ROI with lines and polylines.
    """
    roi = omero.model.RoiI()
    roi.setImage(omero.model.ImageI(imageId, False))
    # create lines and polylines on each timepoint
    for t in range(sizeT):
        # lines
        line = omero.model.LineI()
        line.x1 = omero.rtypes.rdouble(x1)
        line.x2 = omero.rtypes.rdouble(x2)
        line.y1 = omero.rtypes.rdouble(y1)
        line.y2 = omero.rtypes.rdouble(y2)
        line.theZ = omero.rtypes.rint(0)
        line.theT = omero.rtypes.rint(t)
        roi.addShape(line)
        # polylines
        polyline = omero.model.PolylineI()
        polyline.theZ = omero.rtypes.rint(0)
        polyline.theT = omero.rtypes.rint(t)
        points = [[10, 20], [50, 50], [75, 60]]
        polyline.points = omero.rtypes.rstring(pointsToString(points))
        roi.addShape(polyline)
    return roi
