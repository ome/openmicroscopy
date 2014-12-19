#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import time
import omero
import omero.clients

from omero.rtypes import rstring, rtime

client = omero.client(sys.argv)
try:
    i = omero.model.ImageI()
    i.name = rstring("name")
    i.acquisitionDate = rtime(time.time() * 1000)

    sf = client.createSession()
    u = sf.getUpdateService()

    i = u.saveAndReturnObject(i)
finally:
    client.closeSession()
