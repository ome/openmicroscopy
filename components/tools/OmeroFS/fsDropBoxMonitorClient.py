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
from omero.util.decorators import remoted, locked, perf
from omero.util.import_candidates import as_dictionary
from omero.util.concurrency import Timer

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
        self.__entries = {}
        self.__timers = 0

    def checkKey(self, key):
        if not isinstance(key, str):
            self.log.warn("Key isn't a string: %s", key)
            key = str(key)
        return key

    def keys(self):
        """
        Returns the current keys stored. This is mostly used
        for testing. None of the import logic depends on using
        this method.
        """
        return self.__entries.keys()
    keys = locked(keys)
    keys = perf(keys)

    def update(self, data, wait, callback, argsList):
        """
        Central MonitorState method which takes a fileSet dictionary as returned
        from omero.utils.import_candidates.as_dictionary and updates the internal state
        """
        for key, seq in data.items():
            key = self.checkKey(key)
            assert key in seq # Guarantees length > 1
            # Key ignored after this point.

            entry = self.find(seq)

            if entry: # UPDATE
                msg = "Revised"
                entry.seq = seq
                entry.timer.reset()
                self.sync(entry)

            else: # INSERT
                msg = "New"
                self.__timers += 1
                timer = Timer(wait, callback, argsList)
                entry = MonitorState.Entry(seq, timer)
                self.sync(entry)
                # Last activity
                timer.start()

            self.log.info("%s entry %s contains %d file(s). Files=%s Timers=%s", msg, key, len(seq), len(self.__entries), self.__timers)

    update = locked(update)
    update = perf(update)

    def find(self, seq):
        """
        Finds the entry for the given key or None.
        """
        for key in seq:
            try:
                return self.__entries[key]
            except KeyError:
                pass
        return None

    def sync(self, entry):
        """
        Takes an Entry instance and creates all the needed dictionary
        entries from all the keys in the sequence to the Entry itself.
        If the link already exists, it MUST be to the entry.
        """
        for key in entry.seq:
            try:
                entry2 = self.__entries[key]
                if entry2 != entry:
                    raise exceptions.Exception("INTERNAL EXCEPTION: entries out of sync: %s!=%s",entry,entry2)
            except KeyError:
                self.log.debug("Adding entry for %s", key)
                self.__entries[key] = entry

    def clear(self, key, entry = None):
        """
        Used to remove all references to a given Entry. In key contained in
        Entry.seq can be passed. If entry is passed in, then this is
        assumed to be the data in the dictionary to prevent a further lookup.
        """
        if entry is None:
            try:
                entry = self.__entries[key]
            except KeyError:
                self.log.warn("Can't find key for clear: %s", key)
                return

        try:
            self.__timers -= 1
            entry.timer.cancel()
        except:
            self.log.warn("Failed to stop timer: %s", entry.timer)

        for key2 in entry.seq:
            try:
                del self.__entries[key2]
                self.log.debug("Removed key %s", key2)
            except KeyError:
                self.log.warn("Could not remove key %s", key2)

        self.log.info("Removed key %s" % key)

    clear = locked(clear)
    clear = perf(clear)

    def stop(self):
        """
        Shutdown this instance
        """
        self.log.info("Stop called")
        try:
            for k,s in self.__entries.items():
                self.clear(k,s)
        finally:
            del self.__entries
    stop = locked(stop)


class MonitorClientI(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.

        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.

    """

    def __init__(self, communicator = None, getUsedFiles = as_dictionary, getRoot = internal_service_factory):
        """
            Intialise the instance variables.

        """
        self.log = logging.getLogger(make_logname(self))
	self.communicator = communicator
        self.root = None
        self.master = None
        #: Reference back to FSServer.
        self.serverProxy = None
        self.selfProxy = None
        self.dropBoxDir = None
        self.dirImportWait = 0
        #: Id
        self.id = ''

        # Overriding methods to allow for simpler testing
        self.getUsedFiles = perf(getUsedFiles)
        self.getRoot = perf(getRoot)

        # Threading primitives
        self.state = MonitorState()
        self.resources = Resources()
        self.checkRoot()

    def stop(self):
        """
        Shutdown this servant
        """
        try:
            if self.state: self.state.stop()
            self.state = None
        except:
            self.log.exception("Error stopping state")
        try:
            if self.resources: self.resources.cleanup()
            self.resources = None
        except:
            self.log.exception("Error cleaning resources")

    def __del__(self):
        self.stop()

    #
    # Called by server threads.
    #

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

        self.log.info("EVENT_RECORD::%s::%s::%s::%s" % ("Cookie", time.time(), "Batch", len(eventList)))

        for fileInfo in eventList:

            fileId = fileInfo.fileId
            if not fileId:
                self.warnAndThrow(omero.ApiUsageException(), "Empty fieldId")

            self.log.info("EVENT_RECORD::%s::%s::%s::%s" % ("Cookie", time.time(), fileInfo.type, fileId))

            # Checking name first since it's faster
            exName = self.getExperimenterFromPath(fileId)
            if exName:
                # Creation or modification handled by state/timeout system
                fileSets = self.getUsedFiles(fileId)
                if fileSets:
                    self.state.update(fileSets, self.dirImportWait, self.importFileWrapper, [fileId, exName])

    fsEventHappeend = perf(fsEventHappened)
    fsEventHappened = remoted(fsEventHappened)

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

    #
    # Called from callback
    #

    def importFileWrapper(self, fileName, exName):
        """
        Wrapper method which allows plugging error handling code around
        the main call to importFile. In all cases, the key will be removed
        on execution.
        """
        self.state.clear(fileName)
        self.importFile(fileName, exName)

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
            self.root = self.getRoot(self.communicator)
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

    importFile = perf(importFile)

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
