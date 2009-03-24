"""
    OMERO.fs Monitor module for Linux.


"""
import logging
import fsLogger
log = logging.getLogger("fs.Pyinotify-0-7-Monitor")

import pyinotify

import threading
import sys, traceback
import uuid
import socket
import os

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
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

        if str(pathMode) not in ['Flat', 'Follow', 'Recurse']:
            raise UnsupportedPathMode("Path Mode " + str(pathMode) + " not yet supported on this platform")

        recurse = False
        follow = False
        if str(pathMode) == 'Follow':
            recurse = True
            follow = True
        elif str(pathMode) == 'Recurse':
            recurse = True
        
        if str(eventType) not in ['Create']:
            raise UnsupportedEventType("Event Type " + str(eventType) + " not yet supported on this platform")
            
        self.proxy = proxy
            
        pathsToMonitor = pathString
        if pathsToMonitor == None:
            pathsToMonitor = pathModule.path.getcwd()

        wm = pyinotify.WatchManager()
        pr = ProcessEvent(id=monitorId, func=self.callback, wl=whitelist)
        self.notifier = pyinotify.ThreadedNotifier(wm, pr)
        
        if str(eventType) == 'Create':
            wm.add_watch(pathsToMonitor, (pyinotify.IN_CLOSE_WRITE | pyinotify.IN_MOVED_TO), rec=recurse, auto_add=follow)
            log.info('Monitor set-up on =' + str(pathsToMonitor))


    def callback(self, id, eventPath):
        """
            Callback used by ProcessEvent methods
        
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
        log.debug('Event notification on monitor id=' + monitorId + ' => ' + str(eventList))
        self.proxy.callback(monitorId, eventList)

                
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
        self.notifier.start()
        
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

    def __init__(self, **kwargs):
        pyinotify.ProcessEvent.__init__(self)
        self.callback = kwargs['func'] 
        self.id = kwargs['id']
        self.whitelist = kwargs['wl']

    def process_IN_CLOSE_WRITE(self, event):
        # Explicitely registered for this kind of event.
        log.info("Raw pyinotify event = %s", str(event))
        try:
            if event.name:
                pathname = os.path.join(event.path, event.name)
            else:
                pathname = event.path
            if pathModule.path(pathname).ext in self.whitelist:
                self.callback(self.id, pathname)
        except:
            log.exception("Failed to process event: ")

    def process_IN_MOVED_TO(self, event):
        # Explicitely registered for this kind of event.
        log.info("Raw pyinotify event = %s", str(event))
        try:
            if event.name:
                pathname = os.path.join(event.path, event.name)
            else:
                pathname = event.path
            if pathModule.path(pathname).ext in self.whitelist:
                self.callback(self.id, pathname)
        except:
            log.exception("Failed to process event: ")


    def process_default(self, event):
        # Implicitely IN_CREATE and IN_DELETE are watched. They are
        # quietly ignored at the present time.
        pass
	

