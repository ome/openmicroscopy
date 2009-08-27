function projects = AllProjects(query, username)
q = ['select p from Project p join fetch p.datasetLinks dil ',...
     'join fetch dil.child where p.details.owner.omeName = :name'];
p = omero.sys.ParametersI();
p.add('name', omero.rtypes.rstring(username));
projects = query.findAllByQuery(q, p);
