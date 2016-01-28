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

% Crate a new synthetic image and upload it to the server

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
    
    % Information to edit
    datasetId = p.datasetid;
    
    % Read the dimensions
    sizeX = 200;
    sizeY = 100;
    sizeZ = 1; % The number of z-sections.
    sizeT = 5; % The number of timepoints.
    sizeC = 2; % The number of channels.
    type = 'uint16';
    
    % Retrieve pixel type
    pixelsService = session.getPixelsService();
    pixelTypes = toMatlabList(pixelsService.getAllEnumerations('omero.model.PixelsType'));
    pixelTypeValues = arrayfun(@(x) char(x.getValue().getValue()),...
        pixelTypes, 'Unif', false);
    pixelType = pixelTypes(strcmp(pixelTypeValues, type));
    
    % Create a new image
    disp('Uploading new image onto the server');
    description = sprintf('Dimensions: %g x %g x %g x %g x %g',...
        sizeX, sizeY, sizeZ, sizeC, sizeT);
    name = 'New image';
    idNew = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT,...
        toJavaList(0:sizeC-1, 'java.lang.Integer'), pixelType, name, description);
    
    %load the image.
    disp('Checking the created image');
    imageNew = getImages(session, idNew.getValue());
    assert(~isempty(imageNew), 'OMERO:CreateImage', 'Image Id not valid');
    
    % Set channel properties
    disp('Adding metadata to the channels')
    channels = loadChannels(session, imageNew);
    for i = 1: numel(channels)
        channelName = ['Channel ' num2str(i)'];
        emissionWave = omero.model.LengthI;
        emissionWave.setValue(550);
        emissionWave.setUnit(omero.model.enums.UnitsLength.NANOMETER);
        channels(i).getLogicalChannel().setName(rstring(channelName));
        channels(i).getLogicalChannel().setEmissionWave(emissionWave);
    end
    session.getUpdateService().saveArray(toJavaList(channels));
    
    % load the dataset
    fprintf(1, 'Reading dataset: %g\n', datasetId);
    dataset = getDatasets(session, datasetId, false);
    assert(~isempty(dataset), 'OMERO:CreateImage', 'Dataset Id not valid');
    
    % Link the new image to the dataset
    fprintf(1, 'Linking image %g to dataset %g\n', idNew.getValue(), datasetId);
    link = omero.model.DatasetImageLinkI;
    link.setChild(omero.model.ImageI(idNew, false));
    link.setParent(omero.model.DatasetI(dataset.getId().getValue(), false));
    session.getUpdateService().saveAndReturnObject(link);
    
    % Copy the data.
    fprintf(1, 'Copying data to image %g\n', idNew.getValue());
    pixels = imageNew.getPrimaryPixels();
    store = session.createRawPixelsStore();
    store.setPixelsId(pixels.getId().getValue(), false);
    
    % Create template for upload
    range =  cast(1: intmax(type) / sizeX  : intmax(type), type);
    template = repmat(range', 1, sizeY);
    byteArray = toByteArray(template, pixels);
    
    % Upload template for every plane in the image
    for z = 1 : sizeZ,
        for c = 1:sizeC
            for t = 1: sizeT,
                index = sub2ind([sizeZ sizeC sizeT], z, c, t);
                store.setPlane(byteArray, z - 1, c - 1, t - 1);
            end
        end
    end
    store.save(); %save the data
    store.close(); %close
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
