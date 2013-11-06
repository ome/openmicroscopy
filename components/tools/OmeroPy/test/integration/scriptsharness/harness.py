#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Harness for calling scripts via `omero script`
   of omero.client

   Copyright 2008-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero, omero.cli, unittest

def call(*args):
    omero.cli.argv(["omero","script"]+list(args))

class TestScriptsViaOmeroCli(unittest.TestCase):

    def testDefinition(self):
        call("test/scripts/definition.py","--Ice.Config=test/scripts/definition.cfg")
    def testSimpleScript(self):
        call("test/scripts/simple_script.py","--Ice.Config=test/scripts/simple_script.cfg")

