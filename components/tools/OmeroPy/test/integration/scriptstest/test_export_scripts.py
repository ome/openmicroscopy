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
   Integration test for export scripts.
"""

import omero
import omero.scripts
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import runScript
from test.integration.scriptstest.script import checkFileAnnotation


batch_image_export = "/omero/export_scripts/Batch_Image_Export.py"
make_movie = "/omero/export_scripts/Make_Movie.py"


class TestExportScripts(ScriptTest):

    def testBatchImageExport(self):
        scriptId = super(TestExportScripts, self).getScript(batch_image_export)
        assert scriptId > 0

        client, user = self.new_client_and_user()
        # x,y,z,c,t
        image = self.createTestImage(100, 100, 1, 1, 1, client.getSession())
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds)
        }
        ann = runScript(client, scriptId, argMap, "File_Annotation")
        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)

    def testMakeMovie(self):
        scriptId = super(TestExportScripts, self).getScript(make_movie)
        assert scriptId > 0

        client, user = self.new_client_and_user()
        # x,y,z,c,t
        image = self.createTestImage(100, 100, 1, 1, 1, client.getSession())
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds)
        }
        ann = runScript(client, scriptId, argMap, "File_Annotation")
        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)
