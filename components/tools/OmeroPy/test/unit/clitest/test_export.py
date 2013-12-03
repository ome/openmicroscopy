#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the export plugin

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
from path import path
from omero.cli import CLI, NonZeroReturnCode
from omero.plugins.export import ExportControl
from omero.util.temp_files import create_path

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


class TestExport(object):

    def setup_method(self, method):
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
            assert False, "ZeroReturnCode??"
        except NonZeroReturnCode:
            pass
