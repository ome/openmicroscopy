#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Test of the CmdCallbackI object
"""

import threading

import library as lib
import omero
import omero.all
from omero.util.concurrency import get_event


class CmdCallback(omero.callbacks.CmdCallbackI):

    def __init__(self, client, handle):
        super(CmdCallback, self).__init__(client, handle)
        self.t_lock = threading.RLock()
        self.t_steps = 0
        self.t_finished = 0
        self.t_event = get_event("CmdCallback")

    def step(self, complete, total, current=None):
        self.t_lock.acquire()
        try:
            self.t_steps += 1
        finally:
            self.t_lock.release()

    def onFinished(self, rsp, status, current=None):
        self.t_lock.acquire()
        try:
            self.t_event.set()
            self.t_finished += 1
        finally:
            self.t_lock.release()

    def assertSteps(self, expected):
        self.t_lock.acquire()
        try:
            assert expected == self.t_steps
        finally:
            self.t_lock.release()

    def assertFinished(self, expectedSteps=None):
        self.t_lock.acquire()
        try:
            assert self.t_finished != 0
            assert not self.isCancelled()
            assert not self.isFailure()
            rsp = self.getResponse()
            if not rsp:
                assert False, "null response"

            elif isinstance(rsp, omero.cmd.ERR):
                msg = "%s\ncat:%s\nname:%s\nparams:%s\n" % \
                    (rsp, rsp.category, rsp.name, rsp.parameters)
                assert False, msg
        finally:
            self.t_lock.release()

        if expectedSteps is not None:
            self.assertSteps(expectedSteps)

    def assertCancelled(self):
        self.t_lock.acquire()
        try:
            assert self.t_finished != 0
            assert self.isCancelled()
        finally:
            self.t_lock.release()


class TestCmdCallback(lib.ITest):

    def mktestcb(self, req):
        """
        returns a CmdCallback instance for testing
        """
        client = self.new_client(perms="rw----")
        handle = client.getSession().submit(req)
        return CmdCallback(client, handle)

    # Timing
    # =========================================================================

    def timing(self, millis, steps):
        t = omero.cmd.Timing()
        t.millisPerStep = millis
        t.steps = steps
        return self.mktestcb(t)

    def testTimingFinishesOnLatch(self):
        cb = self.timing(25, 4 * 10)  # Runs 1 second
        cb.t_event.wait(1.500)
        assert 1 == cb.t_finished
        cb.assertFinished(10)  # Modulus-10

    def testTimingFinishesOnBlock(self):
        cb = self.timing(25, 4 * 10)  # Runs 1 second
        cb.block(1500)
        cb.assertFinished(10)  # Modulus-10

    def testTimingFinishesOnLoop(self):
        cb = self.timing(25, 4 * 10)  # Runs 1 second
        cb.loop(3, 500)
        cb.assertFinished(10)  # Modulus-10

    # DoAll
    # =========================================================================

    def doAllOfNothing(self):
        return self.mktestcb(omero.cmd.DoAll())

    def doAllTiming(self, count):
        # 6 ms per timing
        timings = [omero.cmd.Timing(3, 2) for x in range(count)]
        return self.mktestcb(omero.cmd.DoAll(timings, None))

    def testDoNothingFinishesOnLatch(self):
        cb = self.doAllOfNothing()
        cb.t_event.wait(5)
        cb.assertCancelled()

    def testDoNothingFinishesOnLoop(self):
        cb = self.doAllOfNothing()
        cb.loop(5, 1000)
        cb.assertCancelled()

    def testDoAllTimingFinishesOnLoop(self):
        cb = self.doAllTiming(5)
        cb.loop(5, 1000)
        cb.assertFinished()
        # For some reason the number of steps is varying between 10 and 15
