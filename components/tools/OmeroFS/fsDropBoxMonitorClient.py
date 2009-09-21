"""
    OMERO.fs  DropBox implementation of a MonitorClient

"""
import logging
import fsLogger

import exceptions
import string
import subprocess as sp
import os
import platform
import uuid
import threading
import time
import path as pathModule

import omero
import omero.java

import Ice
import IceGrid
import Glacier2

from omero.grid import monitors
from omero.util import make_logname
from omero.util.decorators import remoted
from omero.util.import_candidates import as_dictionary

import fsConfig as config

class MonitorState(object):
    """
    Concurrent state which is tracked by a MonitorClientI
    instance.
    """

    def __init__(self):
        self.log = logging.getLogger(make_logname(self))
        self._lock = threading.RLock()
        #: dictionary of files onHold
        self.__onHold = {}
        self.__ometifHold = {}
        #: list of new directories yet to be imported.
        self.__newDirFiles = {}
        self.__newDirTimers = {}

    def checkKey(self, key):
        if not isinstance(key, str):
            self.log.warn("Key isn't a string: %s", key)
            key = str(key)
        return key

    def update(self, fileSets):
        """
        Central MonitorState method which takes a fileSet dictionary as returned
        from omero.utils.import_candidates.as_dictionary and updates the internal state
        """
        for fileKey, fileSet in fileSets.items():
            fileKey = self.checkKey(fileKey)

            # Update
            exists = fileKey in self.__newDirFiles
            if exists:
                if len(fileSet) == 0:
                    raise exceptions.Exception("NYI")
                raise exceptions.Exception("NYI")
                # Cancel and "restart" the Timer.
                if self.__newDirTimers[fileIn].isAlive():
                    self.__newDirTimers[fileIn].cancel()
                self.log.info("Revised set on %s contains %d files", fileIn, len(self.newDirFiles[fileIn]) )

            # Insert
            else:
                if len(fileSet) == 0:
                    return # Nothing to do
                else:
                    self.__newDirFiles[fileKey] = fileSet
                    self.__addDirTimer(fileId, self.dirImportWait, self.importDirectory, [fileKey])
                    self.log.info("New set on %s contains %d files", key, len(fileSet))

    def __addDirTimer(self, key, dirImportWait, callback, args):
        key = self.checkKey(key)
        timer = threading.Timer(dirImportWait, callback, args)
        timer.start()
        self.__newDirTimers[key] = timer


