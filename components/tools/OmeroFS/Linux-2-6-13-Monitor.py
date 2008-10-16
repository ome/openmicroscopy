"""
    OMERO.fs Monitor module for Linux.


"""

import pyinotify
import threading
import sys, traceback
import uuid
import socket

# logging
from logger import log

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid clashes.
import path as pathModule

import monitors
 

class Monitor(threading.Thread):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventType, pathString, pathMode, whitelist, blacklist, proxy, monitorId):
        """
            Initialise Monitor thread.
            
            After initialising the superclass and some instance variables
            try to create an FSEventStream. Throw an exeption if this fails.
            
            :Parameters:
                pathsToMonitor : list<string>
                    This list contains the *single* path string of
                    interest.
                    
                callback : function
                    The callback function that the FSEvents.FSEventStream
                    will call when there is file activity under path.
                    
                idString : string
                    A unique id passed to FSEvents.FSEventStream that is
                    returned in the callback.
         
        """
        threading.Thread.__init__(self)
 
        if str(pathMode) not in ['Flat', 'Follow']:
            raise UnsupportedPathMode("Path Mode " + str(pathMode) + " not yet supported on this platform")
            
        self.pathMode = pathMode
        
        if str(eventType) not in ['Create']:
            raise UnsupportedEventType("Event Type " + str(eventType) + " not yet supported on this platform")
            
        self.eventType = eventType
        
        self.proxy = proxy
            
        self.pathString = pathString
        if pathString == None:
            self.pathString = pathModule.path.getcwd()
        pathsToMonitor = [self.pathString]

        wm = pyinotify.WatchManager()
        pr = ProcessEvent(id=idString, func=callback)
        self.notifier = pyinotify.Notifier(wm, pr)
        
        if self.eventType == 'Create':
            wm.add_watch(pathsToMonitor, pyinotify.IN_CLOSE_WRITE)

    def callback(self, id, eventPath):
        """
            Callback required by FSEvents.FSEventStream.
        
            :Parameters:
                    
                id : string
		    watch id.
                    
                eventPath : string
                    File paths of the event.
                    
            :return: No explicit return value.
            
        """     
        monitorId = id        
        eventList = []
        eventType = monitors.EventType.Create
        eventList.append((eventPath,eventType))  
        self.proxy.callback(monitorId, eventList)
        log.debug('Event notification on monitor id=' + monitorId + ' => ' + str(eventList))

                
    def run(self):
        """
            Start monitoring an FSEventStream.
   
            This method, overridden from Thread, is run by 
            calling the inherited method start(). The method attempts
            to schedule an FSEventStream and then run its CFRunLoop.
            The method then blocks until stop() is called.
            
            :return: No explicit return value.
            
        """

        # Blocks
        self.notifier.loop()
        
    def stop(self):        
        """
            Stop monitoring an FSEventStream.
   
            This method attempts to stop the CFRunLoop. It then
            stops, invalidates and releases the FSEventStream.
            
            There should be a more robust approach in here that 
            still kils the thread even if the first call fails.
            
            :return: No explicit return value.
            
        """
        self.notifier.stop()
        
    def getPathString(self):
        """

        """
        return self.pathString
        

class ProcessEvent(pyinotify.ProcessEvent):

    def my_init(self, **kwargs):
    	self.callback = kwargs['func'] 
	    self.id = kwargs['id']

    def process_IN_CLOSE_WRITE(self, event):
        # We have explicitely registered for this kind of event.
        self.callback(self.id, event.pathname)

    def process_default(self, event):
        # Implicitely IN_CREATE and IN_DELETE are watched too. You can
        # ignore them and provide an empty process_default or you process them,
        # either with process_default or their dedicated method
        # (process_IN_CREATE, process_IN_DELETE), which will override
        # process_default.
	    pass
	

