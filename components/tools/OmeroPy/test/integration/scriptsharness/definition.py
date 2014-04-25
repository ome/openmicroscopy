#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import sys
import types

import omero
import omero.scripts as sc

client = sc.client("script_1", """
    This is a test script used to test the basic parsing functionality
    and attempts to interaction with the server
    """,
                   sc.Int("myint"),
                   sc.Long("mylong"),
                   sc.Bool("mybool"),
                   sc.String("mystring"),
                   sc.String("myoptional", optional=True)
                   )

assert isinstance(client, types.TupleType)

self = sys.argv[0]
cfg = self.replace("py", "cfg")

real_client = omero.client(["--Ice.Config=%s" % cfg])
parse_only = real_client.getProperty("omero.script.parse")
assert parse_only == "true"
