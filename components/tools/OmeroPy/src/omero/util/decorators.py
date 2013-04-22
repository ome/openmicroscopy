#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Decorators
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import time
import logging
import threading
import traceback

import omero

from omero_ext.functional import wraps

perf_log = logging.getLogger("omero.perf")

def perf(func):
    """ Decorator for (optionally) printing performance statistics """
    def handler(*args, **kwargs):

        # Early Exit. Can't do this in up a level
        # because logging hasn't been configured yet.
        lvl = perf_log.getEffectiveLevel()
        if lvl > logging.DEBUG:
            return func(*args, **kwargs)

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
            perf_log.debug("start[%d] time[%d] tag[%s]", startMillis, timeMillis, tag)
    handler = wraps(func)(handler)
    return handler


__FORMAT = "%-.120s"
__RESULT = " Rslt: " + __FORMAT
__EXCEPT = " Excp: " + __FORMAT
def remoted(func):
    """ Decorator for catching any uncaught exception and converting it to an InternalException """
    log = logging.getLogger("omero.remote")
    def exc_handler(*args, **kwargs):
        try:
            self = args[0]
            log.info(" Meth: %s.%s", self.__class__.__name__, func.func_name)
            rv = func(*args, **kwargs)
            log.info(__RESULT, rv)
            return rv
        except Exception, e:
            log.info(__EXCEPT, e)
            if isinstance(e, omero.ServerError):
                raise
            else:
                log.warn("%s raised a non-ServerError (%s): %s", func, type(e), e)
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


class TimeIt (object):
    """
    Decorator to measure the execution time of a function. Assumes that a C{logger} global var
    is available and is the logger instance from C{logging.getLogger()}.

    @param level: the level to use for logging
    @param name: the name to use when logging, function name is used if None
    """
    logger = logging.getLogger('omero.timeit')

    def __init__ (self, level=logging.DEBUG, name=None):
        self._level = level
        self._name = name
 
    def __call__ (self, func):
        def wrapped (*args, **kwargs):
            name = self._name or func.func_name
            self.logger.log(self._level, "timing %s" % (name))
            now = time.time()
            rv = func(*args, **kwargs)
            self.logger.log(self._level, "timed %s: %f" % (name, time.time()-now))
            return rv
        return wrapped

def timeit (func):
    """
    Shortcut version of the L{TimeIt} decorator class.
    Logs at logging.DEBUG level.
    """
    def wrapped (*args, **kwargs):
        TimeIt.logger.log(self._level, "timing %s" % (func.func_name))
        now = time.time()
        rv = func(*args, **kwargs)
        TimeIt.logger.log(self._level, "timed %s: %f" % (func.func_name, time.time()-now))
        return rv
    return TimeIt()(func)

def setsessiongroup (func):
    """
    For BlitzObjectWrapper class derivate functions, sets the session group to match
    the object group.
    """
    def wrapped (self, *args, **kwargs):
        rev = self._conn.setGroupForSession(self.getDetails().getGroup().getId())
        try:
            return func(self, *args, **kwargs)
        finally:
            if rev:
                self._conn.revertGroupForSession()
    return wrapped
