#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Callbacks to be used with asynchronous services. The
   ProcessCallbackI is also included in the omero.scripts
   module for backwards compatibility.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import Ice
import logging
import threading
import uuid

import omero
import omero.all
import omero.util.concurrency


PROC_LOG = logging.getLogger("omero.scripts.ProcessCallback")
DEL_LOG = logging.getLogger("omero.api.DeleteCallback")
CMD_LOG = logging.getLogger("omero.cmd.CmdCallback")


def adapter_and_category(adapter_or_client, category):
    if isinstance(adapter_or_client, Ice.ObjectAdapter):
        # This should be the case either when an
        # instance is created server-side or when
        # the user has passed in a category
        # explicitly. If it's missing, then we'll
        # have to throw
        if not category:
            raise omero.ClientError("No category available")
        return adapter_or_client, category
    else:
        # This is the case client-side, where an
        # omero.client instance is available.
        # If a category is passed we use that
        # (though it's unlikely that that will be useful)
        if not category:
            category = adapter_or_client.getCategory()
        return adapter_or_client.getAdapter(), category


class ProcessCallbackI(omero.grid.ProcessCallback):
    """
    Simple callback which registers itself with the given process.
    """

    FINISHED = "FINISHED"
    CANCELLED = "CANCELLED"
    KILLED = "KILLED"

    def __init__(self, adapter_or_client, process, poll=True, category=None):
        self.event = omero.util.concurrency.get_event(name="ProcessCallbackI")
        self.result = None
        self.poll = poll
        self.process = process
        self.adapter, self.category = \
            adapter_and_category(adapter_or_client, category)

        self.id = Ice.Identity(str(uuid.uuid4()), self.category)
        self.prx = self.adapter.add(self, self.id)  # OK ADAPTER USAGE
        self.prx = omero.grid.ProcessCallbackPrx.uncheckedCast(self.prx)
        process.registerCallback(self.prx)

    def block(self, ms):
        """
        Should only be used if the default logic of the process methods is
        kept in place. If "event.set" does not get called, this method will
        always block for the given milliseconds.
        """
        if self.poll:
            try:
                rc = self.process.poll()
                if rc is not None:
                    self.processFinished(rc.getValue())
            except Exception, e:
                PROC_LOG.warn("Error calling poll: %s" % e)

        self.event.wait(float(ms) / 1000)
        if self.event.isSet():
            return self.result
        return None

    def processCancelled(self, success, current=None):
        self.result = ProcessCallbackI.CANCELLED
        self.event.set()

    def processFinished(self, returncode, current=None):
        self.result = ProcessCallbackI.FINISHED
        self.event.set()

    def processKilled(self, success, current=None):
        self.result = ProcessCallbackI.KILLED
        self.event.set()

    def close(self):
        self.adapter.remove(self.id)  # OK ADAPTER USAGE


