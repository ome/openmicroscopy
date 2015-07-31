#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs DropBox application

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

import logging
log = logging.getLogger("fsclient.DropBox")

import os
import sys
import string
import threading
import shutil
import uuid

# Third party path package. It provides much of the
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid clashes.
import path as pathModule

import omero.all
import omero.grid.monitors as monitors

import omero.rtypes
import Ice

from omero.util import configure_server_logging

import omero.ObjectFactoryRegistrar as ofr
import fsDropBoxMonitorClient


class DropBox(Ice.Application):
    # Used by test client
    imageIds = []
    importCount = 0
    event = threading.Event()

    def run(self, args):
        # Configure our communicator
        ofr.registerObjectFactory(self.communicator())
        for of in omero.rtypes.ObjectFactories.values():
            of.register(self.communicator())

        retVal = -1

        props = self.communicator().getProperties()
        configure_server_logging(props)

        log.debug("Grid Properties:\n%s", str(props))

        testConfig = props.getPropertyWithDefault("omero.fstest.config", "")
        isTestClient = bool(testConfig)

        if isTestClient:
            props.load(testConfig)
            log.info("Updated Test Properties:\n%s", str(props))

        # This tests if the FSServer is supported by the platform
        # if not there's no point starting the FSDropBox client
        import fsUtil
        try:
            checkString = props.getPropertyWithDefault(
                "omero.fs.platformCheck", "True")
            platformCheck = not (checkString == "False")
            fsUtil.monitorPackage(platformCheck)
        except:
            log.exception("System requirements not met: \n")
            log.error("Quitting")
            return retVal

        try:
            host, port = self.getHostAndPort(props)
            omero.client(host, port)
        except:
            log.exception("Failed to get client: \n")
            log.error("Quitting")
            return retVal

        try:
            self.maxRetries = int(
                props.getPropertyWithDefault("omero.fs.maxRetries", "5"))
            self.retryInterval = int(
                props.getPropertyWithDefault("omero.fs.retryInterval", "3"))
            sf = omero.util.internal_service_factory(
                self.communicator(), "root", "system",
                retries=self.maxRetries, interval=self.retryInterval)
        except:
            log.exception("Failed to get Session: \n")
            log.error("Quitting")
            return retVal

        try:
            configService = sf.getConfigService()
        except:
            log.exception("Failed to get configService: \n")
            log.error("Quitting")
            return retVal

        try:
            monitorParameters = self.getMonitorParameters(props)
            log.info("Monitor parameters = %s", str(monitorParameters))
        except:
            log.exception("Failed get properties from templates.xml: \n", )
            log.error("Quitting")
            return retVal

        try:
            if 'default' in monitorParameters.keys():
                if not monitorParameters['default']['watchDir']:
                    dataDir = configService.getConfigValue("omero.data.dir")
                    defaultDropBoxDir = props.getPropertyWithDefault(
                        "omero.fs.defaultDropBoxDir", "DropBox")
                    monitorParameters['default']['watchDir'] = os.path.join(
                        dataDir, defaultDropBoxDir)
                    watchDir = pathModule.path(
                        monitorParameters['default']['watchDir'])
                    if not watchDir.exists():
                        log.info(
                            "Creating default dropbox directory: "
                            + monitorParameters['default']['watchDir'])
                        watchDir.mkdir()
        except OSError:
            log.exception("Failed to create default dropbox directory : \n")
        except:
            log.exception("Failed to use a query service : \n")
            log.error("Quitting")
            return retVal

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
            log.error("Quitting")
            return retVal

        try:
            serverIdString = self.getServerIdString(props)
            fsServer = self.communicator().stringToProxy(serverIdString)
            fsServer = monitors.MonitorServerPrx.checkedCast(
                fsServer.ice_twoway())

            clientAdapterName = self.getClientAdapterName(props)
            clientIdString = self.getClientIdString(props)
            adapter = self.communicator().createObjectAdapter(
                clientAdapterName)
            mClient = {}
            monitorId = {}

            for user in monitorParameters.keys():
                if isTestClient:
                    self.callbackOnInterrupt()
                    log.info("Creating test client for user: %s", user)
                    testUser = user
                    mClient[user] = fsDropBoxMonitorClient.TestMonitorClient(
                        user, monitorParameters[user]['watchDir'],
                        self.communicator(),
                        worker_wait=monitorParameters[user]['fileWait'],
                        worker_batch=monitorParameters[user]['fileBatch'])
                else:
                    log.info("Creating client for user: %s", user)
                    if user == 'default':
                        mClient[user] = fsDropBoxMonitorClient.MonitorClientI(
                            monitorParameters[user]['watchDir'],
                            self.communicator(),
                            worker_wait=monitorParameters[user]['fileWait'],
                            worker_batch=monitorParameters[user]['fileBatch'])
                    else:
                        mClient[user] = \
                            fsDropBoxMonitorClient.SingleUserMonitorClient(
                                user, monitorParameters[user]['watchDir'],
                                self.communicator(),
                                worker_wait=monitorParameters[
                                    user]['fileWait'],
                                worker_batch=monitorParameters[
                                    user]['fileBatch'])

                identity = self.communicator().stringToIdentity(
                    clientIdString + "." + user)
                adapter.add(mClient[user], identity)
                mClientProxy = monitors.MonitorClientPrx.uncheckedCast(
                    adapter.createProxy(identity))

                monitorType = monitors.MonitorType.__dict__["Persistent"]
                try:
                    monitorId[user] = fsServer.createMonitor(
                        monitorType,
                        monitorParameters[user]['eventTypes'],
                        monitorParameters[user]['pathMode'],
                        monitorParameters[user]['watchDir'],
                        monitorParameters[user]['whitelist'],
                        monitorParameters[user]['blacklist'],
                        monitorParameters[user]['timeout'],
                        monitorParameters[user]['blockSize'],
                        monitorParameters[user]['ignoreSysFiles'],
                        monitorParameters[user]['ignoreDirEvents'],
                        platformCheck,
                        mClientProxy)

                    log.info(
                        "Created monitor with id = %s", str(monitorId[user]))
                    mClient[user].setId(monitorId[user])
                    mClient[user].setServerProxy(fsServer)
                    mClient[user].setSelfProxy(mClientProxy)
                    mClient[user].setDirImportWait(
                        monitorParameters[user]['dirImportWait'])
                    mClient[user].setThrottleImport(
                        monitorParameters[user]['throttleImport'])
                    mClient[user].setTimeouts(
                        monitorParameters[user]['timeToLive'],
                        monitorParameters[user]['timeToIdle'])
                    mClient[user].setReaders(
                        monitorParameters[user]['readers'])
                    mClient[user].setImportArgs(
                        monitorParameters[user]['importArgs'])
                    mClient[user].setHostAndPort(host, port)
                    mClient[user].setMaster(self)
                    fsServer.startMonitor(monitorId[user])
                except:
                    log.exception("Failed create or start monitor : \n")
            adapter.activate()
        except:
            log.exception("Failed to access proxy : \n")
            return retVal

        if not mClient:
            log.error("Failed to create any monitors.")
            log.error("Quitting")
            return retVal

        log.info('Started OMERO.fs DropBox client')

        try:
            # If this is TestDropBox then try to copy and import a file.
            if isTestClient:
                timeout = int(props.getPropertyWithDefault(
                    "omero.fstest.timeout", "120"))
                srcFiles = list(props.getPropertyWithDefault(
                    "omero.fstest.srcFile", "").split(';'))
                targetDir = monitorParameters[testUser]['watchDir']
                if not srcFiles or not targetDir:
                    log.error("Bad configuration")
                else:
                    log.info("Copying test file(s) %s to %s" %
                             (srcFiles, targetDir))
                    retVal = self.injectTestFile(srcFiles, targetDir, timeout)
            else:
                self.communicator().waitForShutdown()
        except:
            # Catching here guarantees cleanup.
            log.exception("Executor error")

        for user in mClient.keys():
            try:
                fsServer.stopMonitor(monitorId[user])
                try:
                    fsServer.destroyMonitor(monitorId[user])
                except:
                    log.warn(
                        "Failed to destroy MonitorClient for : %s "
                        "FSServer may have already stopped.", user)
                    retVal = 0
            except:
                log.warn(
                    "Failed to stop and destroy MonitorClient for : %s  "
                    "FSServer may have already stopped.", user)
                retVal = 0

            try:
                mClient[user].stop()
            except:
                log.exception(
                    "Failed to stop DropBoxMonitorClient for: %s", user)

        log.info('Stopping OMERO.fs DropBox client')
        log.info("Exiting with exit code: %d", retVal)
        if retVal != 0:
            log.error("Quitting")

        return retVal

    def interruptCallback(self, sig):
        """
        Called when this is a test run in order to prevent long hangs.
        """
        log.info("Setting event on sig %s" % sig)
        self.event.set()

    def injectTestFile(self, srcFiles, dstDir, timeout):
        """
           Copy test file and wait for import to complete.

        """

        try:
            destFiles = []
            for src in srcFiles:
                ext = pathModule.path(src).ext
                dstFile = os.path.join(dstDir, str(uuid.uuid1()) + ext)
                destFiles.append((src, dstFile))
        except:
            log.exception("Error source files:")
            return -1

        try:
            for filePair in destFiles:
                shutil.copy(filePair[0], filePair[1])
        except:
            log.exception("Error copying file:")
            return -1

        self.importCount = len(srcFiles)
        self.event.wait(timeout)

        if not self.event.isSet():
            log.error("notifyTestFile not called enough times (%s/%s)",
                      len(srcFiles) - self.importCount, len(srcFiles))
        else:
            log.info("All imports completed.")

        try:
            sf = omero.util.internal_service_factory(
                self.communicator(), "root", "system",
                retries=self.maxRetries, interval=self.retryInterval)
        except:
            log.exception("Failed to get Session: \n")
            return -1

        p = omero.sys.Parameters()

        retVal = 0
        for i in self.imageIds:
            query = "select i from Image i where i.id = " + "'" + i + "'"
            out = sf.getQueryService().findAllByQuery(query, p)

            if len(out) > 0:
                for item in out:
                    fname = item._name._val
                    log.info("Query on id=%s returned file %s", i, fname)
            else:
                log.error("No items found.")
                retVal = -1

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")

        return retVal

    def notifyTestFile(self, imageId, fileId):
        """
            Called back by overridden importFileWrapper

        """
        log.info("%s import attempted. image id=%s", fileId, imageId)
        self.imageIds += imageId
        self.importCount -= 1
        if self.importCount == 0:
            self.event.set()

    def getHostAndPort(self, props):
        """
            Get the host and port from the communicator properties.

        """
        host = props.getPropertyWithDefault("omero.fs.host", "localhost")
        port = int(props.getPropertyWithDefault("omero.fs.port", "4064"))

        return host, port

    def getServerIdString(self, props):
        """
            Get serverIdString from the communicator properties.

        """
        return props.getPropertyWithDefault("omero.fs.serverIdString", "")

    def getClientIdString(self, props):
        """
            Get serverIdString from the communicator properties.

        """
        return props.getPropertyWithDefault("omero.fs.clientIdString", "")

    def getClientAdapterName(self, props):
        """
            Get serverIdString from the communicator properties.

        """
        return props.getPropertyWithDefault("omero.fs.clientAdapterName", "")

    def getMonitorParameters(self, props):
        """
            Get the monitor parameters from the communicator properties.

        """
        monitorParams = {}
        try:
            importUser = list(props.getPropertyWithDefault(
                "omero.fs.importUsers", "default").split(';'))
            watchDir = list(props.getPropertyWithDefault(
                "omero.fs.watchDir", "").split(';'))
            eventTypes = list(props.getPropertyWithDefault(
                "omero.fs.eventTypes", "All").split(';'))
            pathMode = list(props.getPropertyWithDefault(
                "omero.fs.pathMode", "Follow").split(';'))
            whitelist = list(props.getPropertyWithDefault(
                "omero.fs.whitelist", "").split(';'))
            blacklist = list(props.getPropertyWithDefault(
                "omero.fs.blacklist", "").split(';'))
            timeout = list(props.getPropertyWithDefault(
                "omero.fs.timeout", "0.0").split(';'))
            blockSize = list(props.getPropertyWithDefault(
                "omero.fs.blockSize", "0").split(';'))
            ignoreSysFiles = list(props.getPropertyWithDefault(
                "omero.fs.ignoreSysFiles", "True").split(';'))
            ignoreDirEvents = list(props.getPropertyWithDefault(
                "omero.fs.ignoreDirEvents", "True").split(';'))
            dirImportWait = list(props.getPropertyWithDefault(
                "omero.fs.dirImportWait", "60").split(';'))
            throttleImport = list(props.getPropertyWithDefault(
                "omero.fs.throttleImport", "5").split(';'))
            timeToLive = list(props.getPropertyWithDefault(
                "omero.fs.timeToLive", "0").split(';'))
            timeToIdle = list(props.getPropertyWithDefault(
                "omero.fs.timeToIdle", "600").split(';'))
            fileBatch = list(props.getPropertyWithDefault(
                "omero.fs.fileBatch", "10").split(';'))
            readers = list(props.getPropertyWithDefault(
                "omero.fs.readers", "").split(';'))
            importArgs = list(props.getPropertyWithDefault(
                "omero.fs.importArgs", "").split(';'))

            for i in range(len(importUser)):
                if importUser[i].strip(string.whitespace):
                    monitorParams[importUser[i].strip(string.whitespace)] = {}

                    try:
                        monitorParams[importUser[i]]['watchDir'] = watchDir[
                            i].strip(string.whitespace)
                    except:
                        monitorParams[importUser[i]]['watchDir'] = ""

                    monitorParams[importUser[i]]['eventTypes'] = []
                    for eType in eventTypes[i].split(','):
                        try:
                            monitorParams[importUser[i]]['eventTypes'].append(
                                monitors.WatchEventType.__dict__[eType.strip(
                                    string.whitespace)])
                        except:
                            monitorParams[importUser[i]]['eventTypes'] = [
                                monitors.WatchEventType.__dict__["All"]]

                    try:
                        monitorParams[importUser[i]]['pathMode'] = \
                            monitors.PathMode.__dict__[pathMode[i].strip(
                                string.whitespace)]
                    except:
                        monitorParams[importUser[i]][
                            'pathMode'] = monitors.PathMode.__dict__["Follow"]

                    monitorParams[importUser[i]]['whitelist'] = []
                    for white in whitelist[i].split(','):
                        if white.strip(string.whitespace):
                            monitorParams[importUser[i]]['whitelist'].append(
                                white.strip(string.whitespace))

                    monitorParams[importUser[i]]['blacklist'] = []
                    for black in blacklist[i].split(','):
                        if black.strip(string.whitespace):
                            monitorParams[importUser[i]]['blacklist'].append(
                                black.strip(string.whitespace))

                    try:
                        monitorParams[importUser[i]]['timeout'] = float(
                            timeout[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]][
                            'timeout'] = 0.0  # seconds

                    try:
                        monitorParams[importUser[i]]['blockSize'] = int(
                            blockSize[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]]['blockSize'] = 0  # number

                    try:
                        monitorParams[importUser[i]]['ignoreSysFiles'] = \
                            ignoreSysFiles[i].strip(
                                string.whitespace)[0] in ('T', 't')
                    except:
                        monitorParams[importUser[i]]['ignoreSysFiles'] = False

                    try:
                        monitorParams[importUser[i]]['ignoreDirEvents'] = \
                            ignoreDirEvents[i].strip(
                                string.whitespace)[0] in ('T', 't')
                    except:
                        monitorParams[importUser[i]]['ignoreDirEvents'] = False

                    try:
                        monitorParams[importUser[i]]['dirImportWait'] = int(
                            dirImportWait[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]][
                            'dirImportWait'] = 60  # seconds
                    monitorParams[importUser[i]]['fileWait'] = monitorParams[
                        importUser[i]]['dirImportWait'] * 0.25

                    try:
                        monitorParams[importUser[i]]['throttleImport'] = int(
                            throttleImport[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]][
                            'throttleImport'] = 5  # seconds

                    try:
                        monitorParams[importUser[i]]['timeToLive'] = long(
                            timeToLive[i].strip(string.whitespace)) * 1000
                    except:
                        # milliseconds
                        monitorParams[importUser[i]]['timeToLive'] = 0L

                    try:
                        monitorParams[importUser[i]]['timeToIdle'] = long(
                            timeToIdle[i].strip(string.whitespace)) * 1000
                    except:
                        # milliseconds
                        monitorParams[importUser[i]]['timeToIdle'] = 600000L

                    try:
                        monitorParams[importUser[i]]['fileBatch'] = int(
                            fileBatch[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]][
                            'fileBatch'] = 10  # number

                    try:
                        readersFile = readers[i].strip(string.whitespace)
                        if os.path.isfile(readersFile):
                            monitorParams[importUser[i]][
                                'readers'] = readersFile
                        else:
                            monitorParams[importUser[i]]['readers'] = ""
                    except:
                        monitorParams[importUser[i]]['readers'] = ""

                    try:
                        monitorParams[importUser[i]]['importArgs'] = \
                            importArgs[i].strip(string.whitespace)
                    except:
                        monitorParams[importUser[i]]['importArgs'] = ""

        except:
            raise

        return monitorParams


if __name__ == '__main__':
    try:
        log.info('Trying to start OMERO.fs DropBox client')
        app = DropBox()
    except:
        log.exception("Failed to start the client:\n")
        log.info("Exiting with exit code: -1")
        sys.exit(-1)

    exitCode = app.main(sys.argv)
    log.info("Exiting with exit code: %d", exitCode)
    sys.exit(exitCode)
