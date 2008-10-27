#!/usr/bin/env python

"""
   Integration test focused running interactive scripts.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
import omero
from omero_model_ScriptJobI import ScriptJobI
from omero.rtypes import *

class TestScripts(lib.ITest):

    def testBasicUsage(self):
        job = ScriptJobI()
        proc = self.client.sf.acquireProcessor(job, 20)
        #proc.

    def testTicket1036(self):
        self.client.setInput("a", rstring("a"));
        self.client.getInput("a");

if __name__ == '__main__':
    unittest.main()
