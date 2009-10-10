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
from omero.util.temp_files import create_path

class TestCase(unittest.TestCase):

    def setUp (self):
        unittest.TestCase.setUp(self)
        self.dir = self.tmpdir()

    def tmpdir(self):
        """
        """
        return create_path(folder=True)
