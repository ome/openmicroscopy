#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients

image = omero.model.ImageI()                # A loaded object by default
assert image.isLoaded()
image.unload()                              # can then be unloaded
assert (not image.isLoaded())

image = omero.model.ImageI(1L, False)     # Creates an unloaded "proxy"
assert (not image.isLoaded())

image.getId()        # Ok
try:
    image.getName()  # No data access is allowed other than id.
except:
    pass
