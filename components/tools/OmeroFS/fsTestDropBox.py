"""
    OMERO.fs DropBox application

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

import logging
log = logging.getLogger("TestDropBox")

import time, os, sys
import string
import uuid
import threading
import shutil

import omero
import omero.rtypes
import omero.sys
import Ice
import IceGrid
import Glacier2

from omero.util import configure_logging

import omero_FS_ice
monitors = Ice.openModule('omero.grid.monitors')

from omero.clients import ObjectFactory

import fsDropBoxMonitorClient

class TestDropBox(Ice.Application):
    
    watchDir = ''
    srcFile = ''
    dstFile = ''
    imageId = None
    event = threading.Event()
    
    def run(self, args):

        # Configure our communicator
        ObjectFactory().registerObjectFactory(self.communicator())
        for of in omero.rtypes.ObjectFactories.values():
            of.register(self.communicator())
        
        props = self.communicator().getProperties()

        self.srcFile =  props.getPropertyWithDefault("omero.fstest.srcFile","")
        self.watchDir = props.getPropertyWithDefault("omero.fstest.watchDir","")
        logDir = getPropertyWithDefault("omero.fstest.logDir","")
        logFile = self.props.getPropertyWithDefault("omero.fstest.logFile","")

        configure_logging(logdir=logDir, logfile=logFile)
        log.info('Trying to start OMERO.fs TestDropBox client')

        # This tests if the FSServer is supported by the platform
        # if not there's no point starting the FSDropBox client
        import fsUtil
        try:
            fsUtil.monitorPackage()
        except:
            log.exception("System requirements not met: \n")
            return -1

        try:
            host, port = self.getHostAndPort(props)
            omero.client(host, port)
        except:
            log.exception("Failed to get client: \n")
            return -1
      
        try:
            maxRetries = int(props.getPropertyWithDefault("omero.fs.maxRetries","5"))
            retryInterval = int(props.getPropertyWithDefault("omero.fs.retryInterval","3"))
            sf = omero.util.internal_service_factory(
                    self.communicator(), "root", "system",
                    retries=maxRetries, interval=retryInterval)
        except:
            log.exception("Failed to get Session: \n")
            return -1
        
        try:
            configService = sf.getConfigService()
        except:
            log.exception("Failed to get configService: \n")
            return -1
        
        try:
            monitorParameters = self.getMonitorParameters(props)
            log.info("Monitor parameters = %s", str(monitorParameters))
        except:
            log.exception("Failed get properties from templates.xml: \n", )
            return -1

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
            return -1

        try:
            serverIdString = self.getFSServerIdString(props)
            fsServer = self.communicator().stringToProxy(serverIdString)
            fsServer = monitors.MonitorServerPrx.checkedCast(fsServer.ice_twoway())

            clientAdapterName = self.getFSClientAdapterName(props)
            clientIdString = self.getFSClientIdString(props)
            
            mClient = {}
            monitorId = {}      
            user = monitorParameters.keys()[0]
            log.info("Creating test client for test user: %s", user)
            
            mClient[user] = fsDropBoxMonitorClient.TestMonitorClient(user, monitorParameters[user]['watchDir'], self.communicator())
            adapter = self.communicator().createObjectAdapter(clientAdapterName)
            identity = self.communicator().stringToIdentity(clientIdString + "." + user)
            mClientProxy = monitors.MonitorClientPrx.uncheckedCast(adapter.createProxy(identity))
            adapter.add(mClient[user], identity)
        
            monitorType = monitors.MonitorType.__dict__["Persistent"]
            try:           
                monitorId[user] = fsServer.createMonitor(monitorType, 
                                                    monitorParameters[user]['eventTypes'], 
                                                    monitorParameters[user]['pathMode'], 
                                                    self.watchDir,
                                                    monitorParameters[user]['whitelist'], 
                                                    monitorParameters[user]['blacklist'], 
                                                    monitorParameters[user]['timeout'], 
                                                    monitorParameters[user]['blockSize'], 
                                                    monitorParameters[user]['ignoreSysFiles'], 
                                                    monitorParameters[user]['ignoreDirEvents'],
                                                    mClientProxy)
                                            
                log.info("Created monitor with id = %s",str(monitorId[user]))
                mClient[user].setId(monitorId[user])
                mClient[user].setServerProxy(fsServer)
                mClient[user].setSelfProxy(mClientProxy)
                mClient[user].setDirImportWait(monitorParameters[user]['dirImportWait'])
                mClient[user].setReaders(monitorParameters[user]['readers'])
                mClient[user].setHostAndPort(host,port)
                mClient[user].setMaster(self)
                fsServer.startMonitor(monitorId[user])
            except:
                log.exception("Failed create or start monitor : \n")
        
            adapter.activate()
        except:
            log.exception("Failed to access proxy : \n")
            return -1

        if not mClient:
            log.error("Failed to create any monitors.")
            return -1
        
        log.info('Started OMERO.fs Test DropBox client')
        
        self.dstFile = os.path.join(self.watchDir, str(uuid.uuid1())+".dv")
        
        try:
            shutil.copy(self.srcFile, self.dstFile)
        except:
            log.exception("Error copying file:")
            return -1
        
        while not self.event.isSet():    
            time.sleep(1)
        
        try:
            sf = omero.util.internal_service_factory(
                    self.communicator(), "root", "system",
                    retries=maxRetries, interval=retryInterval)
        except:
            log.exception("Failed to get Session: \n")
            return -1

        p = omero.sys.Parameters()
        
        query = "select i from Image i where i.name = " + "'" + self.dstFile + "'"
        out = sf.getQueryService().findAllByQuery(query, p)
        log.info("Query 1 says: %s item(s) found.", str(len(out)))
        
        query = "select i from Image i where i.id = " + "'" + self.imageId + "'"
        out = sf.getQueryService().findAllByQuery(query, p)
        
        if len(out) == 1:
            fname = out[0]._name._val
            log.error("Query on id=%s returned file %s", self.imageId, fname)
            if fname == self.dstFile:
                retVal = 0
            else:
                log.error("Filenames do not match %s != %s", fname, self.dstFile)
                retVal = -1
        else:
            log.error("Incorrect number of items found: %s", len(out))
            
        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
    
        for user in mClient.keys():
            try:
                fsServer.stopMonitor(monitorId[user])
                try:
                    fsServer.destroyMonitor(monitorId[user])
                except:
                    log.warn("Failed to destroy MonitorClient for : %s  FSServer may have already stopped.", user)
            except:
                log.warn("Failed to stop and destroy MonitorClient for : %s  FSServer may have already stopped.", user)
                
            try:
                mClient[user].stop()
            except:
                log.exception("Failed to stop DropBoxMonitorClient for: %s", user)

        log.info('Stopping OMERO.fs Test DropBox client')
        log.info("Exiting with exit code: %d", retVal)
        return retVal

    def notifyTestFile(self, imageId, fileId):
        """
            Called back by overridden importFileWrapper
            
        """
        log.info("%s import attempted. image id=%s", fileId, imageId)
        self.imageId = imageId
        self.event.set()
        
    def getHostAndPort(self, props):
        """
            Get the host and port from the communicator properties.
            
        """
        host = props.getPropertyWithDefault("omero.fs.host","localhost")
        port = int(props.getPropertyWithDefault("omero.fs.port","4063"))
            
        return host, port
            
    def getFSServerIdString(self, props):
        """
            Get serverIdString from the communicator properties.
            
        """
        return props.getPropertyWithDefault("omero.fs.serverIdString","")
        
    def getFSClientIdString(self, props):
        """
            Get serverIdString from the communicator properties.
            
        """
        return props.getPropertyWithDefault("omero.fs.clientIdString","")

    def getFSClientAdapterName(self, props):
        """
            Get serverIdString from the communicator properties.
            
        """
        return props.getPropertyWithDefault("omero.fs.clientAdapterName","")

    def getMonitorParameters(self, props):
        """
            Get the monitor parameters from the communicator properties.
            
        """
        monitorParams = {}
        try:
            importUser = list(props.getPropertyWithDefault("omero.fs.importUsers","default").split(';'))
            watchDir = list(props.getPropertyWithDefault("omero.fs.watchDir","").split(';'))   
            eventTypes = list(props.getPropertyWithDefault("omero.fs.eventTypes","All").split(';'))      
            pathMode = list(props.getPropertyWithDefault("omero.fs.pathMode","Follow").split(';'))   
            whitelist = list(props.getPropertyWithDefault("omero.fs.whitelist","").split(';'))   
            blacklist = list(props.getPropertyWithDefault("omero.fs.blacklist","").split(';'))   
            timeout = list(props.getPropertyWithDefault("omero.fs.timeout","0.0").split(';'))   
            blockSize = list(props.getPropertyWithDefault("omero.fs.blockSize","0").split(';'))   
            ignoreSysFiles = list(props.getPropertyWithDefault("omero.fs.ignoreSysFiles","True").split(';'))   
            ignoreDirEvents = list(props.getPropertyWithDefault("omero.fs.ignoreDirEvents","True").split(';'))   
            dirImportWait = list(props.getPropertyWithDefault("omero.fs.dirImportWait","60").split(';'))   
            readers = list(props.getPropertyWithDefault("omero.fs.readers","").split(';'))   

            for i in range(len(importUser)):
                if importUser[i].strip(string.whitespace):
                    monitorParams[importUser[i].strip(string.whitespace)] = {}

                    try:
                        monitorParams[importUser[i]]['watchDir'] = watchDir[i].strip(string.whitespace) 
                    except:
                        monitorParams[importUser[i]]['watchDir'] = ""

                    monitorParams[importUser[i]]['eventTypes'] = []
                    for eType in eventTypes[i].split(','):
                        try:
                            monitorParams[importUser[i]]['eventTypes'].append(monitors.WatchEventType.__dict__[eType.strip(string.whitespace)])
                        except:
                            monitorParams[importUser[i]]['eventTypes'] = [monitors.WatchEventType.__dict__["All"]]
                                                                                            
                    try:
                        monitorParams[importUser[i]]['pathMode'] = monitors.PathMode.__dict__[pathMode[i].strip(string.whitespace)]
                    except:
                        monitorParams[importUser[i]]['pathMode'] = monitors.PathMode.__dict__["Follow"]
                                                                                              
                    monitorParams[importUser[i]]['whitelist'] = []
                    for white in whitelist[i].split(','):
                        if white.strip(string.whitespace):
                            monitorParams[importUser[i]]['whitelist'].append(white.strip(string.whitespace))
                                                                                            
                    monitorParams[importUser[i]]['blacklist'] = []
                    for black in blacklist[i].split(','):
                        if black.strip(string.whitespace):
                            monitorParams[importUser[i]]['blacklist'].append(black.strip(string.whitespace))
                                                                                            
                    try:
                        monitorParams[importUser[i]]['timeout'] = float(timeout[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]]['timeout'] = 0.0 # seconds
                                                                                                                                                                                           
                    try:
                        monitorParams[importUser[i]]['blockSize'] = int(blockSize[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]]['blockSize'] = 0 # number
                                                                                                                                                                                           
                    try:
                        monitorParams[importUser[i]]['ignoreSysFiles'] = ignoreSysFiles[i].strip(string.whitespace)[0] in ('T', 't')
                    except:
                        monitorParams[importUser[i]]['ignoreSysFiles'] = False
                                                                                                                                                                                           
                    try:
                        monitorParams[importUser[i]]['ignoreDirEvents'] = ignoreDirEvents[i].strip(string.whitespace)[0] in ('T', 't')
                    except:
                        monitorParams[importUser[i]]['ignoreDirEvents'] = False
                                                                                                                                                                                           
                    try:
                        monitorParams[importUser[i]]['dirImportWait'] = int(dirImportWait[i].strip(string.whitespace))
                    except:
                        monitorParams[importUser[i]]['dirImportWait'] = 60 # seconds                                                                                                                                                                     
                                                                                                                                                                                           
                    try:
                        readersFile = readers[i].strip(string.whitespace)
                        if os.path.isfile(readersFile):
                            monitorParams[importUser[i]]['readers'] = readersFile
                        else:
                            monitorParams[importUser[i]]['readers'] = ""
                    except:
                        monitorParams[importUser[i]]['readers'] = ""
                                                                                                                                                                                      
        except:
            raise
        
        return monitorParams
        
    
if __name__ == '__main__':
    
    app = TestDropBox()
    exitCode = app.main(sys.argv, "config.testdropbox")
    sys.exit(exitCode)
