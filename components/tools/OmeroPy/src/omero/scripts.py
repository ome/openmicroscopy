#!/usr/bin/env python
"""
   Scripting types
       - Classes:
           - Type        --  Top of parameter type hierarchy
           - Long        --
           - String      --
           - Bool        --
           - List 
           - Map
           - Set 
       - Functions:
           - client      -- Produces an omero.client object with given input/output constraints.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import Ice
import uuid
import exceptions

import omero
import omero_Scripts_ice
import omero.util.concurrency
from omero.rtypes import *


class Type:
    def __init__(self, name, optional = False, out = False, description = None, type = None, min = None, max = None, values = None):
        self._name = name
        self._param = omero.grid.Param()
        self._param.description = description
        self._param.optional = optional
        self._param.min = min
        self._param.max = max
        self._param.values = values
        self._in = True
        self._out = out
    def out(self):
        self._in = False
        self._out = True
        return self
    def inout(self):
        self._in = True
        self._out = True
        return self
    def optional(self):
        self._param.optional = True
        return self
    def values(self, *args):
        self._param.values = rlist([rtype(x) for x in args])
        return self
    def min(self, arg):
        self._param.min = rtype(arg)
        return self
    def max(self, arg):
        self._param.max = rtype(arg)
        return self
    def type(self, arg):
        self._param.prototype = rtype(arg)
        return self
    def param(self):
        return self._param

class Long(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type(rlong(0))
class String(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type(rstring(""))
class Bool(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type(rbool(False))
class Point(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type(rinternal(omero.Point()))
class Plane(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type(rinternal(omero.Plane()))
class Set(Type):
    def __init__(self, name, optional = False, out = False, *contents):
        Type.__init__(self, name, optional, out)
        self.type(rset(contents))
class List(Type):
    def __init__(self, name, optional = False, out = False, *contents):
        Type.__init__(self, name, optional, out)
        self.type(rlist(contents))
class Map(Type):
    def __init__(self, name, optional = False, out = False, **contents):
        Type.__init__(self, name, optional, out)
        self.type(rmap(contents))

class ParseExit(exceptions.Exception):
    """
    Raised when this script should just parse parameters and return.
    """

    def __init__(self, params):
        exceptions.Exception.__init__(self)
        self.params = params

def client(*args, **kwargs):
    """
    Entry point for all script engine scripts.

    Typical usage consists of::

        client = omero.scripts.client("name","description", \
            omero.scripts.Long("name"),...)

    where the returned client is created via the empty constructor to omero.client
    using only --Ice.Config or ICE_CONFIG, and the function arguments are taken
    as metdata about the current script. With this information, all script
    consumers should be able to determine the required types for execution.

    Possible types are all subclasses of omero.scripts.Type

    To change the omero.model.Format of the stdout and stderr produced by
    this script, use the constructor arguments::

        client = omero.scripts.client(..., \
            stdoutFormat = "text/plain",
            stderrFormat = "text/plain")

    If you would like to prevent stdout and/or stderr from being
    uploaded, set the corresponding value to None. If you would like
    to overwrite the value with another file, use
    client.setOutput(). Though it is possible to attach any RType to
    "stdout" or "stderr", there is an assumption that the value will
    be an robject(OriginalFileI())

    Providing your own client is possible via the kwarg "client = ...",
    but be careful since this may break usage with the rest of the
    scripting framework. The client should not have a session, and
    must be configured for the argumentless version of createSession()
    """

    args = list(args)
    if len(args) >= 1:
        if isinstance(args[0], str):
            kwargs["name"] = args.pop(0)
    if len(args) >= 1:
        if isinstance(args[0], str):
            kwargs["description"] = args.pop(0)

    if not kwargs.has_key("client"):
        kwargs["client"] = omero.client()
    c = kwargs["client"]

    if args and isinstance(args[0], omero.grid.JobParams):
        c.params = args.pop(0)
    else:
        c.params = omero.grid.JobParams()
        c.params.inputs = {}
        c.params.outputs = {}

    for k, v in kwargs.items():
        if hasattr(c.params, k):
            setattr(c.params, k, v)

    if not c.params.stdoutFormat:
        c.params.stdoutFormat = "text/plain"

    if not c.params.stderrFormat:
        c.params.stderrFormat = "text/plain"

    for p in args:
        if isinstance(p, Type):
            param = p.param()
            if p._in:
                c.params.inputs[p._name] = param
            if p._out:
                c.params.outputs[p._name] = param
        else:
            raise ValueError("Not Type: %s" % type(p))

    c.createSession().detachOnDestroy()
    handleParse(c) # May throw
    return c

def handleParse(c):
    if len(c.getProperty("omero.scripts.parse")) > 0: # TODO Add to omero/Constants.ice
        c.setOutput("omero.scripts.parse", rinternal(c.params))
        raise ParseExit(c.params)

def error_msg(category, value, *args):
    c = "%-15.15s" % (category.upper())
    s = "\t%s ---   %s\n" % (c, value)
    return s % args

def compare_proto(proto, input, cache=None):

    if cache is None:
        cache = {}

    if id(proto) in cache and id(input) in cache:
        return "" # Prevent StackOverflow
    else:
        cache[id(proto)] = True
        cache[id(input)] = True

    itype = input is None and None or input.__class__
    ptype = proto is None and None or proto.__class__

    if not isinstance(input, ptype):
        return error_msg("Wrong type", "%s != %s", itype, ptype)

    # Now recurse if a collection type
    errors = ""
    if isinstance(proto, omero.RMap) and len(proto.val) > 0:
        for x in input.val.values():
            errors += compare_proto(proto.val.values()[0], x, cache)
    elif isinstance(proto, omero.RCollection) and len(proto.val) > 0:
        for x in input.val:
            errors += compare_proto(proto.val[0], x, cache)
    return errors

def expand(input):
    if input is None:
        items = []
    elif isinstance(input, (list, tuple)):
        items = list(input)
    elif isinstance(input, dict):
        items = input.values()
    else:
        items = [input]
    return items

def check_boundaries(min, max, input):
    errors = ""

    # Unwrap
    min = unwrap(min)
    max = unwrap(max)
    input = unwrap(input)
    items = expand(input)

    # Check
    for x in items:
        if min is not None and min > x:
            errors += error_msg("Out of bounds", "%s is below min %s", x, min)
        if max is not None and max < x:
            errors += error_msg("Out of bounds", "%s is above max %s", x, max)
    return errors

def check_values(values, input):
    errors = ""

    # Unwrap
    values = unwrap(values)
    input = unwrap(input)
    items = expand(input)
    values = expand(values)

    if not values:
        return errors

    for x in items:
        if x not in values:
            errors += error_msg("Value list", "%s not in %s", x, values)

    return errors

def validate_inputs(params, inputs):
    errors = ""
    for key, param in params.inputs.items():
        if key not in inputs:
            if not param.optional:
                errors += error_msg("Missing input", "%s", key)
        else:
            input = inputs[key]
            errors += compare_proto(param.prototype, input)
            errors += check_boundaries(param.min, param.max, input)
            errors += check_values(param.values, input)
    return errors


class ProcessCallbackI(omero.grid.ProcessCallback):
    """
    Simple callback which registers itself with the given process.
    """

    FINISHED = "FINISHED"
    CANCELLED = "CANCELLED"
    KILLED = "KILLED"

    def __init__(self, adapter_or_client, process):
        self.event = omero.util.concurrency.get_event()
        self.result = None
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
        self.result = ProcssCallbackI.KILLED
        self.event.set()

    def close(self):
         self.adapter.remove(self.id) # OK ADAPTER USAGE

