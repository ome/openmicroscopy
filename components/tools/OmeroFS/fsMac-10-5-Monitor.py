"""
    OMERO.fs Monitor module for Mac Os.


"""
import logging
log = logging.getLogger("fsserver."+__name__)

from Foundation import NSAutoreleasePool, NSMutableArray, NSString
import FSEvents

import threading
import sys, traceback
import uuid
import socket

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
import path as pathModule

from omero.grid import monitors
from fsAbstractPlatformMonitor import AbstractPlatformMonitor
import fsDirectory

class UnsupportedPathMode(Exception):
    pass

class UnsupportedEventType(Exception):
    pass

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
        
        #: an FSEvents.FSEventStream StreamRef object reference.
        self.streamRef = None
        #: FSEvents.CFRunLoop object reference.
        self.runLoopRef = None
        self.clientInfo = str(uuid.uuid1())
        #
        # Without using the mutable array, ie using the Python list directly, 
        # the code works but throws up a couple of horrible warnings:
        # "Oject of class OC_PythonArray autoreleased with no pool in place 
        #     - just leaking"
        # With the array there are still warnings about the strings whether 
        # Python native strings are used or NSStrings.
        #
        # All of these warnings are eliminated by using a pool for the lifetime 
        # of the NSMutableArray.
        #
        pool = NSAutoreleasePool.alloc().init()
        pathsToMonitor = NSMutableArray.alloc().init()
        ms = NSString.stringWithString_(self.pathsToMonitor)
        pathsToMonitor.insertObject_atIndex_(ms, 0)

        self.directory = fsDirectory.Directory(pathString=self.pathsToMonitor, 
                                            whitelist=self.whitelist,
                                            pathMode=self.pathMode)

        self.streamRef = FSEvents.FSEventStreamCreate(FSEvents.kCFAllocatorDefault,
                                self.callback,
                                self.clientInfo,
                                pathsToMonitor,
                                FSEvents.kFSEventStreamEventIdSinceNow,
                                1,
                                FSEvents.kFSEventStreamCreateFlagWatchRoot)
 
        #
        # Release the pool now that the NSMutableArray has been used.
        #
        del pool
             
        if self.streamRef == None:
            raise Exception('Failed to create FSEvent Stream')

    def run(self):
        """
            Start monitoring an FSEventStream.
   
            This method, overridden from Thread, is run by 
            calling the inherited method start(). The method attempts
            to schedule an FSEventStream and then run its CFRunLoop.
            The method then blocks until stop() is called.
            
            :return: No explicit return value.
            
        """
        FSEvents.FSEventStreamScheduleWithRunLoop(self.streamRef, 
                        FSEvents.CFRunLoopGetCurrent(), 
                        FSEvents.kCFRunLoopDefaultMode)
 
        self.runLoopRef = FSEvents.CFRunLoopGetCurrent()
 
        if not FSEvents.FSEventStreamStart(self.streamRef):
            raise Exception('Failed to start the FSEventStream')

        # Blocks
        FSEvents.CFRunLoopRun()

    def stop(self):        
        """
            Stop monitoring an FSEventStream.
   
            This method attempts to stop the CFRunLoop. It then
            stops, invalidates and releases the FSEventStream.
            
            There should be a more robust approach in here that 
            still kils the thread even if the first call fails.
            
            :return: No explicit return value.
            
        """
        FSEvents.CFRunLoopStop(self.runLoopRef)
        FSEvents.FSEventStreamStop(self.streamRef)
        FSEvents.FSEventStreamInvalidate(self.streamRef)
        FSEvents.FSEventStreamRelease(self.streamRef)

    def callback(self, streamRef, clientInfo, numEvents, 
                    eventPaths, eventMasks, eventIDs):
        """
            Callback required by FSEvents.FSEventStream.

            :Parameters:
                streamRef : StreamRef
                    A reference to the FSEvents.FSEventStream.

                clientInfo : string
                    A string passed to the FSEvents.FSEventStream when
                    it is created that is passed back as an identifier.

                numEvents : int
                    Number of separate events in this callback.

                eventPaths : list<string>
                    Directory paths of the events (not files!).

                eventMasks : list<int>
                    Masks of event types (including exceptional events.)

                eventIDs : list<long>
                    Sequential IDs of the events.

            :return: No explicit return value.

        """

        # Use the returned client info to access the proxy and
        # the directory snapshot.
        if self.clientInfo == clientInfo:
                    
            dir = self.directory

            # For each event get a list of new, deleted and changed files.
            # Then process those lists to create a list of new non-zero files
            # to be forwarded to the client (proxy).
            for i in range(0, numEvents):
            
                new, old, chg = dir.getChangedFiles(eventPaths[i])

                log.info("Event set : %s", str(i))
                log.info("New files     : %s", str(new))
                log.info("Changed files : %s", str(chg))
                log.info("Old files 1   : %s", str(old))
                # The new and changed files are only potentially new. In both cases
                # zero-sized files are files that have been opened for writing but
                # not written to and closed. It is possible that a zero-sized file
                # has been created genuinely. Such files are unlikely to be of 
                # interest! --- Famous last words.

                # So, the following two methods prune out zero-sized files. This is
                # preferable to having the getChangedFiles() method return pruned
                # lists.
            
                # Files that are new but are not zero-sized. The are likely to 
                # include files that have been moved, renamed or copied.
                #if len(new) > 0: 
                #    new = dir.pruneZeroFiles(new)
                #log.info("New files 2: %s", str(new))
                
                # Files that have changed but are not zero-sized. These 
                # could include new files or files that have had their size, ctime,
                # etc, changed.
                #if len(chg) > 0:
                #    new.extend(dir.pruneZeroFiles(chg))    
                #log.info("New files 3: %s", str(new))
                      
                # Prune out new directories, those are not of interest.
                if self.ignoreDirEvents:
                    if len(new) > 0: 
                        new = dir.pruneDirectories(new)
                    if len(chg) > 0: 
                        chg = dir.pruneDirectories(chg)
                #log.info("New files 4: %s", str(new))
            
                # Prune out 'system files if necessary.
                # (this should be buried deeper, ie they shouldn't even be part of the underlying tree)
                if self.ignoreSysFiles:
                    
                    if len(new) > 0:
                        for fileName in new:
                            try:
                                fileName.index('untitled folder')
                                new.remove(fileName)
                            except:
                                pass
                            try:
                                if pathModule.path(fileName).basename().index('.') == 0:
                                    new.remove(fileName)
                            except:
                                pass

                    if len(chg) > 0:
                        for fileName in chg:
                            try:
                                fileName.index('untitled folder')
                                chg.remove(fileName)
                            except:
                                pass
                            try:
                                if pathModule.path(fileName).basename().index('.') == 0:
                                    chg.remove(fileName)
                            except:
                                pass

                    if len(old) > 0:
                        for fileName in old:
                            try:
                                fileName.index('untitled folder')
                                old.remove(fileName)
                            except:
                                pass
                            try:
                                if pathModule.path(fileName).basename().index('.') == 0:
                                    old.remove(fileName)
                            except:
                                pass
                    
                # If there any new files then tell the client.
                eventList = []

                if "Creation" in self.eTypes:
                    if len(new) > 0:
                        for fileName in new:
                            eventType = monitors.EventType.Create
                            eventList.append((fileName,eventType))
                        
                if "Modification" in self.eTypes:
                    if len(chg) > 0:
                        for fileName in chg:
                            eventType = monitors.EventType.Modify
                            eventList.append((fileName,eventType))
                        
                if "Deletion" in self.eTypes:
                    if len(old) > 0:
                        for fileName in old:
                            eventType = monitors.EventType.Delete
                            eventList.append((fileName,eventType))
                
                if len(eventList) > 0:
                    log.info('Event notification => %s', str(eventList))
                    self.proxy.callback(eventList)
                        
        else:
            log.info("Notification not for this monitor : %s != %s", self.clientInfo, clientInfo)
                    