class MonitorClientI(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.

        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.

    """

    def __init__(self, getUsedFiles = as_dictionary):
        """
            Intialise the instance variables.

        """
        self.getUsedFiles = getUsedFiles
        self.log = logging.getLogger(make_logname(self))
        self.state = MonitorState()
        self.master = None
        #: Reference back to FSServer.
        self.serverProxy = None
        self.selfProxy = None
        self.dropBoxDir = None
        self.dirImportWait = 0
        #: Id
        self.id = ''

    def fsEventHappened(self, monitorid, eventList, current=None):
        """
            Primary monitor client callback.

            If new files appear on the watch, the list is sent as an argument.
            The id should match for the events to be relevant.

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
        # ############## ! Set import to dummy mode for testing purposes.
        # self.importFile = self.dummyImportFile
        # ############## ! If the above line is not commented out nothing will import.
        if self.id != monitorid:
            self.warnAndThrow(omero.ApiUsageException(), "Unknown fs server id: %s", monitorid)

        for fileInfo in eventList:

            fileId = fileInfo.fileId
            if not fileId:
                self.warnAndThrow(omero.ApiUsageException(), "Empty fieldId")

            self.log.info("EVENT_RECORD::%s::%s::%s" % (time.time(), fileInfo.type, fileId))

            # Creation or modification handled by state/timeout system
            if self.handledByState(fileId):
                fileSet = self.getUsedFiles(fileId)
                self.state.update(fileSet)

            # New file at the top level.
            else:
                self.handleOther(fileId)

    fsEventHappened = remoted(fsEventHappened)

    def handledByState(self, fileId):
        """
        Determines if this state object will be
        able to handle the given fileId. If "True",
        then update() can (and *should*) be called
        with any changes to the fileSet stored under
        the given key.
        """
        dirName = pathModule.path(fileId).dirname()
        while str(dirName.abspath()) != str(self.dropBoxDir):
            dirName = pathModule.path(dirName).dirname()
            if dirName.ismount():
                return False
        return True

    def handleOther(self, fileId):
        """
        Import a file at the top level or in an 'old' directory.
        """
        self.log.info("New file *not* in new dir %s", fileId)

        exName = self.getExperimenterFromPath(fileId)
        fileExt, fileName, fileBase = self.getBestGuessImporter(fileId)

        # Deal with root level jpg files
        if fileExt == ".jpg":
            self.importFile(fileId, exName)

        # Deal with root level lsm files
        elif fileExt == ".lsm":
            self.importFile(fileId, exName)

        # Deal with root level dv files and their logs
        elif fileExt == ".dv":
            if (fileName+".log", exName) in self.onHold.keys():
                self.onHold[(fileName+".log", exName)].cancel()
                self.onHold.pop((fileName+".log", exName))
                self.importFile(fileId, exName)
            else:
                self.onHold[(fileName,exName)] = threading.Timer(config.waitTimes[".dv"], self.importAnyway, (fileId, exName))
                self.onHold[(fileName,exName)].start()
        # Deal with root level log files and their dvs
        elif fileExt == ".log":
            if (fileBase, exName) in self.onHold.keys():
                self.onHold[(fileBase, exName)].cancel()
                self.onHold.pop((fileBase, exName))
                self.importFile(pathModule.path(fileId).parent + "/" + fileBase, exName)
            else:
                self.onHold[(fileName,exName)] = threading.Timer(config.dropTimes[".dv"], self.ignoreFile, (fileName, exName))
                self.onHold[(fileName,exName)].start()

        # Deal with root level ome.tif files
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
                        self.log.info("File identified as part of %s", str(omeKey))
                        if len(self.ometifHold[omeKey]) == 0:
                            self.importFile(fileId, exName)
                            self.ometifHold.pop(omeKey)
                    else:
                        self.log.info("First file of %s",str(omeKey))
                        self.ometifHold[omeKey] = set(fileList)
                        self.ometifHold[omeKey].discard(fileName)

        # Deal with all other notified file types.
        else:
            # ignore other file types for now.
            self.log.info("File not imported: file type '%s' not currently handled: %s", fileExt, fileId)

    def getBestGuessImporter(self, fileId):
        """
            For the moment return file details.

            Eventually call some method on the importer

        """
        fileExt = pathModule.path(fileId).ext
        fileName = pathModule.path(fileId).name
        fileBase = pathModule.path(fileId).namebase

        return (fileExt, fileName, fileBase)

    def getExperimenterFromPath(self, fileId):
        """
            Extract experimenter name from path. If the experimenter
            cannot be extracted, then null will be returned, in which
            case no import should take place.
        """

        exName = None
        fileParts = pathModule.path(fileId).splitall()
        try:
            base = fileParts.index(self.dropBoxDir)
            exName = fileParts[base+2]
        except exceptions.Exception, e:
            if isinstance(e, IndexError) or isinstance(e, ValueError):
                self.errAndThrow(omero.InternalException(), "Monitor directory improperly configured: %s", self.dropBoxDir)
            else:
                raise

        try:
            # The following line throws an exception if the file is
            # a level or more below the experimenter name level
            fileParts[base+3]
        except IndexError:
            self.log.info("File added not at user level directory: %s" % fileId)
        return exName


    def importAnyway(self, fileName, exName):
        """
            Force the import of a dv with no accompanying log.

        """
        self.log.info("No accompanying file has appeared, importing primary file %s for user %s", fileName, exName)
        self.onHold.pop((pathModule.path(fileName).name, exName))
        self.importFile(fileName, exName)

    def ignoreFile(self, fileName, exName):
        """
            Remove a log file from onHold that has no accompanying dv.

        """
        self.log.info("No primary file has appeared, ignoring accompanying file %s for user %s", fileName, exName)
        self.onHold.pop((fileName, exName))

    def importDirectory(self, dirName):
        """
            Import a directory.

            Clear it from the new directory list.

        """
        exName = self.getExperimenterFromPath(dirName)
        if exName:
            self.newDirs.discard(dirName)
            self.newDirFiles.pop(dirName, True)
            self.newDirTimers.pop(dirName, True)
            self.importFile(dirName, exName)

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
                self.log.info("Importing file using session key = %s", key)

                if platform.system() == 'Windows':
                    # Windows requires bin/omero to be bin\omero
                    climporter = config.climporter.replace('/','\\')
                    # Awkward file names not yet handled.
                    command = [climporter +
                                " -s " + config.host +
                                " -k " + key +
                                " " + fileName ]
                    self.log.info("Windows command %s", str(command))

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
                    self.log.warn("Import failed, possible cause file locking. Trying once more.")
                    self.log.info("Importing file using command = %s", command[0])
                    process = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
                    output = process.communicate()
                    retCode = process.returncode

                if retCode == 0:
                    self.log.info("Import completed on session key = %s", key)
                else:
                    self.log.error("Import completed on session key = %s, return code = %s", key, str(retCode))
                    self.log.error("***** start of output from importer-cli to stderr *****")
                    for line in output[1].split('\n'):
                        self.log.error(line)
                    self.log.error("***** end of output from importer-cli *****")

            else:
                self.log.info("File not imported: user unknown: %s", exName)

        except:
            raise

    def dummyImportFile(self, fileName, exName):
        """
            Log a potential import for test purposes

        """
        self.log.info("***DUMMY IMPORT***  Would have tried to import: %s ", fileName)


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

    #
    # Various trivial helpers
    #

    def warnAndThrow(self, exc, message, *arguments):
        self.log.warn(message, *arguments)
        exc.message = (message % arguments)
        raise exc

    def errAndThrow(self, exc, message, *arguments):
        self.log.error(message, *arguments)
        exc.message = (message % arguments)
        raise exc
