% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Information to edit
imageId = java.lang.Long(27544);
datasetId = java.lang.Long(2651);

% Create an image from an existing image.
%
% The source image must have at least 2 channels.

try
    [client, session] = connect();
    % First retrieve the image.
    
    iContainer = session.getContainerService();
    list = iContainer.getImages(omero.model.Image.class, java.util.Arrays.asList(imageId), omero.sys.ParametersI()); 
    if (list.size == 0)
        exception = MException('OMERO:CreateImage', 'Image Id not valid');
        throw(exception);
    end
    image = list.get(0);
    pixelsList = image.copyPixels();
    % you should only have one pixels set per image.
    pixels = pixelsList.get(0);
    sizeZ = pixels.getSizeZ().getValue() % The number of z-sections.
    sizeT = pixels.getSizeT().getValue(); % The number of timepoints.
    sizeC = pixels.getSizeC().getValue(); % The number of channels.
    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    if (sizeC <= 1)
        exception = MException('OMERO:CreateImage', 'Image must contain at least 2 channels');
        throw(exception);
    end
    pixelsId = pixels.getId().getValue()
    store = session.createRawPixelsStore();
    store.setPixelsId(pixelsId, false);
    
    map = java.util.LinkedHashMap;
    for z = 0:sizeZ-1,
       for t = 0:sizeT-1,
             planeC1 = store.getPlane(z, 0, t);
             map.put(linearize(z, t, sizeZ), planeC1);
       end
    end
    
    % Close to free space.
    store.close();
    
    proxy = session.getPixelsService();
    l = proxy.getAllEnumerations(omero.model.PixelsType.class);
    original = pixels.getPixelsType().getValue().getValue();
    for j = 0:l.size()-1,
        type = l.get(j);
        if (type.getValue().getValue() == original)
            break;
        end
    end
    
    
    description = char(['Source Image ID: ' int2str(image.getId().getValue())]);
    name = char(['newImageFrom' int2str(image.getId().getValue())]);
    idNew = proxy.createImage(sizeX, sizeY, sizeZ, sizeT, java.util.Arrays.asList(java.lang.Integer(0)), type, name, description);
    
    
    %load the image.
    list = iContainer.getImages(omero.model.Image.class, java.util.Arrays.asList(java.lang.Long(idNew.getValue())), omero.sys.ParametersI()); 
    if (list.size == 0)
        exception = MException('OMERO:CreateImage', 'Image Id not valid');
        throw(exception);
    end
    imageNew = list.get(0);
    param = omero.sys.ParametersI();
    param.noLeaves(); % indicate to load the images.
    % load the dataset
    results = session.getContainerService().loadContainerHierarchy(omero.model.Dataset.class, java.util.Arrays.asList(datasetId), param);
    if (results.size == 0)
        exception = MException('OMERO:CreateImage', 'Dataset Id not valid');
        throw(exception);
    end
    dataset = results.get(0);
    link = omero.model.DatasetImageLinkI;
    link.setChild(omero.model.ImageI(imageNew.getId().getValue(), false));
    link.setParent(omero.model.DatasetI(dataset.getId().getValue(), false));

    session.getUpdateService().saveAndReturnObject(link);

    
    %Copy the data.
    pixelsNewList = imageNew.copyPixels();
    pixelsNew = pixelsNewList.get(0);
    pixelsNewId = pixelsNew.getId().getValue()
    store = session.createRawPixelsStore();
    store.setPixelsId(pixelsNewId, false);
    
    for z = 0:sizeZ-1,
       for t = 0:sizeT-1,
             index = linearize(z, t, sizeZ);
             store.setPlane(map.get(index), z, 0, t);
       end
    end
    %save the data
    store.save();
    %close
    store.close();
catch err
    disp(err.message);
end

%Close the session
client.closeSession();