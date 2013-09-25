#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the integration library.
"""

import unittest
import test.integration.library as lib
import omero

class TestLibrary(lib.ITest):

   def test9188(self):
        self.createTestImage(10,10,1,1,1)
        self.createTestImage(10,10,10,1,1)
        self.createTestImage(10,10,1,10,1)
        self.createTestImage(10,10,1,1,10)
   
if __name__ == '__main__':
    unittest.main()
