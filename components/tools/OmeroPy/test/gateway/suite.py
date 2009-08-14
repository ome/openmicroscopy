#!/usr/bin/env python

"""
   Gateway test suite.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest

class TopLevel(unittest.TestCase):
    pass

#from helpers import *

def _additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("test.gateway.helpers"))
    suite.addTest(load("test.gateway.user"))
    suite.addTest(load("test.gateway.rdefs"))
    suite.addTest(load("test.gateway.image"))
    suite.addTest(load("test.gateway.annotation"))
    suite.addTest(load("test.gateway.connection"))
    suite.addTest(load("test.gateway.wrapper"))
    suite.addTest(load("test.gateway.z_db_cleanup"))
    return suite

if __name__ == '__main__':
    suite = _additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
