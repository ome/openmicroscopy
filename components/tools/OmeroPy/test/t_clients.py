#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Module documentation
"""
"""
/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import Ice
import logging
import threading
import unittest
import omero.clients as base

class MockCommunicator(object):

    def __init__(self):
        self.props = Ice.createProperties()
        self._impl = object()

    def getLogger(self):
        return logging.getLogger("MockCommunicator")

    def getProperties(self):
        return self.props

    def getDefaultRouter(self):
        return object()

    def destroy(self):
        pass


class MockRouter(object):

    def destroySession(self):
        pass


class MockClient(base.BaseClient):

    def __init__(self):
        """
        Replicating a good deal of the __init__ setup, since the method
        is too complicated at the moment to just invoke.
        """
        self._BaseClient__agent = "t_clients" #: See setAgent
        self._BaseClient__insecure = False
        self._BaseClient__previous = None
        self._BaseClient__ic = MockCommunicator()
        self._BaseClient__oa = None
        self._BaseClient__sf = None
        self._BaseClient__uuid = None
        self._BaseClient__resources = None
        self._BaseClient__lock = threading.RLock()

        # Logging
        self._BaseClient__logger = logging.getLogger("omero.client")
        logging.basicConfig() # Does nothing if already configured

    def createSession(self):
        """bit of a cop out"""
        self.startKeepAlive()

    def getRouter(self, ic):
        return MockRouter()

    def assertNoResources(self):
        assert self._BaseClient__resources is None

    def assertResources(self):
        assert self._BaseClient__resources is not None

    def setSession(self):
        self._BaseClient__sf = object()


class TestKeepAlive(unittest.TestCase):
    """
    Test that keep alives and resources only exist
    at the proper times (i.e. when a session is active)

    See #7747
    """

    def setUp(self):
        self.mc = MockClient()

    def tearDown(self):
        self.mc.__del__()

    def testStartsWithout(self):
        self.mc.assertNoResources()

    def testNoneOnJustEnable(self):
        self.mc.enableKeepAlive(60)
        self.mc.assertNoResources()

    def testOneAfterCreateSession(self):
        self.mc.enableKeepAlive(60)
        self.mc.assertNoResources()
        self.mc.createSession()
        self.mc.assertResources()

    def testOneOnEnableWithSession(self):
        self.mc.setSession()
        self.mc.enableKeepAlive(60)
        self.mc.assertResources()

    def testOneStartedManually(self):
        self.mc.setSession()
        self.mc.enableKeepAlive(60)
        self.mc.assertResources()

    def testClosedOnCloseSession(self):
        self.mc.setSession()
        self.mc.enableKeepAlive(60)
        self.mc.assertResources()
        self.mc.closeSession()
        self.mc.assertNoResources()

    def testClosedOnNegativeKeepAlive(self):
        self.mc.enableKeepAlive(60)
        self.mc.startKeepAlive()
        self.mc.assertResources()
        self.mc.enableKeepAlive(-1)
        self.mc.assertNoResources()


if __name__ == '__main__':
    unittest.main()
