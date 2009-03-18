import omero
from omero.rtypes import *
from omero_sys_ParametersI import ParametersI

def getProjects(query_prx, username):
    return query_prx.findAllByQuery(
            "select p from Project p where p.details.owner.name = :name",
            ParametersI().add("name", rstring(username)))
