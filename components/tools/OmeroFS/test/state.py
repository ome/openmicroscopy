#!/usr/bin/env python

"""
    Tests the state which is held by MonitorClients

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions
import logging
import monitors
import os
import tempfile
import threading
import time
import unittest

LOGFORMAT =  """%(asctime)s %(levelname)-5s [%(name)40s] (%(threadName)-10s) %(message)s"""
logging.basicConfig(level=0,format=LOGFORMAT)

from uuid import uuid4
from path import path
from omero_ext.functional import wraps
from fsDropBoxMonitorClient import *

def nullcb(*args):
    pass

def listcb(l):
    l.append(l)

class TestState(unittest.TestCase):

    def setUp(self):
        self.s = MonitorState()

    def tearDown(self):
        self.s.stop()

    def testEmpty(self):
        self.s.update({}, 0, nullcb, [])

    def testSimple(self):
        self.s.update({'key':['file1','file2']}, 0, nullcb, [])

    def testTimerCalled(self):
        l = []
        self.s.update({'key':['file1','file2']}, 0, listcb, [l])
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testMultipleInsert(self):
        l = []
        m = {
            'key':['file1','file2'],
            'key1':['file1','file2'],
            'key2':['file1','file2']
            }
        self.s.update({'key':['file1','file2']}, 0, listcb, [l])
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testAddThenEmpty(self):
        l = []
        self.s.update({'key':['file1','file2']}, 0.1, listcb, [l])
        self.s.update({'key':[               ]}, 0.0, listcb, [l])
        time.sleep(0.25)
        self.assertEquals(0, len(l))

    def testAddThenModify(self):
        l = []
        self.s.update({'key':['file1','file2']}, 0.1, listcb, [l])
        self.s.update({'key':[    'file3'    ]}, 0.0, listcb, [l])
        time.sleep(0.25)
        self.assertEquals(1, len(l))

if __name__ == "__main__":
    unittest.main()
