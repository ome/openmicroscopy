[client,sf] = loadOmero;
try

    i = omero.model.ImageI();
    i.setName(omero.rtypes.rstring('name'));
    i.setAcquisitionDate(omero.rtypes.rtime(java.lang.System.currentTimeMillis()));
    u = sf.getUpdateService();
    i = u.saveAndReturnObject( i );
    disp(i.getId().getValue());

catch ME

    disp(ME);
    client.closeSession();

end
