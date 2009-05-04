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
    suite.addTest(load("test.gateway.helpers"))
    suite.addTest(load("test.gateway.user"))
    suite.addTest(load("test.gateway.rdefs"))
    suite.addTest(load("test.gateway.image"))
    suite.addTest(load("test.gateway.annotation"))
    suite.addTest(load("test.gateway.connection"))
    suite.addTest(load("test.gateway.wrapper"))
    return suite

if __name__ == '__main__':
    suite = additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
