"""
    OMERO.fs DropBox application


"""
import logging
import fsLogger
log = logging.getLogger("fsclient."+__name__)

import time, os, sys
import uuid

import omero
import Ice
import IceGrid
import Glacier2

from omero.grid import monitors
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
            maxRetries = int(self.communicator().getProperties().getPropertyWithDefault(
                            "omero.fs.maxRetries","5"))
            retryInterval = int(self.communicator().getProperties().getPropertyWithDefault(
                                "omero.fs.retryInterval","3"))
            dropBoxDir = self.communicator().getProperties().getPropertyWithDefault(
                                "omero.fs.dropBoxDir","DropBox")
# Hard-code until xml property is changed.
#            eventType = self.communicator().getProperties().getPropertyWithDefault(
#                                "omero.fs.eventType","Create")
            pathMode = self.communicator().getProperties().getPropertyWithDefault(
                                "omero.fs.pathMode","Follow")
            dirImportWait = int(self.communicator().getProperties().getPropertyWithDefault(
                                "omero.fs.dirImportWait","60"))
                                
        except:
            log.exception("Failed get properties from templates.xml: \n", )
        
        try:
            omero.client(config.host, config.port)
        except:
            log.exception("Failed to get client: \n")
            return -1
          
        try:
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
            dropBoxBase = configService.getConfigValue("omero.data.dir")
            dropBoxBase = os.path.join(dropBoxBase, dropBoxDir)
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

            mClient = fsDropBoxMonitorClient.MonitorClientI(dropBoxBase, self.communicator())
            adapter = self.communicator().createObjectAdapter(config.clientAdapterName)
            adapter.add(mClient, identity)
            adapter.activate()

            mClientProxy = monitors.MonitorClientPrx.checkedCast(adapter.createProxy(identity))
            monitorType = monitors.MonitorType.__dict__["Persistent"]
            eventTypes = [ monitors.WatchEventType.__dict__["Creation"], monitors.WatchEventType.__dict__["Modification"] ]
            pathMode = monitors.PathMode.__dict__[pathMode]
            serverId = fsServer.createMonitor(monitorType, eventTypes, pathMode, dropBoxBase, list(config.fileTypes),  [], mClientProxy, 0.0, True)
            log.info("Created monitor with id = %s",str(serverId))
            mClient.setId(serverId)
            mClient.setServerProxy(fsServer)
            mClient.setSelfProxy(mClientProxy)
            mClient.setDirImportWait(dirImportWait)
            mClient.setMaster(self)
            fsServer.startMonitor(serverId)

        except:
            log.exception("Failed to access proxy : \n")
            return -1

        log.info('Started OMERO.fs DropBox client')
        self.communicator().waitForShutdown()

        try:
            if mClient != None:
                mClient.stop()
        except:
            log.warn("Failed to stop MonitorClient")

        try:
            fsServer.stopMonitor(id)
            fsServer.destroyMonitor(id)
        except:
            log.info('Unable to contact FS Server, must have been stopped already.')

        log.info('Stopping OMERO.fs DropBox client')
        return 0

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
