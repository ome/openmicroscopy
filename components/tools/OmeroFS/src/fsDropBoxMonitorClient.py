#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs  DropBox implementation of a MonitorClient

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

import shlex
import logging
import threading
import Queue
import time
import path as pathModule

import omero
import omero.cli
import omero.rtypes

import Ice
import IceImport

from omero.util import ServerContext, Resources
from omero.util.decorators import remoted, locked, perf
from omero.util.import_candidates import as_dictionary
from omero.util.concurrency import Timer, get_event
from omero.util.temp_files import create_path, remove_path

IceImport.load("omero_FS_ice")
monitors = Ice.openModule('omero.grid.monitors')


class MonitorState(object):

    """
    Concurrent state which is tracked by a MonitorClientI
    instance.
    """

    class Entry(object):

        def __init__(self, seq, timer):
            self.seq = seq
            self.timer = timer

        def __repr__(self):
            return self.__str__()

        def __str__(self):
            return "<Entry:%s>" % id(self)

    def __init__(self, event):
        self.log = logging.getLogger("fsclient." + __name__)
        self._lock = threading.RLock()
        self.__entries = {}
        self.__timers = 0
        self.__wait = time.time()
        self.__event = event

    def appropriateWait(self, throttleImport):
        """
        If the last call to appropriateWait was longer than throttleImport
        seconds ago, then wait long enough to make it so and return. The
        __wait property will be set to the current time after this method
        returns. Access to __wait is protected by the _lock lock.
        """
        self.log.debug("Locking for appropriate wait...")
        self._lock.acquire()
        try:
            try:
                elapsed = time.time() - self.__wait
                if elapsed < throttleImport:
                    to_wait = throttleImport - elapsed
                    self.log.info("Waiting %s seconds..." % to_wait)
                    self.__event.wait(to_wait)
                else:
                    self.log.debug("Not waiting.")
            finally:
                self.__wait = time.time()
        finally:
            self._lock.release()

    def addTimer(self, wait, callback, argsList):
        self.__timers += 1
        timer = Timer(wait, callback, argsList)
        return timer

    def removeTimer(self, timer):
        try:
            self.__timers -= 1
            timer.cancel()
        except:
            self.log.warn("Failed to stop timer: %s", timer)

    def checkKey(self, key):
        if not isinstance(key, str):
            self.log.warn("Key isn't a string: %s", key)
            key = str(key)
        return key

    @perf
    @locked
    def keys(self):
        """
        Returns the current keys stored. This is mostly used
        for testing. None of the import logic depends on using
        this method.
        """
        return self.__entries.keys()

    @perf
    @locked
    def count(self):
        """
        Returns the current timers stored. This is mostly used
        for testing. None of the import logic depends on using
        this method.
        """
        return self.__timers

    @perf
    @locked
    def update(self, data, wait, callback):
        """
        Central MonitorState method which takes a fileSet dictionary as
        returned from omero.utils.import_candidates.as_dictionary and updates
        the internal state
        """
        for key, seq in data.items():
            key = self.checkKey(key)
            assert key in seq  # Guarantees length > 1
            # Key ignored after this point.

            entry = self.find(seq)

            if entry:  # UPDATE
                msg = "Revised"
                entry.seq = seq
                entry.timer.reset()
                # Passing the first file returned by as_dictionary
                # as the argument. This prevents a "subsumed"
                # candidate from being imported as a standalone
                # image, or late scragglers from taking over
                # a fileset.
                entry.timer.args = [seq[0]]
                self.sync(entry)

            else:  # INSERT
                msg = "New"
                timer = self.addTimer(wait, callback, [key])
                entry = MonitorState.Entry(seq, timer)
                self.sync(entry)
                # Last activity
                timer.start()

            self.log.info(
                "%s entry %s contains %d file(s). Files=%s Timers=%s",
                msg, key, len(seq), len(self.__entries), self.__timers)

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
        If the link already exists and it is not to the current Entry,
        then the old Entry will be cleaned up.
        """
        for key in entry.seq:
            try:
                entry2 = self.__entries[key]
                if entry2 != entry:
                    self.log.info(
                        "Key %s moved entries:%s=>%s", key, entry2, entry)
                    self.__entries[key] = entry
                    count = 0
                    for v in self.__entries.values():
                        if v == entry2:
                            count += 1
                    if count:
                        self.log.warn(
                            "%s remaining key(s) point to %s", count, entry2)
                    else:
                        self.log.info("Stopping %s", entry2)
                        self.removeTimer(entry2.timer)
            except KeyError:
                self.log.debug("Adding entry for %s", key)
                self.__entries[key] = entry

    @perf
    @locked
    def clear(self, key, entry=None):
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

        self.removeTimer(entry.timer)

        for key2 in entry.seq:
            try:
                del self.__entries[key2]
                self.log.debug("Removed key %s", key2)
            except KeyError:
                self.log.warn("Could not remove key %s", key2)

        self.log.info("Removed key %s" % key)

    @perf
    @locked
    def stop(self):
        """
        Shutdown this instance
        """
        self.log.info("Stop called")
        try:
            for k, s in self.__entries.items():
                self.clear(k, s)
        finally:
            del self.__entries


class MonitorWorker(threading.Thread):

    """
    Worker thread which will consume items from the MonitorClientI.queue
    and add them to the MonitorState
    """

    def __init__(self, wait, batch, event, queue, callback):
        threading.Thread.__init__(self)
        self.log = logging.getLogger("fsclient." + __name__)
        # numbers
        self.wait = wait
        self.batch = batch
        # threading privitives
        self.event = event
        self.queue = queue
        # external functions
        self.callback = callback

    def run(self):
        """
        Repeatedly calls self.execute until self.event is set.
        """
        while not self.event.isSet():
            self.execute()
        self.log.info("Stopping")

    @perf
    def execute(self):
        """
        Loops until either:
         * the number of ids >= self.batch
         * self.wait seconds have passed
         * self.event (a stop Event) is set

        If event is set or no ids are found, this method returns.
        If batch ids are found, pass them to self.callback()

        An inner loop is used since there is no way to signal
        to the Queue that it should cease blocking activities.
        """
        count = 0    # Count of possibly duplicate entries
        ids = set()  # Unique entries
        start = time.time()
        while (len(ids) < self.batch) \
                and (time.time() < (start + self.wait)) \
                and not self.event.isSet():

            try:
                while True:
                    entry = self.queue.get_nowait()
                    if entry:
                        count += 1
                        ids.add(entry.fileId)
                        if len(ids) >= self.batch:
                            break
            except Queue.Empty:
                pass

            # Slowing down this thread to prevent a very busy wait
            self.event.wait(2)

        if len(ids) == 0:
            self.log.debug("No events found")
        else:
            if self.event.isSet():
                self.log.warn(
                    "Skipping processing of %s events (%s ids). %s remaining"
                    % (count, len(ids), self.queue.qsize()))
            else:
                self.log.info("Processing %s events (%s ids). %s remaining"
                              % (count, len(ids), self.queue.qsize()))
                try:
                    self.callback(ids)
                except:
                    self.log.exception("Callback error")


class MonitorClientI(monitors.MonitorClient):

    """
        Implementation of the MonitorClient.

        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.

    """

    def __init__(self, dir, communicator, getUsedFiles=as_dictionary, ctx=None,
                 worker_wait=60, worker_count=1, worker_batch=10):
        """
            Intialise the instance variables.

        """
        self.log = logging.getLogger("fsclient." + __name__)
        self.communicator = communicator

        self.master = None
        #: Reference back to FSServer.
        self.serverProxy = None
        self.selfProxy = None
        self.dropBoxDir = dir
        self.host = ""
        self.port = 0
        self.dirImportWait = 0
        self.throttleImport = 5
        self.timeToLive = 0
        self.timeToIdle = 0
        self.readers = ""
        self.importArgs = ""
        #: Id
        self.id = ''

        # Overriding methods to allow for simpler testing
        self.getUsedFiles = perf(getUsedFiles)

        # Threading primitives
        self.worker_wait = worker_wait
        self.worker_count = worker_count
        self.worker_batch = worker_batch
        self.event = get_event()
        self.queue = Queue.Queue(0)
        self.state = MonitorState(self.event)
        self.resources = Resources(stop_event=self.event)
        if ctx:
            # Primarily used for testing
            self.ctx = ctx
        else:
            self.ctx = ServerContext(
                server_id="DropBox", communicator=communicator,
                stop_event=self.event)
        self.resources.add(self.ctx)

        self.workers = [
            MonitorWorker(
                worker_wait, worker_batch, self.event,
                self.queue, self.callback)
            for x in range(worker_count)]
        for worker in self.workers:
            worker.start()

        self.eventRecord("Directory", self.dropBoxDir)

    @perf
    def stop(self):
        """
        Shutdown this servant
        """

        self.event.set()  # Marks everything as stopping

        # Shutdown all workers first, otherwise
        # there will be contention on the state
        workers = self.workers
        self.workers = None
        if workers:
            self.log.info("Joining workers...")
            for x in workers:
                x.join()

        try:
            state = self.state
            self.state = None
            self.log.info("Stopping state...")
            if state:
                state.stop()
        except:
            self.log.exception("Error stopping state")

        try:
            resources = self.resources
            self.resources = None
            self.log.info("Cleaning up resources state...")
            if resources:
                resources.cleanup()
        except:
            self.log.exception("Error cleaning resources")

    def __del__(self):
        self.stop()

    #
    # Called by server threads.
    #

    @remoted
    @perf
    def fsEventHappened(self, monitorid, eventList, current=None):
        """
            Primary monitor client callback.

            If new files appear on the watch, the list is sent as an argument.
            The id should match for the events to be relevant.

            At the moment each file type is treated as a special case. The
            number of special cases is likely to explode and so a different
            approach is needed. That will be easier with more knowledge of the
            different multi-file formats.

            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Watch created
                    by the OMERO.fs Server.

                eventList : list<string>
                    A list of events, in the current implementation this is
                    a list of strings representing the full path names of new
                    files.

                current
                    An ICE context, this parameter is required to be present
                    in an ICE callback.

            :return: No explicit return value

        """
        # ! Set import to dummy mode for testing purposes.
        # self.importFile = self.dummyImportFile
        # ! If the above line is not commented out nothing will import.
        if self.id != monitorid:
            self.warnAndThrow(
                omero.ApiUsageException(),
                "Unknown fs server id: %s", monitorid)

        self.eventRecord("Batch", len(eventList))

        for fileInfo in eventList:

            fileId = fileInfo.fileId
            if not fileId:
                self.warnAndThrow(omero.ApiUsageException(), "Empty fieldId")

            self.eventRecord(fileInfo.type, fileId)

            # Checking name first since it's faster
            exName = self.getExperimenterFromPath(fileId)
            if exName and self.userExists(exName):
                # Creation or modification handled by state/timeout system
                if (str(fileInfo.type) == "Create"
                        or str(fileInfo.type) == "Modify"):
                    self.queue.put(fileInfo)
                else:
                    self.log.info(
                        "Event not Create or Modify, presently ignored.")

    #
    # Called by worker threads.
    #

    @perf
    def callback(self, ids):
        try:
            self.log.info("Getting filesets on : %s", ids)
            fileSets = self.getUsedFiles(list(ids), readers=self.readers)
            self.eventRecord("Filesets", str(fileSets))
        except:
            self.log.exception("Failed to get filesets")
            fileSets = None

        if fileSets:
            self.state.update(
                fileSets, self.dirImportWait, self.importFileWrapper)

    #
    # Called from state callback (timer)
    #

    def importFileWrapper(self, fileId):
        """
        Wrapper method which allows plugging error handling code around
        the main call to importFile. In all cases, the key will be removed
        on execution.
        """
        self.state.clear(fileId)
        exName = self.getExperimenterFromPath(fileId)
        self.importFile(fileId, exName)

    #
    # Helpers
    #

    def getExperimenterFromPath(self, fileId=""):
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
            # For .../DropBox/user structure
            if len(fileParts) >= 2:
                exName = fileParts[0]
            # For .../DropBox/u/user structure
            # if len(fileParts) >= 3:
            #    exName = fileParts[1]
        if not exName:
            self.log.error("File added outside user directories: %s" % fileId)
        return exName

    def loginUser(self, exName):
        """
        Logs in the given user and returns the session
        """

        if not self.ctx.hasSession():
            self.ctx.newSession()

        sf = None
        try:
            sf = self.ctx.getSession()
        except:
            self.log.exception("Failed to get sf \n")

        if not sf:
            self.log.error("No connection")
            return None

        p = omero.sys.Principal()
        p.name = exName
        p.group = "user"
        p.eventType = "User"

        try:
            sf.getAdminService().lookupExperimenter(exName)
            sess = sf.getSessionService().createSessionWithTimeouts(
                p, self.timeToLive, self.timeToIdle)
            return sess
        except omero.ApiUsageException:
            self.log.info("User unknown: %s", exName)
            return None
        except:
            self.log.exception("Unknown exception during loginUser")
            return None

    def logoutUser(self, sess):
        """
        Logs out the user's session
        """
        if not self.ctx.hasSession():
            self.ctx.newSession()

        sf = None
        try:
            sf = self.ctx.getSession()
        except:
            self.log.exception("Failed to get sf \n")

        if not sf:
            self.log.error("No connection")
        else:
            try:
                sf.getSessionService().closeSession(sess)
            except:
                self.log.exception("Unknown exception during logoutUser")

    def userExists(self, exName):
        """
            Tests if the given user exists.

        """

        if not self.ctx.hasSession():
            self.ctx.newSession()

        sf = None
        try:
            sf = self.ctx.getSession()
        except:
            self.log.exception("Failed to get sf \n")

        if not sf:
            self.log.error("No connection")
            return False

        try:
            sf.getAdminService().lookupExperimenter(exName)
            return True
        except omero.ApiUsageException:
            self.log.info("User unknown: %s", exName)
            return False
        except:
            self.log.exception("Unknown exception during loginUser")
            return False

    @perf
    def importFile(self, fileName, exName):
        """
            Import file or directory using 'bin/omero importer'
            This method is solely responsible for logging the user in,
            attempting (possibly multiply) an import, logging and
            throwing an exception if necessary.
        """

        try:
            self.state.appropriateWait(self.throttleImport)  # See ticket:5739

            sess = self.loginUser(exName)
            if not sess:
                self.log.info("File not imported: %s", fileName)
                return
            key = sess.uuid.val
            self.log.info("Importing %s (session=%s)", fileName, key)

            imageId = []

            t = create_path("dropbox", "err")
            to = create_path("dropbox", "out")

            cli = omero.cli.CLI()
            cli.loadplugins()
            cmd = ["-s", self.host, "-p", str(self.port), "-k", key, "import"]
            cmd.extend(
                [str("---errs=%s" % t),
                    str("---file=%s" % to), "--", "--agent=dropbox"])
            cmd.extend(shlex.split(self.importArgs))
            cmd.append(fileName)
            logging.debug("cli.invoke(%s)" % cmd)
            cli.invoke(cmd)
            retCode = cli.rv

            if retCode == 0:
                self.log.info(
                    "Import of %s completed (session=%s)", fileName, key)
                if to.exists():
                    f = open(str(to), "r")
                    lines = f.readlines()
                    f.close()
                    if len(lines) > 0:
                        for line in lines:
                            imageId.append(line.strip())
                    else:
                        self.log.error("No lines in output file. No image ID.")
                else:
                    self.log.error("%s not found !" % to)

            else:
                self.log.error(
                    "Import of %s failed=%s (session=%s)",
                    fileName, str(retCode), key)
                self.log.error(
                    "***** start of output from importer-cli to stderr *****")
                if t.exists():
                    f = open(str(t), "r")
                    lines = f.readlines()
                    f.close()
                    for line in lines:
                        self.log.error(line.strip())
                else:
                    self.log.error("%s not found !" % t)
                self.log.error("***** end of output from importer-cli *****")
                self.logoutUser(sess)
        finally:
            remove_path(t)
            remove_path(to)

        return imageId

    #
    # Setters
    #

    def dummyImportFile(self, fileName, exName):
        """
            Log a potential import for test purposes

        """
        self.log.info(
            "***DUMMY IMPORT***  Would have tried to import: %s ", fileName)

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

    def setThrottleImport(self, throttleImport):
        """
            Setter for throttleImport

            :Parameters:
                throttleImport : int


            :return: No explicit return value.

        """
        self.throttleImport = throttleImport

    def setTimeouts(self, timeToLive, timeToIdle):
        """
            Set timeToLive and timeToIdle

            :Parameters:
                timeToLive : long
                timeToIdle : long


            :return: No explicit return value.

        """
        self.timeToLive = timeToLive
        self.timeToIdle = timeToIdle

    def setHostAndPort(self, host, port):
        """
            Set the host and port from the communicator properties.

        """
        self.host = host
        self.port = port

    def setReaders(self, readers):
        """
            Set the readers file from the communicator properties.

        """
        self.readers = readers

    def setImportArgs(self, importArgs):
        """
            Set the importArgs from the communicator properties.

        """
        self.importArgs = importArgs

    #
    # Various trivial helpers
    #
    def eventRecord(self, category, value):
        self.log.info("EVENT_RECORD::%s::%s::%s::%s" %
                      ("Cookie", time.time(), category, value))

    def warnAndThrow(self, exc, message, *arguments):
        self.log.warn(message, *arguments)
        exc.message = (message % arguments)
        raise exc

    def errAndThrow(self, exc, message, *arguments):
        self.log.error(message, *arguments)
        exc.message = (message % arguments)
        raise exc


class SingleUserMonitorClient(MonitorClientI):

    """
        Subclass of MonitorClient providing for a single user to import outside
        of the DropBox structure.

    """

    def __init__(self, user, dir, communicator, getUsedFiles=as_dictionary,
                 ctx=None, worker_wait=60, worker_count=1, worker_batch=10):
        """
            Initialise via the superclass

        """
        MonitorClientI.__init__(
            self, dir, communicator, getUsedFiles=getUsedFiles, ctx=None,
            worker_wait=worker_wait, worker_count=worker_count,
            worker_batch=worker_batch)

        self.user = user
        ret = self.loginUser(self.user)
        if not ret:
            raise Exception("No such user " + self.user)

    def getExperimenterFromPath(self, fileId=""):
        """
            Extract experimenter name from path. If the experimenter
            cannot be extracted, then null will be returned, in which
            case no import should take place.
        """
        return self.user


class TestMonitorClient(SingleUserMonitorClient):

    """
        Subclass of SingleUserMonitorClient providing for a test import outside
        of the DropBox structure to be made by copying a given file.


    """

    def __init__(self, user, dir, communicator, getUsedFiles=as_dictionary,
                 ctx=None, worker_wait=60, worker_count=1, worker_batch=10):
        """
            Initialise via the superclass

        """
        SingleUserMonitorClient.__init__(
            self, user, dir, communicator, getUsedFiles=getUsedFiles, ctx=None,
            worker_wait=worker_wait, worker_count=worker_count,
            worker_batch=worker_batch)

    #
    # Called from state callback (timer)
    #
    def importFileWrapper(self, fileId):
        """
            Import a file and then notify the DropBox that the file has been
            imported successfully or not.

        """
        self.state.clear(fileId)
        exName = self.getExperimenterFromPath(fileId)
        imageId = self.importFile(fileId, exName)
        self.log.info(
            "Test file imported or not: %s for test user %s", fileId, exName)
        self.master.notifyTestFile(imageId, fileId)
