import omero

def getProjects(query_prx, username):
    return query_prx.findAllByQuery(
            "select p from Project p where p.details.owner.name = :name",
            ParametersI().add("name", omero.RString(username)))
