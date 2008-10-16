#!/usr/bin/env python
"""
    OMERO.fs FSServer module.

    The Server class is a wrapper to the MonitorServer. It handles the ICE
    formalities. It controls the shutdown.

    
"""

import sys
import Ice
import MonitorServer 

import logging
from logger import log

class Server(Ice.Application):
    """
        A fairly vanilla ICE server application.
        
    """
    def __init__(self, adapterName, identity):
        """
        
        """
        self.adapterName = adapterName
        self.identity = identity
        
    def interruptCallback(self, sig): 
        """
            Handles interrupts to ensure a clean shutdown.
            
            This method overrides that from Ice.Application which did nothing
            other than pass. This method is called when an interrupt SIGNAL is 
            received, if and only if, the Ice.Application is set to 
            callbackOnInterrupt, see the first line of run() below.
            
            The method does very little but is here as a hook to hang other
            shutdown actions on.
            
            :Parameters:
                sig : int
                    The signal number causing the interruption. This is not
                    yet used for anything other than information
            
            :return: No explicit return value.
            
        """   
        self.communicator().shutdown()
        log.info('Terminating OMERO.fs server. Signal ' + str(sig) + ' received.')
        logging.shutdown()
        
    def run(self, args):
        """
            Main method called via app.main() below.
            
            The Ice.Application is set to callbackOnInterrupt so that it can be
            shutdown cleanly by the callback above.
            
            :param args: Arguments required by the ICE system.
            :return: Exit state.
            :rtype: int
        """      
        # Set the interrupt mode
        Ice.Application.callbackOnInterrupt()

        # Create a MonitorServer, its adapter and activate it.
        mServer = MonitorServer.MonitorServerI()
        adapter = self.communicator().createObjectAdapter(self.adapterName)
        adapter.add(mServer, self.communicator().stringToIdentity(self.identity))
        adapter.activate()
        
        # Wait for an interrupt.
        self.communicator().waitForShutdown()
        
        return 0
