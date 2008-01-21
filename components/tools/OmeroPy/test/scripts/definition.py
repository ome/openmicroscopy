#!/usr/bin/env python
#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import omero

client = omero.script("script_1", """
    This is a test script used to test the basic parsing functionality
    and attempts to interaction with the server
    """,
    myint="int", mylong="long", mybool="bool",
    mystring="string", myoptional="string*"
    )


import os, sys, types
assert type(client) == types.DictType

self =  sys.argv[0]
cfg  = self.replace("py","cfg")

real_client = omero.client(["--Ice.Config=%s" % cfg])
parse_only = real_client.getProperty("omero.script.parse")
assert parse_only == "true"


