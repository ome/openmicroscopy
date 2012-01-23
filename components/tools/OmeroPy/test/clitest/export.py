#!/usr/bin/env python

"""
   Test of the export plugin

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, BaseControl, CLI, NonZeroReturnCode
from omero.plugins.sessions import SessionsControl
from omero.plugins.export import ExportControl
from omero.util.temp_files import create_path
from integration.library import ITest

omeroDir = path(os.getcwd()) / "build"

class MockCLI(CLI):

    def conn(self, args):
        return MockClient()


class MockClient(object):

    def getSession(self, *args):
        return MockSession()


class MockSession(object):

    def createExporter(self):
        return MockExporter()


class MockExporter(object):

    def all(self, *args, **kwargs):
        pass

    def __getattr__(self, key):
        if key == "generateTiff":
            return self.generateTiff
        return self.all

    def generateTiff(self, *args):
        return 1

class TestExport(unittest.TestCase):

    def setUp(self):
        self.cli = MockCLI()
        self.cli.register("x", ExportControl, "TEST")
        self.p = create_path()
        self.p.remove()

    def invoke(self, string):
        self.cli.invoke(string, strict=True)

    def testSimpleExport(self):
        self.invoke("x -f %s Image:3" % self.p)

    def testStdOutExport(self):
        """
        "-f -" should export to stdout. See ticket:7106
        """
        self.invoke("x -f - Image:3")

    def testNoStdOutExportForDatasets(self):
        try:
            self.invoke("x -f - --iterate Dataset:3")
            self.fail("ZeroReturnCode??")
        except NonZeroReturnCode:
            pass

if __name__ == '__main__':
    unittest.main()
