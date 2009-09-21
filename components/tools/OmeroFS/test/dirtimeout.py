#!/usr/bin/env python

"""
   Unit tests for the directory timeout logic.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, logging, threading

from monitors import *
from fsDropBoxMonitorClient import *

class DetectError(logging.Handler):
    def __init__(self):
        logging.Handler.__init__(self)
    def handle(self, record):
       print record
       print dir(record)

class OurClient(MonitorClientI):
        def __init__(self):
            MonitorClientI.__init__(self)
            self.imports = []
        def importFile(*args):
            self.imports.append(args)

class TestDirTimeout(unittest.TestCase):

    def testBadId(self):
        m = OurClient()
        logging.root.addHandler(DetectError())
        m.fsEventHappened('foo',[])

    def testBadFileId(self):
        # Could cause infinite loop
        m = OurClient()
        m.fsEventHappened('',[EventInfo()])

    def testEmptyAdd(self):
        m = OurClient()
        m.fsEventHappened('',[])

    def testBasicAdd(self):
        m = OurClient()
        m.fsEventHappened('',[EventInfo('myfile', None)])

if __name__ == '__main__':
    unittest.main()
