#!/usr/bin/env python

"""
   Top-level test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("test.example"))
    suite.addTest(load("test.monitor"))
    suite.addTest(load("test.drivers"))
    suite.addTest(load("test.state"))
    return suite
