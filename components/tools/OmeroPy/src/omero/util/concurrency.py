#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Concurrency Utilities
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import sys
import time
import atexit
import logging
import threading
import omero.util
import logging.handlers


def get_event(name = "Unknown"):
    """
    Returns a threading.Event instance which is registered to be
    "set" (Event.set()) on system exit.
    """
    event = AtExitEvent(name=name)
    atexit.register(event.setAtExit)
    return event


class AtExitEvent(threading._Event):
    """
    threading.Event extension which provides an additional method
    setAtExit() which sets "atexit" to true.

    This class was introduced in 4.2.1 to work around issue #3260
    in which logging from background threads produced error
    messages.
    """

    def __init__(self, verbose = None, name = "Unknown"):
        super(AtExitEvent, self).__init__(verbose)
        self.__name = name
        self.__atexit = False

    name = property(lambda self: self.__name)
    atexit = property(lambda self: self.__atexit)

    def setAtExit(self):
        self.__atexit = True
        super(AtExitEvent, self).set()

    def __repr__(self):
        return "%s (%s)" % (super(AtExitEvent, self).__repr__(), self.__name)


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

    def __init__(self, interval, function, args=None, kwargs=None):
        if args is None:
            args = []
        if kwargs is None:
            kwargs = {}
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
