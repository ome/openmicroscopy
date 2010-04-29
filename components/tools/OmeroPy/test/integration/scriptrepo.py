#!/usr/bin/env python

"""
   Integration test focused on the registering of official
   scripts in the ScriptRepo. (see ticket:#2073)

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
<<<<<<< HEAD
import integration.library as lib
=======
import test.integration.library as lib
>>>>>>> d5e9630... see #2073 - Working ScriptRepository, but no registration
import tempfile
import omero
import omero.all
from omero_model_ScriptJobI import ScriptJobI
from omero.rtypes import *

class TestScriptRepo(lib.ITest):

    def testHowToFindAScriptRepo(self):
        sr = self.client.sf.sharedResources()
        repo = sr.getScriptRepository()
        print repo
        self.assert_( repo )

    def testCantUndulyLoadScriptRepoFromUuid(self):
        self.fail()

    def testMultipleScriptPathsNotSupported(self):
        self.fail()

    def testUploadingViaOfficialScriptShowsUpInRepo(self):
        self.fail()

    def testUploadingViaNonOfficialScriptDoesntShowUpInRepo(self):
        self.fail()

if __name__ == '__main__':
    unittest.main()
