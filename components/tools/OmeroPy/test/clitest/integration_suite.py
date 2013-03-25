#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   cli integration test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("clitest.script"))
    return suite

if __name__ == "__main__":
    unittest.TextTestRunner().run(_additional_tests())
