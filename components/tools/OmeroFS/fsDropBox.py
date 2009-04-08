"""
    OMERO.fs DropBox application


"""
import logging
import fsLogger
log = logging.getLogger("fs."+__name__)

import time, os
import uuid

import omero
import Ice
import IceGrid
import Glacier2

import monitors
import fsDropBoxMonitorClient
import fsConfig as config

# This tests if the FSServer is supported by the platform
# if not there's no point starting the FSDropBox client 
import fsUtil
try:
    fsUtil.monitorPackage()         
except:
    raise


class DropBox(Ice.Application):
    
    def run(self, args):
                 
        try:
            root = omero.client(config.host, config.port)
        except:
            log.exception("Failed to get client: \n")
            raise
          
        try:   
            sf = self.getOmeroServiceFactory()
        except:
            log.exception("Failed to get Session: \n")
            raise
            
        try:
            configService = sf.getConfigService()
        except:
            log.exception("Failed to get configService: \n")
            raise
        
        try:
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase += config.dropBoxDir
        except:
            log.exception("Failed to use a query service : \n")
            raise

        try:
            sf.destroy()
        except:
            log.exception("Failed to get close session: \n")
            raise

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
            raise
            
        log.info('Started OMERO.fs DropBox client')        
        self.communicator().waitForShutdown()

        try:
            fsServer.stopMonitor(id)
            fsServer.destroyMonitor(id)
        except:
            log.info('Unable to contact FS Server, must have been stopped already.')
            
        log.info('Stopping OMERO.fs DropBox client')


    def getOmeroServiceFactory(self):
        """
            Try to return a ServiceFactory from the grid.
            
            Try a number of times then give up and raise the 
            last exception returned.
        """
        gotSession = False 
        tryCount = 0
        excpt = None
        query = self.communicator().stringToProxy("IceGrid/Query")
        query = IceGrid.QueryPrx.checkedCast(query)

        while (not gotSession) and (tryCount < config.maxTries):
            try:
                time.sleep(config.retryInterval)
                blitz = query.findAllObjectsByType("::Glacier2::SessionManager")[0]
                blitz = Glacier2.SessionManagerPrx.checkedCast(blitz)
                sf = blitz.create("root", None, {"omero.client.uuid":str(uuid.uuid1())})
                sf = omero.api.ServiceFactoryPrx.checkedCast(sf)
                gotSession = True
            except Exception, e:
                tryCount += 1
                log.info("Failed to get session on attempt %s", str(tryCount))
                excpt = e

        if gotSession:
            return sf
        else:
            raise Exception(excpt)

        