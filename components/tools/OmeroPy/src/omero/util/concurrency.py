#!/usr/bin/env python
#
# OMERO Concurrency Utilities
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import sys
import time
import uuid
import atexit
import logging
import threading
import omero.util
import exceptions
import logging.handlers

def get_event():
    """
    Returns a threading.Event instance which is registered to be
    "set" (Event.set()) on system exit.
    """
    event = threading.Event()
    atexit.register(event.set)
    return event

class Timer(threading._Timer):
    """Based on threading._Thread but allows for resetting the Timer.

    t = Timer(30.0, f, args=[], kwargs={})
    t.start()
    t.cancel() # stop the timer's action if it's still waiting

    # or

    t.reset()

    After excecution, the status of the run can be checked via the
    "completed" and the "exception" Event instances.
    """

    def __init__(self, interval, function, args=[], kwargs={}):
        threading._Timer.__init__(self, interval, function, args, kwargs)
        self.log = logging.getLogger(omero.util.make_logname(self))
        self.completed = threading.Event()
        self.exception = threading.Event()
        self._reset = threading.Event()

    def reset(self):
        self.log.debug("Reset called")
        self._reset.set() # Set first, so that the loop will continue
        self.finished.set() # Forces waiting thread to fall through

    def run(self):
        while True:
            self.finished.wait(self.interval)
            if self._reset.isSet():
                self.finished.clear()
                self._reset.clear()
                self.log.debug("Resetting")
                continue
            if not self.finished.isSet():
                try:
                    self.log.debug("Executing")
                    self.function(*self.args, **self.kwargs)
                    self.completed.set()
                    self.finished.set()
                except:
                    self.exception.set()
                    self.finished.set()
                    raise
            break
