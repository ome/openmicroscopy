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
from omero_ext import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()

    suite.addTest(load("integration.admin"))
    suite.addTest(load("integration.annotation"))
    suite.addTest(load("integration.bfpixelsstoreexternal"))
    suite.addTest(load("integration.bfpixelsstoreinternal"))
    suite.addTest(load("integration.bigImages"))
    suite.addTest(load("integration.client_ctors"))
    suite.addTest(load("integration.clientusage"))
    suite.addTest(load("integration.cliimport"))
    suite.addTest(load("integration.counts"))
    suite.addTest(load("integration.delete"))
    suite.addTest(load("integration.emanScripts"))
    suite.addTest(load("integration.exporter"))
    suite.addTest(load("integration.figureExportScripts"))
    suite.addTest(load("integration.files"))
    suite.addTest(load("integration.gateway"))
    suite.addTest(load("integration.icontainer"))
    suite.addTest(load("integration.ildap"))
    suite.addTest(load("integration.imetadata"))
    suite.addTest(load("integration.iquery"))
    suite.addTest(load("integration.isession"))
    suite.addTest(load("integration.ishare"))
    suite.addTest(load("integration.itimeline"))
    suite.addTest(load("integration.itypes"))
    suite.addTest(load("integration.iupdate"))
    suite.addTest(load("integration.metadatastore"))
    suite.addTest(load("integration.model42"))
    suite.addTest(load("integration.permissions"))
    suite.addTest(load("integration.pixelsService"))
    suite.addTest(load("integration.rawfilestore"))
    suite.addTest(load("integration.rawpixelsstore"))
    suite.addTest(load("integration.repository"))
    suite.addTest(load("integration.rois"))
    suite.addTest(load("integration.scripts"))
    suite.addTest(load("integration.search"))
    suite.addTest(load("integration.simple"))
    suite.addTest(load("integration.thumbnailPerms"))
    suite.addTest(load("integration.thumbs"))
    suite.addTest(load("integration.tickets1000"))
    suite.addTest(load("integration.tickets2000"))
    suite.addTest(load("integration.tickets3000"))
    suite.addTest(load("integration.tickets4000"))
    suite.addTest(load("integration.tickets5000"))
    suite.addTest(load("integration.tickets6000"))

    suite.addTest(load("clitest.integration_suite._additional_tests"))
    suite.addTest(load("cmdtest.integration_suite._additional_tests"))
    suite.addTest(load("gatewaytest.suite._additional_tests"))
    suite.addTest(load("scriptstest.suite._additional_tests"))
    suite.addTest(load("scriptstest.integration_suite._additional_tests"))
    suite.addTest(load("tablestest.suite._additional_tests"))
    suite.addTest(load("tablestest.integration_suite._additional_tests"))

    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(verbose=True, output='target/reports').run(additional_tests())
