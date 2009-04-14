#!/usr/bin/env python
"""
   Scripting types

   Classes:
       Type          --  Top of parameter type hierarchy
         Long        --
         String      --
         Bool        --

   Functions:
       client        -- Produces an omero.client object with
                        given input/output constraints.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions, omero
from omero_ext import pysys
from omero.rtypes import *

class Type:
    def __init__(self, name, optional = False, out = False):
        self.name = name
        self.type = None
        self.optional = False
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
        self.optional = True
        return self

class Long(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = RLongI(1)
class String(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = RStringI("")
class Bool(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = RBoolI(1)
class Point(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = rinternal(omero.Point())
class Plane(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = rinternal(omero.Plane())
class Set(Type):
    def __init__(self, name, optional = False, out = False, *contents):
        Type.__init__(self, name, optional, out)
        self.type = rset(contents)
class Map(Type):
    def __init__(self, name, optional = False, out = False, **contents):
        Type.__init__(self, name, optional, out)
        self.type = rmap(contents)

def client(name, description = None, *args, **kwargs):
    """
    Entry point for all script engine scripts.

    Typical usage consists of:

        client = omero.scripts.client("name","description", \
            omero.scripts.Long("name"),...)

    where the returned client is created via the empty constructor to omero.client
    using only --Ice.Config or ICE_CONFIG, and the function arguments are taken
    as metdata about the current script. With this information, all script
    consumers should be able to determine the required types for execution.

    Possible types are all subclasses of omero.scripts.Type

    To change the omero.model.Format of the stdout and stderr produced by
    this script, use the constructor arguments:

        client = omero.scripts.client(..., \
            stdoutFormat = "text/plain",
            stderrFormat = "text/plain")

    If you would like to prevent stdout and/or stderr from being
    uploaded, set the corresponding value to None. If you would like
    to overwrite the value with another file, use
    client.setOutput(). Though it is possible to attach any RType to
    "stdout" or "stderr", there is an assumption that the value will
    be an robject(OriginalFileI())

    """
    # Checking kwargs
    if not kwargs.has_key("stdoutFormat"):
        kwargs["stdoutFormat"]="text/plain"
    if not kwargs.has_key("stderrFormat"):
        kwargs["stderrFormat"]="text/plain"

    c = omero.client()
    c.params = omero.grid.JobParams()
    c.params.name = name
    c.params.description = description
    c.params.inputs = {}
    c.params.outputs = {}
    c.params.stdoutFormat = kwargs["stdoutFormat"]
    c.params.stderrFormat = kwargs["stderrFormat"]
    for p in args:
        param = omero.grid.Param()
        param.name = p.name
        param.optional = p.optional
        param.prototype = p.type
        if p._in:
            c.params.inputs[p.name] = param
        if p._out:
            c.params.outputs[p.name] = param
    if len(c.getProperty("omero.scripts.parse")) > 0: # Add to omero/Constants.ice
        c.createSession()
        c.setOutput("omero.scripts.parse", rinternal(c.params))
        pysys.exit(0)
    else:
        return c


