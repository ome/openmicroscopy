#!/usr/bin/env python

"""
   tables test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import logging
logging.basicConfig(level=0)

class TopLevel(unittest.TestCase):
    pass

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("tablestest.servants"))
    suite.addTest(load("tablestest.hdfstorage"))
    return suite

if __name__ == "__main__":
    suite = _additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
