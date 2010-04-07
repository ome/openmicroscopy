#!/usr/bin/env python
"""
   Primary OmeroPy types

   Classes:
      - omero.client    -- Main OmeroPy connector object

   Copyright 2007, 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions

__import_style__ = None

def client_wrapper(*args, **kwargs):
    import omero.gateway
    return omero.gateway.BlitzGateway(*args, **kwargs)

def client(*args, **kwargs):
    import omero.clients
    return omero.clients.BaseClient(*args, **kwargs)

class ClientError(exceptions.Exception):
    """
    Top of client exception hierarchy.
    """
    pass

class UnloadedEntityException(ClientError):
    pass

class UnloadedCollectionException(ClientError):
    pass
