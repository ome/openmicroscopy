#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Uses a omero.cmd.Delete2 request instance
to delete a FileAnnotation along with its associated
OriginalFile and any annotation links.
"""

import omero
import omero.callbacks

c = omero.client()
ice_config = c.getProperty("Ice.Config")

from omero.rtypes import rstring
from omero.model import DatasetI, FileAnnotationI, OriginalFileI
from omero.model import DatasetAnnotationLinkI

try:
    s = c.createSession()
    d = DatasetI()
    d.setName(rstring("FileAnnotationDelete"))
    d = s.getUpdateService().saveAndReturnObject(d)

    file = c.upload(ice_config)
    fa = FileAnnotationI()
    fa.setFile(OriginalFileI(file.id.val, False))
    link = DatasetAnnotationLinkI()
    link.parent = DatasetI(d.id.val, False)
    link.child = fa
    link = s.getUpdateService().saveAndReturnObject(link)
    fa = link.child

    to_delete = {"Annotation": [fa.id.val]}
    delCmd = omero.cmd.Delete2(targetObjects=to_delete)

    handle = s.submit(delCmd)

    callback = None
    try:
        callback = omero.callbacks.CmdCallbackI(c, handle)
        loops = 10
        delay = 500
        callback.loop(loops, delay)  # Throw LockTimeout
        rsp = callback.getResponse()
        if isinstance(rsp, omero.cmd.OK):
            print "OK"
    finally:
        if callback:
            callback.close(True)
        else:
            handle.close()

finally:
    c.closeSession()
