#!/usr/bin/env python
#
# OMERO Decorators
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import time
import logging
import threading
import traceback
import exceptions

import omero

from omero_ext.functional import wraps

def perf(func):
    """ Decorator for (optionally) printing performance statistics """
    log = logging.getLogger("omero.perf")
    if not log.isEnabledFor(logging.DEBUG):
        return func
    def handler(*args, **kwargs):
        try:
            self = args[0]
            mod = self.__class__.__module__
            cls = self.__class__.__name__
            tag = "%s.%s.%s" % (mod, cls, func.func_name)
        except:
            tag = func.func_name
        start = time.time()
        try:
            rv = func(*args, **kwargs)
            return rv
        finally:
            stop = time.time()
            diff = stop - start
            startMillis = int(start * 1000)
            timeMillis = int(diff * 1000)
            log.debug("start[%d] time[%d] tag[%s]", startMillis, timeMillis, tag)
    handler = wraps(func)(handler)
    return handler


def remoted(func):
    """ Decorator for catching any uncaught exception and converting it to an InternalException """
    log = logging.getLogger("omero.remote")
    def exc_handler(*args, **kwargs):
        try:
            rv = func(*args, **kwargs)
            #log.info("%s(%s,%s)=>%s" % (func, args, kwargs, rv))
            return rv
        except exceptions.Exception, e:
            log.info("%s=>%s(%s)" % (func, type(e), e))
            if isinstance(e, omero.ServerError):
                raise e
            else:
                msg = traceback.format_exc()
                raise omero.InternalException(msg, None, "Internal exception")
    exc_handler = wraps(func)(exc_handler)
    return exc_handler

def locked(func):
    """ Decorator for using the self._lock argument of the calling instance """
    def with_lock(*args, **kwargs):
        self = args[0]
        self._lock.acquire()
        try:
            return func(*args, **kwargs)
        finally:
            self._lock.release()
    with_lock = wraps(func)(with_lock)
    return with_lock


