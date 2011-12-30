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
        self.__expect = []
        self.__output = []
        self.__error = []
        self.__popen = []
        self.__call = []
        self.mox = mox.Mox()
        CLI.__init__(self, *args, **kwargs)

    #
    # Overrides
    #

    def input(self, *args, **kwargs):
        key = args[0]

        for I, T in enumerate(self.__expect):
            K, V = T
            if key == K:
                self.__expect.pop(I)
                return V

        # Not found
        msg = """Couldn't find key: "%s". Options: %s""" % \
                (key, [x[0] for x in self.__expect])
        print msg
        raise Exception(msg)

    def out(self, *args):
        self.__output.append(args[0])

    def err(self, *args):
        self.__error.append(args[0])

    def call(self, args):
        rv = self.__call.pop(0)
        if rv != 0:
            raise NonZeroReturnCode(rv)
        return 0

    def conn(self, *args, **kwargs):
        assert False

    def popen(self, *args, **kwargs):
        return self.__popen.pop(0)

    #
    # Test methods
    #

    def expect(self, key, value):
        self.__expect.append((key, value))

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

    def addCall(self, rv):
        self.__call.append(rv)

    def assertCalled(self):
        self.assertEquals(0, len(self.__call))

    def createPopen(self):
        popen = self.mox.CreateMock(subprocess.Popen)
        self.__popen.append(popen)
        return popen

    def checksIceVersion(self):
        popen = self.createPopen()
        popen.communicate().AndReturn([None, "3.3.1"])
        self.replay(popen)

    def checksStatus(self, rcode):
        popen = self.createPopen()
        popen.wait().AndReturn(rcode)
        self.replay(popen)

    def assertPopened(self):
        self.assertEquals(0, len(self.__popen))

    def replay(self, mock):
        mox.Replay(mock)

    def tearDown(self):
        try:
            self.mox.VerifyAll()
        finally:
            self.mox.UnsetStubs()
