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
from omero.util import make_logname, internal_service_factory, Resources, SessionHolder
from omero.util.decorators import remoted, locked
from omero.util.import_candidates import as_dictionary

import fsConfig as config

class MonitorState(object):
    """
    Concurrent state which is tracked by a MonitorClientI
    instance.
    """

    class Entry(object):
        def __init__(self, seq, timer):
            self.seq = seq
            self.timer = timer

    def __init__(self):
        self.log = logging.getLogger(make_logname(self))
        self._lock = threading.RLock()
        self.__data = {}

    def checkKey(self, key):
        if not isinstance(key, str):
            self.log.warn("Key isn't a string: %s", key)
            key = str(key)
        return key

    def update(self, data, wait, callback, argsList):
        """
        Central MonitorState method which takes a fileSet dictionary as returned
        from omero.utils.import_candidates.as_dictionary and updates the internal state
        """
        for key, seq in data.items():
            key = self.checkKey(key)

            # Update
            exists = key in self.__data
            if exists:
                s = self.__data[key]
                if len(seq) == 0:
                    self.clear(key, s)
                else: # True
                    s.seq = seq
                    self.log.info("Revised entry %s contains %d file(s)", key, len(seq))

            # Insert
            else:
                if len(seq) == 0:
                    self.log.info("Not adding entry %s with no data", key)
                    return # Nothing to do
                else:
                    timer = threading.Timer(wait, callback, argsList)
                    entry = MonitorState.Entry(seq, timer)
                    self.__data[key] = entry
                    timer.start()
                    self.log.info("New entry %s contains %d file(s)", key, len(seq))
    update = locked(update)

    def clear(self, key, s = None, cancel = True):
        """
        Used to remove all references to a given key. If s is passed in, then this is
        assumed to be the data in the dictionary to prevent a further lookup.
        """
        if cancel:
            if s is None:
                try:
                    s = self.__data[key]
                except KeyError:
                    self.log.warn("Can't find key for clear: %s", key)

            if s.timer.isAlive():
                    try:
                        s.timer.cancel()
                    except:
                        self.log.warn("Failed to stop timer: %s", s.timer)
            else:
                self.log.warn("Timer not alive: %s", s.timer)

        del self.__data[key]
        self.log.info("Removed key %s" % key)
    clear = locked(clear)

    def stop(self):
        """
        Shutdown this instance
        """
        try:
            for k,s in self.__data.items():
                self.clear(k,s)
        finally:
            del self.__data
    stop = locked(stop)


class MonitorClientI(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.

        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.

    """

    def __init__(self, getUsedFiles = as_dictionary, getRoot = internal_service_factory):
        """
            Intialise the instance variables.

        """
        self.log = logging.getLogger(make_logname(self))

        # Overriding methods to allow for simpler testing
        self.getUsedFiles = getUsedFiles
        self.getRoot = getRoot

        # Threading primitives
        self.state = MonitorState()
        self.resources = Resources()
        self.checkRoot()

        self.master = None
        #: Reference back to FSServer.
        self.serverProxy = None
        self.selfProxy = None
        self.dropBoxDir = None
        self.dirImportWait = 0
        #: Id
        self.id = ''

    def stop(self):
        """
        Shutdown this servant
        """
        # TODO: should probably stop root session here
        self.state.stop()

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

        self.log.info("EVENT_RECORD::%s::%s::%s::%s" % ("Cookie", time.time(), "Batch", ""))

        for fileInfo in eventList:

            fileId = fileInfo.fileId
            if not fileId:
                self.warnAndThrow(omero.ApiUsageException(), "Empty fieldId")

            self.log.info("EVENT_RECORD::%s::%s::%s::%s" % ("Cookie", time.time(), fileInfo.type, fileId))

            # Creation or modification handled by state/timeout system
            if self.handledByState(fileId):
                fileSet = self.getUsedFiles(fileId)
                if fileSet:
                    exName = self.getExperimenterFromPath(fileId)
                    if exName:
                        self.state.update(fileSet, self.dirImportWait, self.importFileWrapper, [fileId, exName])

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

        fileId = pathModule.path(fileId)
        exName = None
        parpath = fileId.parpath(self.dropBoxDir)
        if parpath and len(parpath) >= 2:
            fileParts = fileId.splitall()
            i = -1 * len(parpath)
            fileParts = fileParts[i:]
            if len(fileParts) >= 3:
                exName = fileParts[1]
        if not exName:
            self.log.info("File added outside user directories: %s" % fileId)
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

    def checkRoot(self):
        """
        Checks that
        """
        has = bool(self.root)
        if has:
            try:
                self.root.keepAlive(None)
            except:
                has = False
        if not has:
            self.root = self.getRoot()
            if self.root:
                self.resources.add(SessionHolder(self.root))

    def loginUser(self, exName):
        """
        Logins in the given user and returns the client
        """
        self.checkRoot()
        if not self.root:
            self.log.error("No connection")
            return None

        p = omero.sys.Principal()
        p.name  = exName
        p.group = "user"
        p.eventType = "User"

        try:
            exp = self.root.getAdminService().lookupExperimenter(exName)
            sess = root.sf.getSessionService().createSessionWithTimeout(p, 60000L)
            client = omero.client(config.host, config.port)
            user_sess = client.createSession(sess.uuid, sess.uuid)
            return client
        except omero.ApiUsageException:
            self.log.info("User unknown: %s", exName)
            return None
        except:
            self.log.exception("Unknown exception during loginUser")
            return None


    def importFileWrapper(self, fileName, exName):
        """
        Wrapper method which allows plugging error handling code around
        the main call to importFile. In all cases, the key will be removed
        on execution.
        """
        self.state.clear(fileName, False, False)
        self.importFile(fileName, exName)

    def importFile(self, fileName, exName):
        """
            Import file or directory using 'bin/omero importer'
            This method is solely responsible for logging the user in,
            attempting (possibly multiply) an import, logging and
            throwing an exception if necessary.
        """

        client = self.loginUser(exName)
        if not client:
            self.log.info("File not imported: %s", fileName)
            return

        try:
            key = user_sess.getAdminService().getEventContext().sessionUuid
            self.log.info("Importing file using session key = %s", key)

            if platform.system() == 'Windows':
                # Windows requires bin/omero to be bin\omero
                climporter = config.climporter.replace('/','\\')
                # Awkward file names not yet handled.
                command = [climporter,
                            " -s " + config.host,
                            " -k " + key,
                            " " + "'" + fileName + "'" ]
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

        finally:
            client.closeSession()
            del client

    #
    # Setters
    #

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