class CmdCallbackI(omero.cmd.CmdCallback):
    """
    Callback servant used to wait until a HandlePrx would
    return non-null on getReponse. The server will notify
    of completion to prevent constantly polling on
    getResponse. Subclasses can override methods for handling
    based on the completion status.

    Example usage::

        cb = CmdCallbackI(client, handle)
        response = None
        while (response is None):
            response = cb.block(500)

        # or

        response = cb.loop(5, 500)
    """

    def __init__(self, adapter_or_client, handle, category=None,
                 foreground_poll=True):

        if adapter_or_client is None:
            raise omero.ClientError("Null client")

        if handle is None:
            raise omero.ClientError("Null handle")

        self.event = omero.util.concurrency.get_event(name="CmdCallbackI")
        self.state = (None, None)  # (Response, Status)
        self.handle = handle
        self.adapter, self.category = \
            adapter_and_category(adapter_or_client, category)

        self.id = Ice.Identity(str(uuid.uuid4()), self.category)
        self.prx = self.adapter.add(self, self.id)  # OK ADAPTER USAGE
        self.prx = omero.cmd.CmdCallbackPrx.uncheckedCast(self.prx)
        handle.addCallback(self.prx)
        self.initialPoll(foreground_poll)

    def initialPoll(self, foreground_poll=False):
        """
        Called at the end of construction to check a race condition.

        If HandlePrx finishes its execution before the
        CmdCallbackPrx has been sent set via addCallback,
        then there's a chance that this implementation will never
        receive a call to finished, leading to perceived hangs.

        By default, this method starts a background thread and
        calls poll(). An Ice.ObjectNotExistException
        implies that another caller has already closed the
        HandlePrx. By passing, foreground_poll=True, the poll()
        invocation can be performed in the calling thread as in
        5.1.0 and before.
        """
        if foreground_poll:
            return self.poll()

        class T(threading.Thread):

            def run(this):
                try:
                    self.poll()
                except:
                    # don't throw any exceptions, e.g. if the
                    # handle has already been closed.
                    self.onFinished(None, None, None)

        T().start()

    #
    # Local invocations
    #

    def getResponse(self):
        """
        Returns possibly null Response value. If null, then neither has
        the remote server nor the local poll method called finish
        with non-null values.
        """
        return self.state[0]

    def getStatus(self):
        """
        Returns possibly null Status value. If null, then neither has
        the remote server nor the local poll method called finish
        with non-null values.
        """
        return self.state[1]

    def getStatusOrThrow(self):
        s = self.getStatus()
        if not s:
            raise omero.ClientError("Status not present!")
        return s

    def isCancelled(self):
        """
        Returns whether Status::CANCELLED is contained in
        the flags variable of the Status instance. If no
        Status is available, a ClientError will be thrown.
        """
        s = self.getStatusOrThrow()
        try:
            s.flags.index(omero.cmd.State.CANCELLED)
            return True
        except:
            return False

    def isFailure(self):
        """
        Returns whether Status::FAILURE is contained in
        the flags variable of the Status instance. If no
        Status is available, a ClientError will be thrown.
        """
        s = self.getStatusOrThrow()
        try:
            s.flags.index(omero.cmd.State.FAILURE)
            return True
        except:
            return False

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
        found = False
        while count < loops:
            count += 1
            found = self.block(ms)
            if found:
                break

        if found:
            return self.getResponse()
        else:
            waited = (ms / 1000.0) * loops
            raise omero.LockTimeout(
                None, None, "Command unfinished after %s seconds" % waited,
                5000L, int(waited))

    def block(self, ms):
        """
        Blocks for the given number of milliseconds unless
        finished(Response, Status, Current) has been called in
        which case it returns immediately with true. If false
        is returned, then the timeout was reached.
        """
        self.event.wait(float(ms) / 1000)
        return self.event.isSet()

    #
    # Remote invocations
    #

    def poll(self):
        """
        Calls HandlePrx#getResponse in order to check for a
        non-null value. If so, {@link Handle#getStatus} is also called, and the
        two non-null values are passed to finished(Response, Status, Current).
        This should typically not be used. Instead, favor the use of block and
        loop.
        """
        rsp = self.handle.getResponse()
        if rsp is not None:
            s = self.handle.getStatus()
            # Only time that current should be null
            self.finished(rsp, s, None)

    def step(self, complete, total, current=None):
        """
        Called periodically by the server to signal that processing is
        moving forward. Default implementation does nothing.
        """
        pass

    def finished(self, rsp, status, current=None):
        """
        Called when the command has completed whether with
        a cancellation or a completion.
        """
        self.state = (rsp, status)
        self.event.set()
        self.onFinished(rsp, status, current)

    def onFinished(self, rsp, status, current):
        """
        Method intended to be overridden by subclasses. Default logic does
        nothing.
        """
        pass

    def close(self, closeHandle):
        """
        First removes self from the adapter so as to no longer receive
        notifications, and the calls close on the remote handle if requested.
        """
        self.adapter.remove(self.id)  # OK ADAPTER USAGE
        if closeHandle:
            self.handle.close()
