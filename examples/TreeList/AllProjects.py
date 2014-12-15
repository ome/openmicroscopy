#!/usr/bin/env python
# -*- coding: utf-8 -*-

from omero.rtypes import rstring
from omero_sys_ParametersI import ParametersI


def getProjects(query_prx, username):
    return query_prx.findAllByQuery(
        "select p from Project p"
        " join fetch p.datasetLinks dil"
        " join fetch dil.child"
        " where p.details.owner.omeName = :name",
        ParametersI().add("name", rstring(username)))
