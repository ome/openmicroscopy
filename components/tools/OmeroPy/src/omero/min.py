#!/usr/bin/env python
"""

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
if omero.__import_style__ is None:
    omero.__import_style__ = "min"
    import omero_API_ice
    import omero_ServicesF_ice
    import omero_Constants_ice
    import omero.rtypes
