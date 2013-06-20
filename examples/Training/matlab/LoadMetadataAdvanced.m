% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

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
    disp(err.message);
end


% close the session
client.closeSession();
