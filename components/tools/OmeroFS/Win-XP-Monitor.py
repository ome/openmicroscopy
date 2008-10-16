"""
    OMERO.fs Monitor module for Window XP.


"""

import threading
import os, sys, traceback
import uuid

# logging
from logger import log

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid clashes.
import path as pathModule

import win32file
import win32con

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
        self.pathsToMonitor = pathsToMonitor
        self.callback = callback
        self.idString = idString
        self.event = threading.Event()
                
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
        self.watch() # for now
        
    def stop(self):        
        """
            Stop monitoring an FSEventStream.
   
            This method attempts to stop the CFRunLoop. It then
            stops, invalidates and releases the FSEventStream.
            
            There should be a more robust approach in here that 
            still kils the thread even if the first call fails.
            
            :return: No explicit return value.
            
        """
        self.event.set()
        
    def watch(self):
        ACTIONS = {
            1 : "Created",
            2 : "Deleted",
            3 : "Updated",
            4 : "Renamed to something",
            5 : "Renamed from something" }

        FILE_LIST_DIRECTORY = 0x0001
        
        hDir = win32file.CreateFile (
            self.pathsToMonitor,
            FILE_LIST_DIRECTORY,
            win32con.FILE_SHARE_READ | win32con.FILE_SHARE_WRITE,
            None,
            win32con.OPEN_EXISTING,
            win32con.FILE_FLAG_BACKUP_SEMANTICS,
            None)
            
        while not self.event.isSet():
            results = win32file.ReadDirectoryChangesW (
                hDir,
                1024,
                False, # recurse!
                win32con.FILE_NOTIFY_CHANGE_FILE_NAME | 
                win32con.FILE_NOTIFY_CHANGE_DIR_NAME |
                win32con.FILE_NOTIFY_CHANGE_ATTRIBUTES |
                win32con.FILE_NOTIFY_CHANGE_SIZE |
                win32con.FILE_NOTIFY_CHANGE_LAST_WRITE |
                win32con.FILE_NOTIFY_CHANGE_SECURITY,
                None,
                None)
          
            for action, file in results:
                if action == 1:
                    filename = os.path.join(self.pathsToMonitor, file)
                    log.info("Event : "+ filename)
                    self.callback(self.idString, filename)
