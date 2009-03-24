"""
    OMERO.fs DropBox application


"""
import logging
import fsLogger
log = logging.getLogger("fs.DropBox")

from traceback import format_exc

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
        log.info('Trying to start OMERO.fs DropBox client')
                   
        try:   
            sf = self.getOmeroServiceFactory()
        except Exception, e:
            log.error("Failed to get Session: %s", format_exc())
            raise
            
        try:
            configService = sf.getConfigService()
        except Exception, e:
            log.error("Failed to get configService: %s", format_exc())
            raise
        
        try:
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase += config.dropBoxDir
        except Exception, e:
            log.exception("Failed to use a query service : ")
            raise

        try:
            sf.destroy()
        except Exception, e:
            log.error("Failed to get close session: %s", format_exc())
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
            serverId = fsServer.createMonitor(eventType, dropBoxBase, config.fileTypes, 
                config.blacklist, pathMode, mClientProxy)

            mClient.setId(serverId)
            mClient.setServerProxy(fsServer)
            mClient.setMaster(self)
            fsServer.startMonitor(serverId)

        except Exception, e:
            log.exception("Failed to access proxy : ")
            raise
            
        log.info('Started OMERO.fs DropBox client')        
        self.communicator().waitForShutdown()

        try:
            fsServer.stopMonitor(id)
            fsServer.destroyMonitor(id)
        except:
            log.info('Unable to contact server, must have been stopped already.')
            pass
            
        log.info('Stopping OMERO.fs DropBox client')


    def getOmeroServiceFactory(self):
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

    
    def getOmeroSession(self, client, username, password):
        gotSession = False
        tryCount = 0
        excpt = None
        while (not gotSession) and (tryCount < config.maxTries):
            try:
                time.sleep(config.retryInterval)
                session = client.createSession(username=username, password=password)
                gotSession = True
            except Exception, e:
                tryCount += 1
                log.info("Failed to get session on attempt %s", str(tryCount))
                excpt = e
        if gotSession:
            return session
        else:
            raise Exception(excpt)

        