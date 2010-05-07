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

import os
import Ice
import uuid
import logging
import exceptions

import omero
import omero_Scripts_ice
import omero.util.concurrency
import omero.util.temp_files

from omero.rtypes import *


TYPE_LOG = logging.getLogger("omero.scripts.Type")


class Type(omero.grid.Param):
    """
    omero.grid.Param subclass which provides convenience methods for input/output specification.
    Further subclasses are responsible for creating proper prototypes.

    kwargs
    """
    PROTOTYPE_FUNCTION = None

    def __init__(self, name, optional = False, out = False, description = None, **kwargs):

	omero.grid.Param.__init__(self)

        # Non-Param attributes
        self._name = name
        self._in = True
        self._out = out

        # Other values will be filled in by the kwargs
        # Mostly leaving these for backwards compatibility
        self.description = description
        self.optional = optional

        # First assign all the kwargs
        for k, v in kwargs.items():
            if not hasattr(self, k):
                TYPE_LOG.warn("Unknown property: %s", k)
            setattr(self, k, v)

        # The following use wrap to guarantee that an rtype is present
        self.min = wrap(self.min)
        self.max = wrap(self.max)
        self.values = wrap(self.values)

        if not callable(self.__class__.PROTOTYPE_FUNCTION):
            raise ValueError("Bad prototype function: %s" % self.__class__.PROTOTYPE_FUNCTION)

        self.prototype = self.__class__.PROTOTYPE_FUNCTION(self)

    def out(self):
        self._in = False
        self._out = True
        return self

    def inout(self):
        self._in = True
        self._out = True
        return self

    def type(self, *arg):
        self.prototype = wrap(arg)
        return self


class Long(Type):
    """
    Wraps an rlong
    """
    PROTOTYPE_FUNCTION = lambda self: rlong(0)


class String(Type):
    """
    Wraps an rstring
    """
    PROTOTYPE_FUNCTION = lambda self: rstring("")


class Bool(Type):
    """
    Wraps an rbool
    """
    PROTOTYPE_FUNCTION = lambda self: rbool(False)


class Color(Type):
    """
    Wraps an rinternal(Color)
    """
    PROTOTYPE_FUNCTION = lambda self: rinternal(omero.Color())


class Point(Type):
    """
    Wraps an rinternal(Point)
    """
    PROTOTYPE_FUNCTION = lambda self: rinternal(omero.Point())


class Plane(Type):
    """
    Wraps an rinternal(Plane)
    """
    PROTOTYPE_FUNCTION = lambda self: rinternal(omero.Plane())


class __Coll(Type):
    """
    Base type providing the append and extend functionality.
    Not for user use.
    """

    def append(self, *arg):
        self.prototype.val.append(*arg)

    def extend(self, *arg):
        self.prototype.val.extend(*arg)


class Set(__Coll):
    """
    Wraps an rset. To add values to the contents of the set,
    use "append" or "extend" since set.val is of type list.
    """
    PROTOTYPE_FUNCTION = lambda self: rset()


class List(__Coll):
    """
    Wraps an rlist. To add values to the contents of the list,
    use "append" or "extend" since set.val is of type list.
    """
    PROTOTYPE_FUNCTION = lambda self: rlist()


class Map(Type):
    """
    Wraps an rmap. To add values to the contents of the map,
    use "update" since map.val is of type dict.
    """
    PROTOTYPE_FUNCTION = lambda self: rmap()

    def update(self, *args, **kwargs):
        self.prototype.val.update(*args, **kwargs)


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
    c.setAgent("OMERO.scripts")

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
            if p._in:
                c.params.inputs[p._name] = p
            if p._out:
                c.params.outputs[p._name] = p
        else:
            raise ValueError("Not Type: %s" % type(p))

    handleParse(c) # May throw

    c.createSession().detachOnDestroy()
    return c

def handleParse(c):
    """
    Raises ParseExit if the client has the configuration property
    "omero.scripts.parse". If the value is anything other than "only",
    then the parameters will also be sent to the server.
    """
    parse = c.getProperty("omero.scripts.parse")
    if len(parse) > 0: # TODO Add to omero/Constants.ice
        if parse != "only":
            c.createSession().detachOnDestroy()
            c.setOutput("omero.scripts.parse", rinternal(c.params))
        raise ParseExit(c.params)

