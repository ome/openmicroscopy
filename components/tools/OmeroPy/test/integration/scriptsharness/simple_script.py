#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Minimal script to test the {g,s}et{In,Out}put functionality
   of omero.client

   Copyright 2008-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero.scripts

client = omero.scripts.client("test_harness")
pixelsID = client.getInput("pixelsID")
client.setOutput("newPixelsID",-1)


