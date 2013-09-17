#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the differences in the OmeroPy mapping.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import Ice

from omero.clients import BaseClient

class test_client(BaseClient):
    def __init__(self):
        pass
    def __del__(self):
        pass

class TestPythonOnly(unittest.TestCase):

    def testRepairArguments(self):
        t = test_client()
        ar = [ "a", "b" ]
        id = Ice.InitializationData()
        pm = { 'A':1 }
        ho = "host"
        po = 4064
        no = None
        self.assertEquals([no, no, no, no, no], t._repair(no, no, no, no, no))
        self.assertEquals([ar, no, no, no, no], t._repair(ar, no, no, no, no))
        self.assertEquals([no, id, no, no, no], t._repair(id, no, no, no, no))
        self.assertEquals([no, no, ho, no, no], t._repair(ho, no, no, no, no))
        self.assertEquals([no, no, ho, po, no], t._repair(ho, po, no, no, no))
        self.assertEquals([no, no, no, no, pm], t._repair(pm, no, no, no, no))
        # All mixed up
        self.assertEquals([ar, id, ho, po, pm], t._repair(id, pm, po, ho, ar))
        # Duplicates
        self.assertRaises(omero.ClientError, t._repair, id, id, no, no, no)
        self.assertRaises(omero.ClientError, t._repair, ho, ho, no, no, no)

if __name__ == '__main__':
    unittest.main()
