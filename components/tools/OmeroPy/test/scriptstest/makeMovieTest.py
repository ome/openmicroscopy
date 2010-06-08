#!/usr/bin/env python

"""
   Integration test which checks the various parameters for makemovie.py

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import integration.library as lib
import unittest, os, sys, uuid


class TestMakeMovie(lib.ITest):

    def setUp(self):
        lib.ITest.setUp(self)
        self.svc = self.client.sf.getScriptService()

    def testNoParams(self):
        makeMovieID = self.svc.getScriptID("/omero/export_scripts/makeMovie.py")
        process = self.svc.runScript(makeMovieID, {}, None)

if __name__ == '__main__':
    unittest.main()
