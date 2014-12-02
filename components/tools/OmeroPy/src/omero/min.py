#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

# This file is an import-only file providing a mechanism for other files to
# import a range of modules in a controlled way. It could be made to pass
# flake8 but given its simplicity it is being marked as noqa for now.
#
# flake8: noqa

import omero
import IceImport
if omero.__import_style__ is None:
    omero.__import_style__ = "min"

# Internal types
IceImport.load("omero_model_NamedValue_ice")

# New Command API
IceImport.load("omero_cmd_Admin_ice")
IceImport.load("omero_cmd_API_ice")
IceImport.load("omero_cmd_Basic_ice")
IceImport.load("omero_cmd_FS_ice")
IceImport.load("omero_cmd_Graphs_ice")
IceImport.load("omero_cmd_Mail_ice")

# Previous ServiceFactory API
IceImport.load("omero_API_ice")
IceImport.load("omero_ServicesF_ice")
IceImport.load("omero_Constants_ice")

IceImport.load("Glacier2_Router_ice")
import omero.rtypes
