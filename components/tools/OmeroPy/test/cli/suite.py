#!/usr/bin/env python

"""
   cli test suite. Please add a reference to your subpackage,
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
    suite.addTest(load("test.cli.admin"))
    suite.addTest(load("test.cli.args"))
    suite.addTest(load("test.cli.cli"))
    suite.addTest(load("test.cli.db"))
    suite.addTest(load("test.cli.java"))
    suite.addTest(load("test.cli.node"))
    suite.addTest(load("test.cli.base"))
    suite.addTest(load("test.cli.rcode"))
    suite.addTest(load("test.cli.sess"))
    return suite

if __name__ == "__main__":
    unittest.TextTestRunner().run(additional_tests())
