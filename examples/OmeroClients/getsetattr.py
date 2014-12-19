#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients
from omero.rtypes import rstring

i = omero.model.ImageI()

#
# Without __getattr__ and __setattr__
#
i.setName(rstring("name"))
assert i.getName().getValue() == "name"

#
# With __getattr__ and __setattr__
#
i = omero.model.ImageI()
i.name = rstring("name")
assert i.name.val == "name"

#
# Collections, however, cannot be accessed
# via the special methods due to the dangers
# outlined above
#
try:
    i.datasetLinks[0]
except AttributeError, ae:
    pass
