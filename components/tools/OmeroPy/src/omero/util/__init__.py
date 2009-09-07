#!/usr/bin/env python
#
# OMERO Utilities package
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import Ice
import time
import omero
import logging
import threading
import exceptions
import logging.handlers

from omero.rtypes import *

LOGDIR = os.path.join("var","log")
LOGFORMAT =  """%(asctime)s %(levelname)-5s [%(name)40s] (%(threadName)-10s) %(message)s"""
LOGSIZE = 500000000
LOGNUM = 9
LOGMODE = "a"


def configure_logging(logdir, logfile, loglevel = logging.INFO, format = LOGFORMAT, filemode = LOGMODE, time_rollover = False):

    if not time_rollover:
        fileLog = logging.handlers.RotatingFileHandler(os.path.join(logdir, logfile), maxBytes = LOGSIZE, backupCount = LOGNUM)
    else:
        fileLog = logging.handlers.TimedRotatingFileHandler(os.path.join(logdir, logfile),'midnight',1)
        # Windows will not allow renaming (or deleting) a file that's open.
        # There's nothing the logging package can do about that.
        try:
            sys.getwindowsversion()
        except:
            fileLog.doRollover()

    fileLog.setLevel(loglevel)
    formatter = logging.Formatter(format)
    fileLog.setFormatter(formatter)
    rootLogger = logging.getLogger()
    rootLogger.setLevel(loglevel)
    rootLogger.addHandler(fileLog)
    return rootLogger


def internal_service_factory(self, communicator, user="root", group=None, retries=6, interval=10, client_uuid=None):
    """
        Try to return a ServiceFactory from the grid.

        Try a number of times then give up and raise the
        last exception returned. This method will only
        work internally to the grid, i.e. behind the Glacier2
        firewall. It is intended for internal servers to
        be able to create sessions for accessing the database.

          communicator := Ice.Communicator used to find the registry
          user         := Username which should have a session created
          group        := Group into which the session should be logged
          retries      := Number of session creation retries before throwing
          interval     := Seconds between retries
          client_uuid  := Uuid of the client which should be used

    """
    log = logging.getLogger("omero.utils")
    gotSession = False
    tryCount = 0
    excpt = None
    query = communicator.stringToProxy("IceGrid/Query")
    query = IceGrid.QueryPrx.checkedCast(query)

    if client_uuid is None:
        client_uuid = str(uuid.uuid4())

    while (not gotSession) and (tryCount < retries):
        try:
            time.sleep(interval)
            blitz = query.findAllObjectsByType("::Glacier2::SessionManager")[0]
            blitz = Glacier2.SessionManagerPrx.checkedCast(blitz)
            sf = blitz.create(user, None, {"omero.client.uuid":client_uuid})
            # Group currently unused.
            sf = omero.api.ServiceFactoryPrx.checkedCast(sf)
            gotSession = True
        except Exception, e:
            tryCount += 1
            log.info("Failed to get session on attempt %s", str(tryCount))
            excpt = e

    if gotSession:
        return sf
    else:
        log.info("Reason: %s", str(excpt))
        raise Exception

def long_to_path(id, root=""):
    """
    Converts a long to a path such that for all directiories only
    a 1000 files and a 1000 subdirectories will be returned.

    This method duplicates the logic in
    ome.io.nio.AbstractFileSystemService.java:getPath()
    """
    suffix = ""
    remaining = id
    dirno = 0

    if id is None or id == "":
        raise exceptions.Exception("Expecting a not-null id.")

    id = long(id)

    if id < 0:
        raise exceptions.Exception("Expecting a non-negative id.")

    while (remaining > 999):
        remaining /= 1000

        if remaining > 0:
            dirno = remaining % 1000
            suffix = os.path.join("Dir-%03d" % dirno, suffix)

    return os.path.join(root, "%s%s" %(suffix,id))

class Server(Ice.Application):
    """
    Basic server implementation which can be used for
    implementing a standalone python server which can
    be started from icegridnode.

    The servant implementation MUST:
     * have a no-arg __init__ method
     * have a cleanup() method
     * not provide a "serverid" attribute (will be assigned)

    Logging is configured relative to the current directory
    to be in var/log by default.

    Usage:

    if __name__ == "__main__":
        app=Server(ServicesI, "ServicesAdapter", Ice.Identity("Services",""))
        sys.exit(app.main(sys.argv))

    app.impl now points to an instance of ServicesI

    """

    def __init__(self, impl_class, adapter_name, identity, logdir = LOGDIR):
        self.impl_class = impl_class
        self.adapter_name = adapter_name
        self.identity = identity
        self.logdir = logdir

    def run(self,args):
        program_name = self.communicator().getProperties().getProperty("Ice.ProgramName")
        configure_logging(self.logdir, program_name+".log")
        self.shutdownOnInterrupt()
        self.logger = logging.getLogger("omero.util.Server")
        self.logger.info("Starting")
        try:
            self.objectfactory = omero.ObjectFactory()
            self.objectfactory.registerObjectFactory(self.communicator())
            for of in ObjectFactories.values():
                of.register(self.communicator())
            self.adapter = self.communicator().createObjectAdapter(self.adapter_name)
            self.impl = self.impl_class()
            self.impl.serverid = self.communicator().getProperties().getProperty("Ice.ServerId")

            self.adapter.add(self.impl, self.identity)
            self.logger.info("Activating")
            self.adapter.activate()
        finally:
            self.logger.info("Blocking until shutdown")
            self.communicator().waitForShutdown()
            self.logger.info("Cleanup")
            self.cleanup()
            self.logger.info("Stopped")

    def cleanup(self):
        """
        Cleans up all resources that were created by this server.
        Primarily the one servant instance.
        """
        if hasattr(self,"impl"):
            try:
                self.impl.cleanup()
            finally:
                del self.impl

