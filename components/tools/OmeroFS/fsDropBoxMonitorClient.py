"""
    OMERO.fs  DropBox implementation of a MonitorClient

    
"""
import logging
import fsLogger
log = logging.getLogger("fsclient."+__name__)

import string
import subprocess as sp
import os
import platform
import uuid
import threading
import path as pathModule

import omero
import omero.java

import Ice
import IceGrid
import Glacier2

import monitors
import fsConfig as config

class MonitorClientI(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.
        
        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.
        
    """

    def __init__(self):
        """
            Intialise the instance variables.
        
        """
        self.master = None
        #: Reference back to FSServer.
        self.serverProxy = None
        self.selfProxy = None
        self.dropBoxDir = ''
        #: Id
        self.id = ''
        #: dictionary of files onHold
        self.onHold = {}
        self.ometifHold = {}
        #: list of new directories yet to be imported.
        self.newDirs = set([])
        self.newDirFiles = {}
        self.newDirTimers = {}
        self.directoryImportWait = 0

    def fsEventHappened(self, id, eventList, current=None):
        """
            This is an example callback.
            
            If new files appear on the watch the list is sent as an argument.
            The id should match for the events to be relevant. In this simple 
            exmple the call back outputs the list of new files to stdout.
            
            At the moment each file type is treated as a special case. The number of
            special cases is likely to explode and so a different approach is needed.
            That will be easier with more knowledge of the different multi-file formats.
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Watch created
                    by the OMERO.fs Server.
                      
                eventList : list<string>
                    A list of events, in the current implementation this is 
                    a list of strings representing the full path names of new files.
                    
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE callback.
                           
            :return: No explicit return value.
            
        """
        ############### ! Set import to dummy mode for testing purposes.
        # self.importFile = self.dummyImportFile 
        ############### ! If the above line is not commented out nothing will import.
        if self.id == id:
            try:                       
                for fileInfo in eventList:
                    fileId = fileInfo.fileId
                    fileIn = self.fileInNewDir(fileId)
                    
                    # New file within an existing new directory.
                    if  fileIn != '/':
                        log.info("New file %s in %s", fileId, fileIn)
                        fileSet = self.getUsedFiles(pathModule.path(fileIn))
                        
                        if len(fileSet) > 0 :
                            self.newDirFiles[fileIn] = fileSet
                            # Cancel and "restart" the Timer.
                            if self.newDirTimers[fileIn].isAlive():
                                self.newDirTimers[fileIn].cancel()
                            self.newDirTimers[fileIn] = threading.Timer(self.dirImportWait, self.importDirectory, [fileIn] )
                            self.newDirTimers[fileIn].start()
                            log.info("Revised set on %s contains %d files", fileIn, len(self.newDirFiles[fileIn]) )
                    
                    # New directory      
                    elif pathModule.path(fileId).isdir():
                        log.info("New directory %s", fileId)
                        self.newDirs.add(fileId)
                        self.newDirFiles[fileId] = self.getUsedFiles(pathModule.path(fileId))
                        #Create and start a new Timer
                        self.newDirTimers[fileId] = threading.Timer(self.dirImportWait, self.importDirectory, [fileId] )
                        self.newDirTimers[fileId].start()
                        log.info("New set on %s contains %d files", fileId, len(self.newDirFiles[fileId]) )
                        
                    # New file at the top level.
                    else:
                        # import a file at the top level or in an 'old' directory.
                        log.info("New file *not* in new dir %s", fileId)
                        try:
                            exName = self.getExperimenterFromPath(fileId)
                            fileExt, fileName, fileBase = self.getBestGuessImporter(fileId)
                        
                            # Deal with jpg files
                            if fileExt == ".jpg":
                                self.importFile(fileId, exName)

                            # Deal with lsm files
                            elif fileExt == ".lsm":
                                self.importFile(fileId, exName)

                            # Deal with dv files and their logs
                            elif fileExt == ".dv":
                                if (fileName+".log", exName) in self.onHold.keys():
                                    self.onHold[(fileName+".log", exName)].cancel()
                                    self.onHold.pop((fileName+".log", exName))
                                    self.importFile(fileId, exName)
                                else:
                                    self.onHold[(fileName,exName)] = threading.Timer(config.waitTimes[".dv"], self.importAnyway, (fileId, exName))
                                    self.onHold[(fileName,exName)].start()
                            # Deal with log files and their dvs
                            elif fileExt == ".log":
                                if (fileBase, exName) in self.onHold.keys():
                                    self.onHold[(fileBase, exName)].cancel()
                                    self.onHold.pop((fileBase, exName))
                                    self.importFile(pathModule.path(fileId).parent + "/" + fileBase, exName)
                                else:
                                    self.onHold[(fileName,exName)] = threading.Timer(config.dropTimes[".dv"], self.ignoreFile, (fileName, exName))
                                    self.onHold[(fileName,exName)].start()

                            # Deal with ome.tif files
                            elif fileExt == ".tif" or fileExt == ".tiff":
                                if pathModule.path(fileBase).ext == ".ome":
                                    command = [config.climporter + " -f " + fileId]
                                    output = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True).communicate()
                                    files = output[0].splitlines()
                                    # Single file 
                                    if len(files) == 1:
                                        self.importFile(fileId, exName)
                                    # Multiple files
                                    else:
                                        fileList = []
                                        for line in files:
                                            fileList.append(pathModule.path(line).name)
                                        fileList.sort()
                                        omeKey = tuple(fileList)
                                        if omeKey in self.ometifHold.keys():
                                            self.ometifHold[omeKey].discard(fileName)
                                            log.info("File identified as part of %s", str(omeKey))
                                            if len(self.ometifHold[omeKey]) == 0:
                                                self.importFile(fileId, exName)
                                                self.ometifHold.pop(omeKey)    
                                        else:
                                            log.info("First file of %s",str(omeKey))
                                            self.ometifHold[omeKey] = set(fileList)
                                            self.ometifHold[omeKey].discard(fileName)
                                                                         
                            # Deal with all other notified file types.
                            else:
                                # ignore other file types for now.
                                log.info("File not imported: file type %s not currently handled.", fileExt)
                        except IndexError:
                            log.info("File not imported: file not copied into user level directory")
                        except:
                            log.exception("Import failed: ")
            except:
                log.exception("Failed to get or join session: ")              
        else:
            log.error("Unknown fs server id: %s", id)

    def fileInNewDir(self, fileId):
        """
            Is the file in one of the new and as yet unimported directories?
            
        """
        dirName = pathModule.path(fileId).dirname()
        while dirName != '/' and dirName not in self.newDirs:
            dirName = pathModule.path(dirName).dirname()
        
        return dirName
    
    def getBestGuessImporter(self, fileId):
        """
            For the moment return file details.
            
        """
        fileExt = pathModule.path(fileId).ext
        fileName = pathModule.path(fileId).name
        fileBase = pathModule.path(fileId).namebase
        
        return (fileExt, fileName, fileBase)
    
    def getExperimenterFromPath(self, fileId):
        """
            Extract experimenter name from path.
            
        """
    
        fileParts = fileId.split("/")
        base = fileParts.index(self.dropBoxDir)
        exName = fileParts[base+2]
        # The following line throws an exception if the file is
        # a level or more below the experimenter name level.
        try:
            fileParts[base+3] 
        except:
            raise
            
        return exName
        
            
    def importAnyway(self, fileName, exName):
        """
            Force the import of a dv with no accompanying log.
            
        """
        log.info("No accompanying file has appeared, importing primary file %s for user %s", fileName, exName)
        self.onHold.pop((pathModule.path(fileName).name, exName))
        self.importFile(fileName, exName)

    def ignoreFile(self, fileName, exName):
        """
            Remove a log file from onHold that has no accompanying dv.
            
        """
        log.info("No primary file has appeared, ignoring accompanying file %s for user %s", fileName, exName)
        self.onHold.pop((fileName, exName))

    def getUsedFiles(self, dirName):
        """
            Call importer with -f to get used files.
            
            Return a set of filepaths.
            
        """
        if platform.system() == 'Windows':
            # Windows requires bin/omero to be bin\omero
            climporter = config.climporter.replace('/','\\')
            # Awkward file names not yet handled.
            command = [climporter +
                        " -s " + config.host +
                        " -f " + dirName ]
        else:
            climporter = config.climporter
            # Wrap filename in single quotes, escape any ' characters first.
            # This deals with awkward file names (spaces, quotes, etc.)
            dirName = "'" + dirName.replace("'", r"'\''") + "'"
            command = [climporter +
                        " -s " + config.host +
                        " -f " + dirName ]
                        
        process = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
        output = process.communicate()
        retCode = process.returncode
        
        fileSet = set([])
        if retCode != 0:
            log.info("***** error from importer-cli -f *****")        
            log.info(output[1].split('\n')[0])
            log.info("***** end of output from importer-cli *****")
        else:
            # The 6: is a kludge here need to check what bin/omero import -f returns
            for line in output[0].split('\n')[6:]:
                if line.strip(string.whitespace) != '' and line[0] != '#':
                    fileSet.add(line)
        
        return fileSet
       
    def importDirectory(self, dirName):
        """
            Import a directory.
            
            Clear it from the new directory list.

        """
        try:
            exName = self.getExperimenterFromPath(dirName)
            self.newDirs.discard(dirName)
            self.newDirFiles.pop(dirName, True)
            self.newDirTimers.pop(dirName, True)
            self.importFile(dirName, exName)
        except IndexError:
            log.info("File not imported: file not copied into user level directory")
        except:
            log.exception("Import failed: ")
        

    def importFile(self, fileName, exName):
        """
            Import file or directory using 'bin/omero importer'
            
        """
        try:
            ic = Ice.initialize(["--Ice.Config=etc/internal.cfg"])
            query = ic.stringToProxy("IceGrid/Query")
            query = IceGrid.QueryPrx.checkedCast(query)
            blitz = query.findAllObjectsByType("::Glacier2::SessionManager")[0]
            blitz = Glacier2.SessionManagerPrx.checkedCast(blitz)
            sf = blitz.create("root", None, {"omero.client.uuid":str(uuid.uuid1())})
            sf = omero.api.ServiceFactoryPrx.checkedCast(sf)
            sf.detachOnDestroy()
            sf.destroy()
            sessionUuid = sf.ice_getIdentity().name

            root = omero.client(config.host, config.port)
            root.joinSession(sessionUuid) 

            exp = root.sf.getAdminService().lookupExperimenter(exName)
            if exName == exp._omeName._val:
                p = omero.sys.Principal()
                p.name  = exName
                p.group = "user"
                p.eventType = "Test"

                sess = root.sf.getSessionService().createSessionWithTimeout(p, 60000L)

                client = omero.client(config.host, config.port)
                user_sess = client.createSession(sess.uuid, sess.uuid)

                key = user_sess.getAdminService().getEventContext().sessionUuid
                log.info("Importing file using session key = %s", key)

                if platform.system() == 'Windows':
                    # Windows requires bin/omero to be bin\omero
                    climporter = config.climporter.replace('/','\\')
                    # Awkward file names not yet handled.
                    command = [climporter +
                                " -s " + config.host +
                                " -k " + key +
                                " " + fileName ]
                else:
                    climporter = config.climporter
                    # Wrap filename in single quotes, escape any ' characters first.
                    # This deals with awkward file names (spaces, quotes, etc.)
                    fileName = "'" + fileName.replace("'", r"'\''") + "'"
                    command = [climporter +
                                " -s " + config.host +
                                " -k " + key +
                                " " + fileName]
                                
                process = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
                output = process.communicate()
                retCode = process.returncode

                if retCode == 2:
                    # The file still being in use is one possible cause of retCode == 2 under
                    # Windows. At the moment it is impossible to know for sure so try once
                    # more and then log a failure. A better strategy may be possible with
                    # more error information passed on by bin/omero
                    log.warn("Import failed, possible cause file locking. Trying once more.")
                    log.info("Importing file using command = %s", command[0])                
                    process = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
                    output = process.communicate()
                    retCode = process.returncode

                if retCode == 0:
                    log.info("Import completed on session key = %s", key)
                else:
                    log.error("Import completed on session key = %s, return code = %s", key, str(retCode))
                    log.error("***** start of output from importer-cli to stderr *****")
                    for line in output[1].split('\n'):
                        log.error(line)
                    log.error("***** end of output from importer-cli *****")
                
            else:
                log.info("File not imported: user unknown: %s", exName)

        except:
            raise
               
    def dummyImportFile(self, fileName, exName):
        """
            Log a potential import for test purposes

        """
        log.info("***DUMMY IMPORT***  Would have tried to import: %s ", fileName)

        
    def setMaster(self, master):
        """
            Setter for FSDropBox 

            :Parameters:
                master : DropBox
                    DropBox Server

            :return: No explicit return value.

        """
        self.master = master

    def setDropBoxDir(self, dropBoxDir):
        """
            Setter for FSDropBox 

            :Parameters:
                dropBoxDir : string
                    DropBox directory

            :return: No explicit return value.

        """
        self.dropBoxDir = dropBoxDir


    def setServerProxy(self, serverProxy):
        """
            Setter for serverProxy

            :Parameters:
                serverProxy : monitors.MonitorServerPrx
                    proxy to remote server object

            :return: No explicit return value.

        """
        self.serverProxy = serverProxy


    def setSelfProxy(self, selfProxy):
        """
            Setter for serverProxy

            :Parameters:
                selfProxy : monitors.MonitorClientPrx
                    proxy to this client object

            :return: No explicit return value.

        """
        self.selfProxy = selfProxy


    def setId(self, id):
        """
            Setter for id
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Monitor created
                    by the OMERO.fs Server.
                    
            :return: No explicit return value.
            
        """
        #: A string uniquely identifying the OMERO.fs Monitor
        self.id = id
        
    def setDirImportWait(self, dirImportWait):
        """
            Setter for dirImportWait
            
            :Parameters:
                dirImportWait : int

                    
            :return: No explicit return value.
            
        """
        
        self.dirImportWait = dirImportWait
        