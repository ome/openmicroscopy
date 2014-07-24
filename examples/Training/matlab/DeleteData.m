% Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Delete Data

% It is possible to delete Projects, datasets, images, ROIs etc and objects
% linked to them depending on the specified options
try
    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
    
    % Delete Image. In the following example, we create an image and delete it.
    % First create the image.
    image = omero.model.ImageI;
    image.setName(rstring('image name'))
    image = session.getUpdateService().saveAndReturnObject(image);
    imageId = image.getId().getValue();
    
    % Check the image has been created
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image Id not valid');
    fprintf(1, 'Image %g created\n', imageId);
    
    % Delete the image. You can delete more than one image at a time.
    fprintf(1, 'Deleting image %g\n', imageId);
    deleteImages(session, imageId);
    
    % Check the image has been deleted
    pause(5)
    image = getImages(session, imageId);
    assert(isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image not deleted');
    fprintf(1, 'Image %g deleted\n', imageId);
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
