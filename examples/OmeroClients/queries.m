[client,sf] = loadOmero;

try
    
    q = sf.getQueryService();
    query_string = 'select i from Image i where i.id = :id and name like :namedParameter';

    p = omero.sys.ParametersI();
    p.add('id', omero.rtypes.rlong(1));
    p.add('namedParameter', omero.rtypes.rstring('cell%mit%'));
    
    results = q.findAllByQuery(query_string, p) % java.util.List

catch ME
    client.closeSession();
end
