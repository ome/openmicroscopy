% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% information to edit.
imageId = java.lang.Long(27544);

% Load Metadata
try
    [client, session] = connect();
    iUpdate = session.getUpdateService();
    %Load image acquisition data.
    
    proxy = session.getContainerService();
    param = omero.sys.ParametersI();
    param.acquisitionData();
    results = proxy.getImages(omero.model.Image.class, java.util.Arrays.asList(imageId), param);
    if (results.size == 0)
        exception = MException('OMERO:LoadMetadataAdvanced', 'Image Id not valid');
        throw(exception);
    end
    image = results.get(0);
    %display the humidity
    %image.getImagingEnvironment().getHumidity().getValue();
    
    %Load the channel
    pixelsList = image.copyPixels();
    pixels = pixelsList.get(0);
        
    pixelsDescription = session.getPixelsService().retrievePixDescription(pixels.getId().getValue());
    
    channels = pixelsDescription.copyChannels();
    %handle the channels
    for j = 0:channels.size()-1,
        channel = channels.get(j);
        channel.getId().getValue()
    end
catch err
    disp(err.message);
end


% close the session
client.closeSession();
