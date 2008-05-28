#!/usr/bin/env python
"""
    OMERO.fs Example Client module

    
"""

import sys
from time import localtime, strftime

import Ice
import monitors


class MonitorClientImpl(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.
        
        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.
        
    """

    def __init__(self):
        """
            Intialise the instance variables.
        
        """
        #: Reference back to FSServer.
        self.serverProxy = None
        #: Id
        self.id = ''

    def fsEventHappened(self, id, eventList, current=None):
        """
            This is an example callback.
            
            If new files appear on the watch the list is sent as an argument.
            The id should match for the events to be relevant. In this simple 
            exmple the call back outputs the list of new files to stdout.
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Watch created
                    by the OMERO.fs Server.
                      
                eventList : list<string>
                    A list of events, in the current implementation this is 
                    a list of strings representing the full path names of new files.
                    
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE callback.
                           
            :return: No explicit return value.
            
        """
                
        if self.id == id:
            for fileId in eventList:
                print "Event: New file %s" % (fileId)
                print "\t%s" % (self.serverProxy.getBaseName(self.id, fileId))
                print "\towner: %s" % (self.serverProxy.getOwner(self.id, fileId))
                print "\t size: %i" % (self.serverProxy.getSize(self.id, fileId))
                print "\t%s" % (strftime("atime: %d%m%y %H:%M:%S",localtime(self.serverProxy.getATime(self.id, fileId))))
                print "\t%s" % (strftime("ctime: %d%m%y %H:%M:%S",localtime(self.serverProxy.getCTime(self.id, fileId))))
                print "\t%s" % (strftime("mtime: %d%m%y %H:%M:%S",localtime(self.serverProxy.getMTime(self.id, fileId))))
                
    def setServerProxy(self, serverProxy):
        """
            Setter for serverProxy
            
            :Parameters:
                serverProxy : monitors.MonitorServerPrx
                    proxy to remote server object
                    
            :return: No explicit return value.
            
        """
        #: A string uniquely identifying the OMERO.fs Monitor
        self.serverProxy = serverProxy
    
        
            

    def setId(self, id):
        """
            Setter for id
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Monitor created
                    by the OMERO.fs Server.
                    
            :return: No explicit return value.
            
        """
        #: A string uniquely identifying the OMERO.fs Monitor
        self.id = id
        

class Client(Ice.Application):
    """
        A fairly vanilla ICE client application.
        
        :group Constructor: __init__

    """

    def __init__(self, pathString, wl, bl):
        """
            Initialise the client.
            
            :Parameters:
                pathString : string
                    A path to be watched, a string representing the full path.
                      
                wl : list<string>
                    A list of extensions of interest, strings containing extensions.
            
                bl : list<string>
                    A list of subdirectories to ignore, strings representing the
                    relative path names.
                      
        """
        self.pathToWatch = pathString
        self.whitelist = wl
        self.blacklist = bl
        
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

        base = self.communicator().propertyToProxy('omerofs.MonitorServer')
        self.mServer = monitors.MonitorServerPrx.uncheckedCast(base.ice_twoway())

        mClient = MonitorClientImpl()
        adapter = self.communicator().createObjectAdapter("omerofs.MonitorClient")
        adapter.add(mClient, self.communicator().stringToIdentity("monitorClient"))
        adapter.activate()

        mClientProxy = monitors.MonitorClientPrx.uncheckedCast(adapter.createProxy(self.communicator().stringToIdentity("monitorClient")))
        
        self.id = self.mServer.createMonitor(self.pathToWatch, self.whitelist, self.blacklist , mClientProxy);

        mClient.setId(self.id)
        mClient.setServerProxy(self.mServer)
        self.mServer.startMonitor(self.id);
      
        self.communicator().waitForShutdown()    

        return 0


#: Example path.
pathToWatch = '/Users/cblackburn/Work/OMERO-FS/FS-research/watchDir/'
#: Example whitelist of extensions.
whitelist = ['.jpg', '.dv']
#: Example blacklist of subsdirectories.
blacklist = ['subdir1']     
#: Client object reference
app = Client(pathToWatch, whitelist, blacklist)

sys.exit(app.main(sys.argv, "config.client"))
