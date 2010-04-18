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

import logging
logging.basicConfig(level=logging.WARN)

import unittest
import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("integration.simple"))
    suite.addTest(load("integration.api"))
    suite.addTest(load("integration.client_ctors"))
    suite.addTest(load("integration.clientusage"))
    suite.addTest(load("integration.counts"))
    suite.addTest(load("integration.gateway"))
    suite.addTest(load("integration.icontainer"))
    suite.addTest(load("integration.isession"))
    suite.addTest(load("integration.ishare"))
    suite.addTest(load("integration.itypes"))
    suite.addTest(load("integration.iquery"))
    suite.addTest(load("integration.metadatastore"))
    suite.addTest(load("integration.rawfilestore"))
    suite.addTest(load("integration.scripts"))
    suite.addTest(load("integration.files"))
    suite.addTest(load("integration.tickets1000"))
    suite.addTest(load("integration.tickets2000"))
    suite.addTest(load("clitest.integration_suite._additional_tests"))
    suite.addTest(load("gatewaytest.suite._additional_tests"))
    suite.addTest(load("scriptstest.suite._additional_tests"))
    suite.addTest(load("scriptstest.integration_suite._additional_tests"))
    suite.addTest(load("tablestest.suite._additional_tests"))
    suite.addTest(load("tablestest.integration_suite._additional_tests"))
    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(output='target/test-reports').run(additional_tests())
