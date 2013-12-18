#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
import logging

import omero
import omero.callbacks
import omero.util.concurrency
import omero.util.temp_files

from omero.rtypes import *


TYPE_LOG = logging.getLogger("omero.scripts.Type")
PROC_LOG = logging.getLogger("omero.scripts.ProcessCallback")


class Type(omero.grid.Param):
    """
    omero.grid.Param subclass which provides convenience methods for input/output specification.
    Further subclasses are responsible for creating proper prototypes.

    kwargs
    """
    PROTOTYPE_FUNCTION = None
    PROTOTYPE_DEFAULT = None
    PROTOTYPE_MIN = None
    PROTOTYPE_MAX = None
    PROTOTYPE_VALUES = None

    def __init__(self, name, optional = True, out = False, description = None, default = None, **kwargs):

        # Non-Param attributes
        omero.grid.Param.__init__(self)

        # Non-Param attributes
        self._name = name
        self._in = True
        self._out = out

        # Other values will be filled in by the kwargs
        # Mostly leaving these for backwards compatibility
        self.description = description
        self.optional = optional

        # Assign all the kwargs
        for k, v in kwargs.items():
            if not hasattr(self, k):
                TYPE_LOG.warn("Unknown property: %s", k)
            setattr(self, k, v)

        _DEF = self.__get(self.PROTOTYPE_DEFAULT, False)
        _FUN = self.__get(self.PROTOTYPE_FUNCTION)
        _MAX = self.__get(self.PROTOTYPE_MAX)
        _MIN = self.__get(self.PROTOTYPE_MIN)
        _VAL = self.__get(self.PROTOTYPE_VALUES)

        # Someone specifically set the prototype, then
        # we assume that useDefault should be True
        if default is not None: # For whatever reason, inheritance isn't working.
            newfunc = _FUN
            newdefault = default
            if isinstance(self, List):
                if isinstance(default, (list, tuple)):
                    newdefault = wrap(default).val
                elif isinstance(default, omero.RCollection):
                    newfunc = lambda x: x
                elif isinstance(default, omero.RType):
                    default = [default]
                else:
                    newfunc = lambda x: x
                    newdefault = rlist([rtype(default)])
            self.useDefault = True
            self.prototype = newfunc(newdefault)
        else:
            if not callable(_FUN):
                raise ValueError("Bad prototype function: %s" % _FUN)

            # To prevent weirdness, if the class default is
            # callable, we'll assume its a constructor and
            # create a new one to prevent modification.
            try:
                _def = _DEF()
            except TypeError:
                _def = _DEF
            self.prototype = _FUN(_def)

        # The following use wrap to guarantee that an rtype is present
        if self.min is not None:
            if _MIN is None:
                self.min = _FUN(self.min)
            else:
                self.min = _MIN(self.min)

        if self.max is not None:
            if _MAX is None:
                self.max = _FUN(self.max)
            else:
                self.max = _MAX(self.max)

        if self.values is not None:
            if _VAL is None:
                self.values = wrap(self.values)
            else:
                self.values = _VAL(self.values)

        # Now if useDefault has been set, either manually, or
        # via setting default="..." we check that the default
        # value matches a value if present
        if self.values is not None and self.values and self.useDefault:
            if isinstance(self.prototype, omero.RCollection):
                test = unwrap(self.prototype.val[0])
            else:
                test = unwrap(self.prototype)
            values = unwrap(self.values)
            if test not in values:
                raise ValueError("%s is not in %s" % (test, values))


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

    def __get(self, val, func = True):
        if val is not None:
            if func:
                return val.im_func
            else:
                return val

class Object(Type):
    """
    Wraps an robject
    """
    PROTOTYPE_FUNCTION = robject
    PROTOTYPE_DEFAULT = None


class Long(Type):
    """
    Wraps an rlong
    """
    PROTOTYPE_FUNCTION = rlong
    PROTOTYPE_DEFAULT = 0


class Int(Type):
    """
    Wraps an rint
    """
    PROTOTYPE_FUNCTION = rint
    PROTOTYPE_DEFAULT = 0


class Double(Type):
    """
    Wraps an rdouble
    """
    PROTOTYPE_FUNCTION = rdouble
    PROTOTYPE_DEFAULT = 0.0


class Float(Type):
    """
    Wraps an rfloat
    """
    PROTOTYPE_FUNCTION = rfloat
    PROTOTYPE_DEFAULT = 0.0


class String(Type):
    """
    Wraps an rstring
    """
    PROTOTYPE_FUNCTION = rstring
    PROTOTYPE_DEFAULT = ""


class Bool(Type):
    """
    Wraps an rbool
    """
    PROTOTYPE_FUNCTION = rbool
    PROTOTYPE_DEFAULT = False


class Color(Type):
    """
    Wraps an rinternal(Color)
    """
    PROTOTYPE_FUNCTION = rinternal
    PROTOTYPE_DEFAULT = omero.Color


class Point(Type):
    """
    Wraps an rinternal(Point)
    """
    PROTOTYPE_FUNCTION = rinternal
    PROTOTYPE_FUNCTION = omero.Point


class Plane(Type):
    """
    Wraps an rinternal(Plane)
    """
    PROTOTYPE_FUNCTION = rinternal
    PROTOTYPE_DEFAULT = omero.Plane


