#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Unit tests for the directory timeout logic.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, logging, threading, time
logging.basicConfig(level=logging.INFO)

import omero

import omero.all
import omero.grid.monitors as monitors
from fsDropBoxMonitorClient import *
from drivers import *

from path import path

class TestMonitor(unittest.TestCase):

    def tearDown(self):
        MockMonitor.static_stop()

    def testBadId(self):
        self.assertRaises(omero.ApiUsageException, MockMonitor().fsEventHappened, 'foo', [])

    def testBadFileId(self):
        # Could cause infinite loop
        self.assertRaises(omero.ApiUsageException, MockMonitor().fsEventHappened, '',[monitors.EventInfo()])

    def testEmptyAdd(self):
        MockMonitor().fsEventHappened('',[]) # Does nothing.

    def testBasicAdd(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(self.dir / "r"/ "root" / "dirtimeout", monitors.EventType.Create)))
        self.driver.run()
    testBasicAdd = with_driver(testBasicAdd)

    def testWithSingleImport(self):
        f = self.dir / "r"/ "root" / "file"
        self.client.files = {str(f):[str(f)]}
        self.driver.add(DirInfoEvent(0, monitors.EventInfo(self.dir / "r"/ "root" / "file", monitors.EventType.Create)))
        self.driver.run()
        time.sleep(0.25)
    testWithSingleImport = with_driver(testWithSingleImport)

    def testWithMultiImport(self):
        f1 = str(self.dir / "r"/ "root" / "file1")
        f2 = str(self.dir / "r"/ "root" / "file2")
        f3 = str(self.dir / "r"/ "root" / "file3")
        f4 = str(self.dir / "r"/ "root" / "file4")
        self.client.files = {f1:[f1,f2,f3,f4]}
        self.client.setDirImportWait(1)
        self.driver.add(DirInfoEvent(0.0, monitors.EventInfo(f1, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(100, monitors.EventInfo(f2, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(200, monitors.EventInfo(f3, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(300, monitors.EventInfo(f4, monitors.EventType.Create)))
        time.sleep(1)
        self.driver.run()
    testWithMultiImport = with_driver(testWithMultiImport)

    def testDirectoryInDirectory(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(self.dir / "r"/ "root" / "dir", monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(self.dir / "r"/ "root" / "dir" / "dir", monitors.EventType.Create)))
        self.driver.run()
    testDirectoryInDirectory = with_driver(testDirectoryInDirectory)

if __name__ == '__main__':
    unittest.main()
