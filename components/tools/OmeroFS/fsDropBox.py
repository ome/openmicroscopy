"""
    OMERO.fs DropBox application


"""
import logging
import fsLogger
log = logging.getLogger("fsclient."+__name__)

import time, os
import uuid

import omero
import Ice
import IceGrid
import Glacier2

import monitors
import fsDropBoxMonitorClient
import fsConfig as config


class DropBox(Ice.Application):
    
    def run(self, args):
        
        # This tests if the FSServer is supported by the platform
        # if not there's no point starting the FSDropBox client 
        import fsUtil
        try:
            fsUtil.monitorPackage()         
        except:
            log.exception("System requirements not met: \n")
            return -1
           
        try:
            omero.client(config.host, config.port)
        except:
            log.exception("Failed to get client: \n")
            return -1
          
        try:
            sf = omero.util.internal_service_factory(\
                self.communicator(), "root", "system",\
                retries=config.maxRetries, interval=config.retryInterval)
        except:
            log.exception("Failed to get Session: \n")
            return -1
            
        try:
            configService = sf.getConfigService()
        except:
            log.exception("Failed to get configService: \n")
            return -1
        
        try:
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase = os.path.join(dropBoxBase, config.dropBoxDir)
        except:
            log.exception("Failed to use a query service : \n")
            return -1

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
            return -1

        try:
            fsServer = self.communicator().stringToProxy(config.serverIdString)
            fsServer = monitors.MonitorServerPrx.checkedCast(fsServer.ice_twoway())
            
            identity = self.communicator().stringToIdentity(config.clientIdString)

            mClient = fsDropBoxMonitorClient.MonitorClientI()
            adapter = self.communicator().createObjectAdapter(config.clientAdapterName)
            adapter.add(mClient, identity)
            adapter.activate()

            mClientProxy = monitors.MonitorClientPrx.checkedCast(adapter.createProxy(identity))
            eventType = monitors.EventType.__dict__[config.eventType]
            pathMode = monitors.PathMode.__dict__[config.pathMode]
            serverId = fsServer.createMonitor(eventType, dropBoxBase, list(config.fileTypes), 
                config.blacklist, pathMode, mClientProxy)

            mClient.setId(serverId)
            mClient.setServerProxy(fsServer)
            mClient.setMaster(self)
            fsServer.startMonitor(serverId)

        except:
            log.exception("Failed to access proxy : \n")
            return -1
            
        log.info('Started OMERO.fs DropBox client')        
        self.communicator().waitForShutdown()

        try:
            fsServer.stopMonitor(id)
            fsServer.destroyMonitor(id)
        except:
            log.info('Unable to contact FS Server, must have been stopped already.')
            
        log.info('Stopping OMERO.fs DropBox client')
        return 0


