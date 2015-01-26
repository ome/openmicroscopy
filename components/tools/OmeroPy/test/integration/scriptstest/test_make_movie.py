#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Integration test which checks the various parameters for makemovie.py

"""

import library as lib
import omero.processor


class TestMakeMovie(lib.ITest):

    """
    Requires Pillow being installed
    """

    def setup_method(self, method):
        self.svc = self.client.sf.getScriptService()

    def testNoParams(self):
        makeMovieID = self.svc.getScriptID(
            "/omero/export_scripts/Make_Movie.py")
        imported_pix = ",".join(self.import_image())
        imported_img = self.query.findByQuery(
            "select i from Image i join fetch i.pixels pixels\
            where pixels.id in (%s)" % imported_pix, None)
        inputs = {"IDs": omero.rtypes.rlist([imported_img.id])}
        impl = omero.processor.usermode_processor(self.root)
        try:
            self.svc.runScript(makeMovieID, inputs, None)
        finally:
            impl.cleanup()
