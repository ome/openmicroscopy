#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test which checks the various parameters for makemovie.py

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import os, sys
import omero.processor

class TestMakeMovie(lib.ITest):
    """
    Requires PIL being installed
    """

    def setUp(self):
        lib.ITest.setUp(self)
        self.svc = self.client.sf.getScriptService()

    def testNoParams(self):
        makeMovieID = self.svc.getScriptID("/omero/export_scripts/Make_Movie.py")
        imported_pix = ",".join(self.import_image())
        imported_img = self.query.findByQuery("select i from Image i join fetch i.pixels pixels where pixels.id in (%s)" % imported_pix, None)
        inputs = {"IDs": imported_img.id}
        impl = omero.processor.usermode_processor(self.root)
        try:
            process = self.svc.runScript(makeMovieID, inputs, None)
        finally:
            impl.cleanup()
