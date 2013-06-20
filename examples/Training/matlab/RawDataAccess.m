% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Raw Data access

try
    [client, session] = loadOmero();
    fprintf(1, 'Created connection to %s\n', char(client.getProperty('omero.host')));
    fprintf(1, 'Created session for user %s using group %s\n',...
        char(session.getAdminService().getEventContext().userName),...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    imageId = str2double(client.getProperty('image.id'));
    
    % First retrieve the image.
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:RawDataAccess', 'Image Id not valid');
    
    % you should only have one pixels set per image.
    fprintf(1, 'Reading image %g\n', image.getId.getValue);
    pixels = image.getPrimaryPixels();
    sizeX = pixels.getSizeX().getValue(); % The number of pixels along the X-axis.
    sizeY = pixels.getSizeY().getValue(); % The number of pixels along the Y-axis.
    sizeZ = pixels.getSizeZ().getValue(); % The number of z-sections.
    sizeC = pixels.getSizeC().getValue(); % The number of channels.
    sizeT = pixels.getSizeT().getValue(); % The number of timepoints.
    fprintf(1, 'Size X: %g\n', sizeX);
    fprintf(1, 'Size Y: %g\n', sizeY);
    fprintf(1, 'Size Z: %g\n', sizeZ);
    fprintf(1, 'Size C: %g\n', sizeC);
    fprintf(1, 'Size T: %g\n', sizeT);
    
    % Retrieve planes
    disp('Reading planes');
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Plane Z: %g, C: %g, T: %g\n', z, c, t);
                plane = getPlane(session, image, z, c, t);
            end
        end
    end
    
    % Retrieve tiles
    disp('Reading tiles');
    x = 0;
    y = 0;
    width = sizeX/2;
    height = sizeY/2;
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Tile Z: %g, C: %g, T: %g\n', z, c, t);
                tile = getTile(session, image, z, c, t, x, y, width, height);
            end
        end
    end
    
    % Retrieve stacks
    disp('Reading stacks');
    for t = 0:sizeT-1,
        for c = 0:sizeC-1,
            fprintf(1, '  Stack C: %g, T: %g\n', c, t);
            stack = getStack(session, image, c, t);
        end
    end
    
    
    % Retrieve a given hypercube.
    disp('Reading hypercube');
    %Create the store to load the stack. No access via the gateway
    store = session.createRawPixelsStore();
    %Indicate the pixels set you are working on
    store.setPixelsId(pixels.getId().getValue(), false);
    
    % offset values in each dimension XYZCT
    offset = toJavaList(zeros(1,5), 'java.lang.Integer');
    
    % offset values in each dimension XYZCT
    size = toJavaList([sizeX/2 sizeY/2 sizeZ sizeC sizeT],...
        'java.lang.Integer');
    
    % indicate the step in each direction, step = 1, will return values at index 0, 1, 2.
    % step = 2, values at index 0, 2, 4 etc.
    step = toJavaList(ones(1,5), 'java.lang.Integer');
    
    % Retrieve the data
    store.getHypercube(offset, size, step);
    % close the store
    store.close();
    
catch err
    disp(err.message);
end

%Close the session
client.closeSession();

