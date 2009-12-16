"""
    OMERO.fs Dummy Monitor module.
    
    This does nothing but is there to ensure FSEvents can start
    in a non-monitoring mode. THIS IS A HACK and a proper solution 
    needs to be sorted out. 

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging

from omero.grid import monitors
from fsAbstractPlatformMonitor import AbstractPlatformMonitor

class PlatformMonitor(AbstractPlatformMonitor):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """
    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, ignoreDirEvents, proxy):
        """
            Set-up Monitor thread.
            
            After initialising the superclass and some instance variables
            try to create an FSEventStream. Throw an exeption if this fails.
            
            :Parameters:
                eventTypes : 
                    A list of the event types to be monitored.          
                    
                pathMode : 
                    The mode of directory monitoring: flat, recursive or following.

                pathString : string
                    A string representing a path to be monitored.
                  
                whitelist : list<string>
                    A list of files and extensions of interest.
                    
                blacklist : list<string>
                    A list of subdirectories to be excluded.

                ignoreSysFiles :
                    If true platform dependent sys files should be ignored.
                    
                monitorId :
                    Unique id for the monitor included in callbacks.
                    
                proxy :
                    A proxy to be informed of events         
        """
        AbstractPlatformMonitor.__init__(self, eventTypes, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, ignoreDirEvents, proxy)
        self.log.info('Monitor set-up on %s', str(self.pathsToMonitor))
        self.log.info('Monitoring %s events', str(self.eTypes))
                
    def run(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
            
        """
        self.log.info('Monitor run called')
        
    def stop(self):        
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        self.log.info('Monitor stop called')
        
