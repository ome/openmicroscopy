#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Utility classes for generating file-system-like events
   for testing.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import logging
import threading
import time
import uuid

import omero.all
import omero.grid.monitors as monitors

from path import path
from omero.util import ServerContext
from omero_ext.mox import Mox
from omero_ext.functional import wraps
from omero.util.temp_files import create_path
from fsDropBoxMonitorClient import MonitorClientI

LOGFORMAT = "%(asctime)s %(levelname)-5s [%(name)40s]" \
            "(%(threadName)-10s) %(message)s"
logging.basicConfig(level=0, format=LOGFORMAT)


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
            self.log.error("Reusing event: old=%s new=%s"
                           % (self.client, client))
        self.client = client

    def run(self):
        """
        By default, nothing.
        """
        self.log.info("Sleeping %s" % self.waitMillis)
        time.sleep(self.waitMillis / 1000)
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
        self.client.fsEventHappened("", [self.info], None)  # Ice.Current


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
            except Exception, e:
                self.errors.append((event, e))
                self.log.exception("Error in Driver.run()")


def with_driver(func, errors=0):
    """ Decorator for running a test with a Driver """
    def handler(*args, **kwargs):
        self = args[0]
        self.dir = create_path(folder=True) / "DropBox"
        self.simulator = Simulator(self.dir)
        self.client = MockMonitor(self.dir, pre=[self.simulator], post=[])
        try:
            self.driver = Driver(self.client)
            rv = func(*args, **kwargs)
            assert errors == len(self.driver.errors)
            for i in range(errors):
                self.driver.errors.pop()
            return rv
        finally:
            self.client.stop()
    return wraps(func)(handler)


class Replay(object):

    """
    Utility to read EVENT_RECORD logs and make the proper
    calls on the given target.
    """

    def __init__(self, dir, source, target):
        """
        Uses the dir as the location where files should *appear*
        to be created, regardless of what the EVENT_RECORD suggests.
        """
        self.log = logging.getLogger("Replay")
        self.dir_out = dir
        self.dir_in = None
        self.batch = None
        self.bsize = None
        self.timestamp = None
        self.filesets = None
        self.source = source
        self.target = target

    def run(self):
        for line in self.source.lines():
            if 0 <= line.find("EVENT_RECORD"):
                parts = line.split("::")
                timestamp = float(parts[2])
                category = parts[3]
                data = parts[4].strip()
                if category == "Directory":
                    self.directory(timestamp, data)
                elif category == "Batch":
                    self.batchStart(timestamp, data)
                elif category == "Filesets":
                    self.fileset(timestamp, data)
                elif category == "Create":
                    self.event(timestamp, data, monitors.EventType.Create)
                elif category == "Modify":
                    self.event(timestamp, data, monitors.EventType.Modify)

    def directory(self, timestamp, data):
        self.dir_in = data
        self.timestamp = float(timestamp)
        self.log.info("Replaying from %s at %s", self.dir_in, self.timestamp)

    def batchStart(self, timestamp, data):
        if self.batch:
            # Double due to callbacks
            assert len(self.batch) == (self.bsize * 2)
            self.process()
        self.batch = []
        self.bsize = int(data)

    def fileset(self, timestamp, data):
        filesets = eval(data, {"__builtins__": None}, {})
        self.filesets = dict()
        for k, iv in filesets.items():
            k = self.rewrite(k)
            ov = []
            for i in iv:
                ov.append(self.rewrite(i))
            self.filesets[k] = ov
        return self.filesets

    def event(self, timestamp, data, type):
        data = self.rewrite(data)

        def cb(client):
            client.files = dict(self.filesets)
        self.batch.append(CallbackEvent(0, cb))

        offset = timestamp - self.timestamp
        self.timestamp = timestamp
        info = monitors.EventInfo(data, type)
        event = InfoEvent(offset, info)
        self.batch.append(event)
        return event

    def process(self):
        if self.target:
            for event in self.batch:
                self.target.add(event)

    def rewrite(self, data):
        if not data.startswith(self.dir_in):
            raise Exception("%s doesn't start with %s" % (data, self.dir_in))
        data = data[len(self.dir_in):]
        data = self.dir_out + data
        return data


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

    def fsEventHappened(self, monitorid, eventList, current=None):
        # enum EventType { Create, Modify, Delete, MoveIn, MoveOut, All, System
        # };
        for event in eventList:
            fileid = event.fileId
            file = path(fileid)
            if not file.parpath(self.dir):
                raise Exception("%s is not in %s" % (file, self.dir))
            if monitors.EventType.Create == event.type:
                if file.exists():
                    raise Exception("%s already exists" % file)
                if hasattr(event, "dir"):
                    self.log.info("Creating dir: %s", file)
                    file.makedirs()
                else:
                    #
                    # For the moment, we assume directory events are being
                    # filtered and therefore we will do the creation anyway.
                    #
                    if not file.parent.exists():
                        file.parent.makedirs()

                    self.log.info("Creating file: %s", file)
                    file.write_lines(["Created by event: %s" % event])
            elif monitors.EventType.Modify == event.type:
                if not file.exists():
                    raise Exception("%s doesn't exist" % file)
                if hasattr(event, "dir"):
                    if not file.isdir():
                        raise Exception("%s is not a directory" % file)
                    self.log.info("Creating file in dir %s", file)
                    new_file = file / str(uuid.uuid4())
                    new_file.write_lines(
                        ["Writing new file to modify this"
                         "directory on event: %s" % event])
                else:
                    self.log.info("Modifying file %s", file)
                    file.write_lines(["Modified by event: %s" % event])
            elif monitors.EventType.Delete == event.type:
                if not file.exists():
                    raise Exception("%s doesn't exist" % file)
                if hasattr(event, "dir"):
                    if not file.isdir():
                        raise Exception("%s is not a directory" % file)
                    self.log.info("Deleting dir %s", file)
                    file.rmtree()
                else:
                    self.log.info("Deleting file %s", file)
                    file.remove()
            elif monitors.EventType.MoveIn == event.type:
                raise Exception("TO BE REMOVED")
            elif monitors.EventType.MoveOut == event.type:
                raise Exception("TO BE REMOVED")
            elif monitors.EventType.System == event.type:
                pass  # file id here is simply an informational string
            else:
                self.fail("UNKNOWN EVENT TYPE: %s" % event.eventType)


