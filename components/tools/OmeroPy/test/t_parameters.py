#!/usr/bin/env python

"""
   Simple unit test which makes various calls on the code
   generated model.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
from omero_sys_ParametersI import ParametersI

class TestParameters(unittest.TestCase):

    def testAddId(self):
        p = ParametersI()
        p.addId(1L)

if __name__ == '__main__':
    unittest.main()
