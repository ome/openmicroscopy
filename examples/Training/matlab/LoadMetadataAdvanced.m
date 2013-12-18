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

% Load Metadata
try
    % Create a connection
    [client, session] = loadOmero();
    fprintf(1, 'Created connection to %s\n', char(client.getProperty('omero.host')));
    fprintf(1, 'Created session for user %s using group %s\n',...
        char(session.getAdminService().getEventContext().userName),...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    imageId = str2double(client.getProperty('image.id'));
    
    % Load image acquisition data.
    fprintf(1, 'Reading image: %g\n', imageId);
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image Id not valid');
    pixels = image.getPrimaryPixels();
    pixelsId = pixels.getId().getValue();
    
    % Read channels
    fprintf(1, 'Reading channels for image %g\n', imageId');
    pixelsDescription = session.getPixelsService().retrievePixDescription(pixelsId);
    channels = pixelsDescription.copyChannels();
    for j = 0:channels.size()-1,
        channel = channels.get(j);
        fprintf(1, 'Reading channel %g: %g\n',j+1, channel.getId().getValue());
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