def parse_text(scriptText):
    """
    Parses the given script text with "omero.scripts.parse" set
    and catches the exception. The parameters are returned.

    WARNING: This method calls "exec" on the given text.
    Do NOT use this on data you don't trust.
    """
    try:
        cfg = omero.util.temp_files.create_path()
        cfg.write_lines(["omero.scripts.parse=only", "omero.host=localhost"])
        old = os.environ.get("ICE_CONFIG")
        try:
            os.environ["ICE_CONFIG"] = cfg.abspath()
            exec(scriptText)
        finally:
            if old:
                os.environ["ICE_CONFIG"] = old
        self.fail("Did not throw ParseExit")
    except ParseExit, exit:
        return exit.params

def parse_file(filename):
    """
    Parses the given script file with "omero.scripts.parse" set
    and catches the exception. The parameters are returned.

    WARNING: This method calls "exec" on the given file's contents.
    Do NOT use this on data you don't trust.
    """
    scriptText = path(filename).text()
    return parse_text(scriptText)


def group_params(params):
    """
    Walks through the inputs of the given JobParams
    and returns a map-of-maps with Param names as
    the leaf nodes.

    For example, for the following:

        Params("1", grouping = "A") # "A." is equivalent
        Params("2", grouping = "A.B")
        Params("3", grouping = "A.C")

    this function returns:

        {"A" {"": "1" : "B" : "2", "C" : "3"} }

    while:

        Params("1", grouping = "A")

    returns:

        {"A" : "1"}

    """
    groupings = dict()
    for k, v in params.inputs.items():

        val = v.grouping
        if not val.endswith("."):
            val = val + "."

        parts = val.split(".")

        g = groupings
        while parts:
            p = parts.pop(0)
            try:
                g = g[p]
            except KeyError:
                if parts:
                    g[p] = dict()
                    g = g[p]
                else:
                    g[p] = k

        # Now find all subtrees of the form {"": "key"} and
        # replace them by themselves
        tuples = [(groupings, k, v) for k, v in groupings.items()]
        while tuples:
            new_tuples = []
            for g, k, v in tuples:
                if isinstance(v, dict):
                    if len(v) == 1 and "" in v:
                        g[k] = v[""]
                    else:
                        new_tuples.extend([(v, k2, v2) for k2, v2 in v.items()])
            tuples = new_tuples

    return groupings

def error_msg(category, key, format_string, *args):
    c = "%s" % (category.upper())
    s = """%s for "%s": %s\n""" % (c, key, format_string)
    return s % args

def compare_proto(key, proto, input, cache=None):

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
        return error_msg("Wrong type", key, "%s != %s", itype, ptype)

    # Now recurse if a collection type
    errors = ""
    if isinstance(proto, omero.RMap) and len(proto.val) > 0:
        for x in input.val.values():
            errors += compare_proto(key, proto.val.values()[0], x, cache)
    elif isinstance(proto, omero.RCollection) and len(proto.val) > 0:
        for x in input.val:
            errors += compare_proto(key, proto.val[0], x, cache)
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

def check_boundaries(key, min, max, input):
    errors = ""

    # Unwrap
    min = unwrap(min)
    max = unwrap(max)
    input = unwrap(input)
    items = expand(input)

    # Check
    for x in items:
        if min is not None and min > x:
            errors += error_msg("Out of bounds", key, "%s is below min %s", x, min)
        if max is not None and max < x:
            errors += error_msg("Out of bounds", key, "%s is above max %s", x, max)
    return errors

def check_values(key, values, input):
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
            errors += error_msg("Value list", key, "%s not in %s", x, values)

    return errors

def validate_inputs(params, inputs, svc = None, session = None):
    """
    Method used by processor.py to check the input values
    provided by the user launching the script. If a non-empty
    errors string is returned, then the inputs fail validation.

    A service instance can be provided in order to add default
    values to the session. If no service instance is provided,
    values with a default which are missing will be counted as
    errors.
    """
    errors = ""
    for key, param in params.inputs.items():
        if key not in inputs:
            if param.optional:
                if param.useDefault and svc is not None:
                    ignore = set_input(svc, session, key, param.prototype)
            else: # Not optional
                if param.useDefault:
                    errors += set_input(svc, session, key, param.prototype)
                else:
                    errors += error_msg("Missing input", key, "")
        else:
            input = inputs[key]
            errors += compare_proto(key, param.prototype, input)
            errors += check_boundaries(key, param.min, param.max, input)
            errors += check_values(key, param.values, input)
    return errors

def set_input(svc, session, key, value):
    try:
        svc.setInput(session, key, value)
    except exceptions.Exception, e:
        return error_msg("Failed to set intput", key, "%s=%s. Error: %s", key, value, e)

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

