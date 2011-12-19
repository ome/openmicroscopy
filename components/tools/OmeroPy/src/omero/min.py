#!/usr/bin/env python
"""

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import IceImport
if omero.__import_style__ is None:
    omero.__import_style__ = "min"
    IceImport.load("omero_API_ice")
    IceImport.load("omero_ServicesF_ice")
    IceImport.load("omero_Constants_ice")
    IceImport.load("Glacier2_Router_ice")
    import omero.rtypes