class Servant(object):
    """
    Abstract servant which can be used along with a slice2py
    generated dispatch class as the base type of servants.
    These provide resource cleanup as per the omero.util.Server
    class.
    """

    def __init__(self):
        self.resources = omero.util.Resources()
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.info("Initialized")

    def cleanup(self):
        """
        Cleanups all resoures created by this servant. Calling
        cleanup multiple times should be safe.
        """
        resources = self.resources
        self.resources = None
        if resources != None:
            self.logger.info("Cleaning up")
            resources.cleanup()
            self.logger.info("Done")

    def __del__(self):
        self.cleanup()


class Task(threading.Thread):
    """
    Thread which is used to periodically call
    a task and then sleep for a given time.

    Use stop() to cancel the thread. Joins by
    default.
    """

    def __init__(self, sleeptime, task):

        if sleeptime < 5:
            raise exceptions.Exception("Sleep time should be greater than 5")

        threading.Thread.__init__(self)
        self.sleeptime = sleeptime
        self.task = task
        self.exit = False
        self.logger = logging.getLogger("omero.util.Task")

    def run(self):
        self.logger.info("Starting")
        while not self.exit:
            try:
                self.task()
            except:
                self.logger.warning("Exception on task", exc_info = True)
            self.logger.debug("Sleeping")
            self.sleep()
        self.logger.info("Stopping")

    def sleep(self):
        start = time.time()
        while not self.exit and (time.time() - start) < self.sleeptime:
            time.sleep(5)

    def stop(self, wait = True):
        """
        Should be called by another thread.
        """
        self.logger.info("Stop called")
        self.exit = True
        if wait:
            self.logger.info("Waiting on task")
            self.join()
            self.logger.info("Stopped")


class Resources:
    """
    Container class for storing resources which should be
    cleaned up on close and periodically checked.
    """

    def __init__(self, sleeptime = 60):
        """
        Add resources via add(object). They should have a no-arg cleanup()
        and a check() method.

        The check method will be called periodically (default: 60 seconds)
        on each resource. The cleanup method will be called on
        Resources.cleanup()
        """

        self.logger = logging.getLogger("omero.util.Resources")
        self.logger.info("Starting")

        self.stuff = []
        def task():
            remove = []
            for m in self.stuff:
                self.logger.info("Checking %s" % m[0])
                method = getattr(m[0],m[2])
                if not method():
                    remove.append(m)
            for r in remove:
                self.logger.info("Removing %s" % r[0])
                self.stuff.remove(r)

        self.thread = Task(sleeptime, task)
        self.thread.start()

    def add(self, object, cleanupMethod = "cleanup", checkMethod = "check"):
        lock = threading.RLock()
        lock.acquire()
        try:
            entry = (object,cleanupMethod,checkMethod)
            self.logger.info("Adding object " % object)
            self.stuff.append(entry)
        finally:
            lock.release()

    def cleanup(self):
        """
        """
        lock = threading.RLock()
        lock.acquire()
        self.thread.stop(wait=False)
        try:
            for m in self.stuff:
                try:
                    m = self.stuff.pop(0)
                    self.logger.info("Cleaning %s" % m[0])
                    method = getattr(m[0],m[1])
                    method()
                except:
                    print "Error cleaning resource:", m
                    traceback.print_exc()
            self.stuff = None
            self.logger.info("Stopping")
        finally:
            lock.release()
            self.thread.stop(wait=True)
            self.logger.info("Stopped")

class Environment:
    """
    Simple class for creating an executable environment
    """

    def __init__(self,*args):
        """
        Takes an number of environment variable names which
        should be copied to the target environment if present
        in the current execution environment.
        """
        self.env = {}
        for arg in args:
            if os.environ.has_key(arg):
                self.env[arg] = os.environ[arg]
    def __call__(self):
        """
        Returns the environment map when called.
        """
        return self.env

    def set(self, key, value):
        """
        Manually sets a value in the target environment.
        """
        self.env[key] = value

    def append(self, key, addition):
        """
        Manually adds a value to the environment string
        """
        if self.env.has_key(key):
            self.env[key] = os.pathsep.join([self.env[key], addition])
        else:
            self.set(key, addition)
