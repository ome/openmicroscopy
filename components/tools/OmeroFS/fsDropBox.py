"""
    OMERO.fs DropBox application


"""

import logging
log = logging.getLogger("fsclient."+__name__)

import time, os, sys
import string
import uuid

import omero
import omero.rtypes
import Ice
import IceGrid
import Glacier2

from omero.util import configure_server_logging

import omero_FS_ice
monitors = Ice.openModule('omero.grid.monitors')

from omero.clients import ObjectFactory

import fsDropBoxMonitorClient

class DropBox(Ice.Application):

    def run(self, args):

        props = self.communicator().getProperties()
        configure_server_logging(props)

        # This tests if the FSServer is supported by the platform
        # if not there's no point starting the FSDropBox client
        import fsUtil
        try:
            fsUtil.monitorPackage()
        except:
            log.exception("System requirements not met: \n")
            return -1

        # Finally, configure our communicator
        ObjectFactory().registerObjectFactory(self.communicator())
        for of in omero.rtypes.ObjectFactories.values():
            of.register(self.communicator())
        
        try:
            host, port = self.getHostAndPort(self.communicator())
            omero.client(host, port)
        except:
            log.exception("Failed to get client: \n")
            return -1
          
        try:
            maxRetries = int(self.communicator().getProperties().getPropertyWithDefault("omero.fs.maxRetries","5"))
            retryInterval = int(self.communicator().getProperties().getPropertyWithDefault("omero.fs.retryInterval","3"))
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
            monitorParameters = self.getMonitorParameters(self.communicator())
            log.info("monitor params = %s", str(monitorParameters))
        except:
            log.exception("Failed get properties from templates.xml: \n", )
            return -1

        try:
            if 'default' in monitorParameters.keys():
                if not monitorParameters['default']['watchDir']:
                    dataDir = configService.getConfigValue("omero.data.dir")
                    defaultDropBoxDir = self.communicator().getProperties().getPropertyWithDefault("omero.fs.defaultDropBoxDir","DropBox")
                    monitorParameters['default']['watchDir'] = os.path.join(dataDir, defaultDropBoxDir)
            log.info("default drop box base = %s", monitorParameters['default']['watchDir'])
        except:
            log.exception("Failed to use a query service : \n")
            return -1

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
            return -1

        try:
            serverIdString = self.getFSServerIdString(self.communicator())
            fsServer = self.communicator().stringToProxy(serverIdString)
            fsServer = monitors.MonitorServerPrx.checkedCast(fsServer.ice_twoway())

            clientAdapterName = self.getFSClientAdapterName(self.communicator())
            clientIdString = self.getFSClientIdString(self.communicator())
            adapter = self.communicator().createObjectAdapter(clientAdapterName)
            mClient = {}
            
            for user in monitorParameters.keys():
                log.info("Creating client for user: %s", user)
                if user == 'default':
                    mClient[user] = fsDropBoxMonitorClient.MonitorClientI(monitorParameters[user]['watchDir'], self.communicator())
                else:
                    mClient[user] = fsDropBoxMonitorClient.SingleUserMonitorClient(user, monitorParameters[user]['watchDir'], self.communicator())
            
                identity = self.communicator().stringToIdentity(clientIdString + "." + user)
                adapter.add(mClient[user], identity)
                mClientProxy = monitors.MonitorClientPrx.uncheckedCast(adapter.createProxy(identity))
                
                monitorType = monitors.MonitorType.__dict__["Persistent"]
                try:           
                    serverId = fsServer.createMonitor(monitorType, 
                                                        monitorParameters[user]['eventTypes'], 
                                                        monitorParameters[user]['pathMode'], 
                                                        monitorParameters[user]['watchDir'], 
                                                        monitorParameters[user]['whitelist'], 
                                                        monitorParameters[user]['blacklist'], 
                                                        monitorParameters[user]['timeout'], 
                                                        monitorParameters[user]['blockSize'], 
                                                        monitorParameters[user]['ignoreSysFiles'], 
                                                        monitorParameters[user]['ignoreDirEvents'],
                                                        mClientProxy)
                                                    
                    log.info("Created monitor with id = %s",str(serverId))
                    mClient[user].setId(serverId)
                    mClient[user].setServerProxy(fsServer)
                    mClient[user].setSelfProxy(mClientProxy)
                    mClient[user].setDirImportWait(monitorParameters[user]['dirImportWait'])
                    mClient[user].setHostAndPort(host,port)
                    mClient[user].setMaster(self)
                    fsServer.startMonitor(serverId)
                except:
                    log.exception("Failed create or start monitor : \n")
            adapter.activate()
        except:
            log.exception("Failed to access proxy : \n")
            return -1
        if not mClient:
            log.error("Failed to create any monitors.")
            return -1
            
        log.info('Started OMERO.fs DropBox client')
        self.communicator().waitForShutdown()

        for user, client in mClient.items():
            try:
                if client != None:
                    client.stop()
            except:
                log.warn("Failed to stop MonitorClient: %s", user)

        try:
            fsServer.stopMonitor(id)
            fsServer.destroyMonitor(id)
        except:
            log.info('Unable to contact FS Server, must have been stopped already.')

        log.info('Stopping OMERO.fs DropBox client')
        return 0

    def getHostAndPort(self, communicator):
        """
            Get the host and port from the communicator properties.
            
        """
        host = communicator.getProperties().getPropertyWithDefault("omero.fs.host","localhost")
        port = int(communicator.getProperties().getPropertyWithDefault("omero.fs.port","4063"))
            
        return host, port
            
    def getFSServerIdString(self, communicator):
        """
            Get serverIdString from the communicator properties.
            
        """
        return communicator.getProperties().getPropertyWithDefault("omero.fs.serverIdString","")
        
    def getFSClientIdString(self, communicator):
        """
            Get serverIdString from the communicator properties.
            
        """
        return communicator.getProperties().getPropertyWithDefault("omero.fs.clientIdString","")

    def getFSClientAdapterName(self, communicator):
        """
            Get serverIdString from the communicator properties.
            
        """
        return communicator.getProperties().getPropertyWithDefault("omero.fs.clientAdapterName","")

    def getMonitorParameters(self, communicator):
        """
            Get the monitor parameters from the communicator properties.
            
        """
        monitorParams = {}
        try:
            importUser = list(communicator.getProperties().getPropertyWithDefault("omero.fs.importUsers","default").split(';'))
            watchDir = list(communicator.getProperties().getPropertyWithDefault("omero.fs.watchDir","").split(';'))   
            eventTypes = list(communicator.getProperties().getPropertyWithDefault("omero.fs.eventTypes","All").split(';'))      
            pathMode = list(communicator.getProperties().getPropertyWithDefault("omero.fs.pathMode","Follow").split(';'))   
            whitelist = list(communicator.getProperties().getPropertyWithDefault("omero.fs.whitelist","").split(';'))   
            blacklist = list(communicator.getProperties().getPropertyWithDefault("omero.fs.blacklist","").split(';'))   
            timeout = list(communicator.getProperties().getPropertyWithDefault("omero.fs.timeout","0.0").split(';'))   
            blockSize = list(communicator.getProperties().getPropertyWithDefault("omero.fs.blockSize","0").split(';'))   
            ignoreSysFiles = list(communicator.getProperties().getPropertyWithDefault("omero.fs.ignoreSysFiles","True").split(';'))   
            ignoreDirEvents = list(communicator.getProperties().getPropertyWithDefault("omero.fs.ignoreDirEvents","True").split(';'))   
            dirImportWait = list(communicator.getProperties().getPropertyWithDefault("omero.fs.dirImportWait","60").split(';'))   

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
