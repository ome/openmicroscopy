% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Delete Data
try
    [client, session] = Connect();
    
    % It is possible to delete Projects, datasets, images, ROIs etc and objects linked to them depending on the
    % specified options (see [wiki:Delete]).

    % Delete Image. In the following example, we create an image and delete it.
    % First create the image.
    image = omero.model.ImageI;
    image.setName(omero.rtypes.rstring('image name'))
    image.setAcquisitionDate(omero.rtypes.rtime(2000000));
    image = session.getUpdateService().saveAndReturnObject(image);
    imageId = image.getId().getValue();

    % Create the command to delete the image using a delete callback.
    % You can delete more than one image at a time.
    list = javaArray('omero.api.delete.DeleteCommand', 1);
    % Indicate the type of object e.g. /Image, /Project etc., the identifier
    % and the annotations to keep (nothing in the following example)
    list(1) = omero.api.delete.DeleteCommand('/Image', imageId, []);
    %Delete the image.
    prx = session.getDeleteService().queueDelete(list);
catch err
    disp(err.message);
end


%Close the session
client.closeSession();