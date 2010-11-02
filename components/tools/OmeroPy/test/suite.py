#!/usr/bin/env python

"""
   Top-level test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("t_bin"))
    suite.addTest(load("t_config"))
    suite.addTest(load("t_rtypes"))
    suite.addTest(load("t_model"))
    suite.addTest(load("t_parameters"))
    suite.addTest(load("t_permissions"))
    suite.addTest(load("t_tempfiles"))
    suite.addTest(load("clitest.suite"))
    #suite.addTest(load("scriptstest.harness"))
    suite.addTest(load("scriptstest.suite"))
    suite.addTest(load("tablestest.suite"))
    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(verbose=True, output='target/reports').run(additional_tests())
