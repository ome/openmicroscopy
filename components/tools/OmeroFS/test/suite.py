#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Top-level test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
from omero_ext import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("example"))
    suite.addTest(load("monitor"))
    suite.addTest(load("drivers"))
    suite.addTest(load("replay"))
    suite.addTest(load("state"))
    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(verbose=True, output='target/reports').run(additional_tests())
