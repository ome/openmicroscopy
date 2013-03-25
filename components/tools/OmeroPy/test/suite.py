#!/usr/bin/env python

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
        "t_clients",
        "t_config",
        "t_ext",
        "t_rtypes",
        "t_model",
        "t_parameters",
        "t_permissions",
        "t_tempfiles",
        "clitest.suite",
        "cmdtest.suite",
        # "scriptstest.harness"
        "clitest.suite._additional_tests",
        "fstest.suite",
        "scriptstest.suite._additional_tests",
        "tablestest.suite._additional_tests"])