class mock_communicator(object):

    def findObjectFactory(self, *args):
        return None

    def addObjectFactory(self, *args):
        pass


class MockServerContext(ServerContext):

    def __init__(self, ic, get_root):
        self.mox = Mox()
        self.communicator = ic
        self.getSession = get_root
        self.stop_event = threading.Event()

    def newSession(self, *args):
        sess = self.mox.CreateMock(omero.api.ServiceFactoryPrx.__class__)
        return sess


class MockMonitor(MonitorClientI):

    """
    Mock Monitor Client which can also delegate to other clients.
    """
    INSTANCES = []

    def static_stop():
        for i in MockMonitor.INSTANCES:
            i.stop()
    static_stop = staticmethod(static_stop)

    def __init__(self, dir=None, pre=None, post=None):
        if pre is None:
            pre = []
        if post is None:
            post = []
        self.root = None
        ic = mock_communicator()
        MonitorClientI.__init__(
            self, dir, ic, getUsedFiles=self.used_files,
            ctx=MockServerContext(ic, self.get_root), worker_wait=0.1)
        self.log = logging.getLogger("MockMonitor")
        self.events = []
        self.files = {}
        self.pre = list(pre)
        self.post = list(post)
        MockMonitor.INSTANCES.append(self)

    def fake_meth(self, name, rv, *args, **kwargs):
        self.log.info("%s(%s, %s)=>%s", name, args, kwargs, rv)
        if isinstance(rv, Exception):
            raise rv
        else:
            return rv

    def used_files(self, *args, **kwargs):
        return self.fake_meth("getUsedFiles", self.files, *args, **kwargs)

    def get_root(self, *args, **kwargs):
        return self.fake_meth("getRoot", self.root, *args, **kwargs)

    def fsEventHappened(self, monitorid, eventList, current=None):
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


def with_errors(func, count=1):
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
