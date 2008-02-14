#!/usr/bin/env python

"""
   Integration test focused running interactive scripts.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero
from omero_model_ScriptJobI import ScriptJobI

class TestScripts(lib.ITest):

    def testBasicUsage(self):
        job = ScriptJobI()
        proc = self.client.sf.acquireInteractiveProcess(job, 20)
        print proc

if __name__ == '__main__':
    unittest.main()
