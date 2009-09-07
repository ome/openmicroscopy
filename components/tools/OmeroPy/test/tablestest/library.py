#!/usr/bin/env python

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, tempfile, exceptions
import omero.tables
import portalocker
import tables

from path import path

class TestCase(unittest.TestCase):

    def setUp (self):
        unittest.TestCase.setUp(self)
        self.dir = path(tempfile.gettempdir()) / "test-omero"
        if not self.dir.exists():
            self.dir.mkdir()
        # Creates a single tempdir under /tmp/test-omero
        # and under *that* directory we'll create more
        # directories for each test. That prevents the
        # horrendous of directories that might otherwise
        # getet created.
        self.dir = self.tmpdir()

    def tmpdir(self):
        """
        """
        return tempfile.mkdtemp(dir=str(self.dir))
