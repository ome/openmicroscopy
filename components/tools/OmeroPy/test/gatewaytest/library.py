#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Library for gateway tests

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.gateway.scripts.testdb_create import *

class GTest(unittest.TestCase, TestDBHelper):
    pass
