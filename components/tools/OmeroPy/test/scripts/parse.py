#!/usr/bin/env python

"""
   Test of the omero.scripts.parse functionality

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import integration.library as lib
from uuid import uuid4 as uuid
from path import path

import sys
import unittest

import omero
from omero.scripts import *

class TestParse(lib.ITest):
    def testParse(self):
        try:
            cfg = self.tmpfile().name
            cfg = "/tmp/t"
            f = open(cfg, "w")
            for prefix in ["Ice","omero"]:
                m = self.client.ic.getProperties().getPropertiesForPrefix(prefix)
                for k, v in m.items():
                    f.write("%s=%s\n" % (k,v))
            f.write("omero.scripts.parse=true\n")
            f.close()
            c = omero.client(["--Ice.Config=%s" % cfg])
            script_client = client(str(uuid()), "simple ping script", Long("a").inout(), String("b").inout(), client = c)
            print "IN CLIENT: " + script_client.getProperty("omero.scripts.parse")
            self.fail("Should have raised ParseExit")
        except ParseExit, pe:
            pass

if __name__ == '__main__':
    unittest.main()
