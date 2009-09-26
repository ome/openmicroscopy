#!/usr/bin/env python
"""
   Primary OmeroPy types

   Classes:
      - omero.client    -- Main OmeroPy connector object

   Copyright 2007, 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

def client_wrapper(*args, **kwargs):
    import omero.gateway
    return omero.gateway.BlitzGateway(*args, **kwargs)

def client(*args, **kwargs):
    import omero.clients
    return omero.clients.BaseClient(*args, **kwargs)
