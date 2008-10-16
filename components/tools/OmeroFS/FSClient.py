"""
    OMERO.fs Example FSClient module

    
"""

import Ice
import monitors
import MonitorClient
        

class Client(Ice.Application):
    """
        A fairly vanilla ICE client application.
        
        :group Constructor: __init__

    """

    def __init__(self, eType, pathString, whitelist, blacklist, pMode, proxy, adapter, identity ):
        """eType, pathString, whitelist, blacklist, pMode, proxy,
            Initialise the client.
            
            :Parameters:
                eType : 
                    The event type to be monitored.
                    
                pathString : string
                    A string representing a path to be monitored.
                  
                whitelist : list<string>
                    A list of extensions of interest.
                    
                blacklist : list<string>
                    A list of subdirectories to be excluded.

                pMode : 
                    The mode of directory monitoring: flat, recursive or following.
                      
        """
        self.pathToWatch = pathString
        self.whitelist = whitelist
        self.blacklist = blacklist
        
        try:
            self.eventType = monitors.EventType.__dict__[eType]
        except:
            raise Exception("Unknown eventType : " + eType)
        
        try:    
            self.pathMode = monitors.PathMode.__dict__[pMode]
        except:
            raise Exception("Unknown pathMode : " + pMode)
        
        self.proxy = proxy
        self.adapter = adapter
        self.identity = identity
        
        #: a reference to the OMERO.fs remote server
        self.mServer = None
        #: A string uniquely identifying the OMERO.fs Watch
        self.id = ''

    def interruptCallback(self, sig): 
        """
            Handles interrupts to ensure a clean shutdown.
            
            This method overrides that from Ice.Application which did nothing
            other than pass. This method is called when an interrupt SIGNAL is 
            received, if and only if, the Ice.Application is set to 
            callbackOnInterrupt, see the first line of run() below.
            
            The method attempts to stop and destroy the server monitor. This
            method cannot raise exceptions and so failure to stop and destroy
            is flagged to stdout and then shutdown is called. The most likely
            cause of failure would be either a comms breakdown between the 
            client and server or that the server has been shutdown first.
            
            :Parameters:
                sig : int
                    The signal number causing the interruption. This is not
                    yet used for anything other than information
            
            :return: No explicit return value.
            
        """    
        print 'Shutting down FS client. Signal ', str(sig), ' received.'
        
        try:
            self.mServer.stopMonitor(self.id);
            self.mServer.destroyMonitor(self.id);
        except:
            print 'Failed to tidy up on shutdown.'
            
        self.communicator().shutdown()        

    def run(self, args):
        """
            Main method called via app.main() below.
            
            The Ice.Application is set to callbackOnInterrupt so that it can be
            shutdown cleanly by the callback above.
            
            :param args: Arguments required by the ICE system.
            :return: Exit state.
            :rtype: int
            
        """      
        Ice.Application.callbackOnInterrupt()

        base = self.communicator().propertyToProxy(self.proxy)
        self.mServer = monitors.MonitorServerPrx.checkedCast(base.ice_twoway())

        clientIdentity = self.communicator().stringToIdentity(self.identity)
        
        mClient = MonitorClient.MonitorClientI()
        adapter = self.communicator().createObjectAdapter(self.adapter)
        adapter.add(mClient, clientIdentity)
        adapter.activate()

        mClientProxy = monitors.MonitorClientPrx.uncheckedCast(adapter.createProxy(clientIdentity))

        print 'Directory of ' + self.pathToWatch + '(Not yet subscribed)'
        dir = self.mServer.getDirectory(self.pathToWatch, '*')
        for fname in dir:
            print fname
        print
    
        self.id = self.mServer.createMonitor(self.eventType, self.pathToWatch, self.whitelist, self.blacklist, self.pathMode, mClientProxy);

        print 'Directory of ' + self.pathToWatch + '(Subscribed)'
        dir = self.mServer.getMonitorDirectory(self.id, '', '*')
        for fname in dir:
            print fname
        print
      
        mClient.setId(self.id)
        mClient.setServerProxy(self.mServer)
        
        self.mServer.startMonitor(self.id);
        
        self.communicator().waitForShutdown()    


