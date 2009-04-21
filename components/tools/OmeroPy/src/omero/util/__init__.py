#!/usr/bin/env python
#
# OMERO Utilities package
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import Ice
import omero
import threading

from omero.rtypes import *

class Server(Ice.Application):
    """
    Basic server implementation which can be used for
    implementing a standalone python server which can
    be started from icegridnode.

    The servant implementation MUST:
     * have the following __init__ signature: (logger)
     * have a cleanup() method
     * not provide a "serverid" attribute (will be assigned)

    Usage:

    if __name__ == "__main__":
        app=Server(ServicesI, "ServicesAdapter", Ice.Identity("Services",""))
        sys.exit(app.main(sys.argv))

    app.impl now points to an instance of ServicesI

    """

    def __init__(self, impl_class, adapter_name, identity):
        self.impl_class = impl_class
        self.adapter_name = adapter_name
        self.identity = identity

    def run(self,args):
        self.shutdownOnInterrupt()
        try:
            self.objectfactory = omero.ObjectFactory()
            self.objectfactory.registerObjectFactory(self.communicator())
            for of in ObjectFactories.values():
                of.register(self.communicator())
            self.adapter = self.communicator().createObjectAdapter(self.adapter_name)
            self.impl = self.impl_class(self.communicator().getLogger())
            self.impl.serverid = self.communicator().getProperties().getProperty("Ice.ServerId")

            self.adapter.add(self.impl, self.identity)
            self.adapter.activate()
        finally:
            self.communicator().waitForShutdown()
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

class Resources:
    """
    Container class for storing resources which should be
    cleaned up on close.
    """
    def __init__(self):
        self.stuff = []
    def add(self, object, cleanupMethod = "cleanup"):
        lock = threading.RLock()
        lock.acquire()
        try:
            self.stuff.append((object,cleanupMethod))
        finally:
            lock.release()
    def cleanupNext(self):
        lock = threading.RLock()
        lock.acquire()
        try:
            try:
                if len(self.stuff) > 0:
                    m = self.stuff.pop(0)
                    method = getattr(m[0],m[1])
                    method()
                    return len(self.stuff) > 0
                else:
                    return False
            except:
                print "Error cleaning resource:",m
                traceback.print_exc()
        finally:
            lock.release()


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
