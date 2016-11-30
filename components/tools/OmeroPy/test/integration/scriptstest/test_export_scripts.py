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

import pytest
import omero
import omero.scripts
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import run_script
from test.integration.scriptstest.script import check_file_annotation


batch_image_export = "/omero/export_scripts/Batch_Image_Export.py"
make_movie = "/omero/export_scripts/Make_Movie.py"


class TestExportScripts(ScriptTest):

    def test_batch_image_export(self):
        sid = super(TestExportScripts, self).get_script(batch_image_export)
        assert sid > 0

        client, user = self.new_client_and_user()
        # x,y,z,c,t
        image = self.createTestImage(100, 100, 1, 1, 1, client.getSession())
        image_ids = []
        image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids)
        }
        ann = run_script(client, sid, args, "File_Annotation")
        c = self.new_client(user=user)
        check_file_annotation(c, ann)

    @pytest.mark.broken(reason="needs mencoder installed")
    def test_make_movie(self):
        script_id = super(TestExportScripts, self).get_script(make_movie)
        assert script_id > 0

        client, user = self.new_client_and_user()
        # x,y,z,c,t
        image = self.createTestImage(100, 100, 1, 1, 1, client.getSession())
        image_ids = []
        image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids)
        }
        ann = run_script(client, script_id, args, "File_Annotation")
        c = self.new_client(user=user)
        check_file_annotation(c, ann)
