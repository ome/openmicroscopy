#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


from omero.util.temp_files import create_path


class TestCase(object):

    def setup_method(self, method):
        self.dir = self.tmpdir()

    def tmpdir(self):
        """
        """
        return create_path(folder=True)
