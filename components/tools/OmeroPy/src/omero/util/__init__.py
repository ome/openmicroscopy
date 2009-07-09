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
            self.adapter.activate()
            self.logger.info("Activating")
        finally:
            self.logger.info("Waiting for shutdown")
            self.communicator().waitForShutdown()
            self.logger.info("Cleanup")
            self.cleanup()

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
        self.logger = logging.getLogger("omero.util.Resources")

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
            self.logger.info("Disabling")
        finally:
            lock.release()
            self.thread.stop(wait=True)

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
