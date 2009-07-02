"""
    OMERO.fs  DropBox implementation of a MonitorClient

    
"""
import logging
import fsLogger
log = logging.getLogger("fsclient."+__name__)

import string
import subprocess as sp
import os
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
        #: Id
        self.id = ''
        #: dictionary of files onHold
        self.onHold = {}
        self.ometifHold = {}
        

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
        if self.id == id:
            try:                       
                for fileInfo in eventList:
                    log.info("New file %s", fileInfo.fileId)
                    try:
                        fileParts = fileInfo.fileId.split("/")
                        base = fileParts.index(config.dropBoxDir)
                        exName = fileParts[base+2]
                        # The following line throws an exception if the file is
                        # a level or more below the experimenter name level.
                        fileParts[base+3] 
                        fileExt = pathModule.path(fileInfo.fileId).ext
                        fileName = pathModule.path(fileInfo.fileId).name
                        fileBase = pathModule.path(fileInfo.fileId).namebase

                        # Deal with lsm files
                        if fileExt == ".lsm":
                            self.importFile(fileInfo.fileId, exName)

                        # Deal with dv files and their logs
                        elif fileExt == ".dv":
                            if (fileName+".log", exName) in self.onHold.keys():
                                self.onHold[(fileName+".log", exName)].cancel()
                                self.onHold.pop((fileName+".log", exName))
                                self.importFile(fileInfo.fileId, exName)
                            else:
                                self.onHold[(fileName,exName)] = threading.Timer(config.waitTimes[".dv"], self.importAnyway, (fileInfo.fileId, exName))
                                self.onHold[(fileName,exName)].start()
                        # Deal with log files and their dvs
                        elif fileExt == ".log":
                            if (fileBase, exName) in self.onHold.keys():
                                self.onHold[(fileBase, exName)].cancel()
                                self.onHold.pop((fileBase, exName))
                                self.importFile(pathModule.path(fileInfo.fileId).parent + "/" + fileBase, exName)
                            else:
                                self.onHold[(fileName,exName)] = threading.Timer(config.dropTimes[".dv"], self.ignoreFile, (fileName, exName))
                                self.onHold[(fileName,exName)].start()

                        # Deal with ome.tif files
                        elif fileExt == ".tif" or fileExt == ".tiff":
                            if pathModule.path(fileBase).ext == ".ome":
                                command = [config.climporter + " -f " + fileInfo.fileId]
                                output = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True).communicate()
                                files = output[0].splitlines()
                                # Single file 
                                if len(files) == 1:
                                    self.importFile(fileInfo.fileId, exName)
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
                                            self.importFile(fileInfo.fileId, exName)
                                            self.ometifHold.pop(omeKey)    
                                    else:
                                        log.info("First file of %s",str(omeKey))
                                        self.ometifHold[omeKey] = set(fileList)
                                        self.ometifHold[omeKey].discard(fileName)
                                                                         
                        # Deal with all other notified file types.
                        else:
                            # ignore other file types for now.
                            pass
                    except IndexError:
                        log.info("File not imported: file not copied into user level directory")
                    except:
                        log.exception("Import failed: ")
            except:
                log.exception("Failed to get or join session: ")              
        else:
            log.error("Unknown fs server id: %s", id)

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

    def importFile(self, fileName, exName):
        """
            Import file using cli-importer
            
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

            if exName not in config.excludedUsers:                 
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
                    command = [config.climporter +
                                " -s " + config.host +
                                " -k " + key +
                                " " + fileName]
                    output = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True).communicate()
                    
                    # Debugging information from stdout and stderr is required
                    if log.getEffectiveLevel() == logging.DEBUG:
                        log.debug("Import call finished. Result:")
                        log.debug("***** start of output from importer-cli *****")
                        log.debug("To stdout: ")
                        for line in output[0].split('\n'):
                            log.debug(line)
                        log.debug("To stderr: ")
                        for line in output[1].split('\n'):
                            log.debug(line)
                        log.debug("***** end *****")
                    # End of debugging output
                    
                    log.info("Import completed on session key = %s", key)
                else:
                    log.info("File not imported: user unknown: %s", exName)
            else:
                log.info("File not imported: excluded user: %s", exName)

        except:
            raise
        
    def dummyimportFile(self, fileName, exName):
        """
            Log a potential import for test purposes

        """
        log.info("***DUMMY IMPORT***  File: %s  User: %s ", fileName, exName)

        
    def setMaster(self, master):
        """
            Setter for FSDropBox 

            :Parameters:
                master : DropBox
                    DropBox Server

            :return: No explicit return value.

        """
        self.master = master


    def setServerProxy(self, serverProxy):
        """
            Setter for serverProxy

            :Parameters:
                serverProxy : monitors.MonitorServerPrx
                    proxy to remote server object

            :return: No explicit return value.

        """
        self.serverProxy = serverProxy


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
        

