#!/usr/bin/env python
"""
    OMERO.fs FSServer module.

    The Server class is a wrapper to the MonitorServer. It handles the ICE
    formalities. It controls the shutdown.

    
"""
import logging
import fsLogger
log = logging.getLogger("fsserver."+__name__)

import sys
import Ice
import IceGrid


import fsConfig as config

class Server(Ice.Application):
    """
        A fairly vanilla ICE server application.
        
    """
    
    def run(self, args):
        """
            Main method called via app.main() below.
            
            The Ice.Application is set to callbackOnInterrupt so that it can be
            shutdown cleanly by the callback above.
            
            :param args: Arguments required by the ICE system.
            :return: Exit state.
            :rtype: int
        """
        
        try:
            import fsMonitorServer 
        except:
            log.exception("System requirements not met: \n")
            return -1
 
        # Create a MonitorServer, its adapter and activate it.
        try:
            mServer = fsMonitorServer.MonitorServerI()
            adapter = self.communicator().createObjectAdapter(config.serverAdapterName)
            mServerPrx = adapter.add(mServer, self.communicator().stringToIdentity(config.serverIdString))
            adapter.activate() 
        except:
            log.exception("Failed create OMERO.fs Server: \n")
            return -1
            
        log.info('Started OMERO.fs Server')
        
        # Wait for an interrupt.
        self.communicator().waitForShutdown()
        
        log.info('Stopping OMERO.fs Server')
        return 0
