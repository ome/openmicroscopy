#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

import logging

logging.basicConfig(level=0)

import omero
import omero.util

import Ice

import omero.all
import omero.grid.monitors as monitors
from drivers import MockMonitor


class MockDropBox(Ice.Application):

    def run(self, args):
        retries = 5
        interval = 3
        dropBoxDir = "DropBox"
        dirImportWait = 60
        pathMode = "Follow"

        sf = omero.util.internal_service_factory(
            self.communicator(), "root", "system",
            retries=retries, interval=interval)
        try:
            configService = sf.getConfigService()
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase = os.path.join(dropBoxBase, dropBoxDir)
        finally:
            sf.destroy()

        config = None  # Satisfies flake8 but needs fixing

        fsServer = self.communicator().stringToProxy(config.serverIdString)
        fsServer = monitors.MonitorServerPrx.checkedCast(fsServer.ice_twoway())

        identity = self.communicator().stringToIdentity(config.clientIdString)

        mClient = MockMonitor(dropBoxBase)
        adapter = self.communicator().createObjectAdapter(
            config.clientAdapterName)
        adapter.add(mClient, identity)
        adapter.activate()

        mClientProxy = monitors.MonitorClientPrx.checkedCast(
            adapter.createProxy(identity))
        monitorType = monitors.MonitorType.__dict__["Persistent"]
        eventTypes = [monitors.EventType.__dict__["Create"],
                      monitors.EventType.__dict__["Modify"]]
        pathMode = monitors.PathMode.__dict__[pathMode]
        serverId = fsServer.createMonitor(
            monitorType, eventTypes, pathMode, dropBoxBase,
            list(config.fileTypes),  [], mClientProxy, 0.0, True)

        mClient.setId(serverId)
        mClient.setServerProxy(fsServer)
        mClient.setSelfProxy(mClientProxy)
        mClient.setDirImportWait(dirImportWait)
        mClient.setMaster(self)
        fsServer.startMonitor(serverId)

        self.communicator().waitForShutdown()

        if mClient is not None:
            mClient.stop()
        fsServer.stopMonitor(id)
        fsServer.destroyMonitor(id)


class TestDropBoxClient(object):

    def test1(self):
        app = MockDropBox()
        app.main(sys.argv)

    def teardown_method(self, method):
        MockMonitor.static_stop()
