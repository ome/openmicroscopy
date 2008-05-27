#!/usr/bin/env python

"""
   Top-level test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("test.cli.suite"))
    suite.addTest(load("test.t_model"))
    suite.addTest(load("test.t_permissions"))
    suite.addTest(load("test.scripts.harness"))
    suite.addTest(load("test.processor"))
    return suite
