[client,sf] = loadOmero;
try

    i = omero.model.ImageI();
    i.setName(omero.rtypes.rstring('name'));
    u = sf.getUpdateService();
    i = u.saveAndReturnObject( i );
    disp(i.getId().getValue());

catch ME

    disp(ME);
    client.closeSession();

end
