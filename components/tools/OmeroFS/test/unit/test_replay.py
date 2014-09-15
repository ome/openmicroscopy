#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Replays all the log records under records/ to see if they cause exceptions

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, logging, threading, time
logging.basicConfig(level=logging.WARN)

import omero

import omero.all
import omero.grid.monitors as monitors
from fsDropBoxMonitorClient import *
from drivers import *

from path import path

class TestReplay(unittest.TestCase):

    def tearDown(self):
        MockMonitor.static_stop()

    @with_driver
    def XestOutOfSyncFind(self):
        """
        This was used to print out a record so that we could detect
        what the groups of used files were.
        """
        source = path(".") / "test" / "records" / "outofsync.txt"
        l = len(self.dir)
        class MyReplay(Replay):
            def fileset(self, timestamp, data):
               f = Replay.fileset(self, timestamp, data)
               print "="*80
               for k,v in f.items():
                    print k[l:]
                    for i in v:
                        print "\t",i[l:]
               return f
        MyReplay(self.dir, source, None).run()
        self.driver.run()

    @with_driver
    def testOutOfSync(self):
        source = path(".") / "test" / "records" / "outofsync.txt"
        Replay(self.dir, source, self.driver).run()
        self.driver.run()

if __name__ == '__main__':
    unittest.main()