class __Coll(Type):
    """
    Base type providing the append and extend functionality.
    Not for user use.
    """
    PROTOTYPE_DEFAULT = list

    def append(self, *arg):
        self.prototype.val.append(*arg)
        return self

    def extend(self, *arg):
        self.prototype.val.extend(*arg)
        return self

    def ofType(self, obj):
        if callable(obj):
            obj = obj() # Ctors, etc.

        # If someone used default=, then "ofType()" is not necessary
        # and so we check for their correspondence.
        if self.useDefault and self.prototype.val:
            if not isinstance(obj, self.prototype.val[0].__class__):
                raise ValueError("ofType values doesn't match default value: %s <> %s" % (unwrap(obj), unwrap(self.prototype.val[0])))
        else:
            self.prototype.val.append(wrap(obj))

        return self

class Set(__Coll):
    """
    Wraps an rset. To add values to the contents of the set,
    use "append" or "extend" since set.val is of type list.
    """
    PROTOTYPE_FUNCTION = rset


class List(__Coll):
    """
    Wraps an rlist. To add values to the contents of the list,
    use "append" or "extend" since set.val is of type list.
    """
    PROTOTYPE_FUNCTION = rlist


class Map(Type):
    """
    Wraps an rmap. To add values to the contents of the map,
    use "update" since map.val is of type dict.
    """
    PROTOTYPE_FUNCTION = rmap
    PROTOTYPE_DEFAULT = dict

    def update(self, *args, **kwargs):
        self.prototype.val.update(*args, **kwargs)
        return self


class ParseExit(Exception):
    """
    Raised when this script should just parse parameters and return.
    """

    def __init__(self, params):
        Exception.__init__(self)
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
            exec(scriptText, {"__name__":"__main__"})
            raise Exception("Did not throw ParseExit: %s" % scriptText)
        finally:
            if old:
                os.environ["ICE_CONFIG"] = old
    except ParseExit, exit:
        return exit.params

def parse_file(filename):
    """
    Parses the given script file with "omero.scripts.parse" set
    and catches the exception. The parameters are returned.

    WARNING: This method calls "exec" on the given file's contents.
    Do NOT use this on data you don't trust.
    """
    from path import path
    scriptText = path(filename).text()
    return parse_text(scriptText)


class MissingInputs(Exception):
    def __init__(self):
        Exception.__init__(self)
        self.keys = []


def parse_inputs(inputs_strings, params):
    """
    Parses a command-line like string representation of input parameters
    into an RDict (a map from string to RType). The input should be an
    iterable of arguments of the form "key=value".

    For example, "a=1" gets mapped to {"a":1}
    """
    inputs = {}
    for input_string in inputs_strings:
        rv = parse_input(input_string, params)
        inputs.update(rv)

    missing = MissingInputs()
    for key in sorted(params.inputs, key=lambda name: params.inputs.get(name).grouping):
        param = params.inputs.get(key)
        a = inputs.get(key, None)
        if not a:
            if param.useDefault:
                inputs[key] = param.prototype
            elif not param.optional:
                missing.keys.append(key)

    if missing.keys:
        missing.inputs = inputs
        raise missing

    return inputs


def parse_input(input_string, params):
    """
    Parse a single input_string. The params
    """
    parts = input_string.split("=")
    if len(parts) == 1:
        parts.append("")
    key = parts[0]
    val = parts[1]

    param = params.inputs.get(key)
    if param is None:
        return {}
    elif isinstance(param.prototype, omero.RBool):
        if val.lower() in ("false", "False", "0", ""):
            val = rbool(False)
        else:
            val = rbool(True)
    elif isinstance(param.prototype,\
        (omero.RLong, omero.RString, omero.RInt, \
            omero.RTime, omero.RDouble, omero.RFloat)):
        val = param.prototype.__class__(val)
    elif isinstance(param.prototype, omero.RList):
        items = val.split(",")
        if len(param.prototype.val) == 0:
            # Don't know what needs to be added here, so calling wrap
            # which will produce an rlist of rstrings.
            val = omero.rtypes.wrap(items)
        else:
            p = param.prototype.val[0]
            val = omero.rtypes.rlist([p.__class__(x) for x in items])
    elif isinstance(param.prototype, omero.RObject):
        try:
            parts2 = val.split(":")
            kls = parts2[0]
            _id = long(parts2[1])
            if not kls.endswith("I"):
                kls = "%sI" % kls
            kls = getattr(omero.model, kls)
        except:
            raise ValueError("Format for objects: Class:id or ClassI:id. Not:%s" % val)
        val = omero.rtypes.robject(kls(_id, False))
    else:
        raise ValueError("No converter for: %s (type=%s)" % (key, param.prototype.__class__))

    return {key:val}


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
        return ""
    except Exception, e:
        return error_msg("Failed to set intput", key, "%s=%s. Error: %s", key, value, e)

#
# Importing into omero.scripts namespace
#
ProcessCallbackI = omero.callbacks.ProcessCallbackI

def wait(client, process, ms = 500):
    """
    Wrapper around the use of ProcessCallbackI
    """
    cb = ProcessCallbackI(client, process)
    while cb.block(500) is None:
        process.poll()
