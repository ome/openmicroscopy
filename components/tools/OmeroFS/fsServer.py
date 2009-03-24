#!/usr/bin/env python
"""
    OMERO.fs FSServer module.

    The Server class is a wrapper to the MonitorServer. It handles the ICE
    formalities. It controls the shutdown.

    
"""
import logging
import fsLogger
log = logging.getLogger("fs.Server")

from traceback import format_exc

import sys
import Ice
import IceGrid

try:
    import fsMonitorServer 
except:
    raise

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
        log.info('Trying to start OMERO.fs Server')
        
        # Create a MonitorServer, its adapter and activate it.
        mServer = fsMonitorServer.MonitorServerI()
        adapter = self.communicator().createObjectAdapter(config.serverAdapterName)
        mServerPrx = adapter.add(mServer, self.communicator().stringToIdentity(config.serverIdString))
        adapter.activate() 

        """       
        try:
            reg = self.communicator().stringToProxy("IceGrid/Registry")
            reg = IceGrid.RegistryPrx.checkedCast(reg)
            session = reg.createAdminSession("null", "")           
            try:
                session.getAdmin().addObject(mServerPrx)
            except IceGrid.ObjectExistsException:
                try:
                    session.getAdmin().updateObject(mServerPrx)
                except:
                    raise
            except:
                raise
        except:
            log.error("Error adding object to registry: %s", format_exc())
        """
        
        log.info('Started OMERO.fs Server')
        
        # Wait for an interrupt.
        self.communicator().waitForShutdown()
        log.info('Stopping OMERO.fs Server')
        
        return 0
