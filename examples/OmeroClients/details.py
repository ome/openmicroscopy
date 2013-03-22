#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients

image = omero.model.ImageI()
details = image.getDetails()

p = omero.model.PermissionsI()
p.setUserRead(True)
assert p.isUserRead()
details.setPermissions(p)

# Available when returned from server
# Possibly modifiable
details.getOwner()
details.setGroup(omero.model.ExperimenterGroupI(1L, False))
# Available when returned from server
# Not modifiable
details.getCreationEvent()
details.getUpdateEvent()
