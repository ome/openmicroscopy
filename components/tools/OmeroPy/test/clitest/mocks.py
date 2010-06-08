#!/usr/bin/env python

"""
   Mock context objects for cli tests.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions
import subprocess

from omero.cli import CLI
from omero.cli import Context
from omero.cli import BaseControl
from omero.cli import NonZeroReturnCode

from omero_ext import mox


class MockCLI(CLI):

    def __init__(self, *args, **kwargs):
        self.__output = []
        self.__error = []
        self.__popen = []
        self.mox = mox.Mox()
        CLI.__init__(self, *args, **kwargs)

    #
    # Overrides
    #

    def out(self, *args):
        self.__output.append(args[0])

    def err(self, *args):
        self.__error.append(args[0])

    def call(self, *args, **kwargs):
        assert False

    def conn(self, *args, **kwargs):
        assert False

    def popen(self, *args, **kwargs):
        return self.__popen.pop(0)

    #
    # Test methods
    #

    def assertEquals(self, a, b):
        if a != b:
            raise AssertionError("%s!=%s" % (a, b))

    def assertStdout(self, args):
        try:
            self.assertEquals(set(args), set(self.__output))
        finally:
            self.__output = []

    def assertStderr(self, args):
        try:
            self.assertEquals(set(args), set(self.__error))
        finally:
            self.__error = []

    def createPopen(self):
        popen = self.mox.CreateMock(subprocess.Popen)
        self.__popen.append(popen)
        return popen

    def replay(self, mock):
        mox.Replay(mock)

    def tearDown(self):
        try:
            self.mox.VerifyAll()
        finally:
            self.mox.UnsetStubs()
