#!/usr/bin/env python

"""
   Mock context objects for cli tests.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import Context, BaseControl, CLI, NonZeroReturnCode

class MockCLI(CLI):

    def __init__(self, *args, **kwargs):
        self.__output = []
        self.__error = []
        CLI.__init__(self, *args, **kwargs)

    def out(self, *args):
        self.__output.append(args[0])

    def err(self, *args):
        self.__error.append(args[0])

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

    def conn(self, *args, **kwargs):
        raise

    def popen(self, *args, **kwargs):
        raise

    def call(self, *args, **kwargs):
        raise

