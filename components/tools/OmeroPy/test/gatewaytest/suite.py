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
    suite.addTest(load("gatewaytest.helpers"))
    suite.addTest(load("gatewaytest.user"))
    suite.addTest(load("gatewaytest.rdefs"))
    suite.addTest(load("gatewaytest.image"))
    suite.addTest(load("gatewaytest.annotation"))
    suite.addTest(load("gatewaytest.connection"))
    suite.addTest(load("gatewaytest.wrapper"))
    suite.addTest(load("gatewaytest.get_objects"))
    suite.addTest(load("gatewaytest.z_db_cleanup"))
    return suite

if __name__ == '__main__':
    suite = _additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
