% Copyright (C) 2011-2013 University of Dundee & Open Microscopy Environment.
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
    disp('Creating connection');
    [client, session] = loadOmero();
    fprintf(1, 'Created session for  %s', char(client.getProperty('omero.host')));
    fprintf(1, ' for user %s',...
        char(session.getAdminService().getEventContext().userName));
    fprintf(1, ' using group %s\n',...
        char(session.getAdminService().getEventContext().groupName));
    
    % Delete Image. In the following example, we create an image and delete it.
    % First create the image.
    image = omero.model.ImageI;
    image.setName(rstring('image name'))
    image.setAcquisitionDate(rtime(2000000));
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
