#!/usr/bin/env python

"""
   Integration test focused on the model changes in 4.2

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
from omero.rtypes import *


class TestModel42(lib.ITest):

    def testNs(self):
        ns = omero.model.NamespaceI()
        ns.name = rstring(self.uuid())
        ns.keywords = ["a", "b"]
        ns = self.root.sf.getUpdateService().saveAndReturnObject(ns)

    def testRoi(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)

if __name__ == '__main__':
    unittest.main()
