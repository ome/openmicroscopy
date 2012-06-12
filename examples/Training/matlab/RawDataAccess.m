% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Information to edit
imageId = java.lang.Long(27544);

% Raw Data access

% You can retrieve data, plane by plane, a stack or an hypercube.

% Retrieve a given plane. 
% This is useful when you need the pixels intensity. 

try
    [client, session] = connect();
    
    % First retrieve the image.
    
    proxy = session.getContainerService();
    list = proxy.getImages(omero.model.Image.class, java.util.Arrays.asList(imageId), omero.sys.ParametersI());
    if (list.size == 0)
        exception = MException('OMERO:RawDataAccess', 'Image Id not valid');
        throw(exception);
    end
    image = list.get(0);
    pixelsList = image.copyPixels();
    % you should only have one pixels set per image.
    pixels = pixelsList.get(0);
    sizeX = pixels.getSizeX().getValue() % The number of pixels along the X-axis.
    sizeY = pixels.getSizeY().getValue() % The number of pixels along the Y-axis.
    sizeZ = pixels.getSizeZ().getValue() % The number of z-sections.
    sizeT = pixels.getSizeT().getValue() % The number of timepoints.
    sizeC = pixels.getSizeC().getValue() % The number of channels.
    pixelsId = pixels.getId().getValue();
    store = session.createRawPixelsStore(); 
    store.setPixelsId(pixelsId, false);
    for z = 0:sizeZ-1,
       for t = 0:sizeT-1,
         for c = 0:sizeC-1,
             plane = store.getPlane(z, c, t);
             tPlane = toMatrix(plane, pixels);
             %do something e.g. draw
         end
       end
    end
    store.close();

    % Retrieve a tile.
    store = session.createRawPixelsStore(); 
    store.setPixelsId(pixelsId, false);
    x = 0;
    y = 0;
    width = pixels.getSizeX().getValue()/2;
    height = pixels.getSizeY().getValue()/2;
    for z = 0:sizeZ-1,
       for t = 0:sizeT-1,
         for c = 0:sizeC-1,
             tile = store.getTile(z, c, t, x, y, width, height);
             %do something e.g. draw
         end
       end
    end
    store.close();

    % Retrieve a given stack. 

    % This is useful when you need the pixels intensity. 
    %Create the store to load the stack. No access via the gateway
    store = session.createRawPixelsStore(); 
    store.setPixelsId(pixelsId, false); %Indicate the pixels set you are working on
    for t = 0:sizeT-1,
        for c = 0:sizeC-1,
            stack = store.getStack(c, t);
            % do something with the stack
        end
    end
    store.close();


    % Retrieve a given hypercube. 

    % This is useful when you need the pixels intensity. 

    % To retrieve the pixels, see above.
    % 5-Dimensional 

    %Create the store to load the stack. No access via the gateway
    store = session.createRawPixelsStore(); 
    store.setPixelsId(pixelsId, false); %Indicate the pixels set you are working on

    % offset values in each dimension XYZCT
    offset = java.util.ArrayList;
    offset.add(java.lang.Integer(0));
    offset.add(java.lang.Integer(0));
    offset.add(java.lang.Integer(0));
    offset.add(java.lang.Integer(0));
    offset.add(java.lang.Integer(0));

    size = java.util.ArrayList;
    size.add(java.lang.Integer(sizeX));
    size.add(java.lang.Integer(sizeY));
    size.add(java.lang.Integer(sizeZ));
    size.add(java.lang.Integer(sizeC));
    size.add(java.lang.Integer(sizeT));

    % indicate the step in each direction, step = 1, will return values at index 0, 1, 2.
    % step = 2, values at index 0, 2, 4 etc.
    step = java.util.ArrayList;
    step.add(java.lang.Integer(1));
    step.add(java.lang.Integer(1));
    step.add(java.lang.Integer(1));
    step.add(java.lang.Integer(1));
    step.add(java.lang.Integer(1));
    % Retrieve the data
    store.getHypercube(offset, size, step);
    % close the store
    store.close();

catch err
    disp(err.message);
end

%Close the session
client.closeSession();

