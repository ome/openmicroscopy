#!/usr/bin/env python

"""
   Unit tests for the directory timeout logic.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, logging, threading, time

from monitors import *
from fsDropBoxMonitorClient import *
from test.drivers import *
from path import path
from uuid import uuid4

logging.basicConfig(level=0)

def with_errors(func, count = 1):
    """ Decorator for catching any ERROR logging messages """
    def exc_handler(*args, **kwargs):
        handler = DetectError()
        logging.root.addHandler(handler)
        try:
            rv = func(*args, **kwargs)
            return rv
        finally:
            logging.root.removeHandler(handler)
    exc_handler = wraps(func)(exc_handler)
    return exc_handler

class DetectError(logging.Handler):
    def __init__(self):
        logging.Handler.__init__(self)
        self.errors = []
    def handle(self, record):
        self.errors.append(record)

class OurClient(MonitorClientI):
        def __init__(self):
            MonitorClientI.__init__(self)
            self.imports = []
        def importFile(*args):
            self.imports.append(args)

class TestDirTimeout(unittest.TestCase):

    def testBadId(self):
        OurClient().fsEventHappened('foo',[])
    testBadId = with_errors(testBadId, 1)

    def testBadFileId(self):
        # Could cause infinite loop
        OurClient().fsEventHappened('',[EventInfo()])

    def testEmptyAdd(self):
        OurClient().fsEventHappened('',[]) # Does nothing.

    def testBasicAdd(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(self.dir / "dirtimeout", monitors.EventType.Create)))
        self.driver.run()
    testBasicAdd = with_driver(testBasicAdd, OurClient())

if __name__ == '__main__':
    unittest.main()
