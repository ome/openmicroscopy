#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs FileServer module.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging
import sys, traceback

try:
    from hashlib import sha1 as sha
except:
    from sha import sha

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
import path as pathModule

import omero.all
import omero.grid.monitors as monitors

class FileServerI(monitors.FileServer):
    """
        Provides access to the local file system
        
        :group Constructor: __init__
        :group Methods exposed in Slice: getDirectory, getBaseName, getStats, getSize,
            getOwner, getCTime, getATime, getMTime, isDir, isFile, getSHA1, readBlock
        :group Other methods: _getPathString
        
    """
    def __init__(self):
        """
            Intialise the instance variables and logging.
        
        """
        self.log = logging.getLogger("fsserver."+__name__)                            

    """
        Methods published in the slice interface omerofs.ice
    
    """

    def fileExists(self, fileId, current=None):
        """
            Return true if fileId represents a file or directory that exists.
        
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                  
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE interface method.
                       
            :return: isfile
            :rtype: bolean
     
        """
        try:
            pathString = self._getPathString(fileId)
        except Exception, e:
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            isfile = pathModule.path(pathString).isfile
            return True
        except Exception, e:
            return False
            
            
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
            self.log.error('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))
            raise omero.OmeroFSError(reason='Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))       
    
        fullList = []
        for d in dirList:
            fullList.append(d.name + '/')
        for f in fileList:
            fullList.append(f.name)
    
        return fullList

    def getBulkDirectory(self, absPath, filter, current=None):
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
                       
            :return: a list of files stats.
            :rtype: list<monitors.FileStats>

        """

        if filter == "": filter = "*"
        fullPath = pathModule.path(absPath)
        try:
            dirList = fullPath.dirs(filter)
            fileList = fullPath.files(filter)
        except Exception, e:
            self.log.error('Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))
            raise omero.OmeroFSError(reason='Unable to get directory of  ' + str(fullPath) + ' : ' + str(e))       
    
        fullList = []
        try:
            for d in dirList:
                fullList.append(self._getFileStats(d.name))
            for f in fileList:
                fullList.append(self._getFileStats(f.name))
        except Exception, e:
            self.log.error('Failed to get file stats : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to getfile stats : '  + str(e))       
    
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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
    
        try:
            name = pathModule.path(pathString).name
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' base name : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' base name : ' + str(e))       

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            return self._getFileStats(pathString)
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' stats : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' stats : '  + str(e))       
    

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            size = pathModule.path(pathString).size
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' size : ' +  str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' size : '  + str(e))       

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            owner = pathModule.path(pathString).owner
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' owner : '  + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' owner : ' +  str(e))       

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            ctime = pathModule.path(pathString).ctime
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' ctime : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' ctime : ' + str(e))       

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            mtime = pathModule.path(pathString).mtime
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' mtime : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' mtime : ' + str(e))       

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            atime = pathModule.path(pathString).atime
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' atime : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' atime : ' + str(e))       
    
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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            isdir = pathModule.path(pathString).isdir
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' isdir : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' isdir : ' + str(e))       
    
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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
   
        try:
            isfile = pathModule.path(pathString).isfile
        except Exception, e:
            self.log.error('Failed to get  ' + str(fileId) + ' isfile : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to get  ' + str(fileId) + ' isfile : ' + str(e))       
    
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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       
    
        try:
            sha1 = self._getSHA1(pathString)
        except Exception, e:
            self.log.error('Failed to get SHA1 digest  ' + pathString + ' : ' + str(e))
            raise omero.OmeroFSError(reason=str(e))       

        return sha1
        

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
            self.log.error('File ID  ' + str(fileId) + ' not on this FSServer')
            raise omero.OmeroFSError(reason='File ID  ' + str(fileId) + ' not on this FSServer')       

        try:
            file = open(pathString, 'rb')
            file.seek(offset)
            bytes = file.read(size)
            file.close()
        except Exception, e:
            self.log.error('Failed to read data from  ' + str(fileId) + ' : ' + str(e))
            raise omero.OmeroFSError(reason='Failed to read data from  ' + str(fileId) + ' : ' + str(e))       
    
        return bytes      
       
    def _getFileStats(self, pathString):
        """
            Private method to get stats for a file
            
            :Parameters:
                fileId : string
                    A string uniquely identifying a file on this Monitor.
                  
            :return: stats
            :rtype: monitors.FileStats
     
        """
        stats = monitors.FileStats()
        
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
                    
        return stats
        

    def _getSHA1(self, pathString):
        """docstring for _getSHA1"""
        try:
            file = open(pathString, 'rb')
        except Exception, e:
            self.log.error('Failed to open file ' + pathString + ' : ' + str(e))
            raise Exception('Failed to open file ' + pathString + ' : ' + str(e))       

        digest = sha()
        try:
            try:
                block = file.read(1024)
                while block:
                    digest.update(block)
                    block = file.read(1024)
            except Exception, e:
                self.log.error('Failed to SHA1 digest file ' + pathString + ' : ' + str(e))
                raise Exception('Failed to SHA1 digest file ' + pathString + ' : ' + str(e))       
        finally:
            file.close()

        return digest.hexdigest()
   
    def _getPathString(self, fileId):
        """
            docstring for _getPathString
        
            This will eventually separate the omero-fs url from the path string
            and return the path string if the url is for this FS instance otherwise
            an exception will be raised.
            
        """
        return fileId

