#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
    Tests the state which is held by MonitorClients

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
import logging
import time

LOGFORMAT = "%(asctime)s %(levelname)-5s [%(name)40s] " \
            "(%(threadName)-10s) %(message)s"
logging.basicConfig(level=0, format=LOGFORMAT)

from omero.util import make_logname

import fsDropBoxMonitorClient as fsDBMC


def nullcb(*args):
    pass


def listcb(l):
    def cb(*args):
        l.append(args)
    return cb


def clearcb(log, state, key):
    def cb(*args):
        assert key == args[0]
        log.info("clearcb called: %s", args[0])
        state.clear(args[0])
    return cb


class TestState(object):

    def setup_method(self, method):
        self.s = fsDBMC.MonitorState()
        self.log = logging.getLogger(make_logname(self))

    def teardown_method(self, method):
        self.s.stop()

    @pytest.mark.broken(ticket="12566")
    def testEmpty(self):
        self.s.update({}, 0, nullcb)

    @pytest.mark.broken(ticket="12566")
    def testSimple(self):
        self.s.update({'file1': ['file1', 'file2']}, 0, nullcb)

    @pytest.mark.broken(ticket="12566")
    def testTimerCalled(self):
        l = []
        self.s.update({'file1': ['file1', 'file2']}, 0, listcb(l))
        time.sleep(0.25)
        assert 1 == len(l)

    @pytest.mark.broken(ticket="12566")
    def testMultipleInsert(self):
        l = []
        m = {
            'file1': ['file1', 'file2'],
            'file2': ['file1', 'file2'],
            'file3': ['file1', 'file2', 'file3']
        }
        self.s.update(m, 0, listcb(l))
        time.sleep(0.25)
        assert 1 == len(l)

    @pytest.mark.broken(ticket="12566")
    def testAddThenReAdd(self):
        l = []
        self.s.update({'file1': ['file1', 'file2']}, 0.1, listcb(l))
        self.s.update({'file1': ['file1', 'file2']}, 0.1, listcb(l))
        time.sleep(0.25)
        assert 1 == len(l)

    @pytest.mark.broken(ticket="12566")
    def testAddThenModify(self):
        l = []
        self.s.update({'file1': ['file1', 'file2']}, 0.1, listcb(l))
        self.s.update({'file1': ['file1', 'file3']}, 0.0, listcb(l))
        time.sleep(0.25)
        assert 1 == len(l)

    @pytest.mark.broken(ticket="12566")
    def testEntryMoved1(self):
        l = []
        self.s.update({'file1': ['file1']}, 0.1, listcb(l))
        assert 1 == self.s.keys()
        self.s.update({'file2': ['file1', 'file2']}, 0.1, listcb(l))
        assert 2 == self.s.keys()
        time.sleep(0.25)
        assert 1 == len(l)

    @pytest.mark.broken(ticket="12566")
    def testEntryMoved2(self):
        self.s.update(
            {'file1': ['file1']}, 0.1, clearcb(self.log, self.s, 'file1'))
        assert 1 == len(self.s.keys())
        assert 1 == self.s.count()
        self.s.update(
            {'file2': ['file1', 'file2']}, 0.1,
            clearcb(self.log, self.s, 'file2'))
        assert 2 == len(self.s.keys())
        assert 1 == self.s.count()
        time.sleep(0.25)
        assert 0 == len(self.s.keys())
        assert 0 == self.s.count()

    @pytest.mark.broken(ticket="12566")
    def testEntryOutOfSyncSubsume(self):
        self.s.update({'file1': ['file1']}, 0.1, nullcb)
        assert 1 == len(self.s.keys())
        self.s.update({'file2': ['file2']}, 0.1, nullcb)
        assert 2 == len(self.s.keys())
        self.s.update({'file2': ['file1', 'file2']}, 0.1, nullcb)
        assert 2 == len(self.s.keys())

    @pytest.mark.broken(ticket="12566")
    def testEntryOutOfSyncSteal(self):
        self.s.update({'file1': ['file1', 'file3']}, 0.1, nullcb)
        assert 2 == len(self.s.keys())
        self.s.update({'file2': ['file2']}, 0.1, nullcb)
        assert 3 == len(self.s.keys())
        self.s.update({'file2': ['file2', 'file3']}, 0.1, nullcb)
        assert 3 == len(self.s.keys())
