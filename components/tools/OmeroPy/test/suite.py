#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Top-level test suite. Please add a reference to your subpackage,
   module, or specific class here.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero_ext.xmlrunner.main import ome_test_main

if __name__ == "__main__":
    ome_test_main([
        "PythonOnly",
        "cmdtest.suite",
        # "scriptstest.harness"
        "clitest.suite._additional_tests",
        "fstest.suite",
        "scriptstest.suite._additional_tests",
        "tablestest.suite._additional_tests"])
