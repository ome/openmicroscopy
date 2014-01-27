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
    fa = FileAnnotationI()
    file = c.upload("test.xml")
    # fa.setFile(file)
    fa.setFile(OriginalFileI(file.id.val, False))
    fa = s.getUpdateService().saveAndReturnObject(fa)

    # Get Dataset with annotations loaded, so we can add fa to the list
    query = "select d from Dataset as d " \
            "left outer join fetch d.annotationLinks as dal " \
            "left outer join fetch dal.child as a where d.id = :d"
    params = omero.sys.ParametersI()
    params.addLong('d', d.id.val)
    d = s.getQueryService().findByQuery(query, params)
    d.linkAnnotation(fa)
    d = s.getUpdateService().saveAndReturnObject(d)
    # fa = d.linkedAnnotationList()[0]

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
