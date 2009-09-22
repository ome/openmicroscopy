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

from omero.grid import monitors

import win32file
import win32con

class PlatformMonitor(object):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def setUp(self, eventTypes, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, monitorId, proxy):
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
        self.eventTypes = str(eventTypes)
        # This dictionary should be thinned depending on the above list
        self.actions = {
            1 : monitors.EventType.Create, # Created
            2 : monitors.EventType.Delete, # Deleted
            3 : monitors.EventType.Modify, # Updated
            4 : monitors.EventType.Modify, # Renamed to something
            5 : monitors.EventType.Modify  # Renamed from something
            }

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
            
            Callback on file events.
            
        """

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

            eventList = []
            for action, file in results:
                log.debug("Event : " + str(results))
                # At the moment this gets around 'New Folder' appearing and then changing
                if action in self.actions.keys():
                    # Ignore default name for GUI created folders.
                    if self.ignoreSysFiles and file.find('New Folder') >= 0:
                        pass # ignore New Folder event.
                    else:
                        eventType = self.actions[action]
                        filename = os.path.join(self.pathsToMonitor, file)
                        # Should have richer filename matching here.
                        if (len(self.whitelist) == 0) or (pathModule.path(filename).ext in self.whitelist):
                            eventList.append((filename.replace('\\\\','\\').replace('\\','/'), eventType))
                            
            if len(eventList) > 0:
                try:
                    log.info('Event notification on monitor id=' + self.idString + ' => ' + str(eventList))
                    self.proxy.callback(self.idString, eventList)
                except:
                    log.exception("Failed to make callback: ")


