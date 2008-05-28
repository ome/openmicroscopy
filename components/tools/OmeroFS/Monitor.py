"""
    OMERO.fs Monitor module.


"""

from Foundation import NSAutoreleasePool, NSMutableArray, NSString
import FSEvents
import threading
import sys, traceback

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
        self.monitors[id].start()
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
        self.monitors[id].stop()
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
        del self.monitors[id]
        del self.proxies[id]
        del self.directory[id]
        return True
        
    def getDirectory(self, id, plusPath, filter, current=None):
        """
            Get a list of subdirectories and files in a directory.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                pathPlus : string
                    A path to be added to the base directory path.

                filter : string
                    A pattern to filter the listing (cf. ls).
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: a list of files.
            :rtype: list<string>
         
        """
        
        if filter == "": filter = "*"
        fullPath = self.directory[id].getPath() / plusPath
        dirList = fullPath.dirs(filter)
        fileList = fullPath.files(filter)
        
        fullList = []
        for d in dirList:
            fullList.append(d.name + '/')
        for f in fileList:
            fullList.append(f.name)
        
        return fullList

    def getBaseName(self, id, fileId, current=None):
        """
            Return an at most size block of bytes from offset.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: Data.
            :rtype: list<byte>
         
        """
        
        return pathModule.path(fileId).name
        
    def getSize(self, id, fileId, current=None):
        """
            Return the size of the file.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: size of file in bytes
            :rtype: long integer
         
        """
        
        return pathModule.path(fileId).size
        
    def getOwner(self, id, fileId, current=None):
        """
            Return the owner of the file.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: owner
            :rtype: string
         
        """
        
        return pathModule.path(fileId).owner
        
    def getCTime(self, id, fileId, current=None):
        """
            Return the ctime of the file.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: ctime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).ctime
        
    def getMTime(self, id, fileId, current=None):
        """
            Return the mtime of the file.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: mtime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).mtime
        
    def getATime(self, id, fileId, current=None):
        """
            Return the atime of the file.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                      
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                           
            :return: atime
            :rtype: float
         
        """
        
        return pathModule.path(fileId).atime
        
        
    def readBlock(self, id, fileId, offset, size, current=None):
        """
            Return an at most size block of bytes from offset.
            
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                      
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                
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
    
    
    def getNextMonitorId(self):
        """
            Return next monitor ID and increment.
            
            The monitorID is a unique key to identify a monitor on the
            file system. In the present implementation this is simply 
            a string created from an integer. However, in a system with
            multiple file system servers the key might need to have 
            some additional hash key added in.
            
            :return: Next monitor Id
            :rtype: string
            
        """
        self.monitorId += 1    
        return str(self.monitorId)
        

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

        monitorId = self.getNextMonitorId()
        self.proxies[monitorId] = proxy    
        self.monitors[monitorId] = Monitor(ma, self.callback, monitorId)
        self.directory[monitorId] = Directory.Directory(pathString=pathString, 
                                            whitelist=wl,
                                            blacklist=bl)
        #
        # Release the pool now that the NSMutableArray has been used.
        #
        del pool     

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
                try:
                    proxy.fsEventHappened(monitorId, new)
                except Exception, e:
                    self.stopMonitor(monitorId)
                    self.destroyMonitor(monitorId)
                    print 'Proxy id=' + monitorId+ ' lost.\n Reason: ' + str(e)



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
        

