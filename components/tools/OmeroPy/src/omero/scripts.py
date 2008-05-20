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
        self.type = omero.RLong()
class String(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = omero.RString()
class Bool(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = omero.RBool()
class Point(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = omero.RInternal(omero.Point())
class Plane(Type):
    def __init__(self, name, optional = False, out = False):
        Type.__init__(self, name, optional, out)
        self.type = omero.RInternal(omero.Plane())
class Set(Type):
    def __init__(self, name, optional = False, out = False, *contents):
        Type.__init__(self, name, optional, out)
        self.type = omero.RSet(contents)
class Map(Type):
    def __init__(self, name, optional = False, out = False, **contents):
        Type.__init__(self, name, optional, out)
        self.type = omero.RMap(contents)

def client(name, description = None, *args):
    """
    Entry point for all script engine scripts.

    Typical usage consists of:

      client = omero.scripts.client("name","description", omero.scripts.Long("name"),...)

    where the returned client is created via the empty constructor to omero.client
    using only --Ice.Config or ICE_CONFIG, and the function arguments are taken
    as metdata about the current script. With this information, all script
    consumers should be able to determine the required types for execution.

    Possible types are all subclasses of omero.scripts.Type

    """
    c = omero.client()
    if len(c.getProperty("omero.scripts.parse")) > 0: # Add to omero/Constants.ice
        params = omero.grid.JobParams()
        params.name = name
        params.description = description
        params.inputs = {}
        params.outputs = {}
        for p in args:
            param = omero.grid.Param()
            param.name = p.name
            param.optional = p.optional
            param.prototype = p.type
            if p._in:
                params.inputs[p.name] = param
            if p._out:
                params.outputs[p.name] = param
        print params

        c.createSession()
        c.setOutput("omero.scripts.parse", omero.RInternal(params))
        pysys.exit(0)
    else:
        return c


