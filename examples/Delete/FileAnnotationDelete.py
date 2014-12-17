#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Uses the default {@link DeleteCallbackI} instance.
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

    graph_spec = "/Annotation"
    options = {}
    delCmd = omero.cmd.Delete(graph_spec, long(fa.id.val), options)

    dcs = [delCmd]
    doall = omero.cmd.DoAll()
    doall.requests = dcs
    handle = s.submit(doall)

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
