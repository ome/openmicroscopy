#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#   $Id$
#
#   Copyright 2008-2013 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import omero, omero.scripts as sc

client = sc.client("script_1", """
    This is a test script used to test the basic parsing functionality
    and attempts to interaction with the server
    """,
    #sc.Int("myint"),
    sc.Long("mylong"),
    sc.Bool("mybool"),
    sc.String("mystring"),
    sc.String("myoptional",optional=True)
    )

import os, sys, types
assert type(client) == types.TupleType

self =  sys.argv[0]
cfg  = self.replace("py","cfg")

real_client = omero.client(["--Ice.Config=%s" % cfg])
parse_only = real_client.getProperty("omero.script.parse")
assert parse_only == "true"


