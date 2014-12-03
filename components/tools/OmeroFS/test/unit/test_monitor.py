#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Unit tests for the directory timeout logic.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
import logging
import time

import omero
import omero.all
import omero.grid.monitors as monitors

from drivers import DirInfoEvent, MockMonitor, with_driver

logging.basicConfig(level=logging.INFO)


class TestMonitor(object):

    def teardown_method(self, method):
        MockMonitor.static_stop()

    def testBadId(self):
        with pytest.raises(omero.ApiUsageException):
            MockMonitor().fsEventHappened('foo', [])

    def testBadFileId(self):
        # Could cause infinite loop
        with pytest.raises(omero.ApiUsageException):
            MockMonitor().fsEventHappened('', [monitors.EventInfo()])

    def testEmptyAdd(self):
        MockMonitor().fsEventHappened('', [])  # Does nothing.

    @pytest.mark.broken(ticket="12566")
    @with_driver
    def testBasicAdd(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(
            self.dir / "root" / "dirtimeout",
            monitors.EventType.Create)))
        self.driver.run()

    @pytest.mark.broken(ticket="12566")
    @with_driver
    def testWithSingleImport(self):
        f = self.dir / "root" / "file"
        self.client.files = {str(f): [str(f)]}
        self.driver.add(DirInfoEvent(0, monitors.EventInfo(
            self.dir / "root" / "file", monitors.EventType.Create)))
        self.driver.run()
        time.sleep(0.25)

    @pytest.mark.broken(ticket="12566")
    @with_driver
    def testWithMultiImport(self):
        f1 = str(self.dir / "root" / "file1")
        f2 = str(self.dir / "root" / "file2")
        f3 = str(self.dir / "root" / "file3")
        f4 = str(self.dir / "root" / "file4")
        self.client.files = {f1: [f1, f2, f3, f4]}
        self.client.setDirImportWait(1)
        self.driver.add(DirInfoEvent(0.0, monitors.EventInfo(
            f1, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(100, monitors.EventInfo(
            f2, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(200, monitors.EventInfo(
            f3, monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(300, monitors.EventInfo(
            f4, monitors.EventType.Create)))
        time.sleep(1)
        self.driver.run()

    @pytest.mark.broken(ticket="12566")
    @with_driver
    def testDirectoryInDirectory(self):
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(
            self.dir / "root" / "dir", monitors.EventType.Create)))
        self.driver.add(DirInfoEvent(1, monitors.EventInfo(
            self.dir / "root" / "dir" / "dir", monitors.EventType.Create)))
        self.driver.run()
