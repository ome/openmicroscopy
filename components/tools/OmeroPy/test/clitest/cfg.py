#!/usr/bin/env python

"""
   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, os, subprocess, StringIO
from exceptions import Exception as Exc
from path import path
from omero.cli import Context, BaseControl

omeroDir = path(os.getcwd()) / "build"

class TestCfg(unittest.TestCase):

    def testArgWithSpacesIsntParsed(self):
        Arguments()
        Arguments("a string")
        Arguments("a list of strings".split())
        Arguments((["list2"],))

        # Failing calls
        self.assertRaises(Exc, lambda: Arguments({"a":"map"}))
        self.assertRaises(Exc, lambda: Arguments(([],[])))

if __name__ == '__main__':
    unittest.main()
