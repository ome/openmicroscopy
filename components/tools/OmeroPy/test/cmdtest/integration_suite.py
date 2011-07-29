#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   cli integration test suite. Please add a reference to your subpackage,
   module, or specific class here.
"""

import unittest

class TopLevel(unittest.TestCase):
    pass

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("cmdtest.example"))
    return suite

if __name__ == "__main__":
    unittest.TextTestRunner().run(_additional_tests())
