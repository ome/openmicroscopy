#!/usr/bin/env python

"""
   Library for integration tests

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero

class ITest(unittest.TestCase):

    def setUp(self):
        self.client = omero.client()
        self.client.createSession()

    def tearDown(self):
        self.client.closeSession()
