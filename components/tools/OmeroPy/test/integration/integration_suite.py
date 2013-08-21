#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test suite. Please add a reference to your subpackage,
   module, or specific class here.

   This suite is called via `ant python-integration` (defined in
   tools/python.xml) and requires that a blitz server be running to
   perform properly.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero_ext.xmlrunner.main import ome_test_main

if __name__ == "__main__":
    ome_test_main([
        "integration.admin",
        "integration.annotation",
        "integration.bfpixelsstoreexternal",
        "integration.bfpixelsstoreinternal",
        "integration.bigImages",
        "integration.chmod",
        "integration.client_ctors",
        "integration.clientusage",
        "integration.cliimport",
        "integration.cmdcallback",
        "integration.counts",
        "integration.delete",
        "integration.exporter",
        "integration.figureExportScripts",
        "integration.files",
        "integration.gateway",
        "integration.icontainer",
        "integration.ildap",
        "integration.imetadata",
        "integration.iquery",
        "integration.isession",
        "integration.ishare",
        "integration.itimeline",
        "integration.itypes",
        "integration.iupdate",
        "integration.metadatastore",
        "integration.model42",
        "integration.permissions",
        "integration.pixelsService",
        "integration.rawfilestore",
        "integration.rawpixelsstore",
        "integration.repository",
        "integration.rois",
        "integration.scripts",
        "integration.search",
        "integration.simple",
        "integration.thumbnailPerms",
        "integration.thumbs",
        "integration.tickets1000",
        "integration.tickets2000",
        "integration.tickets3000",
        "integration.tickets4000",
        "integration.tickets5000",
        "integration.tickets6000",
        "clitest.integration_suite._additional_tests",
        "cmdtest.integration_suite._additional_tests",
        "gatewaytest.suite._additional_tests",
        "scriptstest.suite._additional_tests",
        "scriptstest.integration_suite._additional_tests",
        "tablestest.suite._additional_tests",
        "tablestest.integration_suite._additional_tests"])
