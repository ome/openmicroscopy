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

from uuid import uuid4
from path import path
from omero_ext.functional import wraps
from fsDropBoxMonitorClient import *

LOGFORMAT =  """%(asctime)s %(levelname)-5s [%(name)40s] (%(threadName)-10s) %(message)s"""
logging.basicConfig(level=0,format=LOGFORMAT)

class TestState(unittest.TestCase):

    def testEmpty(self):
        state = MonitorState()
        state.update({})

if __name__ == "__main__":
    unittest.main()
