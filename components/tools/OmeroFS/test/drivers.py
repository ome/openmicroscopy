#!/usr/bin/env python

"""
   Utility classes for generating file-system-like events
   for testing.

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

class AbstractEvent(object):
    """
    Event which is configured in a Driver instance
    to be executed at a specified time.
    """
    def __init__(self, waitMillis):
        """
        wait is the time that should elapse after the
        previous event (or on startup) and this event.

        in millis.
        """
        self.log = logging.getLogger("Event")
        self.client = None
        self.waitMillis = waitMillis

    def setClient(self, client):
        """
        Sets the client which will receive the event on run.
        """
        if self.client:
            log.error("Reusing event: old=%s new=%s" % (self.client, client))
        self.client = client

    def run(self):
        """
        By default, nothing.
        """
        self.log.info("Sleeping %s" % self.waitMillis)
        time.sleep(self.waitMillis/1000)
        if not self.client:
            self.log.error("No client")
        self.doRun()


class CallbackEvent(AbstractEvent):
    """
    Not really an Event, but allows some action
    to be executed in the driver thread.
    """
    def __init__(self, waitMillis, delegate):
        AbstractEvent.__init__(self, waitMillis)
        self.delegate = delegate

    def doRun(self):
        """
        Calls the delegate.
        """
        m = self.delegate
        m()


class InfoEvent(AbstractEvent):
    """
    Event with an info to pass to the client
    """
    def __init__(self, waitMillis, info):
        AbstractEvent.__init__(self, waitMillis)
        self.info = info

    def doRun(self):
        """
        Runs run on the delegate
        """
        self.client.fsEventHappened("", [self.info], None) # Ice.Current


class DirInfoEvent(InfoEvent):
    """
    Adds a test-specific "dir" attribute to EventInfo
    instance. Used by the Simulator and perhaps other
    test monitor clients
    """
    def __init__(self, waitMillis, info):
        InfoEvent.__init__(self, waitMillis, info)
        self.info.dir = True


class Driver(threading.Thread):
    """
    Class which generates fsEvents at a pre-defined time.
    """
    def __init__(self, client):
        assert client.fsEventHappened
        threading.Thread.__init__(self)
        self.log = logging.getLogger("Driver")
        self.client = client
        self.events = []
        self.errors = []

    def add(self, event):
        # Force attribute error
        assert event.setClient
        assert event.run
        self.events.append(event)
        self.log.debug("Added: %s" % event)

    def run(self):
        self.log.debug("Running %s event(s)" % len(self.events))
        for event in self.events:
            try:
                event.setClient(self.client)
                event.run()
            except exceptions.Exception, e:
                self.errors.append((event, e))
                self.log.exception("Error in Driver.run()")


def with_driver(func, errors = 0):
    """ Decorator for running a test with a Driver """
    def handler(*args, **kwargs):
        self = args[0]
        self.dir = path(tempfile.gettempdir()) / "test-omero" / str(uuid4()) / "DropBox"
        self.simulator = Simulator(self.dir)
        self.client = MockMonitor(pre=[self.simulator], post=[])
        self.client.setDropBoxDir(self.dir)
        self.driver = Driver(self.client)
        rv = func(*args, **kwargs)
        self.assertEquals(errors, len(self.driver.errors))
        for i in range(errors):
            self.driver.errors.pop()
        return rv
    return wraps(func)(handler)


class Simulator(monitors.MonitorClient):
    """
    Adapter object which takes mocked Events from
    a Driver (for example, can be any event source)
    and creates files to simulate that those events
    really happened.
    """
    def __init__(self, dir):
        self.dir = path(dir)
        self.log = logging.getLogger("Simulator")

    def fsEventHappened(self, monitorid, eventList, current = None):
        # enum EventType { Create, Modify, Delete, MoveIn, MoveOut, All, System };
        for event in eventList:
            fileid = event.fileId
            file = path(fileid)
            if not file.parpath(self.dir):
                raise exceptions.Exception("%s is not in %s" % (file, self.dir))
            if monitors.EventType.Create == event.type:
                if file.exists():
                    raise exceptions.Exception("%s already exists" % file)
                if hasattr(event, "dir"):
                    self.log.info("Creating dir: %s", file)
                    file.makedirs()
                else:
                    self.log.info("Creating file: %s", file)
                    file.write_lines(["Created by event: %s" % event])
            elif monitors.EventType.Modify == event.type:
                if not file.exists():
                    raise exceptions.Exception("%s doesn't exist" % file)
                if hasattr(event, "dir"):
                    if not file.isdir():
                        raise exceptions.Exception("%s is not a directory" % file)
                    self.log.info("Creating file in dir %s", file)
                    new_file = file / str(uuid4())
                    new_file.write_lines(["Writing new file to modify this directory on event: %s" % event])
                else:
                    self.log.info("Modifying file %s", file)
                    file.write_lines(["Modified by event: %s" % event])
            elif monitors.EventType.Delete == event.type:
                if not file.exists():
                    raise exceptions.Exception("%s doesn't exist" % file)
                if hasattr(event, "dir"):
                    if not file.isdir():
                        raise exceptions.Exception("%s is not a directory" % file)
                    self.log.info("Deleting dir %s", file)
                    file.rmtree()
                else:
                    self.log.info("Deleting file %s", file)
                    file.remove()
            elif monitors.EventType.MoveIn == event.type:
                raise exceptions.Exception("TO BE REMOVED")
            elif monitors.EventType.MoveOut == event.type:
                raise exceptions.Exception("TO BE REMOVED")
            elif monitors.EventType.System == event.type:
                pass # file id here is simply an informational string
            else:
                self.fail("UNKNOWN EVENT TYPE: %s" % event.eventType)

class MockMonitor(MonitorClientI):
    """
    Mock Monitor Client which can also delegate to other clients.
    """
    def __init__(self, pre = [], post = []):
        self.root = None
        MonitorClientI.__init__(self, getUsedFiles = self.used_files, getRoot = self.get_root)
        self.log = logging.getLogger("MockMonitor")
        self.events = []
        self.files = {}
        self.pre = list(pre)
        self.post = list(post)

    def fake_meth(self, name, rv, *args, **kwargs):
        self.log.info("%s(%s, %s)=>%s", name, args, kwargs, rv)
        if isinstance(rv, exceptions.Exception):
            raise rv
        else:
            return rv

    def used_files(self, *args, **kwargs):
        return self.fake_meth("getUsedFiles", self.files, *args, **kwargs)

    def get_root(self, *args, **kwargs):
        return self.fake_meth("getRoot", self.root, *args, **kwargs)

    def fsEventHappened(self, monitorid, eventList, current = None):
        """
        Dispatches the event first to pre, then to the true implementation
        and finally to the post monitor clients. This allows for Simulator
        or similar to set the event up properly.
        """
        self.events.extend(eventList)
        for client in self.pre:
            client.fsEventHappened(monitorid, eventList, current)
        MonitorClientI.fsEventHappened(self, monitorid, eventList, current)
        for client in self.post:
            client.fsEventHappened(monitorid, eventList, current)

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
