#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Copyright 2007 Glencoe Software, Inc. All rights reserved.
Use is subject to license terms supplied in LICENSE.txt

Simple renaming module which forwards items to sys. Unfortunately,
before the creation of the python bindings, blitz started using the
omero.sys package which causes weird issues at certain levels in the
hierarchy. When those arise, pysys can be used as a replacement.
"""

import logging
logging.basicConfig()
logging.getLogger("omero_ext").warn("Deprecated: use __import__('sys') if necessary")

import sys
argv = sys.argv
stdout = sys.stdout
stderr = sys.stderr
exit = sys.exit
path = sys.path
exc_info = sys.exc_info
