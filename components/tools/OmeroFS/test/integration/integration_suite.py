#!/usr/bin/env python

"""
   Integration test suite. Please add a reference to your subpackage,
   module, or specific class here.

   This suite is called via `ant python-integration` (defined in
   tools/python.xml) and requires that a blitz server be running to
   perform properly.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import logging
logging.basicConfig(level=logging.ERROR)

import unittest

from omero_ext import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("integration.example"))
    suite.addTest(load("integration.dbclient"))
    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(verbose=True, output='target/reports').run(_additional_tests())
