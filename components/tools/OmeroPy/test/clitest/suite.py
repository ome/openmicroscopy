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

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("clitest.admin"))
    suite.addTest(load("clitest.args"))
    suite.addTest(load("clitest.base"))
    suite.addTest(load("clitest.cfg"))
    suite.addTest(load("clitest.cli"))
    suite.addTest(load("clitest.db"))
    suite.addTest(load("clitest.export"))
    suite.addTest(load("clitest.java"))
    suite.addTest(load("clitest.import"))
    suite.addTest(load("clitest.node"))
    suite.addTest(load("clitest.prefs"))
    suite.addTest(load("clitest.rcode"))
    suite.addTest(load("clitest.sess"))
    return suite

if __name__ == "__main__":
    unittest.TextTestRunner().run(additional_tests())
