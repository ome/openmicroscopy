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

import sys, exceptions, omero

class Type:
    def __init__(self, name, optional = False):
        self.name = name
        self.type = None
        self.optional = False
        self._in = True
        self._out = False
    def out(self):
        self._in = False
        self._out = True
        return self
    def inout(self):
        self._in = True
        self._out = True
        return self

class Long(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = omero.RLong()
class String(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = omero.RString()
class Bool(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = omero.RBool()

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
        sys.exit(0)
    else:
        return c


