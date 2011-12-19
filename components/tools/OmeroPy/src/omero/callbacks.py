#!/usr/bin/env python
"""
   Callbacks to be used with asynchronous services. The
   ProcessCallbackI is also included in the omero.scripts
   module for backwards compatibility.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import Ice
import logging
import exceptions

import omero
import omero.util.concurrency
import omero_ext.uuid as uuid # see ticket:3774

from omero.rtypes import *


PROC_LOG = logging.getLogger("omero.scripts.ProcessCallback")
DEL_LOG = logging.getLogger("omero.api.DeleteCallback")


class ProcessCallbackI(omero.grid.ProcessCallback):
    """
    Simple callback which registers itself with the given process.
    """

    FINISHED = "FINISHED"
    CANCELLED = "CANCELLED"
    KILLED = "KILLED"

    def __init__(self, adapter_or_client, process, poll = True):
        self.event = omero.util.concurrency.get_event(name="ProcessCallbackI")
        self.result = None
        self.poll = poll
        self.process = process
        self.adapter = adapter_or_client
        self.id = Ice.Identity(str(uuid.uuid4()), "ProcessCallback")
        if not isinstance(self.adapter, Ice.ObjectAdapter):
            self.adapter = self.adapter.adapter
        self.prx = self.adapter.add(self, self.id) # OK ADAPTER USAGE
        self.prx = omero.grid.ProcessCallbackPrx.uncheckedCast(self.prx)
        process.registerCallback(self.prx)

    def block(self, ms):
        """
        Should only be used if the default logic of the process methods is kept
        in place. If "event.set" does not get called, this method will always
        block for the given milliseconds.
        """
        if self.poll:
            try:
                rc = self.process.poll()
                if rc is not None:
                    self.processFinished(rc.getValue())
            except exceptions.Exception, e:
                PROC_LOG.warn("Error calling poll: %s" % e)

        self.event.wait(float(ms) / 1000)
        if self.event.isSet():
            return self.result
        return None

    def processCancelled(self, success, current = None):
        self.result = ProcessCallbackI.CANCELLED
        self.event.set()

    def processFinished(self, returncode, current = None):
        self.result = ProcessCallbackI.FINISHED
        self.event.set()

    def processKilled(self, success, current = None):
        self.result = ProcessCallbackI.KILLED
        self.event.set()

    def close(self):
         self.adapter.remove(self.id) # OK ADAPTER USAGE


class DeleteCallbackI(object):
    """
    Callback used for waiting until DeleteHandlePrx will return true on
    finished(). The block(long) method will wait the given number of
    milliseconds and then return the number of errors if any or None
    if the delete is not yet complete.

    Example usage:

        cb = DeleteCallbackI(client, handle)
        errors = None
        while (errors is None):
            errors = cb.block(500)
    """

    def __init__(self, adapter_or_client, handle, poll = True):
        self.event = omero.util.concurrency.get_event(name="DeleteCallbackI")
        self.result = None
        self.poll = poll
        self.handle = handle
        self.adapter = adapter_or_client
        self.id = Ice.Identity(str(uuid.uuid4()), "DeleteHandleCallback")
        if not isinstance(self.adapter, Ice.ObjectAdapter):
            self.adapter = self.adapter.adapter
        #self.prx = self.adapter.add(self, self.id) # OK ADAPTER USAGE
        #self.prx = omero.grid.ProcessCallbackPrx.uncheckedCast(self.prx)
        #process.registerCallback(self.prx)

    def loop(self, loops, ms):
        """
        Calls block(long) "loops" number of times with the "ms"
        argument. This means the total wait time for the delete to occur
        is: loops X ms. Sensible values might be 10 loops for 500 ms, or
        5 seconds.

        @param loops Number of times to call block(long)
        @param ms Number of milliseconds to pass to block(long
        @throws omero.LockTimeout if block(long) does not return
        a non-null value after loops calls.
        """

        count = 0
        errors = None
        while errors is None and count < loops:
            errors = self.block(ms)
            count += 1

        if errors is None:
            waited = (ms / 1000) * loops
            raise omero.LockTimeout(None, None,
                    "Delete unfinished after %s seconds" % waited,
                    5000L, waited)
        else:
            return self.handle.report()

    def block(self, ms):
        """
        Should only be used if the default logic of the handle methods is kept
        in place. If "event.set" does not get called, this method will always
        block for the given milliseconds.
        """
        if self.poll:
            try:
                if self.handle.finished():
                    try:
                        self.finished(self.handle.errors())
                    except exceptions.Exception, e:
                        DEL_LOG.warn("Error calling DeleteCallbackI.finished: %s" % e, exc_info=True)
            except Ice.ObjectNotExistException, onee:
                raise omero.ClientError("Handle is gone! %s" % self.handle)
            except:
                DEL_LOG.warn("Error polling DeleteHandle:" + str(self.handle), exc_info=True)


        self.event.wait(float(ms) / 1000)
        if self.event.isSet():
            return self.result
        return None


    def finished(self, errors):
        self.result = errors
        self.event.set()

    def close(self):
        #self.adapter.remove(self.id) # OK ADAPTER USAGE
        try:
            self.handle.close() # ticket:2978
        except exceptions.Exception, e:
            DEL_LOG.warn("Error calling DeleteHandlePrx.close: %s" % self.handle, exc_info=True)



