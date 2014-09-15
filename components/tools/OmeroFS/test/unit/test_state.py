#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
    Tests the state which is held by MonitorClients

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import logging
import os
import time
import unittest

LOGFORMAT =  """%(asctime)s %(levelname)-5s [%(name)40s] (%(threadName)-10s) %(message)s"""
logging.basicConfig(level=0,format=LOGFORMAT)

from path import path
from omero.util import make_logname
from omero_ext.functional import wraps

import fsDropBoxMonitorClient as fsDBMC

def nullcb(*args):
    pass

def listcb(l):
    def cb(*args):
        l.append(args)
    return cb

def clearcb(log, state, key):
    def cb(*args):
        assert key == args[0]
        log.info("clearcb called: %s", args[0])
        state.clear(args[0])
    return cb

class TestState(unittest.TestCase):

    def setUp(self):
        self.s = fsDBMC.MonitorState()
        self.log = logging.getLogger(make_logname(self))

    def tearDown(self):
        self.s.stop()

    def testEmpty(self):
        self.s.update({}, 0, nullcb)

    def testSimple(self):
        self.s.update({'file1':['file1','file2']}, 0, nullcb)

    def testTimerCalled(self):
        l = []
        self.s.update({'file1':['file1','file2']}, 0, listcb(l))
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testMultipleInsert(self):
        l = []
        m = {
            'file1':['file1','file2'],
            'file2':['file1','file2'],
            'file3':['file1','file2','file3']
            }
        self.s.update({'file1':['file1','file2']}, 0, listcb(l))
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testAddThenReAdd(self):
        l = []
        self.s.update({'file1':['file1','file2']}, 0.1, listcb(l))
        self.s.update({'file1':['file1','file2']}, 0.1, listcb(l))
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testAddThenModify(self):
        l = []
        self.s.update({'file1':['file1','file2']}, 0.1, listcb(l))
        self.s.update({'file1':['file1','file3']}, 0.0, listcb(l))
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testEntryMoved(self):
        l = []
        self.s.update({'file1':['file1'        ]}, 0.1, listcb(l))
        self.assertEquals(1, self.s.keys())
        self.s.update({'file2':['file1','file2']}, 0.1, listcb(l))
        self.assertEquals(2, self.s.keys())
        time.sleep(0.25)
        self.assertEquals(1, len(l))

    def testEntryMoved(self):
        self.s.update({'file1':['file1'        ]}, 0.1, clearcb(self.log, self.s, 'file1'))
        self.assertEquals(1, len(self.s.keys()))
        self.assertEquals(1, self.s.count())
        self.s.update({'file2':['file1','file2']}, 0.1, clearcb(self.log, self.s, 'file2'))
        self.assertEquals(2, len(self.s.keys()))
        self.assertEquals(1, self.s.count())
        time.sleep(0.25)
        self.assertEquals(0, len(self.s.keys()))
        self.assertEquals(0, self.s.count())

    def testEntryOutOfSyncSubsume(self):
        self.s.update({'file1':['file1'        ]}, 0.1, nullcb)
        self.assertEquals(1, len(self.s.keys()))
        self.s.update({'file2':['file2'        ]}, 0.1, nullcb)
        self.assertEquals(2, len(self.s.keys()))
        self.s.update({'file2':['file1','file2']}, 0.1, nullcb)
        self.assertEquals(2, len(self.s.keys()))

    def testEntryOutOfSyncSteal(self):
        self.s.update({'file1':['file1','file3']}, 0.1, nullcb)
        self.assertEquals(2, len(self.s.keys()))
        self.s.update({'file2':['file2'        ]}, 0.1, nullcb)
        self.assertEquals(3, len(self.s.keys()))
        self.s.update({'file2':['file2','file3']}, 0.1, nullcb)
        self.assertEquals(3, len(self.s.keys()))

if __name__ == "__main__":
    unittest.main()
