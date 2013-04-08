#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import time, os, sys
from path import path

import unittest
import logging

logging.basicConfig(level=0)

import omero
import omero.util

import Ice
import IceGrid
import Glacier2

import omero.all
import omero.grid.monitors as monitors
from drivers import MockMonitor

class MockDropBox(Ice.Application):

    def run(self, args):
        retries = 5
        interval = 3
        dir = "DropBox"
        wait = 60
        mode = "Follow"

        sf = omero.util.internal_service_factory(
                self.communicator(), "root", "system",
                retries=retries, interval=interval)
        try:
            configService = sf.getConfigService()
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase = os.path.join(dropBoxBase, dropBoxDir)
        finally:
            sf.destroy()

        fsServer = self.communicator().stringToProxy(config.serverIdString)
        fsServer = monitors.MonitorServerPrx.checkedCast(fsServer.ice_twoway())

        identity = self.communicator().stringToIdentity(config.clientIdString)

        mClient = MockMonitor(dirBoxBase)
        adapter = self.communicator().createObjectAdapter(config.clientAdapterName)
        adapter.add(mClient, identity)
        adapter.activate()

        mClientProxy = monitors.MonitorClientPrx.checkedCast(adapter.createProxy(identity))
        monitorType = monitors.MonitorType.__dict__["Persistent"]
        eventTypes = [ monitors.EventType.__dict__["Create"], monitors.EventType.__dict__["Modify"] ]
        pathMode = monitors.PathMode.__dict__[pathMode]
        serverId = fsServer.createMonitor(monitorType, eventTypes, pathMode, dropBoxBase, list(config.fileTypes),  [], mClientProxy, 0.0, True)

        mClient.setId(serverId)
        mClient.setServerProxy(fsServer)
        mClient.setSelfProxy(mClientProxy)
        mClient.setDirImportWait(dirImportWait)
        mClient.setMaster(self)
        fsServer.startMonitor(serverId)

        self.communicator().waitForShutdown()

        if mClient != None:
            mClient.stop()
        fsServer.stopMonitor(id)
        fsServer.destroyMonitor(id)

class TestDropBoxClient(unittest.TestCase):

    def test1(self):
        app = MockDropBox()
        app.main(sys.argv)
    def tearDown(self):
        MockMonitor.static_stop()

if __name__ == '__main__':
    unittest.main()


