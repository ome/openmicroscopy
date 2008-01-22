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

class Type:
    def __init__(self, name, optional = False):
        self.name = name
        self.type = None
        self.optional = False
        self.out = False
    def out(self):
        self.out = true
        return self

class Long(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = "long"
class String(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = "string"
class Bool(Type):
    def __init__(self, name, optional = False):
        Type.__init__(self, name, optional)
        self.type = "bool"

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
    if "true" == c.getProperty("omero.script.parse"): # Add to omero/Constants.ice
        print "Name:       ", name
        print "Description:", description
        print "Parameters:\n",args
        return args
    else:
        return c


