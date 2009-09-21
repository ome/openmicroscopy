#!/usr/bin/env python

"""
   Unit tests for the directory timeout logic.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, logging, threading, time
logging.basicConfig(level=0)

import omero
import omero_ServerErrors_ice

from monitors import *
from fsDropBoxMonitorClient import *
from test.drivers import *

from path import path
from uuid import uuid4


class TestDirTimeout(unittest.TestCase):

    def testBadId(self):
        self.assertRaises(omero.ApiUsageException, MockMonitor().fsEventHappened, 'foo', [])

    def testBadFileId(self):
        # Could cause infinite loop
        self.assertRaises(omero.ApiUsageException, MockMonitor().fsEventHappened, '',[EventInfo()])

    def testEmptyAdd(self):
        MockMonitor().fsEventHappened('',[]) # Does nothing.

    def testBasicAdd(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(self.dir / "DropBox" / "r"/ "root" / "dirtimeout", monitors.EventType.Create)))
        self.driver.run()
    testBasicAdd = with_driver(testBasicAdd)

if __name__ == '__main__':
    unittest.main()
