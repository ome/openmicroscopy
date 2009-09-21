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
        self.log = logging.getLogger("event")
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
    Event with an info to pass to the d
    """
    def __init__(self, waitMillis, info):
        AbstractEvent.__init__(self, waitMillis)
        self.info = info

    def doRun(self):
        """
        Runs run on the delegate
        """
        self.client.fsEventHappend([self.info])


class Driver(threading.Thread):
    """
    Class which generates fsEvents at a pre-defined time.
    """
    def __init__(self, client):
        assert client.fsEventHappend
        threading.Thread.__init__(self)
        self.log = logging.getLogger("driver")
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
        self.log.debug("Running %s events" % len(self.events))
        for event in self.events:
            try:
                event.setClient(self.client)
                event.run()
            except exceptions.Exception, e:
                self.errors.append((event, e))
                self.log.exception("Error in Driver.run()")

class Simulator(monitors.MonitorClient):
    """
    Adapter object which takes mocked Events from
    a Driver (for example, can be any event source)
    and creates files to simulate that those events
    really happened.
    """
    def __init__(self, dir):
        self.dir = path(dir)

    def isrelto(self, p):
        """
        Checks if this path is relative to (i.e. within)
        the given directory.
        """
        p = path(p)
        rel = p.relpathto(self.dir)
        dots = rel.split(os.sep)
        for dot in dots:
            if os.pardir != dot:
                return False
        return True

    def fsEventHappend(self, eventList, current = None):
        # enum EventType { Create, Modify, Delete, MoveIn, MoveOut, All, System };
        for event in eventList:
            id = event.fileId
            file = path(id)
            if not self.isrelto(file):
                raise exceptions.Exception("%s is not in %s" % (id, self.dir))
            if monitors.EventType.Create == event.type:
                if file.exists():
                    raise exceptions.Exception("%s already exists" % file)
                file.write_lines(["Created by event: %s" % event])
            elif monitors.EventType.Modify == event.type:
                if not file.exists():
                    raise exceptions.Exception("%s doesn't exist" % file)
                file.write_lines(["Modified by event: %s" % event])
            elif monitors.EventType.Delete == event.type:
                if not file.exists():
                    raise exceptions.Exception("%s doesn't exist" % file)
                file.remove()
            elif monitors.EventType.MoveIn == event.type:
                if file.exists():
                    raise exceptions.Exception("%s already exists" % file)
                file.write_lines(["Moved in by event: %s" % event])
            elif monitors.EventType.MoveOut == event.type:
                if not file.exists():
                    raise exceptions.Exception("%s doesn't exist" % file)
                file.remove()
            elif monitors.EventType.System == event.type:
                pass # file id here is simply an informational string
            else:
                self.fail("UNKNOWN EVENT TYPE: %s" % event.eventType)


class MockMonitor(object):
    def __init__(self):
        self.events = []
    def fsEventHappend(self, eventList):
        self.events.extend(eventList)

class TestDrivers(unittest.TestCase):
    """
    Simple test to test the testing functionality (driver)
    """

    def setUp(self):
        self.monitor = MockMonitor()
        self.driver = Driver(self.monitor)

    def assertEventCount(self, count):
        self.assertEquals(count, len(self.monitor.events))

    def testCallback(self):
        l = []
        self.driver.add(CallbackEvent(1, lambda: l.append(True)))
        self.driver.run()
        self.assertEquals(1, len(l));
        self.assertEventCount(0) # Events don't get passed on callbacks

    def testInfo(self):
        self.driver.add(InfoEvent(1, monitors.EventInfo()))
        self.driver.run()
        self.assertEventCount(1)

class TestSimulator(unittest.TestCase):
    """
    Simple test to test the testing functionality (simulator)
    """

    def beforeMethod(self):
        self.uuid = str(uuid4())
        self.dir = path(tempfile.gettempdir()) / "test-omero" / self.uuid
        self.dir.makedirs()
        self.sim = Simulator(self.dir)
        self.driver = Driver(self.sim)

    def tearDown(self):
        self.assertEquals(0, len(self.driver.errors))

    def assertErrors(self, count = 1):
        self.assertEquals(count, len(self.driver.errors))
        for i in range(count):
            self.driver.errors.pop()

    def testRelativeTest(self):
        self.beforeMethod()
        self.assertTrue(self.sim.isrelto(self.dir / "foo"))
        self.assertTrue(self.sim.isrelto(self.dir / "foo" / "bar" / "baz"))
        # Not relative
        self.assertFalse(self.sim.isrelto(path("/")))
        self.assertFalse(self.sim.isrelto(path("/root")))
        self.assertFalse(self.sim.isrelto(path(".")))

    def testBad(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo("foo", monitors.EventType.Create)))
        self.driver.run()
        self.assertErrors()

    def testSimpleCreate(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.run()

    def testBadCreate(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.run()
        self.assertErrors()

    def testBadModify(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Modify)))
        self.driver.run()
        self.assertErrors()

    def testSimpleModify(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Modify)))
        self.driver.run()

    def testBadDelete(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Delete)))
        self.driver.run()
        self.assertErrors()

    def testSimpleDelete(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Delete)))
        self.driver.run()

    def testSimpleDeleteWithModify(self):
        self.beforeMethod()
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Create)))
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Modify)))
        self.driver.add(InfoEvent(1, monitors.EventInfo(self.dir / "foo", monitors.EventType.Delete)))
        self.driver.run()

if __name__ == "__main__":
    unittest.main()
