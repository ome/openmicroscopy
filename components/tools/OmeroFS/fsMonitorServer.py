"""
    OMERO.fs MonitorServer module.


"""
import logging
import fsLogger
log = logging.getLogger("fsserver."+__name__)

import sys, traceback
import uuid
import socket

try:
    from hashlib import sha1 as sha
except:
    from sha import sha

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
import path as pathModule

from fsMonitor import MonitorFactory

from omero.grid import monitors

class MonitorServerI(monitors.MonitorServer):
    """
        Co-ordinates a number of Monitors
        
        :group Constructor: __init__
        :group Methods exposed in Slice: createMonitor, startMonitor, stopMonitor, destroyMonitor,
            getMonitorState, getMonitorDirectory, getDirectory, getBaseName, getStats, getSize,
            getOwner, getCTime, getATime, getMTime, isDir, isFile, getSHA1, readBlock
        :group Other methods: _getNextMonitorId, _getPathString, callback
        
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
        #: Dictionary of MonitorClientI proxies by Id
        self.proxies = {}
        #: Dictionary of fileId proxies by sha1
        self.sha1ToFileId = {}
                    
        

    """
        Methods published in the slice interface omerofs.ice
    
    """
    def createMonitor(self, mType, eTypes, pMode, pathString, whitelist, blacklist, proxy, timeout, ignoreSysFiles, current=None):
        """
            Create a the Monitor for a given path.
        
            :Parameters:
                mType : 
                    The type of monitor.
                    
                eTypes : 
                    A list of the event types to be monitored.          
                    
                pathString : string
                    A string representing a path to be monitored.
                  
                whitelist : list<string>
                    A list of extensions of interest.
                    
                blacklist : list<string>
                    A list of subdirectories to be excluded.

                pMode : 
                    The mode of directory monitoring: flat, recursive or following.

                proxy :
                    A proxy to be informed of events
                    
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                       
            :return: Monitor Id.
            :rtype: string
            
        """
     
        if pathString == None:
            pathString = pathModule.path.getcwd()

        monitorId = self._getNextMonitorId()

        try:
            self.monitors[monitorId] = MonitorFactory.createMonitor(mType, eTypes, pMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, self, monitorId)
        except Exception, e:
            log.exception('Failed to create monitor: ')
            raise monitors.OmeroFSError('Failed to create monitor: ' + str(e))       

        self.proxies[monitorId] = proxy    

        log.info('Monitor id = ' + monitorId + ' created. Proxy: ' + str(proxy))

        return monitorId
    

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
        except Exception, e:
            log.error('Monitor id = ' + id + ' failed to start: ' + str(e))
            raise monitors.OmeroFSError('Monitor id = ' + id + ' failed to start: ' + str(e))       
        

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
        except Exception, e:
            log.error('Monitor id = ' + id + ' failed to stop: ' + str(e))
            raise monitors.OmeroFSError('Monitor id = ' + id + ' failed to stop: ' + str(e))       


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
            log.info('Monitor id = ' + id + ' destroyed')
        except Exception, e:
            log.error('Monitor id = ' + id + ' not destroyed: ' + str(e))
            raise monitors.OmeroFSError('Monitor id = ' + id + ' not destroyed: ' + str(e))       

    def getMonitorState(self, id):
        """
            Get the state of a monitor.
            
            Return the state of an existing monitor.
            Raise an exception if the monitor does no exist.
        
        """
        log.info('Monitor id = ' + id + ' state requested')
        # *****  TO BE IMPLEMENTED  *****
        # If monitor exists return state
        # otherwise raise an exception (no subscription).
        # (and ICE exception implies no server)
        
        raise monitors.OmeroFSError('Method not yet implemented.')
        
        
    def getMonitorDirectory(self, id, relPath, filter, current=None):
        """
            Get a list of subdirectories and files in a directory.
        
            :Parameters:
                id : string
                    A string uniquely identifying a Monitor.
                  
                relPath : string
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
        fullPath = pathModule.path(self.monitors[id].getPathString()) / relPath
        try:
            dirList = fullPath.dirs(filter)
            fileList = fullPath.files(filter)
        except Exception, e:
            log.error('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))
            raise monitors.OmeroFSError('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))       

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
        try:
            dirList = fullPath.dirs(filter)
            fileList = fullPath.files(filter)
        except Exception, e:
            log.error('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))
            raise monitors.OmeroFSError('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))       
    
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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       
    
        try:
            name = pathModule.path(pathString).name
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' base name : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' base name : ' + str(e))       

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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        stats = monitors.FileStats()
        
        try:
            stats.baseName = pathModule.path(pathString).name
            stats.owner = pathModule.path(pathString).owner
            stats.size = pathModule.path(pathString).size
            stats.mTime = pathModule.path(pathString).mtime
            stats.cTime = pathModule.path(pathString).ctime
            stats.aTime = pathModule.path(pathString).atime
            if pathModule.path(pathString).isfile():
                stats.type = monitors.FileType.File
            elif pathModule.path(pathString).isdir():
                stats.type = monitors.FileType.Dir
            elif pathModule.path(pathString).islink():
                stats.type = monitors.FileType.Link
            elif pathModule.path(pathString).ismount():
                stats.type = monitors.FileType.Mount
            else:
                stats.type = monitors.FileType.Unknown
                    
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' stats : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' stats : '  + str(e))       
    
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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            size = pathModule.path(pathString).size
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' size : ' +  str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' size : '  + str(e))       

        return size
    

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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            owner = pathModule.path(pathString).owner
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' owner : '  + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' owner : ' +  str(e))       

        return owner
    

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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            ctime = pathModule.path(pathString).ctime
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' ctime : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' ctime : ' + str(e))       

        return ctime


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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            mtime = pathModule.path(pathString).mtime
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' mtime : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' mtime : ' + str(e))       

        return mtime
    

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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            atime = pathModule.path(pathString).atime
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' atime : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' atime : ' + str(e))       
    
        return atime


    def isDir(self, fileId, current=None):
        """
            Return true if fileId represents a directory.
        
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                  
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                       
            :return: atime
            :rtype: float
     
        """
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            isdir = pathModule.path(pathString).isdir
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' isdir : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' isdir : ' + str(e))       
    
        return isdir


    def isFile(self, fileId, current=None):
        """
            Return true if fileId represents a file.
        
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                  
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                       
            :return: isfile
            :rtype: float
     
        """
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            isfile = pathModule.path(pathString).isfile
        except Exception, e:
            log.error('Failed to get  ' + str(fileId) + ' isfile : ' + str(e))
            raise monitors.OmeroFSError('Failed to get  ' + str(fileId) + ' isfile : ' + str(e))       
    
        return isfile
        
    def getSHA1(self, fileId, current=None):
        """
            Calculates the local sha1 for a file.
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.

                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                  
                    :return: sha1 hexdigest
                    :rtype: string
                    
        """
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       
    
        try:
            sha1 = self._getSHA1(pathString)
        except Exception, e:
            log.error('Failed to get SHA1 digest  ' + pathString + ' : ' + str(e))
            raise monitors.OmeroFSError(str(e))       

        return sha1
        
    def getFileId(self, sha1, current=None):
        """
            Returns the fileID given a sha1 digest for a file.

            :Parameters:
                sha1 : string
                    A sha 1 hexdigest.

                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.

                    :return: fileId
                    :rtype: string

        """
        pass
        

    def readBlock(self, fileId, offset, size, current=None):
        """
            Return an at most size block of bytes from offset.
        
            :Parameters:
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
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise monitors.OmeroFSError('File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            file = open(pathString, 'rb')
            file.seek(offset)
            bytes = file.read(size)
            file.close()
        except Exception, e:
            log.error('Failed to read data from  ' + str(fileId) + ' : ' + str(e))
            raise monitors.OmeroFSError('Failed to read data from  ' + str(fileId) + ' : ' + str(e))       
    
        return bytes      
       
    def _getSHA1(self, pathString):
        """docstring for _getSHA1"""
        try:
            file = open(pathString, 'rb')
        except Exception, e:
            log.error('Failed to open file ' + pathString + ' : ' + str(e))
            raise Exception('Failed to open file ' + pathString + ' : ' + str(e))       

        digest = sha()
        try:
            try:
                block = file.read(1024)
                while block:
                    digest.update(block)
                    block = file.read(1024)
            except Exception, e:
                log.error('Failed to SHA1 digest file ' + pathString + ' : ' + str(e))
                raise Exception('Failed to SHA1 digest file ' + pathString + ' : ' + str(e))       
        finally:
            file.close()

        return digest.hexdigest()


   
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
        
    def _getPathString(self, fileId):
        """
            docstring for _getPathString
        
            This will eventually separate the omero-fs url from the path string
            and return the path string if the url is for this FS instance otherwise
            an exception will be raised.
            
        """
        return fileId

    def callback(self, monitorId, fileList):
        """
            Callback required by FSEvents.FSEventStream.
        
            :Parameters:

                    
            :return: No explicit return value.
            
        """

        eventList = []
        for fileEvent in fileList:
            info = monitors.EventInfo(fileEvent[0],fileEvent[1])
            eventList.append(info)
              
        proxy = self.proxies[monitorId]
        
        try:
            log.info('Event notification on monitor id=' + monitorId + ' => ' + str(eventList))
            proxy.fsEventHappened(monitorId, eventList)
        except Exception, e:
            log.info('Callback to monitor id=' + monitorId + ' failed. Reason: ' + str(e))


