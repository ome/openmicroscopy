#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   scripts test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("scriptstest.parse"))
    suite.addTest(load("scriptstest.processor"))
    suite.addTest(load("scriptstest.prototypes"))
    return suite

if __name__ == "__main__":
    unittest.TextTestRunner().run(_additional_tests())

