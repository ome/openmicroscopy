#!/usr/bin/env python

"""
   Integration test focused on the omero.api.ITypes interface
   a running server.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
import omero_Constants_ice

class TestTypes(lib.ITest):

    # ticket:1436
    def testGetEnumerationTypes(self):
        i = self.client.sf.getTypesService().getEnumerationTypes()

if __name__ == '__main__':
    unittest.main()
