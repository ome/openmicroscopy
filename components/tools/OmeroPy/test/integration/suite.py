#!/usr/bin/env python

"""
   Integration test suite. Please add a reference to your subpackage,
   module, or specific class here.

   This suite is called via `ant python-integration` (defined in
   tools/python.xml) and requires that a blitz server be running to
   perform properly.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("test.integration.simple"))
    suite.addTest(load("test.integration.counts"))
    suite.addTest(load("test.integration.ipojos"))
    suite.addTest(load("test.integration.isession"))
    suite.addTest(load("test.integration.scripts"))
    suite.addTest(load("test.integration.files"))
    suite.addTest(load("test.integration.ping"))
    suite.addTest(load("test.integration.tickets1000"))
    return suite
