"""
    OMERO.fs Monitor module.


"""

from Foundation import NSAutoreleasePool, NSMutableArray, NSString
import FSEvents
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
import Directory

class MonitorServerImpl(monitors.MonitorServer):
    """
        Co-ordinates the a number of 
        
        :group Constructor: __init__
        :group Methods exposed in Slice: createMonitor, startMonitor, stopMonitor, destroyMonitor
        :group Other methods: getNextMonitorId, callback
        
    """
    def __init__(self):
        """
            Intialise the instance variables.
        
        """
        #self.fsPrefix = 'omero-fs://' + socket.gethostbyname(socket.gethostname())
        #self.prefixLne = len(self.fsPrefix) ## Save calculating it each time?
        #: Numerical component of a Monitor Id
        self.monitorId = 0
        #: Dictionary of Monitors by Id
        self.monitors = {}
        #: Dictionary of Directories by Id
        self.directory = {}
        #: Dictionary of FSEvent StreamRefs by Id
        self.streamRefs = {}
        #: Dictionary of MonitorClientI proxies by Id
        self.proxies = {}

        
    def startMonitor(self, id, current=None):
        """
            Start the Monitor with the given Id.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Success status.
            :rtype: boolean
                
        """
        try:
            self.monitors[id].start()
            log.info('Monitor id = ' + id + ' started')
        except:
            return False ## eventually raise an exception
            
        return True

    def stopMonitor(self, id, current=None):
        """
            Stop the Monitor with the given Id.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Success status.
            :rtype: boolean
                
        """
        try:
            self.monitors[id].stop()
            log.info('Monitor id = ' + id + ' stopped')
        except:
            return False ## eventually raise an exception

        return True

    def destroyMonitor(self, id, current=None):
        """
            Destroy the Monitor with the given Id.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Success status.
            :rtype: boolean
                
        """
        try:
            del self.monitors[id]
            del self.proxies[id]
            del self.directory[id]
            log.info('Monitor id = ' + id + ' destroyed')
        except:
            return False ## eventually raise an exception
        
        return True
        
    def getMonitorDirectory(self, id, relPath, filter, current=None):
        """
            Get a list of subdirectories and files in a directory.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                relPlus : string
                    A relative path to be added to the base directory path.

                filter : string
                    A pattern to filter the listing (cf. ls).
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: a list of files.
            :rtype: list<string>
         
        """
        
        if filter == "": filter = "*"
        fullPath = self.directory[id].getPath() / relPath
        dirList = fullPath.dirs(filter)
        fileList = fullPath.files(filter)
        
        fullList = []
        for d in dirList:
            fullList.append(d.name + '/')
        for f in fileList:
            fullList.append(f.name)
        
        return fullList

    def getDirectory(self, absPath, filter, current=None):
        """
            Get a list of subdirectories and files in a directory.
            
            :Parameters:
                     
                absPath : string
                    An absolute path.

                filter : string
                    A pattern to filter the listing (cf. ls).
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: a list of files.
            :rtype: list<string>
         
        """
        
        if filter == "": filter = "*"
        fullPath = pathModule.path(absPath)
        dirList = fullPath.dirs(filter)
        fileList = fullPath.files(filter)
        
        fullList = []
        for d in dirList:
            fullList.append(d.name + '/')
        for f in fileList:
            fullList.append(f.name)
        
        return fullList

    def getBaseName(self, fileId, current=None):
        """
            Return the base names of the file, ie no path.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: base name
            :rtype: string
         
        """
        
        name = pathModule.path(fileId).name

        return name
        
        
    def getStats(self, fileId, current=None):
        """
            Return an at most size block of bytes from offset.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: stats
            :rtype: monitors.FileStats
         
        """
        stats = monitors.FileStats()
            
        stats.owner = pathModule.path(fileId).owner
        stats.size = pathModule.path(fileId).size
        stats.mTime = pathModule.path(fileId).mtime
        stats.cTime = pathModule.path(fileId).ctime
        stats.aTime = pathModule.path(fileId).atime

        return stats
        
    def getSize(self, fileId, current=None):
        """
            Return the size of the file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: size of file in bytes
            :rtype: long integer
         
        """
        
        return pathModule.path(fileId).size
        
    def getOwner(self, fileId, current=None):
        """
            Return the owner of the file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: owner
            :rtype: string
         
        """
        
        return pathModule.path(fileId).owner
        
    def getCTime(self, fileId, current=None):
        """
            Return the ctime of the file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: ctime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).ctime
        
    def getMTime(self, fileId, current=None):
        """
            Return the mtime of the file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: mtime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).mtime
        
    def getATime(self, fileId, current=None):
        """
            Return the atime of the file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: atime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).atime
        
        
    def readBlock(self, fileId, offset, size, current=None):
        """
            Return an at most size block of bytes from offset.
            
            :Parameters:
                offset : long integer
                    The byte position in the file to read from.
                    
                size : integer
                    The number of bytes to read.
                    
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Data.
            :rtype: list<byte>
         
        """

        file = open(fileId, 'rb')
        file.seek(offset)
        bytes = file.read(size)
        file.close()
        
        return bytes
    
    
    def _getNextMonitorId(self):
        """
            Return next monitor ID and increment.
            
            The monitorID is a unique key to identify a monitor on the
            file system. In the present implementation this is a string 
            generated by uuid.uuid1()
            
            :return: Next monitor Id
            :rtype: string
            
        """
        return str(uuid.uuid1())
        

    def createMonitor(self, pathString=None, 
                        wl=None, bl=None, 
                        proxy=None, current=None):
        """
            Create a the Monitor for a given path.
            
            :Parameters:
                pathString : string
                    A string representing a path to be monitored.
                      
                wl : list<string>
                    A list of extensions of interest.
                      
                bl : list<string>
                    A list of subdirectories not of interest.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Monitor Id.
            :rtype: string
                
        """
         
        if pathString == None:
            pathString = pathModule.path.getcwd()

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
        ma = NSMutableArray.alloc().init()
        ms = NSString.stringWithString_(pathString)
        ma.insertObject_atIndex_(ms, 0)

        monitorId = self._getNextMonitorId()
        self.proxies[monitorId] = proxy    
        self.monitors[monitorId] = Monitor(ma, self.callback, monitorId)
        self.directory[monitorId] = Directory.Directory(pathString=pathString, 
                                            whitelist=wl,
                                            blacklist=bl)
        #
        # Release the pool now that the NSMutableArray has been used.
        #
        del pool     
                    
        log.info('Monitor id = ' + monitorId + ' created')

        return monitorId
        

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
        monitorId = clientInfo        
        proxy = self.proxies[monitorId]
        dir = self.directory[monitorId]
        
        # For each event get a list of new, deleted and changed files.
        # Then process those lists to create a list of new non-zero files
        # to be forwarded to the client (proxy).
        for i in range(0, numEvents):
            
            new, old, chg = dir.getChangedFiles(
                                eventPaths[i], recurse=False)
            
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
            if len(new) > 0: 
                new = dir.pruneZeroFiles(new)
            
            # Files that have changed but are not zero-sized. These 
            # could include new files or files that have had their size, ctime,
            # etc, changed.
            if len(chg) > 0:
                new.extend(dir.pruneZeroFiles(chg))          
            
            # If there any new files then tell the client.
            if len(new) > 0:
                eventList = []
                for file in new:
                    info = monitors.FileInfo()
                    info.fileId = file
                    info.baseName = pathModule.path(file).name
                    info.size = pathModule.path(file).size
                    info.mTime = pathModule.path(file).mtime
                    eventList.append(info)
                try:
                    proxy.fsEventHappened(monitorId, eventList)
                    log.debug('Event notification on monitor id=' + monitorId + ' => ' + str(eventList))
                except Exception, e:
                    log.info('Proxy attached to monitor id=' + monitorId + ' lost. Reason: ' + str(e))
                    self.stopMonitor(monitorId)
                    self.destroyMonitor(monitorId)




class Monitor(threading.Thread):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, pathsToMonitor, callback, idString):
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
 
        #: an FSEvents.FSEventStream StreamRef object reference.
        self.streamRef = None
        #: FSEvents.CFRunLoop object reference.
        self.runLoopRef = None
        
        self.streamRef = FSEvents.FSEventStreamCreate(FSEvents.kCFAllocatorDefault,
                                callback,
                                idString,
                                pathsToMonitor,
                                FSEvents.kFSEventStreamEventIdSinceNow,
                                1,
                                FSEvents.kFSEventStreamCreateFlagWatchRoot)
 
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
        

