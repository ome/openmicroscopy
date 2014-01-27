#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Uses the default {@link DeleteCallbackI} instance.
to delete a FileAnnotation along with its associated
OriginalFile and any annotation links.
"""

import omero
import omero.callbacks

c = omero.client("localhost")
ice_config = c.getProperty("Ice.Config")

from omero.rtypes import *
from omero.model import *

try:
    s = c.createSession("will", "ome")
    d = DatasetI()
    d.setName(rstring("FileAnnotationDelete"))
    d = s.getUpdateService().saveAndReturnObject(d)

    file = c.upload("test.xml")
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

    callback = omero.callbacks.CmdCallbackI(c, handle)
    loops = 10
    delay = 500
    callback.loop(loops, delay) # Throw LockTimeout
    rsp = callback.getResponse()

finally:
    c.closeSession()
