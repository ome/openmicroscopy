"""
    OMERO.fs Monitor module for Window XP.


"""
import logging
import fsLogger
log = logging.getLogger("fsserver."+__name__)

import threading
import os, sys, traceback
import uuid

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid clashes.
import path as pathModule

import monitors

import win32file
import win32con

class PlatformMonitor(object):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def setUp(self, eventType, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, monitorId, proxy):
        """
            Initialise Monitor thread.
                        
            :Parameters:
                pathsString : string
                    The *single* path string of
                    interest.
                    
                idString : string
                    A unique id passed to that is
                    returned in the callback.
         
                event : Event
                    A threading.Event used to terminate the watch.
         
        """
        self.eventType = str(eventType)
        self.recurse = not (str(pathMode) == "Flat")
        self.pathsToMonitor = pathString
        self.whitelist = whitelist
        self.blacklist = blacklist
        self.ignoreSysFiles = ignoreSysFiles
        self.idString = monitorId
        self.proxy = proxy

        self.event = threading.Event()
        log.debug('Monitor set-up on =' + str(self.pathsToMonitor))
                
    def start(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
            
        """

        # Blocks
        self.watch() # for now
        
    def stop(self):        
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        self.event.set()
        
    def watch(self):
        """
            Create a monitor on created files.
            
        """
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
                4096,
                self.recurse, # recurse
                win32con.FILE_NOTIFY_CHANGE_FILE_NAME |
                win32con.FILE_NOTIFY_CHANGE_DIR_NAME |
                win32con.FILE_NOTIFY_CHANGE_ATTRIBUTES |
                win32con.FILE_NOTIFY_CHANGE_SIZE |
                win32con.FILE_NOTIFY_CHANGE_LAST_WRITE |
                win32con.FILE_NOTIFY_CHANGE_SECURITY,
                None,
                None)

            for action, file in results:
                log.debug("Event : " + str(results))
                # At the moment this gets around 'New Folder' appearing and then changing
                if action in (1,5) :
                    # Ignore default name for GUI created folders.
                    if file.find('New Folder') == -1 :
                        filename = os.path.join(self.pathsToMonitor, file)
                        try:
                            if (len(self.whitelist) == 0) or (pathModule.path(filename).ext in self.whitelist):
                                self.callback(self.idString, filename)
                        except:
                            log.exception("Failed to make callback: ")

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
        eventList.append((eventPath.replace('\\\\','\\').replace('\\','/'),eventType))  
        log.info('Event notification on monitor id=' + monitorId + ' => ' + str(eventList))
        self.proxy.callback(monitorId, eventList)
