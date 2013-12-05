#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Mock context objects for cli tests.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess
import logging

from omero.cli import CLI, NonZeroReturnCode

from omero_ext import mox
from omero_version import ice_compatibility

LOG = logging.getLogger("climocks")


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

    def out(self, *args, **kwargs):
        self.__output.append(args[0])

    def err(self, *args):
        self.__error.append(args[0])

    def call(self, args):
        LOG.debug("call:%s" % args)
        rv = self.__call.pop(0)
        if rv != 0:
            raise NonZeroReturnCode(rv)
        return 0

    def conn(self, *args, **kwargs):
        assert False

    def popen(self, *args, **kwargs):
        LOG.debug("popen:%s {%s}" % (args, kwargs))
        return self.__popen.pop(0)

    #
    # Test methods
    #

    def expect(self, key, value):
        self.__expect.append((key, value))

    def assertStdout(self, args):
        try:
            assert set(args) == set(self.__output)
        finally:
            self.__output = []

    def assertStderr(self, args):
        try:
            assert set(args) == set(self.__error)
        finally:
            self.__error = []

    def addCall(self, rv):
        self.__call.append(rv)

    def assertCalled(self):
        assert 0 == len(self.__call)

    def createPopen(self):
        popen = self.mox.CreateMock(subprocess.Popen)
        self.__popen.append(popen)
        return popen

    def checksIceVersion(self):
        popen = self.createPopen()
        popen.communicate().AndReturn([None, ice_compatibility])
        self.replay(popen)

    def checksStatus(self, rcode):
        popen = self.createPopen()
        popen.wait().AndReturn(rcode)
        self.replay(popen)

    def assertPopened(self):
        assert 0 == len(self.__popen)

    def replay(self, mock):
        mox.Replay(mock)

    def teardown_method(self, method):
        try:
            self.mox.VerifyAll()
        finally:
            self.mox.UnsetStubs()
