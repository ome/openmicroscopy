#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import omero

from omero.rtypes import rstring
from omero_sys_ParametersI import ParametersI

client = omero.client(sys.argv)
try:
    sf = client.createSession()
    q = sf.getQueryService()

    query_string = (
        "select i from Image i where i.id = :id and name"
        " like :namedParameter")

    p = ParametersI()
    p.addId(1L)
    p.add("namedParameter", rstring("cell%mit%"))

    results = q.findAllByQuery(query_string, p)
finally:
    client.closeSession()
